package com.macro.mall.mapper;

import com.macro.mall.model.UmsMemberProfile;
import com.macro.mall.model.UmsMemberProfileExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UmsMemberProfileMapper {
    long countByExample(UmsMemberProfileExample example);

    int deleteByExample(UmsMemberProfileExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UmsMemberProfile row);

    int insertSelective(UmsMemberProfile row);

    List<UmsMemberProfile> selectByExampleWithBLOBs(UmsMemberProfileExample example);

    List<UmsMemberProfile> selectByExample(UmsMemberProfileExample example);

    UmsMemberProfile selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") UmsMemberProfile row, @Param("example") UmsMemberProfileExample example);

    int updateByExampleWithBLOBs(@Param("row") UmsMemberProfile row, @Param("example") UmsMemberProfileExample example);

    int updateByExample(@Param("row") UmsMemberProfile row, @Param("example") UmsMemberProfileExample example);

    int updateByPrimaryKeySelective(UmsMemberProfile row);

    int updateByPrimaryKeyWithBLOBs(UmsMemberProfile row);

    int updateByPrimaryKey(UmsMemberProfile row);
}