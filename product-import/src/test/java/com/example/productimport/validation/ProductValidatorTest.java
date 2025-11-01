package com.example.productimport.validation;

import com.example.productimport.dto.ProductCsvRecord;
import com.example.productimport.mapper.CategoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * ProductValidator テストクラス
 * requirements.md 4章のバリデーション仕様のテスト
 */
@ExtendWith(MockitoExtension.class)
class ProductValidatorTest {

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private ProductValidator productValidator;

    private ProductCsvRecord validRecord;

    @BeforeEach
    void setUp() {
        // 正常なレコードを用意
        validRecord = ProductCsvRecord.builder()
                .lineNumber(2L)
                .categoryName("家電")
                .manufacturer("メーカーA")
                .modelNumber("MODEL-001")
                .productName("テレビ")
                .description("55インチ4K液晶テレビ")
                .photo("tv_001.jpg")
                .price("89800")
                .startDate("2025-01-01")
                .endDate("2025-12-31")
                .originalLine("家電,メーカーA,MODEL-001,テレビ,55インチ4K液晶テレビ,tv_001.jpg,89800,2025-01-01,2025-12-31")
                .build();

        // カテゴリーマスターのモック設定（lenientモードで設定）
        lenient().when(categoryMapper.existsByName("家電")).thenReturn(true);
        lenient().when(categoryMapper.existsByName("不明カテゴリ")).thenReturn(false);
    }

    // ========================================
    // 4.1 必須チェック
    // ========================================

    @Test
    @DisplayName("必須チェック: カテゴリー名が空の場合エラー")
    void testValidate_必須チェック_カテゴリー名() {
        validRecord.setCategoryName("");

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isPresent();
        assertThat(error.get()).isEqualTo("カテゴリー名は必須です");
    }

    @Test
    @DisplayName("必須チェック: メーカーがnullの場合エラー")
    void testValidate_必須チェック_メーカー() {
        validRecord.setManufacturer(null);

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isPresent();
        assertThat(error.get()).isEqualTo("メーカーは必須です");
    }

    @Test
    @DisplayName("必須チェック: 価格が空白のみの場合エラー")
    void testValidate_必須チェック_価格() {
        validRecord.setPrice("   ");

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isPresent();
        assertThat(error.get()).isEqualTo("価格は必須です");
    }

    // ========================================
    // 4.2 データ型チェック
    // ========================================

    @Test
    @DisplayName("データ型チェック: 価格が整数でない場合エラー")
    void testValidate_データ型チェック_価格整数() {
        validRecord.setPrice("abc");

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isPresent();
        assertThat(error.get()).isEqualTo("価格は整数で入力してください");
    }

    @Test
    @DisplayName("データ型チェック: 価格が小数の場合エラー")
    void testValidate_データ型チェック_価格小数() {
        validRecord.setPrice("89800.50");

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isPresent();
        assertThat(error.get()).isEqualTo("価格は整数で入力してください");
    }

    @Test
    @DisplayName("データ型チェック: 適用開始日が不正な形式の場合エラー")
    void testValidate_データ型チェック_開始日形式() {
        validRecord.setStartDate("2025/01/01"); // スラッシュ区切り

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isPresent();
        assertThat(error.get()).isEqualTo("適用開始日は yyyy-MM-dd 形式で入力してください");
    }

    @Test
    @DisplayName("データ型チェック: 適用終了日が不正な形式の場合エラー")
    void testValidate_データ型チェック_終了日不正() {
        validRecord.setEndDate("2025-13-01"); // 13月は存在しない

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isPresent();
        assertThat(error.get()).isEqualTo("適用終了日は yyyy-MM-dd 形式で入力してください");
    }

    // ========================================
    // 4.3 文字列長チェック
    // ========================================

    @Test
    @DisplayName("文字列長チェック: カテゴリー名が50文字を超える場合エラー")
    void testValidate_文字列長チェック_カテゴリー名() {
        validRecord.setCategoryName("あ".repeat(51)); // 51文字

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isPresent();
        assertThat(error.get()).isEqualTo("カテゴリー名は50文字以内で入力してください");
    }

