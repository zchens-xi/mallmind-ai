package com.macro.mall.service;

import com.macro.mall.model.OmsOrderItem;

import java.util.List;

public interface OmsOrderItemService {
    /**
     * 获取所有订单项
     */
    List<OmsOrderItem> listAll();

    /**
     * 根据订单ID获取订单项
     */
    List<OmsOrderItem> listByOrderId(Long orderId);

    /**
     * 根据ID获取订单项
     */
    OmsOrderItem getById(Long id);

    /**
     * 创建订单项
     */
    int create(OmsOrderItem orderItem);

    /**
     * 更新订单项
     */
    int update(OmsOrderItem orderItem);

    /**
     * 删除订单项
     */
    int delete(Long id);
}