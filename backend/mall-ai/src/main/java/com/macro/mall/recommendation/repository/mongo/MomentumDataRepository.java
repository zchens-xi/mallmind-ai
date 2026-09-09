package com.macro.mall.recommendation.repository.mongo;

import com.macro.mall.recommendation.dto.MomentumData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 动量数据MongoDB仓库
 * @author macro
 */
@Repository
public interface MomentumDataRepository extends MongoRepository<MomentumData, String> {

    /**
     * 根据用户ID和动量类型查找动量数据
     * @param userId 用户ID
     * @param type 动量类型
     * @return 动量数据
     */
    Optional<MomentumData> findByUserIdAndType(Long userId, String type);

    /**
     * 根据用户ID查找所有动量数据
     * @param userId 用户ID
     * @return 动量数据列表
     */
    List<MomentumData> findByUserId(Long userId);

    /**
     * 根据动量类型查找所有动量数据
     * @param type 动量类型
     * @return 动量数据列表
     */
    List<MomentumData> findByType(String type);

    /**
     * 查找短期动量数据，按创建时间倒序
     * @param userId 用户ID
     * @return 短期动量数据列表
     */
    @Query("{'userId': ?0, 'type': 'short_term_momentum'}")
    List<MomentumData> findShortTermMomentumByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 查找长期动量数据
     * @param userId 用户ID
     * @return 长期动量数据
     */
    @Query("{'userId': ?0, 'type': 'long_term_momentum'}")
    Optional<MomentumData> findLongTermMomentumByUserId(Long userId);

    /**
     * 查找推送动量数据
     * @param userId 用户ID
     * @return 推送动量数据
     */
    @Query("{'userId': ?0, 'type': 'push_momentum'}")
    Optional<MomentumData> findPushMomentumByUserId(Long userId);

    /**
     * 删除用户的特定类型动量数据
     * @param userId 用户ID
     * @param type 动量类型
     */
    void deleteByUserIdAndType(Long userId, String type);

    /**
     * 查找所有默认配置的动量数据
     * @return 默认动量数据列表
     */
    List<MomentumData> findByIsDefaultTrue();

    /**
     * 根据数据来源查找动量数据
     * @param dataSource 数据来源
     * @return 动量数据列表
     */
    List<MomentumData> findByDataSource(String dataSource);
}
