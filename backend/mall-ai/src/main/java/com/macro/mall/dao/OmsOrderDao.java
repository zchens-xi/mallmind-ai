package com.macro.mall.dao;

import com.macro.mall.dto.OmsOrderQueryParam;
import com.macro.mall.model.OmsOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OmsOrderDao {
    /**
     * 条件查询订单
     */
    List<OmsOrder> list(@Param("queryParam") OmsOrderQueryParam queryParam);

    /**
     * 获取所有订单
     */
    List<OmsOrder> listAll();

    /**
     * 根据ID获取订单
     */
    OmsOrder getById(@Param("id") Long id);

    /**
     * 创建订单
     */
    int insert(@Param("order") OmsOrder order);

    /**
     * 更新订单
     */
    int update(@Param("order") OmsOrder order);

    /**
     * 删除订单
     */
    int delete(@Param("id") Long id);
}