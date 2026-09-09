package com.macro.mall.recommendation.dto;

import lombok.Data;

/**
 * 服务基础状态响应
 * 支持最新FastAPI接口文档v2.0格式
 * @author zchens
 */
@Data
public class ServiceStatusResponse {

    /**
     * 服务名称
     */
    private String service;

    /**
     * 服务状态
     */
    private String status;

    /**
     * 版本号
     */
    private String version;

    /**
     * 服务描述
     */
    private String description;

    /**
     * 响应时间戳
     */
    private String timestamp;
}
