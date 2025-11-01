package com.example.productimport.validation;

import com.example.productimport.dto.ProductCsvRecord;
import com.example.productimport.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * 商品マスターCSVレコードのバリデーター
 * requirements.md 4章のバリデーション仕様に従って実装
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductValidator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MAX_LENGTH_50 = 50;
    private static final int MAX_LENGTH_256 = 256;
    private static final int MIN_PRICE = 1;
    private static final int MAX_PRICE = 100_000_000;

    private final CategoryMapper categoryMapper;

    /**
     * 商品CSVレコードを総合的にバリデーション
     *
     * @param record バリデーション対象のCSVレコード
     * @return バリデーション結果（エラーメッセージ、エラーがない場合は空）
     */
    public Optional<String> validate(ProductCsvRecord record) {
        // 4.7 バリデーション実行順序に従って実行

        // 1. 必須チェック
        Optional<String> requiredError = validateRequired(record);
        if (requiredError.isPresent()) {
            return requiredError;
        }

        // 2. データ型チェック（価格、日付）
        Optional<String> dataTypeError = validateDataType(record);
        if (dataTypeError.isPresent()) {
            return dataTypeError;
        }

        // 3. 文字列長チェック
        Optional<String> lengthError = validateLength(record);
        if (lengthError.isPresent()) {
            return lengthError;
        }

        // 4. 範囲チェック（価格）
        Optional<String> rangeError = validateRange(record);
        if (rangeError.isPresent()) {
            return rangeError;
        }

        // 5. 論理チェック（日付前後関係）
        Optional<String> logicalError = validateLogic(record);
        if (logicalError.isPresent()) {
            return logicalError;
        }

        // 6. 参照整合性チェック（カテゴリー）
        Optional<String> referentialError = validateReferentialIntegrity(record);
        if (referentialError.isPresent()) {
            return referentialError;
        }

        // すべてのバリデーションをパス
        return Optional.empty();
    }

    /**
     * 4.1 必須チェック
     * すべての項目が空文字（空文字列、null、空白のみ）でないこと
     */
    private Optional<String> validateRequired(ProductCsvRecord record) {
        if (isBlank(record.getCategoryName())) {
            return Optional.of("カテゴリー名は必須です");
        }
        if (isBlank(record.getManufacturer())) {
            return Optional.of("メーカーは必須です");
        }
        if (isBlank(record.getModelNumber())) {
            return Optional.of("型番は必須です");
        }
        if (isBlank(record.getProductName())) {
            return Optional.of("商品名は必須です");
        }
        if (isBlank(record.getDescription())) {
            return Optional.of("説明文は必須です");
        }
        if (isBlank(record.getPhoto())) {
            return Optional.of("写真は必須です");
        }
        if (isBlank(record.getPrice())) {
            return Optional.of("価格は必須です");
        }
        if (isBlank(record.getStartDate())) {
            return Optional.of("適用開始日は必須です");
        }
        if (isBlank(record.getEndDate())) {
            return Optional.of("適用終了日は必須です");
        }
        return Optional.empty();
    }

    /**
     * 4.2 データ型チェック
     * 価格: 整数型に変換可能であること
     * 適用開始日/終了日: yyyy-MM-dd形式の日付であること
     */
    private Optional<String> validateDataType(ProductCsvRecord record) {
        // 価格の整数チェック
        try {
            Integer.parseInt(record.getPrice().trim());
        } catch (NumberFormatException e) {
            return Optional.of("価格は整数で入力してください");
        }

        // 適用開始日の日付フォーマットチェック
        try {
            LocalDate.parse(record.getStartDate().trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return Optional.of("適用開始日は yyyy-MM-dd 形式で入力してください");
        }

        // 適用終了日の日付フォーマットチェック
        try {
            LocalDate.parse(record.getEndDate().trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return Optional.of("適用終了日は yyyy-MM-dd 形式で入力してください");
        }

        return Optional.empty();
    }

    /**
     * 4.3 文字列長チェック
     */
    private Optional<String> validateLength(ProductCsvRecord record) {
        if (record.getCategoryName().length() > MAX_LENGTH_50) {
            return Optional.of("カテゴリー名は50文字以内で入力してください");
        }
        if (record.getManufacturer().length() > MAX_LENGTH_50) {
            return Optional.of("メーカーは50文字以内で入力してください");
        }
        if (record.getModelNumber().length() > MAX_LENGTH_50) {
            return Optional.of("型番は50文字以内で入力してください");
        }
        if (record.getProductName().length() > MAX_LENGTH_50) {
            return Optional.of("商品名は50文字以内で入力してください");
        }
        if (record.getDescription().length() > MAX_LENGTH_256) {
            return Optional.of("説明文は256文字以内で入力してください");
        }
        if (record.getPhoto().length() > MAX_LENGTH_50) {
            return Optional.of("写真は50文字以内で入力してください");
        }
        return Optional.empty();
    }

    /**
     * 4.4 範囲チェック
     * 価格: 1 ≦ 価格 ≦ 100,000,000
     */
    private Optional<String> validateRange(ProductCsvRecord record) {
        int price = Integer.parseInt(record.getPrice().trim());
        if (price < MIN_PRICE || price > MAX_PRICE) {
            return Optional.of("価格は1円以上1億円以下で入力してください");
        }
        return Optional.empty();
    }

    /**
     * 4.6 論理チェック
     * 適用開始日 ≦ 適用終了日
     */
    private Optional<String> validateLogic(ProductCsvRecord record) {
        LocalDate startDate = LocalDate.parse(record.getStartDate().trim(), DATE_FORMATTER);
        LocalDate endDate = LocalDate.parse(record.getEndDate().trim(), DATE_FORMATTER);

        if (startDate.isAfter(endDate)) {
            return Optional.of("適用終了日は適用開始日以降の日付を入力してください");
        }
        return Optional.empty();
    }

    /**
     * 4.5 参照整合性チェック
     * CSV内のカテゴリー名がカテゴリーマスターテーブルに存在すること
     */
    private Optional<String> validateReferentialIntegrity(ProductCsvRecord record) {
        boolean exists = categoryMapper.existsByName(record.getCategoryName());
        if (!exists) {
            return Optional.of(String.format("カテゴリー名 '%s' がカテゴリーマスターに存在しません",
                    record.getCategoryName()));
        }
        return Optional.empty();
    }

    /**
     * 文字列が空白（null、空文字列、空白のみ）かどうかを判定
     *
     * @param value 検証する文字列
     * @return 空白の場合true
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
