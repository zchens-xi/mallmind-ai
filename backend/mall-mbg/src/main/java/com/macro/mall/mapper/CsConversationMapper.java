package com.macro.mall.mapper;

import com.macro.mall.model.CsConversation;
import com.macro.mall.model.CsConversationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CsConversationMapper {
    long countByExample(CsConversationExample example);

    int deleteByExample(CsConversationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CsConversation row);

    int insertSelective(CsConversation row);

    List<CsConversation> selectByExample(CsConversationExample example);

    CsConversation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") CsConversation row, @Param("example") CsConversationExample example);

    int updateByExample(@Param("row") CsConversation row, @Param("example") CsConversationExample example);

    int updateByPrimaryKeySelective(CsConversation row);

    int updateByPrimaryKey(CsConversation row);
}