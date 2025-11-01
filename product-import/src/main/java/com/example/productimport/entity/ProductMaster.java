package com.example.productimport.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商品マスターエンティティ
 * 商品の基本情報と価格情報を管理
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMaster {

    /**
     * 商品ID（主キー、自動採番）
     */
    private Long productId;

    /**
     * カテゴリー名（NOT NULL、最大50文字）
     * カテゴリーマスターに存在する必要がある
     */
    private String categoryName;

    /**
     * メーカー（NOT NULL、最大50文字）
     */
    private String manufacturer;

    /**
     * 型番（NOT NULL、最大50文字、ユニークキー①）
     */
    private String modelNumber;

    /**
     * 商品名（NOT NULL、最大50文字）
     */
    private String productName;

    /**
     * 説明文（NOT NULL、最大256文字）
     */
    private String description;

    /**
     * 写真（NOT NULL、最大50文字）
     * S3オブジェクト名（ダミー）
     */
    private String photo;

    /**
     * 価格（NOT NULL、1〜100,000,000円）
     */
    private Integer price;

    /**
     * 適用開始日（NOT NULL、ユニークキー②）
     */
    private LocalDate startDate;

    /**
     * 適用終了日（NOT NULL）
     * 適用開始日以降である必要がある
     */
    private LocalDate endDate;

    /**
     * 作成日時
     */
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    private LocalDateTime updatedAt;
}
