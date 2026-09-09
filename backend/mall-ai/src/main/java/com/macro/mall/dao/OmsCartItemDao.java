package com.macro.mall.dao;

import com.macro.mall.model.OmsCartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OmsCartItemDao {
    /**
     * 获取所有购物车项
     */
    List<OmsCartItem> listAll();

    /**
     * 根据会员ID获取购物车项
     */
    List<OmsCartItem> listByMemberId(@Param("memberId") Long memberId);

    /**
     * 根据ID获取购物车项
     */
    OmsCartItem getById(@Param("id") Long id);

    /**
     * 创建购物车项
     */
    int insert(@Param("cartItem") OmsCartItem cartItem);

    /**
     * 更新购物车项
     */
    int update(@Param("cartItem") OmsCartItem cartItem);

    /**
     * 删除购物车项
     */
    int delete(@Param("id") Long id);
}