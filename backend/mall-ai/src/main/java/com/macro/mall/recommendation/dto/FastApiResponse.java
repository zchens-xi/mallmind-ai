package com.macro.mall.recommendation.dto;

import lombok.Data;

/**
 * FastAPI标准响应格式
 * 支持最新FastAPI接口文档v2.0格式
 * @author macro
 */
@Data
public class FastApiResponse<T> {

    /**
     * 请求是否成功
     */
    private Boolean success;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 错误信息 (失败时)
     */
    private String error;

    /**
     * 请求路径 (失败时)
     */
    private String requestPath;

    /**
     * 响应时间戳
     */
    private String timestamp;
}
