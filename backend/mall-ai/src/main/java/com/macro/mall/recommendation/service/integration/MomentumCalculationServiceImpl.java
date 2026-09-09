package com.macro.mall.recommendation.service.integration;

import com.macro.mall.recommendation.client.PythonModelClient;
import com.macro.mall.recommendation.dto.*;
import com.macro.mall.recommendation.service.MomentumCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 动量计算服务实现类
 * 实现了正确的短期动量和推送动量计算逻辑
 * @author zchen
 */
@Slf4j
@Service
public class MomentumCalculationServiceImpl implements MomentumCalculationService {

    @Autowired
    private PythonModelClient pythonModelClient;

    /**
     * 计算短期动量
     * 短期动量的计算是独立的，只需要统计当前点击的商品中的类别、价格、质量
     * 不需要长期动量作为基础
     */
    @Override
    public MomentumData calculateShortTermMomentum(Long userId, List<ProductInfo> clickedProducts, List<Double> clickStrengths) {
        log.info("开始计算用户{}的短期动量，点击商品数量：{}", userId, clickedProducts.size());

        // 构建短期动量请求
        ShortTermMomentumRequest request = new ShortTermMomentumRequest();
        request.setUserId(userId);
        request.setClickedProducts(clickedProducts);
        request.setClickStrengths(clickStrengths);

        try {
            // 直接调用FastAPI计算短期动量，不依赖长期动量
            FastApiResponse<MomentumData> response = pythonModelClient.calculateShortTermMomentum(request);

            if (response.getSuccess()) {
                MomentumData shortTermMomentum = response.getData();
                log.info("短期动量计算成功，类别数量：{}, 总点击数：{}",
                    shortTermMomentum.getClickedCategories() != null ? shortTermMomentum.getClickedCategories().size() : 0,
                    shortTermMomentum.getTotalClicks());
                return shortTermMomentum;
            } else {
                log.error("短期动量计算失败：{}", response.getError());
                return null;
            }
        } catch (Exception e) {
            log.error("调用FastAPI计算短期动量时发生异常", e);
            return null;
        }
    }

    /**
     * 计算推送动量
     * 推送动量需要融合长期动量和短期动量
     */
    @Override
    public MomentumData calculatePushMomentum(MomentumData longTermMomentum, MomentumData shortTermMomentum, MomentumData currentPushMomentum) {
        log.info("开始计算推送动量，融合长期动量和短期动量");

        // 构建推送动量请求
        PushMomentumRequest request = new PushMomentumRequest();
        request.setUserId(longTermMomentum != null ? longTermMomentum.getUserId() : 
                         (shortTermMomentum != null ? shortTermMomentum.getUserId() : 
                         (currentPushMomentum != null ? currentPushMomentum.getUserId() : null)));
        request.setLongTermMomentum(longTermMomentum);
        request.setShortTermMomentum(shortTermMomentum);
        request.setCurrentPushMomentum(currentPushMomentum);

        try {
            FastApiResponse<MomentumData> response = pythonModelClient.calculatePushMomentum(request);

            if (response.getSuccess()) {
                MomentumData pushMomentum = response.getData();
                log.info("推送动量计算成功，累积点击数：{}, 类别偏好数量：{}",
                    pushMomentum.getCumulativeClicks(),
                    pushMomentum.getCategoryPreferences() != null ? pushMomentum.getCategoryPreferences().size() : 0);
                return pushMomentum;
            } else {
                log.error("推送动量计算失败：{}", response.getError());
                return null;
            }
        } catch (Exception e) {
            log.error("调用FastAPI计算推送动量时发生异常", e);
            return null;
        }
    }

    /**
     * 处理单次商品点击，更新短期动量和推送动量
     * 这是完整的处理流程
     * return 更新后的推送动量数据
     */
    @Override
    public MomentumData handleProductClick(Long userId, ProductInfo clickedProduct, Double clickStrength,
                                          MomentumData longTermMomentum, MomentumData currentPushMomentum) {
        log.info("处理用户{}的商品点击：{}", userId, clickedProduct.getProductId());

        // 1. 计算短期动量（独立计算，不需要长期动量）
        List<ProductInfo> clickedProducts = new ArrayList<>();
        clickedProducts.add(clickedProduct);

        List<Double> clickStrengths = new ArrayList<>();
        clickStrengths.add(clickStrength);

        MomentumData shortTermMomentum = calculateShortTermMomentum(userId, clickedProducts, clickStrengths);
        if (shortTermMomentum == null) {
            log.error("短期动量计算失败，无法更新推送动量");
            return currentPushMomentum;
        }

        // 2. 融合计算推送动量
        MomentumData updatedPushMomentum = calculatePushMomentum(longTermMomentum, shortTermMomentum, currentPushMomentum);
        if (updatedPushMomentum == null) {
            log.error("推送动量计算失败，返回当前推送动量");
            return currentPushMomentum;
        }

        log.info("商品点击处理完成，推送动量已更新");
        return updatedPushMomentum;
    }

