package com.macro.mall.dto;

import lombok.Data;

/**
 * 用户访问时段分布DTO
 */
@Data
public class UserVisitTimeDistributionDto {

    /**
     * 小时数 (0-23)
     */
    private Integer hour;

    /**
     * 访问次数
     */
    private Long count;

    // 手动添加setter方法解决编译问题
    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
