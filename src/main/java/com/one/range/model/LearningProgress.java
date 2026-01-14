package com.one.range.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 学习进度实体类
 */
public class LearningProgress implements Serializable {
    private Integer id;
    private Integer userId;
    private String vulnModule;
    private Boolean completed;
    private Date completedAt;
    private Date createdAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getVulnModule() {
        return vulnModule;
    }

    public void setVulnModule(String vulnModule) {
        this.vulnModule = vulnModule;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "LearningProgress{" +
                "id=" + id +
                ", userId=" + userId +
                ", vulnModule='" + vulnModule + '\'' +
                ", completed=" + completed +
                ", completedAt=" + completedAt +
                ", createdAt=" + createdAt +
                '}';
    }
}
