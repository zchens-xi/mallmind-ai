package com.macro.mall.service;

import com.macro.mall.dto.*;

import java.util.Date;
import java.util.List;

/**
 * 商品分析Service
 */
public interface ProductAnalyticsService {

    /**
     * 获取商品概览数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 商品概览数据
     */
    ProductOverviewDto getProductOverview(Date startDate, Date endDate);

    /**
     * 获取商品销售排行
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 返回条数
     * @return 商品销售排行列表
     */
    List<ProductSalesRankDto> getProductSalesRank(Date startDate, Date endDate, Integer limit);

    /**
     * 获取商品浏览排行
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 返回条数
     * @return 商品浏览排行列表
     */
    List<ProductViewRankDto> getProductViewRank(Date startDate, Date endDate, Integer limit);

    /**
     * 获取商品转化率排行
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 返回条数
     * @return 商品转化率排行列表
     */
    List<ProductConversionRankDto> getProductConversionRank(Date startDate, Date endDate, Integer limit);

    /**
     * 获取商品价格区间分布
     * @return 商品价格区间分布列表
     */
    List<ProductPriceDistributionDto> getProductPriceDistribution();

    /**
     * 获取商品库存分布
     * @return 商品库存分布列表
     */
    List<ProductStockDistributionDto> getProductStockDistribution();
}
