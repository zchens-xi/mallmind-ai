package com.macro.mall.dao;

import com.macro.mall.model.UmsMemberReceiveAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UmsMemberReceiveAddressDao {
    /**
     * 获取所有收货地址
     */
    List<UmsMemberReceiveAddress> listAll();

    /**
     * 根据会员ID获取收货地址
     */
    List<UmsMemberReceiveAddress> listByMemberId(@Param("memberId") Long memberId);

    /**
     * 根据ID获取收货地址
     */
    UmsMemberReceiveAddress getById(@Param("id") Long id);

    /**
     * 创建收货地址
     */
    int insert(@Param("address") UmsMemberReceiveAddress address);

    /**
     * 更新收货地址
     */
    int update(@Param("address") UmsMemberReceiveAddress address);

    /**
     * 删除收货地址
     */
    int delete(@Param("id") Long id);
}