    @Test
    @DisplayName("文字列長チェック: 説明文が256文字を超える場合エラー")
    void testValidate_文字列長チェック_説明文() {
        validRecord.setDescription("あ".repeat(257)); // 257文字

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isPresent();
        assertThat(error.get()).isEqualTo("説明文は256文字以内で入力してください");
    }

    @Test
    @DisplayName("文字列長チェック: 説明文が256文字ちょうどの場合OK")
    void testValidate_文字列長チェック_説明文_境界値() {
        validRecord.setDescription("あ".repeat(256)); // 256文字
        when(categoryMapper.existsByName(anyString())).thenReturn(true);

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isEmpty();
    }

    // ========================================
    // 4.4 範囲チェック
    // ========================================

    @Test
    @DisplayName("範囲チェック: 価格が0円の場合エラー")
    void testValidate_範囲チェック_価格下限未満() {
        validRecord.setPrice("0");

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isPresent();
        assertThat(error.get()).isEqualTo("価格は1円以上1億円以下で入力してください");
    }

    @Test
    @DisplayName("範囲チェック: 価格が1円の場合OK（境界値）")
    void testValidate_範囲チェック_価格下限() {
        validRecord.setPrice("1");
        when(categoryMapper.existsByName(anyString())).thenReturn(true);

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isEmpty();
    }

    @Test
    @DisplayName("範囲チェック: 価格が1億円の場合OK（境界値）")
    void testValidate_範囲チェック_価格上限() {
        validRecord.setPrice("100000000");
        when(categoryMapper.existsByName(anyString())).thenReturn(true);

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isEmpty();
    }

    @Test
    @DisplayName("範囲チェック: 価格が1億円を超える場合エラー")
    void testValidate_範囲チェック_価格上限超過() {
        validRecord.setPrice("100000001");

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isPresent();
        assertThat(error.get()).isEqualTo("価格は1円以上1億円以下で入力してください");
    }

    // ========================================
    // 4.6 論理チェック
    // ========================================

    @Test
    @DisplayName("論理チェック: 適用終了日が適用開始日より前の場合エラー")
    void testValidate_論理チェック_日付前後関係() {
        validRecord.setStartDate("2025-12-31");
        validRecord.setEndDate("2025-01-01");

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isPresent();
        assertThat(error.get()).isEqualTo("適用終了日は適用開始日以降の日付を入力してください");
    }

    @Test
    @DisplayName("論理チェック: 適用開始日と適用終了日が同じ日付の場合OK")
    void testValidate_論理チェック_同一日付() {
        validRecord.setStartDate("2025-01-01");
        validRecord.setEndDate("2025-01-01");
        when(categoryMapper.existsByName(anyString())).thenReturn(true);

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isEmpty();
    }

    // ========================================
    // 4.5 参照整合性チェック
    // ========================================

    @Test
    @DisplayName("参照整合性チェック: カテゴリーがカテゴリーマスターに存在しない場合エラー")
    void testValidate_参照整合性チェック_カテゴリー存在しない() {
        validRecord.setCategoryName("不明カテゴリ");

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isPresent();
        assertThat(error.get()).isEqualTo("カテゴリー名 '不明カテゴリ' がカテゴリーマスターに存在しません");
    }

    @Test
    @DisplayName("参照整合性チェック: カテゴリーがカテゴリーマスターに存在する場合OK")
    void testValidate_参照整合性チェック_カテゴリー存在する() {
        // validRecordは「家電」カテゴリーで、モックでtrueを返すように設定済み

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isEmpty();
    }

    // ========================================
    // 総合テスト
    // ========================================

    @Test
    @DisplayName("総合テスト: すべてのバリデーションをパスする正常なレコード")
    void testValidate_正常系_全項目OK() {
        // validRecordはすべて正常値で設定済み

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isEmpty();
    }

    @Test
    @DisplayName("総合テスト: 複数のエラーがある場合、最初のエラーのみ返す")
    void testValidate_異常系_複数エラー() {
        // カテゴリー名が空（最初のエラー）
        validRecord.setCategoryName("");
        // 価格も不正だが、カテゴリー名のチェックが先に実行される
        validRecord.setPrice("abc");

        Optional<String> error = productValidator.validate(validRecord);

        assertThat(error).isPresent();
        // 最初に検出されるエラー（必須チェック）のみ返される
        assertThat(error.get()).isEqualTo("カテゴリー名は必須です");
    }
}
