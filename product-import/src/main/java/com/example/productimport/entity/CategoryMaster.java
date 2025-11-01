package com.example.productimport.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * カテゴリーマスターエンティティ
 * 商品のカテゴリー情報を管理
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMaster {

    /**
     * カテゴリーID（主キー、自動採番）
     */
    private Long categoryId;

    /**
     * カテゴリー名（ユニーク制約）
     */
    private String categoryName;
}
