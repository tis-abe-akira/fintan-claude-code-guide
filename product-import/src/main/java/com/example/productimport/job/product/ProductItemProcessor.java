package com.example.productimport.job.product;

import com.example.productimport.dto.ProductCsvRecord;
import com.example.productimport.entity.ProductMaster;
import com.example.productimport.validation.ProductValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 商品マスターインポート Processor
 * バリデーション実行とProductCsvRecord→ProductMasterへの変換を行う
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductItemProcessor implements ItemProcessor<ProductCsvRecord, ProductMaster> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ProductValidator productValidator;

    /**
     * CSVレコードを処理してProductMasterエンティティに変換
     * バリデーションエラーがある場合はnullを返す（スキップ）
     *
     * @param record CSVレコード
     * @return ProductMasterエンティティ、バリデーションエラーの場合null
     * @throws Exception 処理エラー
     */
    @Override
    public ProductMaster process(ProductCsvRecord record) throws Exception {
        // バリデーション実行
        Optional<String> validationError = productValidator.validate(record);

        if (validationError.isPresent()) {
            // バリデーションエラーがある場合
            // nullを返すことでSpring Batchにスキップさせる
            // エラー詳細はSkipListenerで処理される
            log.warn("行{}: バリデーションエラー - {}", record.getLineNumber(), validationError.get());

            // 例外をスローしてSkipListenerでキャッチさせる
            throw new ValidationException(validationError.get(), record);
        }

        // バリデーションOKの場合、ProductMasterに変換
        ProductMaster product = convertToProductMaster(record);

        log.debug("行{}: バリデーション成功 - 型番: {}, 商品名: {}",
                record.getLineNumber(), record.getModelNumber(), record.getProductName());

        return product;
    }

    /**
     * ProductCsvRecordをProductMasterエンティティに変換
     *
     * @param record CSVレコード
     * @return ProductMasterエンティティ
     */
    private ProductMaster convertToProductMaster(ProductCsvRecord record) {
        return ProductMaster.builder()
                .categoryName(record.getCategoryName().trim())
                .manufacturer(record.getManufacturer().trim())
                .modelNumber(record.getModelNumber().trim())
                .productName(record.getProductName().trim())
                .description(record.getDescription().trim())
                .photo(record.getPhoto().trim())
                .price(Integer.parseInt(record.getPrice().trim()))
                .startDate(LocalDate.parse(record.getStartDate().trim(), DATE_FORMATTER))
                .endDate(LocalDate.parse(record.getEndDate().trim(), DATE_FORMATTER))
                .build();
    }

    /**
     * バリデーション例外
     * スキップ可能な例外として定義
     */
    public static class ValidationException extends Exception {
        private final ProductCsvRecord record;

        public ValidationException(String message, ProductCsvRecord record) {
            super(message);
            this.record = record;
        }

        public ProductCsvRecord getRecord() {
            return record;
        }
    }
}
