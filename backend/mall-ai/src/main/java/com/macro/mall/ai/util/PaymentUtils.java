package com.macro.mall.ai.util;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付工具类
 * 提供支付相关的通用工具方法
 */
public class PaymentUtils {

    /**
     * 从HttpServletRequest中提取所有参数
     * 主要用于支付回调参数处理
     */
    public static Map<String, String> extractRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();

        for (String name : requestParams.keySet()) {
            String value = request.getParameter(name);
            if (value != null) {
                params.put(name, value);
            }
        }

        return params;
    }

    /**
     * 验证支付参数是否完整
     */
    public static boolean validatePaymentParams(String outTradeNo, String subject, String totalAmount) {
        return outTradeNo != null && !outTradeNo.trim().isEmpty() &&
               subject != null && !subject.trim().isEmpty() &&
               totalAmount != null && !totalAmount.trim().isEmpty();
    }

    /**
     * 生成订单号（示例实现）
     */
    public static String generateOrderNo() {
        return "ORDER_" + System.currentTimeMillis();
    }
}
