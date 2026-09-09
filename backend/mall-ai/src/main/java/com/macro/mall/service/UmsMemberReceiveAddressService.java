package com.macro.mall.service;

import com.macro.mall.model.UmsMemberReceiveAddress;

import java.util.List;

public interface UmsMemberReceiveAddressService {
    /**
     * 获取所有收货地址
     */
    List<UmsMemberReceiveAddress> listAll();

    /**
     * 根据会员ID获取收货地址
     */
    List<UmsMemberReceiveAddress> listByMemberId(Long memberId);

    /**
     * 根据ID获取收货地址
     */
    UmsMemberReceiveAddress getById(Long id);

    /**
     * 创建收货地址
     */
    int create(UmsMemberReceiveAddress address);

    /**
     * 更新收货地址
     */
    int update(UmsMemberReceiveAddress address);

    /**
     * 删除收货地址
     */
    int delete(Long id);
}