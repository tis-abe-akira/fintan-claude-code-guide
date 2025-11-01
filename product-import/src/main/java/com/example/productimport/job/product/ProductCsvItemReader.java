package com.example.productimport.job.product;

import com.example.productimport.dto.ProductCsvRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

/**
 * 商品マスターCSVファイル読み込みReader設定
 * FlatFileItemReaderを使用してCSVファイルから商品データを読み込む
 */
@Component
@Slf4j
public class ProductCsvItemReader {

    /**
     * CSVファイルを読み込むFlatFileItemReaderを作成
     *
     * @param filePath 読み込むCSVファイルのパス
     * @return 設定済みのFlatFileItemReader
     */
    public FlatFileItemReader<ProductCsvRecord> createReader(String filePath) {
        FlatFileItemReader<ProductCsvRecord> reader = new FlatFileItemReader<>();

        // ファイルリソース設定
        reader.setResource(new FileSystemResource(filePath));

        // エンコーディング設定（UTF-8）
        reader.setEncoding("UTF-8");

        // ヘッダー行をスキップ（1行目）
        reader.setLinesToSkip(1);

        // 行番号をトラッキング（エラー出力用）
        reader.setLineMapper(createLineMapper());

        // Reader名設定
        reader.setName("productCsvItemReader");

        // 厳密モード（ファイルが存在しない場合はエラー）
        reader.setStrict(true);

        return reader;
    }

    /**
     * CSVの行をProductCsvRecordオブジェクトにマッピング
     *
     * @return LineMapper
     */
    private LineMapper<ProductCsvRecord> createLineMapper() {
        DefaultLineMapper<ProductCsvRecord> lineMapper = new DefaultLineMapper<>();

        // CSV列の区切り設定
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        tokenizer.setNames(
                "categoryName",
                "manufacturer",
                "modelNumber",
                "productName",
                "description",
                "photo",
                "price",
                "startDate",
                "endDate"
        );

        // フィールドセッターでProductCsvRecordに設定
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSet -> {
            // 行番号を取得（ヘッダー1行 + データ行番号）
            long lineNumber = fieldSet.getProperties().getProperty("lineNumber") != null
                    ? Long.parseLong(fieldSet.getProperties().getProperty("lineNumber"))
                    : 0L;

            // 元のCSV行データを保持（エラー出力用）
            String originalLine = String.join(",",
                    fieldSet.readString("categoryName"),
                    fieldSet.readString("manufacturer"),
                    fieldSet.readString("modelNumber"),
                    fieldSet.readString("productName"),
                    fieldSet.readString("description"),
                    fieldSet.readString("photo"),
                    fieldSet.readString("price"),
                    fieldSet.readString("startDate"),
                    fieldSet.readString("endDate")
            );

            return ProductCsvRecord.builder()
                    .lineNumber(lineNumber)
                    .categoryName(fieldSet.readString("categoryName"))
                    .manufacturer(fieldSet.readString("manufacturer"))
                    .modelNumber(fieldSet.readString("modelNumber"))
                    .productName(fieldSet.readString("productName"))
                    .description(fieldSet.readString("description"))
                    .photo(fieldSet.readString("photo"))
                    .price(fieldSet.readString("price"))
                    .startDate(fieldSet.readString("startDate"))
                    .endDate(fieldSet.readString("endDate"))
                    .originalLine(originalLine)
                    .build();
        });

        return lineMapper;
    }
}
