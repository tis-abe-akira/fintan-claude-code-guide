package com.example.productimport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * バリデーションエラー情報
 * エラーファイル出力用のデータを保持
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationError {

    /**
     * 行番号（CSV内の行番号）
     */
    private Long lineNumber;

    /**
     * エラー理由
     */
    private String errorMessage;

    /**
     * 元のCSV行データ
     */
    private String originalLine;

    /**
     * エラーが発生した項目名（オプション）
     */
    private String fieldName;
}