    /**
     * 批量处理多个商品点击
     */
    @Override
    public MomentumData handleMultipleClicks(Long userId, List<ProductInfo> clickedProducts, List<Double> clickStrengths,
                                           MomentumData longTermMomentum, MomentumData currentPushMomentum) {
        log.info("批量处理用户{}的商品点击，数量：{}", userId, clickedProducts.size());

        // 1. 一次性计算所有点击的短期动量
        MomentumData shortTermMomentum = calculateShortTermMomentum(userId, clickedProducts, clickStrengths);
        if (shortTermMomentum == null) {
            log.error("短期动量计算失败");
            return currentPushMomentum;
        }

        // 2. 更新推送动量
        MomentumData updatedPushMomentum = calculatePushMomentum(longTermMomentum, shortTermMomentum, currentPushMomentum);
        if (updatedPushMomentum == null) {
            log.error("推送动量计算失败");
            return currentPushMomentum;
        }

        return updatedPushMomentum;
    }

    /**
     * 计算长期动量
     * 基于用户历史行为数据计算长期购买偏好动量
     */
    @Override
    public MomentumData calculateLongTermMomentum(Long userId, UserBehaviorData userBehaviorData) {
        log.info("开始计算用户{}的长期动量", userId);

        // 构建长期动量请求
        LongTermMomentumRequest request = new LongTermMomentumRequest();
        request.setUserId(userId);
        request.setUserBehaviorData(userBehaviorData);

        try {
            // 调用FastAPI计算长期动量
            FastApiResponse<MomentumData> response = pythonModelClient.calculateLongTermMomentum(request);

            if (response.getSuccess()) {
                MomentumData longTermMomentum = response.getData();
                log.info("长期动量计算成功，类别偏好数量：{}, 价格模型数据点：{}",
                        longTermMomentum.getCategoryPreferences() != null ? longTermMomentum.getCategoryPreferences().size() : 0,
                        longTermMomentum.getPriceModel() != null ? longTermMomentum.getPriceModel().getCount() : 0);
                return longTermMomentum;
            } else {
                log.error("长期动量计算失败：{}", response.getError());
                return null;
            }
        } catch (Exception e) {
            log.error("调用FastAPI计算长期动量时发生异常", e);
            return null;
        }
    }

    /**
     * 计算单个商品得分
     * @param productInfo 商品信息
     * @param pushMomentum 推送动量
     * @return 商品得分，如果计算失败则返回null
     */
    @Override
    public Double calculateProductScore(ProductInfo productInfo, MomentumData pushMomentum) {
        if (productInfo == null || pushMomentum == null) {
            log.warn("计算商品得分失败：商品信息或推送动量为空");
            return null;
        }

        log.info("开始计算商品 {} 的得分，用户ID: {}", productInfo.getProductId(), pushMomentum.getUserId());

        // 构建商品得分计算请求
        ProductScoreRequest request = new ProductScoreRequest();
        request.setUserId(pushMomentum.getUserId());
        request.setProduct(productInfo);
        request.setMomentum(pushMomentum);

        try {
            // 调用FastAPI计算商品得分
            FastApiResponse<ProductScoreResponse> response = pythonModelClient.calculateProductScore(request);

            if (response.getSuccess() && response.getData() != null) {
                Double score = response.getData().getRecommendationScore();
                log.info("商品 {} 的计算得分为: {}", productInfo.getProductId(), score);
                return score;
            } else {
                log.error("商品得分计算失败：{}", response.getError());
                return null;
            }
        } catch (Exception e) {
            log.error("调用FastAPI计算商品得分时发生异常", e);
            return null;
        }
    }

    /**
     * 基于动量推荐商品
     * @param userId 用户ID
     * @param candidateProducts 候选商品列表
     * @param pushMomentum 推送动量
     * @return 推荐结果，如果计算失败则返回null
     */
    @Override
    public RecommendationResult recommendProducts(Long userId, List<ProductInfo> candidateProducts, MomentumData pushMomentum, int requestCount) {
        if (candidateProducts == null || candidateProducts.isEmpty() || pushMomentum == null) {
            log.warn("推荐商品失败：候选商品列表为空或推送动量为空");
            return null;
        }

        log.info("开始为用户 {} 推荐商品，候选商品数量: {}，请求数量: {}", userId, candidateProducts.size(), requestCount);

        // 构建推荐请求
        RecommendProductsRequest request = new RecommendProductsRequest();
        request.setUserId(userId);
        request.setProducts(candidateProducts);  // 使用正确的字段名
        request.setUserMomentum(pushMomentum);   // 使用正确的字段名
        request.setTopK(requestCount);           // 设置请求的推荐数量

        try {
            // 调用FastAPI进行商品推荐
            FastApiResponse<RecommendationResult> response = pythonModelClient.recommendProducts(request);

            if (response.getSuccess() && response.getData() != null) {
                RecommendationResult result = response.getData();
                log.info("商品推荐成功，返回 {} 个推荐结果", result.getRecommendations() != null ? result.getRecommendations().size() : 0);
                return result;
            } else {
                log.error("商品推荐失败：{}", response.getError());
                return null;
            }
        } catch (Exception e) {
            log.error("调用FastAPI推荐商品时发生异常", e);
            return null;
        }
    }
}
