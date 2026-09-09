package com.macro.mall.dao;

import com.macro.mall.model.UmsMemberProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UmsMemberProfileDao {
    /**
     * 获取所有会员资料
     */
    List<UmsMemberProfile> listAll();

    /**
     * 根据会员ID获取资料
     */
    UmsMemberProfile getByMemberId(@Param("memberId") Long memberId);

    /**
     * 创建会员资料
     */
    int insert(@Param("profile") UmsMemberProfile profile);

    /**
     * 更新会员资料
     */
    int update(@Param("profile") UmsMemberProfile profile);

    /**
     * 删除会员资料
     */
    int delete(@Param("memberId") Long memberId);
}