package com.one.range.service.impl;

import com.one.range.dao.LearningProgressDao;
import com.one.range.model.LearningProgress;
import com.one.range.service.LearningProgressService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学习进度服务实现类
 */
@Service
public class LearningProgressServiceImpl implements LearningProgressService {

    final LearningProgressDao learningProgressDao;

    public LearningProgressServiceImpl(LearningProgressDao learningProgressDao) {
        this.learningProgressDao = learningProgressDao;
    }

    @Override
    public boolean markAsCompleted(Integer userId, String vulnModule) {
        return learningProgressDao.markAsCompleted(userId, vulnModule) > 0;
    }

    @Override
    public List<LearningProgress> getProgressByUserId(Integer userId) {
        return learningProgressDao.getProgressByUserId(userId);
    }

    @Override
    public int getCompletedCount(Integer userId) {
        return learningProgressDao.getCompletedCount(userId);
    }

    @Override
    public LearningProgress getProgressByUserIdAndModule(Integer userId, String vulnModule) {
        return learningProgressDao.getProgressByUserIdAndModule(userId, vulnModule);
    }
}
