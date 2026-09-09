package com.macro.mall.portal.service;

import com.macro.mall.model.PmsProduct;
import com.macro.mall.portal.domain.PmsPortalProductDetail;
import com.macro.mall.portal.domain.PmsProductCategoryNode;
import com.macro.mall.recommendation.dto.ProductInfo;

import java.util.List;

/**
 * 前台商品管理Service
 * Created by macro on 2020/4/6.
 */
public interface PmsPortalProductService {
    /**
     * 综合搜索商品
     */
    List<PmsProduct> search(String keyword, Long brandId, Long productCategoryId, Integer pageNum, Integer pageSize, Integer sort);

    /**
     * 以树形结构获取所有商品分类
     */
    List<PmsProductCategoryNode> categoryTreeList();

    /**
     * 获取前台商品详情
     */
    PmsPortalProductDetail detail(Long id);
    
    /**
     * 获取商品信息
     */
    PmsProduct getProductById(Long id);
    
    /**
     * 获取所有商品（用于推荐系统）
     */
    List<ProductInfo> listAllProducts();
    
    /**
     * 获取热门商品（用于推荐系统）
     */
    List<ProductInfo> listPopularProducts(int count);
    
    /**
     * 分批获取人工推荐商品，支持分页处理
     * @param offset 起始位置
     * @param limit 获取数量
     * @return 商品列表
     */
    List<ProductInfo> listRecommendedProductsBatch(int offset, int limit);
}
