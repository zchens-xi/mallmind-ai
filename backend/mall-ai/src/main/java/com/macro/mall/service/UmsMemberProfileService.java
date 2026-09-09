package com.macro.mall.service;

import com.macro.mall.model.UmsMemberProfile;

import java.util.List;

public interface UmsMemberProfileService {
    /**
     * 获取所有会员资料
     */
    List<UmsMemberProfile> listAll();

    /**
     * 根据会员ID获取资料
     */
    UmsMemberProfile getByMemberId(Long memberId);

    /**
     * 创建会员资料
     */
    int create(UmsMemberProfile profile);

    /**
     * 更新会员资料
     */
    int update(UmsMemberProfile profile);

    /**
     * 删除会员资料
     */
    int delete(Long memberId);
}