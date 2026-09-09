package com.macro.mall.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户概览数据DTO
 */
@Data
public class UserOverviewDto {

    /**
     * 总用户数
     */
    private Long totalUsers;

    /**
     * 新增用户数
     */
    private Long newUsers;

    /**
     * 活跃用户数
     */
    private Long activeUsers;

    /**
     * 付费用户数
     */
    private Long paidUsers;

    /**
     * 用户增长率(%)
     */
    private Double userGrowthRate;

    /**
     * 日均新增用户数
     */
    private Double avgNewUsers;

    /**
     * 活跃率(%)
     */
    private Double activeRate;

    /**
     * 付费率(%)
     */
    private Double paidRate;

    /**
     * 日期列表
     */
    private List<String> dateList;

    /**
     * 累计用户趋势数据
     */
    private List<Long> totalUsersList;

    /**
     * 新增用户趋势数据
     */
    private List<Long> newUsersList;

    /**
     * 活跃用户趋势数据
     */
    private List<Long> activeUsersList;

    // 手动添加setter方法解决编译问题
    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public void setNewUsers(Long newUsers) {
        this.newUsers = newUsers;
    }

    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public void setPaidUsers(Long paidUsers) {
        this.paidUsers = paidUsers;
    }

    public void setUserGrowthRate(Double userGrowthRate) {
        this.userGrowthRate = userGrowthRate;
    }

    public void setAvgNewUsers(Double avgNewUsers) {
        this.avgNewUsers = avgNewUsers;
    }

    public void setActiveRate(Double activeRate) {
        this.activeRate = activeRate;
    }

    public void setPaidRate(Double paidRate) {
        this.paidRate = paidRate;
    }

    public void setDateList(List<String> dateList) {
        this.dateList = dateList;
    }

    public void setTotalUsersList(List<Long> totalUsersList) {
        this.totalUsersList = totalUsersList;
    }

    public void setNewUsersList(List<Long> newUsersList) {
        this.newUsersList = newUsersList;
    }

    public void setActiveUsersList(List<Long> activeUsersList) {
        this.activeUsersList = activeUsersList;
    }
}
