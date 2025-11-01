package com.example.productimport.job.product;

import com.example.productimport.dto.ProductCsvRecord;
import com.example.productimport.entity.ProductMaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;

/**
 * 商品マスターインポートジョブ設定
 * requirements.mdの仕様に従ってSpring Batchジョブを構成
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ProductImportJobConfig {

    private static final String JOB_NAME = "productImportJob";
    private static final String STEP_NAME = "productImportStep";
    private static final int CHUNK_SIZE = 100; // requirements.md 3.2 チャンクサイズ

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ProductCsvItemReader csvItemReaderFactory;
    private final ProductItemProcessor productItemProcessor;
    private final ProductItemWriter productItemWriter;
    private final ErrorFileSkipListener errorFileSkipListener;
    private final ProductImportJobExecutionListener jobExecutionListener;

    /**
     * 商品マスターインポートジョブ
     *
     * @return Job
     */
    @Bean(name = JOB_NAME)
    public Job productImportJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(productImportStep())
                .listener(jobExecutionListener)
                .build();
    }

    /**
     * 商品マスターインポートステップ
     * チャンクサイズ: 100件
     * スキップポリシー: バリデーションエラーは継続
     *
     * @return Step
     */
    @Bean
    public Step productImportStep() {
        return new StepBuilder(STEP_NAME, jobRepository)
                .<ProductCsvRecord, ProductMaster>chunk(CHUNK_SIZE, transactionManager)
                .reader(productCsvReader(null))
                .processor(productItemProcessor)
                .writer(productItemWriter)
                // スキップ設定
                .faultTolerant()
                .skip(ProductItemProcessor.ValidationException.class)
                .skipLimit(Integer.MAX_VALUE) // 無制限（すべてのバリデーションエラーをスキップ）
                .listener(errorFileSkipListener)
                .build();
    }

    /**
     * CSVファイルリーダー（ステップスコープ）
     * ジョブパラメータからファイルパスを取得
     *
     * @param inputFilePath ジョブパラメータから取得する入力ファイルパス
     * @return FlatFileItemReader
     */
    @Bean
    @StepScope
    public FlatFileItemReader<ProductCsvRecord> productCsvReader(
            @Value("#{jobParameters['inputFilePath']}") String inputFilePath) {

        log.info("CSVファイルリーダーを初期化: {}", inputFilePath);

        // エラーファイルを初期化
        try {
            errorFileSkipListener.initializeErrorFile(inputFilePath);
        } catch (IOException e) {
            log.error("エラーファイルの初期化に失敗しました: {}", e.getMessage(), e);
            throw new RuntimeException("エラーファイルの初期化に失敗しました", e);
        }

        return csvItemReaderFactory.createReader(inputFilePath);
    }
}
