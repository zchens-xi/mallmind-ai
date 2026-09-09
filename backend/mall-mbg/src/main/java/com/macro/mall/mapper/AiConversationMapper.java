package com.macro.mall.mapper;

import com.macro.mall.model.AiConversation;
import com.macro.mall.model.AiConversationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface AiConversationMapper {
    long countByExample(AiConversationExample example);

    int deleteByExample(AiConversationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AiConversation row);

    int insertSelective(AiConversation row);

    List<AiConversation> selectByExample(AiConversationExample example);

    AiConversation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") AiConversation row, @Param("example") AiConversationExample example);

    int updateByExample(@Param("row") AiConversation row, @Param("example") AiConversationExample example);

    int updateByPrimaryKeySelective(AiConversation row);

    int updateByPrimaryKey(AiConversation row);
}