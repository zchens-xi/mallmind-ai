package com.macro.mall.recommendation.service.impl;

import com.macro.mall.recommendation.dto.MomentumData;
import com.macro.mall.recommendation.repository.mongo.MomentumDataRepository;
import com.macro.mall.recommendation.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Autowired
    private MomentumDataRepository momentumDataRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public MomentumData saveLongTermMomentum(Long userId, MomentumData momentumData) {
        try {
            // 设置基本信息
            momentumData.setUserId(userId);
            momentumData.setType("long_term_momentum");
            momentumData.setLastUpdated(LocalDateTime.now().format(DATE_FORMATTER));
            
            // 确保类别偏好不为空
            if (momentumData.getCategoryPreferences() == null || momentumData.getCategoryPreferences().isEmpty()) {
                log.info("用户{}的长期动量类别偏好为空，初始化默认类别偏好", userId);
                momentumData.initDefaultCategoryPreferences();
            }
            
            // 确保价格模型和质量模型不为空
            if (momentumData.getPriceModel() == null) {
                momentumData.setPriceModel(new MomentumData.StatisticsModel());
            }
            
            if (momentumData.getQualityModel() == null) {
                momentumData.setQualityModel(new MomentumData.StatisticsModel());
            }

            // 保存到MongoDB
            MomentumData savedData = momentumDataRepository.save(momentumData);
            log.info("长期动量数据已保存: userId={}, id={}, 类别数量={}", 
                    userId, savedData.getId(), 
                    savedData.getCategoryPreferences() != null ? savedData.getCategoryPreferences().size() : 0);
            return savedData;
        } catch (Exception e) {
            log.error("保存长期动量数据失败: userId={}", userId, e);
            throw new RuntimeException("保存长期动量数据失败", e);
        }
    }

    @Override
    public MomentumData saveShortTermMomentum(Long userId, MomentumData momentumData) {
        try {
            // 设置基本信息
            momentumData.setUserId(userId);
            momentumData.setType("short_term_momentum");
            momentumData.setLastUpdated(LocalDateTime.now().format(DATE_FORMATTER));
            
            // 确保类别偏好不为空
            if (momentumData.getCategoryPreferences() == null || momentumData.getCategoryPreferences().isEmpty()) {
                log.info("用户{}的短期动量类别偏好为空，初始化默认类别偏好", userId);
                momentumData.initDefaultCategoryPreferences();
            }

            // 保存到MongoDB
            MomentumData savedData = momentumDataRepository.save(momentumData);
            log.info("短期动量数据已保存: userId={}, id={}", userId, savedData.getId());
            return savedData;
        } catch (Exception e) {
            log.error("保存短期动量数据失败: userId={}", userId, e);
            throw new RuntimeException("保存短期动量数据失败", e);
        }
    }

    @Override
    public MomentumData savePushMomentum(Long userId, MomentumData momentumData) {
        try {
            // 设置基本信息
            momentumData.setUserId(userId);
            momentumData.setType("push_momentum");
            momentumData.setLastUpdated(LocalDateTime.now().format(DATE_FORMATTER));
            
            // 确保类别偏好不为空
            if (momentumData.getCategoryPreferences() == null || momentumData.getCategoryPreferences().isEmpty()) {
                log.info("用户{}的推送动量类别偏好为空，初始化默认类别偏好", userId);
                momentumData.initDefaultCategoryPreferences();
            }
            
            // 确保价格模型和质量模型不为空
            if (momentumData.getPriceModel() == null) {
                momentumData.setPriceModel(new MomentumData.StatisticsModel());
            }
            
            if (momentumData.getQualityModel() == null) {
                momentumData.setQualityModel(new MomentumData.StatisticsModel());
            }

            // 保存到MongoDB
            MomentumData savedData = momentumDataRepository.save(momentumData);
            log.info("推送动量数据已保存: userId={}, id={}", userId, savedData.getId());
            return savedData;
        } catch (Exception e) {
            log.error("保存推送动量数据失败: userId={}", userId, e);
            throw new RuntimeException("保存推送动量数据失败", e);
        }
    }

    @Override
    public MomentumData getLatestLongTermMomentum(Long userId) {
        try {
            Optional<MomentumData> result = momentumDataRepository.findLongTermMomentumByUserId(userId);
            if (result.isPresent()) {
                log.info("获取用户{}最新长期动量成功: id={}", userId, result.get().getId());
            } else {
                log.warn("未找到用户{}的长期动量数据", userId);
            }
            return result.orElse(null);
        } catch (Exception e) {
            log.error("获取用户{}最新长期动量失败", userId, e);
            return null;
        }
    }

    @Override
    public MomentumData getLatestShortTermMomentum(Long userId) {
        try {
            List<MomentumData> results = momentumDataRepository.findShortTermMomentumByUserIdOrderByCreatedAtDesc(userId);
            MomentumData result = results.isEmpty() ? null : results.get(0);
            if (result != null) {
                log.info("获取用户{}最新短期动量成功: id={}", userId, result.getId());
            } else {
                log.warn("未找到用户{}的短期动量数据", userId);
            }
            return result;
        } catch (Exception e) {
            log.error("获取用户{}最新短期动量失败", userId, e);
            return null;
        }
    }

    @Override
    public MomentumData getLatestPushMomentum(Long userId) {
        try {
            Optional<MomentumData> result = momentumDataRepository.findPushMomentumByUserId(userId);
            if (result.isPresent()) {
                log.info("获取用户{}最新推送动量成功: id={}", userId, result.get().getId());
            } else {
                log.warn("未找到用户{}的推送动量数据", userId);
            }
            return result.orElse(null);
        } catch (Exception e) {
            log.error("获取用户{}最新推送动量失败", userId, e);
            return null;
        }
    }

    @Override
    public void deleteAllMomentumData(Long userId) {
        try {
            List<MomentumData> allData = momentumDataRepository.findByUserId(userId);
            momentumDataRepository.deleteAll(allData);
            log.info("删除用户{}的所有动量数据，共删除{}条", userId, allData.size());
        } catch (Exception e) {
            log.error("删除用户{}所有动量数据失败", userId, e);
            throw new RuntimeException("删除动量数据失败", e);
        }
    }

    @Override
    public void deleteMomentumDataByType(Long userId, String type) {
        try {
            momentumDataRepository.deleteByUserIdAndType(userId, type);
            log.info("删除用户{}的{}类型动量数据", userId, type);
        } catch (Exception e) {
            log.error("删除用户{}的{}类型动量数据失败", userId, type, e);
            throw new RuntimeException("删除动量数据失败", e);
        }
    }

    @Override
    public List<MomentumData> getMomentumDataHistory(Long userId, String type, Integer limit) {
        try {
            List<MomentumData> results;
            if (type != null && !type.trim().isEmpty()) {
                Optional<MomentumData> singleResult = momentumDataRepository.findByUserIdAndType(userId, type);
                results = singleResult.isPresent() ?
                    Collections.singletonList(singleResult.get()) :
                    new ArrayList<>();
            } else {
                results = momentumDataRepository.findByUserId(userId);
            }

            // 如果需要限制数量，取前N条
            if (limit != null && limit > 0 && results.size() > limit) {
                results = results.subList(0, limit);
            }

            log.info("获取用户{}动量数据历史，类型: {}, 限制: {}, 结果数: {}", userId, type, limit, results.size());
            return results;
        } catch (Exception e) {
            log.error("获取用户{}动量数据历史失败", userId, e);
            throw new RuntimeException("获取动量数据历史失败", e);
        }
    }

    @Override
    public boolean hasLongTermMomentum(Long userId) {
        try {
            boolean exists = momentumDataRepository.findLongTermMomentumByUserId(userId).isPresent();
            log.info("用户{}是否有长期动量数据: {}", userId, exists);
            return exists;
        } catch (Exception e) {
            log.error("检查用户{}长期动量数据失败", userId, e);
            return false;
        }
    }

    @Override
    public List<MomentumData> batchSaveMomentumData(List<MomentumData> momentumDataList) {
        try {
            // 设置保存时间
            String currentTime = LocalDateTime.now().format(DATE_FORMATTER);
            for (MomentumData data : momentumDataList) {
                if (data.getLastUpdated() == null) {
                    data.setLastUpdated(currentTime);
                }
                
                // 确保类别偏好不为空，避免警告
                if (data.getCategoryPreferences() == null || data.getCategoryPreferences().isEmpty()) {
                    log.warn("用户ID={}的动量数据中类别偏好为空，这可能导致推荐不准确", data.getUserId());
                    data.initDefaultCategoryPreferences();
                }
            }

            // 批量保存
            List<MomentumData> savedList = momentumDataRepository.saveAll(momentumDataList);
            log.info("批量保存动量数据成功，共保存{}条", savedList.size());
            return savedList;
        } catch (Exception e) {
            log.error("批量保存动量数据失败", e);
            throw new RuntimeException("批量保存动量数据失败", e);
        }
    }
}
