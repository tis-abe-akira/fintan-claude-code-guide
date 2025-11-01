package com.example.productimport.mapper;

import com.example.productimport.entity.CategoryMaster;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * カテゴリーマスターMapperインターフェース
 * カテゴリーマスターテーブルへのアクセスを提供
 */
@Mapper
public interface CategoryMapper {

    /**
     * カテゴリー名の存在チェック
     *
     * @param categoryName カテゴリー名
     * @return 存在する場合true、存在しない場合false
     */
    boolean existsByName(@Param("categoryName") String categoryName);

    /**
     * カテゴリー名でカテゴリーを検索
     *
     * @param categoryName カテゴリー名
     * @return カテゴリーマスターエンティティ、存在しない場合null
     */
    CategoryMaster findByName(@Param("categoryName") String categoryName);
}
