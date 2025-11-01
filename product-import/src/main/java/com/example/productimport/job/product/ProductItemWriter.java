package com.example.productimport.job.product;

import com.example.productimport.entity.ProductMaster;
import com.example.productimport.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

/**
 * 商品マスターインポート Writer
 * UPSERT処理（既存レコードがあればUPDATE、なければINSERT）を実行
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductItemWriter implements ItemWriter<ProductMaster> {

    private final ProductMapper productMapper;

    /**
     * 商品マスターをデータベースに登録/更新
     * ユニークキー（型番、適用開始日）で既存レコードを検索し、
     * 存在すればUPDATE、存在しなければINSERTを実行
     *
     * @param chunk 処理対象のProductMasterチャンク
     * @throws Exception 処理エラー
     */
    @Override
    public void write(Chunk<? extends ProductMaster> chunk) throws Exception {
        for (ProductMaster product : chunk) {
            // ユニークキー（型番、適用開始日）で既存レコードを検索
            ProductMaster existingProduct = productMapper.findByModelNumberAndStartDate(
                    product.getModelNumber(),
                    product.getStartDate().toString()
            );

            if (existingProduct != null) {
                // 既存レコードがある場合はUPDATE
                product.setProductId(existingProduct.getProductId());
                int updatedCount = productMapper.update(product);

                if (updatedCount > 0) {
                    log.info("商品更新: 型番={}, 適用開始日={}, 商品名={}",
                            product.getModelNumber(),
                            product.getStartDate(),
                            product.getProductName());
                } else {
                    log.warn("商品更新失敗: 型番={}, 適用開始日={}",
                            product.getModelNumber(),
                            product.getStartDate());
                }
            } else {
                // 既存レコードがない場合はINSERT
                int insertedCount = productMapper.insert(product);

                if (insertedCount > 0) {
                    log.info("商品登録: 型番={}, 適用開始日={}, 商品名={}",
                            product.getModelNumber(),
                            product.getStartDate(),
                            product.getProductName());
                } else {
                    log.warn("商品登録失敗: 型番={}, 適用開始日={}",
                            product.getModelNumber(),
                            product.getStartDate());
                }
            }
        }

        log.debug("{}件の商品をデータベースに書き込みました", chunk.size());
    }
}
