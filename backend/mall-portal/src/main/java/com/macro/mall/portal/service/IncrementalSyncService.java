package com.macro.mall.portal.service;

import com.macro.mall.recommendation.dto.ProductInfo;

import java.util.List;

/**
 * 增量同步服务接口
 * 负责商品数据的增量更新和时间控制
 */
public interface IncrementalSyncService {

    /**
     * 获取候选商品列表（用于个性化推荐）
     * @param userId 用户ID
     * @param maxSize 最大候选数量
     * @return 候选商品列表
     */
    List<ProductInfo> getCandidateProducts(Long userId, int maxSize);

    /**
     * 检查是否需要更新商品数据缓存
     * @return true表示需要更新
     */
    boolean needsDataRefresh();

    /**
     * 更新商品数据缓存的时间戳
     */
    void updateSyncTimestamp();

    /**
     * 获取热门商品（用于补充推荐结果）
     * @param excludeIds 排除的商品ID列表
     * @param count 需要的数量
     * @return 热门商品列表
     */
    List<ProductInfo> getPopularProducts(List<Long> excludeIds, int count);

    /**
     * 刷新商品基础数据缓存
     * @param forceRefresh 是否强制刷新
     */
    void refreshProductDataCache(boolean forceRefresh);
}

