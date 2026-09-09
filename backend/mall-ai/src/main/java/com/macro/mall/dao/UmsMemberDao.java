package com.macro.mall.dao;

import com.macro.mall.model.UmsMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UmsMemberDao {
    /**
     * 获取所有会员
     */
    List<UmsMember> listAll();

    /**
     * 根据ID获取会员
     */
    UmsMember getById(@Param("id") Long id);

    /**
     * 创建会员
     */
    int insert(@Param("member") UmsMember member);

    /**
     * 更新会员
     */
    int update(@Param("member") UmsMember member);

    /**
     * 删除会员
     */
    int delete(@Param("id") Long id);
}