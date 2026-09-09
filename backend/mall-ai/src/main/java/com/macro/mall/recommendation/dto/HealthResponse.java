package com.macro.mall.recommendation.dto;

import lombok.Data;

/**
 * 健康检查响应
 * 支持最新FastAPI接口文档v2.0格式
 * @author macro
 */
@Data
public class HealthResponse {

    /**
     * 服务状态
     */
    private String status;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 服务信息
     */
    private ServiceInfo serviceInfo;

    @Data
    public static class ServiceInfo {
        /**
         * 算法引擎状态
         */
        private String algorithmEngine;

        /**
         * 依赖项状态
         */
        private String dependencies;

        /**
         * 数据存储状态
         */
        private String dataStorage;
    }
}
