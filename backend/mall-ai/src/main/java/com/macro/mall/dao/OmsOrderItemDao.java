package com.macro.mall.dao;

import com.macro.mall.model.OmsOrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OmsOrderItemDao {
    /**
     * 获取所有订单项
     */
    List<OmsOrderItem> listAll();

    /**
     * 根据订单ID获取订单项
     */
    List<OmsOrderItem> listByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据ID获取订单项
     */
    OmsOrderItem getById(@Param("id") Long id);

    /**
     * 创建订单项
     */
    int insert(@Param("orderItem") OmsOrderItem orderItem);

    /**
     * 更新订单项
     */
    int update(@Param("orderItem") OmsOrderItem orderItem);

    /**
     * 删除订单项
     */
    int delete(@Param("id") Long id);
}