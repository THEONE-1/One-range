package com.one.range.service;

import com.one.range.model.LearningProgress;

import java.util.List;

/**
 * 学习进度服务接口
 */
public interface LearningProgressService {

    /**
     * 标记模块为已完成
     */
    boolean markAsCompleted(Integer userId, String vulnModule);

    /**
     * 获取用户的所有学习进度
     */
    List<LearningProgress> getProgressByUserId(Integer userId);

    /**
     * 获取用户已完成的模块数量
     */
    int getCompletedCount(Integer userId);

    /**
     * 获取用户某个模块的学习进度
     */
    LearningProgress getProgressByUserIdAndModule(Integer userId, String vulnModule);
}
