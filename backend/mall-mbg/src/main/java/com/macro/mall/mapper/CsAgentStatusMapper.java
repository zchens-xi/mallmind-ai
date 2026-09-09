package com.macro.mall.mapper;

import com.macro.mall.model.CsAgentStatus;
import com.macro.mall.model.CsAgentStatusExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CsAgentStatusMapper {
    long countByExample(CsAgentStatusExample example);

    int deleteByExample(CsAgentStatusExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CsAgentStatus row);

    int insertSelective(CsAgentStatus row);

    List<CsAgentStatus> selectByExample(CsAgentStatusExample example);

    CsAgentStatus selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("row") CsAgentStatus row, @Param("example") CsAgentStatusExample example);

    int updateByExample(@Param("row") CsAgentStatus row, @Param("example") CsAgentStatusExample example);

    int updateByPrimaryKeySelective(CsAgentStatus row);

    int updateByPrimaryKey(CsAgentStatus row);
}