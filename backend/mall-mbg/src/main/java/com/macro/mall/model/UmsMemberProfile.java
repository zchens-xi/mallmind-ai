package com.macro.mall.model;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;

public class UmsMemberProfile implements Serializable {
    private Long id;

    @ApiModelProperty(value = "会员ID, 关联ums_member.id")
    private Long memberId;

    @ApiModelProperty(value = "最近一次活跃时间（登录/浏览/下单/加购等）")
    private Date lastActiveTime;

    @ApiModelProperty(value = "本条画像记录的最后更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "订单核心统计信息。结构见下方示例。")
    private String orderStats;

    @ApiModelProperty(value = "用户的兴趣标签集合，包含分数和动量。结构见下方示例。")
    private String interestTags;

    @ApiModelProperty(value = "用户的行为标签集合，用于用户分群。结构见下方示例。")
    private String behavioralTags;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Date getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(Date lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getOrderStats() {
        return orderStats;
    }

    public void setOrderStats(String orderStats) {
        this.orderStats = orderStats;
    }

    public String getInterestTags() {
        return interestTags;
    }

    public void setInterestTags(String interestTags) {
        this.interestTags = interestTags;
    }

    public String getBehavioralTags() {
        return behavioralTags;
    }

    public void setBehavioralTags(String behavioralTags) {
        this.behavioralTags = behavioralTags;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", memberId=").append(memberId);
        sb.append(", lastActiveTime=").append(lastActiveTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", orderStats=").append(orderStats);
        sb.append(", interestTags=").append(interestTags);
        sb.append(", behavioralTags=").append(behavioralTags);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}