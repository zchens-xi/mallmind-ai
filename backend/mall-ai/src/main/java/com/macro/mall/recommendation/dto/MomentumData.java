package com.macro.mall.recommendation.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * 动量数据模型，同时作为MongoDB实体
 * 支持最新的FastAPI接口文档v2.0结构
 * @author macro
 */
@Data
@Document(collection = "momentum_data")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MomentumData {

    @Id
    private String id;

    /**
     * 动量类型: long_term | short_term | push_momentum
     */
    private String type;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 类别偏好权重（Softmax归一化）
     */
    private Map<String, Double> categoryPreferences;

    /**
     * 价格模型
     */
    private StatisticsModel priceModel;

    /**
     * 质量模型
     */
    private StatisticsModel qualityModel;

    /**
     * 最后更新时间
     */
    private String lastUpdated;

    /**
     * 累积点击数 (仅推送动量)
     */
    private Integer cumulativeClicks;

    /**
     * 数据来源：user_behavior_analysis | user_click | cumulative_push_momentum | user_clicks_aggregated | config_default
     */
    private String dataSource;

    /**
     * 是否为默认配置
     */
    private Boolean isDefault;

    // ========== 短期动量特有字段 ==========
    /**
     * 总点击数 (仅短期动量)
     */
    private Integer totalClicks;

    /**
     * 总点击强度 (仅短期动量)
     */
    private Double totalStrength;

    /**
     * 点击的类别列表 (仅短期动量)
     */
    private List<String> clickedCategories;

    // ========== 推送动量特有字段 ==========
    /**
     * 最新更新信息 (仅推送动量)
     */
    private LatestUpdate latestUpdate;
    
    /**
     * 未知属性的存储容器，用于处理Python和Java字段名不一致的情况
     */
    private Map<String, Object> additionalProperties = new HashMap<>();
    
    /**
     * 处理未知属性，将其存储到additionalProperties中
     * 这样可以处理Python和Java字段名不一致的情况
     */
    @JsonAnySetter
    public void handleUnknownProperty(String key, Object value) {
        additionalProperties.put(key, value);
    }
    
    /**
     * 获取所有额外属性
     */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }
    
    /**
     * 默认构造函数，初始化必要的字段
     */
    public MomentumData() {
        this.categoryPreferences = new HashMap<>();
        this.priceModel = new StatisticsModel();
        this.qualityModel = new StatisticsModel();
        this.additionalProperties = new HashMap<>();
    }
    
    /**
     * 初始化默认类别偏好
     */
    public void initDefaultCategoryPreferences() {
        if (this.categoryPreferences == null) {
            this.categoryPreferences = new HashMap<>();
        }
        
        // 添加一些默认的类别偏好
        this.categoryPreferences.put("手机通讯", 0.15);
        this.categoryPreferences.put("笔记本", 0.15);
        this.categoryPreferences.put("家用电器", 0.1);
        this.categoryPreferences.put("服装", 0.1);
        this.categoryPreferences.put("休闲裤", 0.05);
        this.categoryPreferences.put("T恤", 0.05);
        this.categoryPreferences.put("洗衣机", 0.05);
        this.categoryPreferences.put("冰箱", 0.05);
        this.categoryPreferences.put("空调", 0.05);
        this.categoryPreferences.put("厨房小电", 0.05);
        this.categoryPreferences.put("食品", 0.05);
        this.categoryPreferences.put("图书", 0.05);
        this.categoryPreferences.put("美妆", 0.05);
        this.categoryPreferences.put("家居", 0.05);
    }

    @Data
    public static class LatestUpdate {
        /**
         * 长期动量类型
         */
        private String longTermBase;

        /**
         * 更新时间戳
         */
        private String updateTimestamp;

        /**
         * 更新说明
         */
        private String note;
        
        /**
         * 未知属性的存储容器
         */
        private Map<String, Object> additionalProperties = new HashMap<>();
        
        /**
         * 处理未知属性
         */
        @JsonAnySetter
        public void handleUnknownProperty(String key, Object value) {
            additionalProperties.put(key, value);
        }
        
        /**
         * 获取所有额外属性
         */
        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }
        
        /**
         * 默认构造函数
         */
        public LatestUpdate() {
            this.additionalProperties = new HashMap<>();
        }
    }

    /**
     * 统计模型
     */
    @Data
    public static class StatisticsModel {
        private Double mean;
        private Double std;
        private Double skewness;
        private Double min;
        private Double max;
        private Double median;
        private Integer count;
        
        /**
         * 未知属性的存储容器
         */
        private Map<String, Object> additionalProperties = new HashMap<>();
        
        /**
         * 处理未知属性
         */
        @JsonAnySetter
        public void handleUnknownProperty(String key, Object value) {
            additionalProperties.put(key, value);
        }
        
        /**
         * 获取所有额外属性
         */
        @JsonAnyGetter
        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }
        
        /**
         * 默认构造函数，初始化基本值
         */
        public StatisticsModel() {
            this.mean = 0.0;
            this.std = 1.0;
            this.skewness = 0.0;
            this.min = 0.0;
            this.max = 0.0;
            this.median = 0.0;
            this.count = 0;
            this.additionalProperties = new HashMap<>();
        }
    }
}
