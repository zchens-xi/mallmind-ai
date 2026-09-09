package com.macro.mall.service;

import com.macro.mall.dto.OmsOrderQueryParam;
import com.macro.mall.model.OmsOrder;

import java.util.List;

public interface OmsOrderService {
    /**
     * 条件查询订单
     */
    List<OmsOrder> list(OmsOrderQueryParam queryParam);

    /**
     * 获取所有订单
     */
    List<OmsOrder> listAll();

    /**
     * 根据ID获取订单
     */
    OmsOrder getById(Long id);

    /**
     * 创建订单
     */
    int create(OmsOrder order);

    /**
     * 更新订单
     */
    int update(OmsOrder order);

    /**
     * 删除订单
     */
    int delete(Long id);
}