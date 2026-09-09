package com.macro.mall.recommendation.service;

import com.macro.mall.recommendation.dto.UserBehaviorData;

/**
 * 用户行为数据服务
 * 负责准备和格式化用户行为数据，确保包含正确的字段名称
 * @author macro
 */
public interface UserBehaviorDataService {
    
    /**
     * 获取用户行为数据
     * 确保包含正确的字段名称，特别是类别、价格和评分字段
     * @param userId 用户ID
     * @return 格式化的用户行为数据
     */
    UserBehaviorData getUserBehaviorData(Long userId);
} 