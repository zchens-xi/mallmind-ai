package com.macro.mall.recommendation.service;

import com.macro.mall.recommendation.dto.MomentumData;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 推荐服务接口
 * 负责管理用户动量数据的存储和检索
 */
@Service
public interface RecommendationService {

    /**
     * 保存长期动量数据
     *
     * @param userId      用户ID
     * @param momentumData 长期动量数据
     * @return 保存后的动量数据（包含MongoDB生成的ID）
     */
    MomentumData saveLongTermMomentum(Long userId, MomentumData momentumData);

    /**
     * 保存短期动量数据
     *
     * @param userId      用户ID
     * @param momentumData 短期动量数据
     * @return 保存后的动量数据
     */
    MomentumData saveShortTermMomentum(Long userId, MomentumData momentumData);

    /**
     * 保存推送动量数据
     *
     * @param userId      用户ID
     * @param momentumData 推送动量数据
     * @return 保存后的动量数据
     */
    MomentumData savePushMomentum(Long userId, MomentumData momentumData);

    /**
     * 获取用户最新的长期动量数据
     *
     * @param userId 用户ID
     * @return 最新的长期动量数据，如果不存在返回null
     */
    MomentumData getLatestLongTermMomentum(Long userId);

    /**
     * 获取用户最新的短期动量数据
     *
     * @param userId 用户ID
     * @return 最新的短期动量数据，如果不存在返回null
     */
    MomentumData getLatestShortTermMomentum(Long userId);

    /**
     * 获取用户最新的推送动量数据
     *
     * @param userId 用户ID
     * @return 最新的推送动量数据，如果不存在返回null
     */
    MomentumData getLatestPushMomentum(Long userId);

    /**
     * 删除用户的所有动量数据
     *
     * @param userId 用户ID
     */
    void deleteAllMomentumData(Long userId);

    /**
     * 删除用户指定类型的动量数据
     *
     * @param userId 用户ID
     * @param type   动量类型 (long_term | short_term | push_momentum)
     */
    void deleteMomentumDataByType(Long userId, String type);

    /**
     * 获取用户所有动量数据历史记录
     *
     * @param userId 用户ID
     * @param type   动量类型，可选，如果为null则返回所有类型
     * @param limit  返回记录数限制
     * @return 动量数据列表，按创建时间倒序
     */
    List<MomentumData> getMomentumDataHistory(Long userId, String type, Integer limit);

    /**
     * 检查用户是否有长期动量数据
     *
     * @param userId 用户ID
     * @return true如果存在，false如果不存在
     */
    boolean hasLongTermMomentum(Long userId);

    /**
     * 批量保存动量数据
     *
     * @param momentumDataList 动量数据列表
     * @return 保存后的动量数据列表
     */
    List<MomentumData> batchSaveMomentumData(List<MomentumData> momentumDataList);
}