package com.macro.mall.service;

import com.macro.mall.model.UmsMember;

import java.util.List;

public interface UmsMemberService {
    /**
     * 获取所有会员
     */
    List<UmsMember> listAll();

    /**
     * 根据ID获取会员
     */
    UmsMember getById(Long id);

    /**
     * 创建会员
     */
    int create(UmsMember member);

    /**
     * 更新会员
     */
    int update(UmsMember member);

    /**
     * 删除会员
     */
    int delete(Long id);
}