package com.example.productimport.mapper;

import com.example.productimport.entity.ProductMaster;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品マスターMapperインターフェース
 * 商品マスターテーブルへのアクセスを提供
 */
@Mapper
public interface ProductMapper {

    /**
     * 型番と適用開始日で商品を検索（ユニークキー検索）
     *
     * @param modelNumber 型番
     * @param startDate 適用開始日（yyyy-MM-dd形式の文字列）
     * @return 商品マスターエンティティ、存在しない場合null
     */
    ProductMaster findByModelNumberAndStartDate(
            @Param("modelNumber") String modelNumber,
            @Param("startDate") String startDate
    );

    /**
     * 商品マスター新規登録
     *
     * @param product 登録する商品マスターエンティティ
     * @return 登録件数
     */
    int insert(ProductMaster product);

    /**
     * 商品マスター更新
     *
     * @param product 更新する商品マスターエンティティ
     * @return 更新件数
     */
    int update(ProductMaster product);
}
