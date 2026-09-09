package com.macro.mall.portal.service.impl;

import lombok.Data;

/**
 * 缓存统计信息
 */
@Data
public class CacheStats {

    /**
     * 总缓存数量
     */
    private int totalCached = 0;

    /**
     * 已消费数量
     */
    private int consumedCount = 0;

    /**
     * 最后更新时间
     */
    private long lastUpdateTime;

    /**
     * 缓存命中率（百分比）
     */
    private double hitRate = 0.0;

    /**
     * 缓存剩余数量
     */
    public int getRemainingCount() {
        return Math.max(0, totalCached - consumedCount);
    }

    /**
     * 是否缓存充足
     */
    public boolean isSufficient(int threshold) {
        return getRemainingCount() >= threshold;
    }
}
