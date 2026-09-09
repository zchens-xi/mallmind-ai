package com.macro.mall.model;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;

public class CsAgentStatus implements Serializable {
    private Long id;

    @ApiModelProperty(value = "客服ID，关联ums_admin.id")
    private Long adminId;

    @ApiModelProperty(value = "坐席状态：0->离线; 1->在线; 2->忙碌; 3->小休")
    private Integer status;

    @ApiModelProperty(value = "当前正在处理的会话数")
    private Integer currentConversations;

    @ApiModelProperty(value = "最大可同时处理的会话数")
    private Integer maxConversations;

    private Date lastLoginTime;

    private Date lastUpdateTime;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCurrentConversations() {
        return currentConversations;
    }

    public void setCurrentConversations(Integer currentConversations) {
        this.currentConversations = currentConversations;
    }

    public Integer getMaxConversations() {
        return maxConversations;
    }

    public void setMaxConversations(Integer maxConversations) {
        this.maxConversations = maxConversations;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", adminId=").append(adminId);
        sb.append(", status=").append(status);
        sb.append(", currentConversations=").append(currentConversations);
        sb.append(", maxConversations=").append(maxConversations);
        sb.append(", lastLoginTime=").append(lastLoginTime);
        sb.append(", lastUpdateTime=").append(lastUpdateTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}