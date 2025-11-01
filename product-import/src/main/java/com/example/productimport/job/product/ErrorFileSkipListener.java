package com.example.productimport.job.product;

import com.example.productimport.dto.ProductCsvRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * エラーファイル出力SkipListener
 * バリデーションエラーが発生した行をエラーファイルに出力
 */
@Component
@Slf4j
public class ErrorFileSkipListener implements SkipListener<ProductCsvRecord, Object> {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private BufferedWriter errorWriter;
    private String errorFilePath;
    private boolean headerWritten = false;

    /**
     * エラーファイルを初期化
     *
     * @param inputFilePath 入力CSVファイルパス
     * @throws IOException ファイル作成エラー
     */
    public void initializeErrorFile(String inputFilePath) throws IOException {
        // エラーファイル名を生成
        Path inputPath = Paths.get(inputFilePath);
        String inputFileName = inputPath.getFileName().toString();
        String baseFileName = inputFileName.replaceFirst("[.][^.]+$", ""); // 拡張子を除去
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String errorFileName = String.format("%s_error_%s.csv", baseFileName, timestamp);

        // エラーファイルパスを生成（入力ファイルと同じディレクトリ）
        Path errorPath = inputPath.getParent() != null
                ? inputPath.getParent().resolve(errorFileName)
                : Paths.get(errorFileName);

        this.errorFilePath = errorPath.toString();

        // エラーファイルを作成
        Files.createDirectories(errorPath.getParent());
        this.errorWriter = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(errorFilePath),
                        StandardCharsets.UTF_8
                )
        );

        log.info("エラーファイルを作成しました: {}", errorFilePath);
    }

    /**
     * 読み込み時のスキップ処理
     * CSVパースエラーなど
     */
    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("読み込みエラー: {}", t.getMessage());
        writeErrorToFile(0L, t.getMessage(), "パースエラー");
    }

    /**
     * 処理時のスキップ処理
     * バリデーションエラー
     */
    @Override
    public void onSkipInProcess(ProductCsvRecord item, Throwable t) {
        log.warn("行{}: 処理エラー - {}", item.getLineNumber(), t.getMessage());

        // バリデーションエラーの場合、エラーファイルに出力
        if (t instanceof ProductItemProcessor.ValidationException) {
            ProductItemProcessor.ValidationException ve = (ProductItemProcessor.ValidationException) t;
            ProductCsvRecord record = ve.getRecord();
            writeErrorToFile(record.getLineNumber(), ve.getMessage(), record.getOriginalLine());
        } else {
            // その他のエラー
            writeErrorToFile(item.getLineNumber(), t.getMessage(), item.getOriginalLine());
        }
    }

    /**
     * 書き込み時のスキップ処理
     * DB書き込みエラーなど
     */
    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.warn("書き込みエラー: {}", t.getMessage());
        writeErrorToFile(0L, t.getMessage(), item.toString());
    }

    /**
     * エラー情報をCSVファイルに出力
     *
     * @param lineNumber 行番号
     * @param errorMessage エラーメッセージ
     * @param originalLine 元のCSV行データ
     */
    private void writeErrorToFile(Long lineNumber, String errorMessage, String originalLine) {
        try {
            if (errorWriter == null) {
                log.error("エラーファイルが初期化されていません");
                return;
            }

            // ヘッダー行を出力（初回のみ）
            if (!headerWritten) {
                errorWriter.write("行番号,エラー理由,元のCSV行");
                errorWriter.newLine();
                headerWritten = true;
            }

            // エラー行を出力
            String errorLine = String.format("%d,\"%s\",\"%s\"",
                    lineNumber,
                    escapeQuotes(errorMessage),
                    escapeQuotes(originalLine)
            );
            errorWriter.write(errorLine);
            errorWriter.newLine();
            errorWriter.flush();

        } catch (IOException e) {
            log.error("エラーファイルへの書き込みに失敗しました: {}", e.getMessage(), e);
        }
    }

    /**
     * CSV用のダブルクォートエスケープ処理
     *
     * @param value エスケープ対象の文字列
     * @return エスケープ後の文字列
     */
    private String escapeQuotes(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\"\"");
    }

    /**
     * エラーファイルをクローズ
     */
    public void closeErrorFile() {
        if (errorWriter != null) {
            try {
                errorWriter.close();
                log.info("エラーファイルをクローズしました: {}", errorFilePath);
            } catch (IOException e) {
                log.error("エラーファイルのクローズに失敗しました: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * エラーファイルパスを取得
     *
     * @return エラーファイルパス
     */
    public String getErrorFilePath() {
        return errorFilePath;
    }

    /**
     * エラーが記録されたかどうかを判定
     *
     * @return ヘッダーが書き込まれている場合true
     */
    public boolean hasErrors() {
        return headerWritten;
    }
}
