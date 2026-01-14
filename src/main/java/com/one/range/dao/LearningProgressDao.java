package com.one.range.dao;

import com.one.range.model.LearningProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学习进度DAO接口
 */
@Mapper
public interface LearningProgressDao {

    /**
     * 记录或更新学习进度
     */
    int saveOrUpdate(LearningProgress progress);

    /**
     * 获取用户的所有学习进度
     */
    List<LearningProgress> getProgressByUserId(@Param("userId") Integer userId);

    /**
     * 获取用户某个模块的学习进度
     */
    LearningProgress getProgressByUserIdAndModule(@Param("userId") Integer userId, @Param("vulnModule") String vulnModule);

    /**
     * 获取用户已完成的模块数量
     */
    int getCompletedCount(@Param("userId") Integer userId);

    /**
     * 标记模块为已完成
     */
    int markAsCompleted(@Param("userId") Integer userId, @Param("vulnModule") String vulnModule);
}
