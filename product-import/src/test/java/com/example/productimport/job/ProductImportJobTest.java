package com.example.productimport.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 商品マスターインポートジョブ E2Eテスト
 */
@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class ProductImportJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("productImportJob")
    private Job productImportJob;

    private String testDataDir;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public JobLauncherTestUtils jobLauncherTestUtils(
                @Qualifier("productImportJob") Job productImportJob,
                JobRepository jobRepository,
                PlatformTransactionManager transactionManager) {
            JobLauncherTestUtils utils = new JobLauncherTestUtils();
            utils.setJob(productImportJob);
            utils.setJobRepository(jobRepository);
            return utils;
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        // テストデータディレクトリを作成
        testDataDir = System.getProperty("java.io.tmpdir") + "/product-import-test";
        Files.createDirectories(Paths.get(testDataDir));
    }

    @Test
    void testProductImportJob_正常系_新規登録() throws Exception {
        // テストCSVファイルを作成
        String csvContent = """
                カテゴリー名,メーカー,型番,商品名,説明文,写真,価格,適用開始日,適用終了日
                家電,メーカーA,MODEL-001,テレビ,55インチ4K液晶テレビ,tv_001.jpg,89800,2025-01-01,2025-12-31
                家具,メーカーB,MODEL-002,ソファー,3人掛けソファー,sofa_001.jpg,120000,2025-01-01,2025-12-31
                """;

        Path csvFile = createTestCsvFile("test_valid.csv", csvContent);

        // ジョブパラメータを設定
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFilePath", csvFile.toString())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // ジョブを実行
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // 検証
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(jobExecution.getStepExecutions()).hasSize(1);
        assertThat(jobExecution.getStepExecutions().iterator().next().getReadCount()).isEqualTo(2);
        assertThat(jobExecution.getStepExecutions().iterator().next().getWriteCount()).isEqualTo(2);
        assertThat(jobExecution.getStepExecutions().iterator().next().getSkipCount()).isEqualTo(0);
    }

    @Test
    void testProductImportJob_異常系_バリデーションエラー() throws Exception {
        // バリデーションエラーを含むCSVファイルを作成
        String csvContent = """
                カテゴリー名,メーカー,型番,商品名,説明文,写真,価格,適用開始日,適用終了日
                家電,メーカーA,MODEL-001,テレビ,55インチ4K液晶テレビ,tv_001.jpg,89800,2025-01-01,2025-12-31
                不明カテゴリ,メーカーB,MODEL-002,ソファー,3人掛けソファー,sofa_001.jpg,120000,2025-01-01,2025-12-31
                家電,メーカーC,MODEL-003,冷蔵庫,大型冷蔵庫,fridge_001.jpg,150000000,2025-01-01,2025-12-31
                """;

        Path csvFile = createTestCsvFile("test_validation_error.csv", csvContent);

        // ジョブパラメータを設定
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputFilePath", csvFile.toString())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        // ジョブを実行
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // 検証（バリデーションエラーはスキップされ、ジョブは正常完了）
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(jobExecution.getStepExecutions().iterator().next().getReadCount()).isEqualTo(3);
        assertThat(jobExecution.getStepExecutions().iterator().next().getWriteCount()).isEqualTo(1); // 1件のみ成功
        assertThat(jobExecution.getStepExecutions().iterator().next().getSkipCount()).isEqualTo(2); // 2件スキップ

        // エラーファイルが生成されていることを確認
        String errorFileName = csvFile.getFileName().toString().replace(".csv", "_error_");
        Path errorFile = Files.list(csvFile.getParent())
                .filter(p -> p.getFileName().toString().startsWith(errorFileName.split("_error_")[0] + "_error_"))
                .findFirst()
                .orElse(null);

        assertThat(errorFile).isNotNull();
        assertThat(Files.exists(errorFile)).isTrue();
    }

    @Test
    void testProductImportJob_正常系_更新処理() throws Exception {
        // 1回目: 新規登録
        String csvContent1 = """
                カテゴリー名,メーカー,型番,商品名,説明文,写真,価格,適用開始日,適用終了日
                家電,メーカーA,MODEL-001,テレビ,55インチ4K液晶テレビ,tv_001.jpg,89800,2025-01-01,2025-12-31
                """;

        Path csvFile1 = createTestCsvFile("test_insert.csv", csvContent1);

        JobParameters jobParameters1 = new JobParametersBuilder()
                .addString("inputFilePath", csvFile1.toString())
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution1 = jobLauncherTestUtils.launchJob(jobParameters1);
        assertThat(jobExecution1.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        // 2回目: 同じ型番・適用開始日で更新
        String csvContent2 = """
                カテゴリー名,メーカー,型番,商品名,説明文,写真,価格,適用開始日,適用終了日
                家電,メーカーA,MODEL-001,テレビ改良版,65インチ4K液晶テレビ,tv_002.jpg,99800,2025-01-01,2025-12-31
                """;

        Path csvFile2 = createTestCsvFile("test_update.csv", csvContent2);

        JobParameters jobParameters2 = new JobParametersBuilder()
                .addString("inputFilePath", csvFile2.toString())
                .addLong("timestamp", System.currentTimeMillis() + 1000)
                .toJobParameters();

        JobExecution jobExecution2 = jobLauncherTestUtils.launchJob(jobParameters2);
        assertThat(jobExecution2.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(jobExecution2.getStepExecutions().iterator().next().getWriteCount()).isEqualTo(1);
    }

    /**
     * テスト用CSVファイルを作成
     *
     * @param fileName ファイル名
     * @param content  CSVコンテンツ
     * @return 作成したファイルのPath
     * @throws IOException ファイル作成エラー
     */
    private Path createTestCsvFile(String fileName, String content) throws IOException {
        Path filePath = Paths.get(testDataDir, fileName);
        Files.writeString(filePath, content);
        return filePath;
    }
}
