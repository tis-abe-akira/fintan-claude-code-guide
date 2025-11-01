package com.example.productimport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品マスターCSV入力レコード
 * CSVファイルから読み込んだ1行分のデータを保持
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCsvRecord {

    /**
     * CSVファイル内の行番号（エラー出力用）
     */
    private Long lineNumber;

    /**
     * カテゴリー名
     */
    private String categoryName;

    /**
     * メーカー
     */
    private String manufacturer;

    /**
     * 型番
     */
    private String modelNumber;

    /**
     * 商品名
     */
    private String productName;

    /**
     * 説明文
     */
    private String description;

    /**
     * 写真（S3オブジェクト名）
     */
    private String photo;

    /**
     * 価格（文字列のまま保持、バリデーションで整数変換）
     */
    private String price;

    /**
     * 適用開始日（文字列のまま保持、バリデーションで日付変換）
     */
    private String startDate;

    /**
     * 適用終了日（文字列のまま保持、バリデーションで日付変換）
     */
    private String endDate;

    /**
     * 元のCSV行データ（エラー出力用）
     */
    private String originalLine;
}
