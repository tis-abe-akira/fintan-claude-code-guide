package com.example.productimport.job.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 商品マスターインポートジョブ実行リスナー
 * requirements.md 6章のログ仕様に従ってログ出力を行う
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductImportJobExecutionListener implements JobExecutionListener {

    private final ErrorFileSkipListener errorFileSkipListener;

    /**
     * ジョブ開始前処理
     * requirements.md 6.1 処理開始ログ
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
        String inputFilePath = jobExecution.getJobParameters().getString("inputFilePath");

        log.info("========================================");
        log.info("商品マスターインポートバッチを開始します");
        log.info("入力ファイル: {}", inputFilePath);
        log.info("========================================");
    }

    /**
     * ジョブ完了後処理
     * requirements.md 6.3 処理完了ログ / 6.4 異常終了ログ
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        // エラーファイルをクローズ
        errorFileSkipListener.closeErrorFile();

        // ステップ実行情報を取得
        long totalCount = 0;
        long successCount = 0;
        long errorCount = 0;

        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            long readCount = stepExecution.getReadCount();
            long writeCount = stepExecution.getWriteCount();
            long skipCount = stepExecution.getSkipCount();

            totalCount = readCount;
            successCount = writeCount;
            errorCount = skipCount;
        }

        // 処理時間を計算
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();
        long processingTimeSeconds = Duration.between(startTime, endTime).getSeconds();

        // ジョブステータスによって異なるログを出力
        if (jobExecution.getStatus().isUnsuccessful()) {
            // 6.4 異常終了ログ
            log.error("========================================");
            log.error("商品マスターインポートバッチが異常終了しました");
            log.error("処理件数: {}件", totalCount);

            // エラー詳細を取得
            String errorDetails = jobExecution.getAllFailureExceptions().stream()
                    .map(Throwable::getMessage)
                    .findFirst()
                    .orElse("不明なエラー");
            log.error("エラー内容: {}", errorDetails);
            log.error("========================================");
        } else {
            // 6.3 処理完了ログ
            log.info("========================================");
            log.info("商品マスターインポートバッチが正常に完了しました");
            log.info("処理件数: {}件", totalCount);
            log.info("成功: {}件", successCount);
            log.info("エラー: {}件", errorCount);

            // エラーがある場合のみエラーファイルパスを出力
            if (errorCount > 0 && errorFileSkipListener.hasErrors()) {
                log.info("エラーファイル: {}", errorFileSkipListener.getErrorFilePath());
            } else {
                log.info("エラーファイル: なし");
            }

            log.info("処理時間: {}秒", processingTimeSeconds);
            log.info("========================================");
        }
    }
}
