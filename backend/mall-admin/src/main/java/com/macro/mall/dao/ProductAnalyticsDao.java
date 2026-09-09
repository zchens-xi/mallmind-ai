package com.macro.mall.dao;

import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 商品分析Dao
 */
public interface ProductAnalyticsDao {

    /**
     * 获取商品总数
     */
    Long getTotalProductCount();

    /**
     * 获取在售商品总数
     */
    Long getOnSaleProductCount();

    /**
     * 获取总浏览量（从MongoDB）
     */
    Long getTotalViewCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取总收藏量（从MongoDB）
     */
    Long getTotalCollectCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取浏览用户数（从MongoDB）
     */
    Long getViewUserCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取商品分类销售占比
     */
    List<Map<String, Object>> getCategorySalesPercent(@Param("startDate") Date startDate,
                                                      @Param("endDate") Date endDate);

    /**
     * 获取商品销售排行
     */
    List<Map<String, Object>> getProductSalesRank(@Param("startDate") Date startDate,
                                                  @Param("endDate") Date endDate,
                                                  @Param("limit") Integer limit);

    /**
     * 获取商品浏览排行（从MongoDB获取浏览数据）
     */
    List<Map<String, Object>> getProductViewRank(@Param("startDate") Date startDate,
                                                 @Param("endDate") Date endDate,
                                                 @Param("limit") Integer limit);

    /**
     * 获取商品转化率排行
     */
    List<Map<String, Object>> getProductConversionRank(@Param("startDate") Date startDate,
                                                       @Param("endDate") Date endDate,
                                                       @Param("limit") Integer limit);

    /**
     * 获取商品价格区间分布
     */
    List<Map<String, Object>> getProductPriceDistribution();

    /**
     * 获取商品库存分布
     */
    List<Map<String, Object>> getProductStockDistribution();

    /**
     * 获取商品购买用户数
     */
    List<Map<String, Object>> getProductBuyUserStats(@Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate);

    /**
     * 获取商品浏览用户数统计
     */
    List<Map<String, Object>> getProductViewUserStats(@Param("startDate") Date startDate,
                                                      @Param("endDate") Date endDate);

    /**
     * 获取购买用户数
     */
    Long getBuyUserCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取加购车数量统计
     */
    Long getCartCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
