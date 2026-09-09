package com.macro.mall.model;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;

public class AiConversation implements Serializable {
    private Long id;

    @ApiModelProperty(value = "会员ID，关联ums_member.id")
    private Long memberId;

    @ApiModelProperty(value = "聊天记录引用ID (对应MongoDB中的文档_id)")
    private String chatHistoryRefId;

    @ApiModelProperty(value = "产品ID，将客服按照商品区分开")
    private String productId;

    @ApiModelProperty(value = "会话开始时间")
    private Date startTime;

    @ApiModelProperty(value = "会话结束时间")
    private Date endTime;

    @ApiModelProperty(value = "来源渠道，如：PC, App, WeChat")
    private String sourceChannel;

    @ApiModelProperty(value = "会话状态：0->进行中; 1->AI处理结束; 2->已转人工; 3->用户已关闭")
    private Integer status;

    @ApiModelProperty(value = "转接后分配的人工客服ID，关联ums_admin.id")
    private Long assignedAdminId;

    @ApiModelProperty(value = "会话标签，逗号分隔，如：退货咨询, 物流查询")
    private String tags;

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

    public String getChatHistoryRefId() {
        return chatHistoryRefId;
    }

    public void setChatHistoryRefId(String chatHistoryRefId) {
        this.chatHistoryRefId = chatHistoryRefId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", memberId=").append(memberId);
        sb.append(", chatHistoryRefId=").append(chatHistoryRefId);
        sb.append(", productId=").append(productId);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", sourceChannel=").append(sourceChannel);
        sb.append(", status=").append(status);
        sb.append(", assignedAdminId=").append(assignedAdminId);
        sb.append(", tags=").append(tags);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}