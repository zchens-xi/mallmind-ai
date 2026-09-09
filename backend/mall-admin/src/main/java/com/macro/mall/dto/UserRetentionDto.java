package com.macro.mall.dto;

import lombok.Data;

/**
 * 用户留存率DTO
 */
@Data
public class UserRetentionDto {

    /**
     * 日期
     */
    private String date;

    /**
     * 次日留存率(%)
     */
    private Double nextDayRate;

    /**
     * 7日留存率(%)
     */
    private Double day7Rate;

    /**
     * 30日留存率(%)
     */
    private Double day30Rate;

    // 手动添加setter方法解决编译问题
    public void setDate(String date) {
        this.date = date;
    }

    public void setNextDayRate(Double nextDayRate) {
        this.nextDayRate = nextDayRate;
    }

    public void setDay7Rate(Double day7Rate) {
        this.day7Rate = day7Rate;
    }

    public void setDay30Rate(Double day30Rate) {
        this.day30Rate = day30Rate;
    }
}
