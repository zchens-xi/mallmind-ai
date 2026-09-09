package com.macro.mall.model;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;

public class CsConversation implements Serializable {
    private Long id;

    @ApiModelProperty(value = "会员ID，关联ums_member.id")
    private Long memberId;

    @ApiModelProperty(value = "会话开始时间")
    private Date startTime;

    @ApiModelProperty(value = "会话结束时间")
    private Date endTime;

    @ApiModelProperty(value = "来源渠道，如：PC, App, WeChat")
    private String sourceChannel;

    @ApiModelProperty(value = "会话状态：0->AI处理中; 1->排队等待人工; 2->人工处理中; 3->已结束")
    private Integer status;

    @ApiModelProperty(value = "当前处理人ID（人工客服），关联ums_admin.id")
    private Long assignedAdminId;

    @ApiModelProperty(value = "会话摘要，由AI或人工在会话结束后生成")
    private String summary;

    @ApiModelProperty(value = "会话标签，逗号分隔，如：退货咨询, 物流查询")
    private String tags;

    @ApiModelProperty(value = "用户满意度评分 (1-5分)")
    private Integer satisfactionScore;

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

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getSourceChannel() {
        return sourceChannel;
    }

    public void setSourceChannel(String sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getAssignedAdminId() {
        return assignedAdminId;
    }

    public void setAssignedAdminId(Long assignedAdminId) {
        this.assignedAdminId = assignedAdminId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getSatisfactionScore() {
        return satisfactionScore;
    }

    public void setSatisfactionScore(Integer satisfactionScore) {
        this.satisfactionScore = satisfactionScore;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", memberId=").append(memberId);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", sourceChannel=").append(sourceChannel);
        sb.append(", status=").append(status);
        sb.append(", assignedAdminId=").append(assignedAdminId);
        sb.append(", summary=").append(summary);
        sb.append(", tags=").append(tags);
        sb.append(", satisfactionScore=").append(satisfactionScore);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}