package com.macro.mall.service;

import com.macro.mall.model.OmsCartItem;

import java.util.List;

public interface OmsCartItemService {
    /**
     * 获取所有购物车项
     */
    List<OmsCartItem> listAll();

    /**
     * 根据会员ID获取购物车项
     */
    List<OmsCartItem> listByMemberId(Long memberId);

    /**
     * 根据ID获取购物车项
     */
    OmsCartItem getById(Long id);

    /**
     * 创建购物车项
     */
    int create(OmsCartItem cartItem);

    /**
     * 更新购物车项
     */
    int update(OmsCartItem cartItem);

    /**
     * 删除购物车项
     */
    int delete(Long id);
}