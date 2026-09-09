package com.macro.mall.mapper;

import com.macro.mall.model.CsQuickReply;
import com.macro.mall.model.CsQuickReplyExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CsQuickReplyMapper {
    long countByExample(CsQuickReplyExample example);

    int deleteByExample(CsQuickReplyExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CsQuickReply row);

    int insertSelective(CsQuickReply row);

    List<CsQuickReply> selectByExampleWithBLOBs(CsQuickReplyExample example);

    List<CsQuickReply> selectByExample(CsQuickReplyExample example);

    CsQuickReply selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") CsQuickReply row, @Param("example") CsQuickReplyExample example);

    int updateByExampleWithBLOBs(@Param("row") CsQuickReply row, @Param("example") CsQuickReplyExample example);

    int updateByExample(@Param("row") CsQuickReply row, @Param("example") CsQuickReplyExample example);

    int updateByPrimaryKeySelective(CsQuickReply row);

    int updateByPrimaryKeyWithBLOBs(CsQuickReply row);

    int updateByPrimaryKey(CsQuickReply row);
}