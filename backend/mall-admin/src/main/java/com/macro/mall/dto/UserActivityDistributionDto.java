package com.macro.mall.dto;

import lombok.Data;

/**
 * 用户活跃度分布DTO
 */
@Data
public class UserActivityDistributionDto {

    /**
     * 活跃度等级
     */
    private String level;

    /**
     * 用户数量
     */
    private Long count;

    // 手动添加setter方法解决编译问题
    public void setLevel(String level) {
        this.level = level;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
