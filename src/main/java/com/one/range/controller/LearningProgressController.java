package com.one.range.controller;

import com.one.range.model.Admin;
import com.one.range.model.LearningProgress;
import com.one.range.service.AdminService;
import com.one.range.service.LearningProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 学习进度控制器
 */
@RestController
@RequestMapping("/api/progress")
public class LearningProgressController {

    @Autowired
    private LearningProgressService learningProgressService;

    @Autowired
    private AdminService adminService;

    /**
     * 标记模块为已完成
     */
    @PostMapping("/complete")
    public String markAsCompleted(@RequestParam String vulnModule, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "{\"success\":false,\"message\":\"请先登录\"}";
        }

        Admin admin = adminService.getInfoByUserName(username);
        if (admin == null) {
            return "{\"success\":false,\"message\":\"用户不存在\"}";
        }

        Integer userId = admin.getId();
        boolean result = learningProgressService.markAsCompleted(userId, vulnModule);

        if (result) {
            return "{\"success\":true,\"message\":\"已标记为完成\"}";
        } else {
            return "{\"success\":false,\"message\":\"标记失败\"}";
        }
    }

    /**
     * 获取用户的学习进度统计
     */
    @GetMapping("/stats")
    public String getProgressStats(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "{\"success\":false,\"message\":\"请先登录\"}";
        }

        Admin admin = adminService.getInfoByUserName(username);
        if (admin == null) {
            return "{\"success\":false,\"message\":\"用户不存在\"}";
        }

        Integer userId = admin.getId();
        int completedCount = learningProgressService.getCompletedCount(userId);
        int totalCount = 25; // 总共25个漏洞模块 (XSS:3 + SQL:4 + RCE:2 + File:2 + XXE:3 + Other:5 + Component:5 + Deserialize:1)

        return String.format("{\"success\":true,\"completedCount\":%d,\"totalCount\":%d,\"percentage\":%.1f}",
                completedCount, totalCount, (completedCount * 100.0 / totalCount));
    }

    /**
     * 获取用户的所有学习进度详情
     */
    @GetMapping("/list")
    public String getProgressList(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "{\"success\":false,\"message\":\"请先登录\"}";
        }

        Admin admin = adminService.getInfoByUserName(username);
        if (admin == null) {
            return "{\"success\":false,\"message\":\"用户不存在\"}";
        }

        Integer userId = admin.getId();
        List<LearningProgress> progressList = learningProgressService.getProgressByUserId(userId);

        // 构建JSON响应
        StringBuilder json = new StringBuilder("{\"success\":true,\"data\":[");
        for (int i = 0; i < progressList.size(); i++) {
            LearningProgress progress = progressList.get(i);
            if (i > 0) json.append(",");

            // 格式化日期
            String completedAtStr = "";
            if (progress.getCompletedAt() != null) {
                completedAtStr = progress.getCompletedAt().toString();
            }

            json.append(String.format("{\"vulnModule\":\"%s\",\"completed\":%b,\"completedAt\":\"%s\"}",
                    progress.getVulnModule(),
                    progress.getCompleted() != null ? progress.getCompleted() : false,
                    completedAtStr));
        }
        json.append("]}");

        return json.toString();
    }

    /**
     * 检查某个模块是否已完成
     */
    @GetMapping("/check")
    public String checkModuleCompleted(@RequestParam String vulnModule, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "{\"success\":false,\"message\":\"请先登录\"}";
        }

        Admin admin = adminService.getInfoByUserName(username);
        if (admin == null) {
            return "{\"success\":false,\"message\":\"用户不存在\"}";
        }

        Integer userId = admin.getId();
        LearningProgress progress = learningProgressService.getProgressByUserIdAndModule(userId, vulnModule);

        boolean completed = progress != null && progress.getCompleted();
        return String.format("{\"success\":true,\"completed\":%b}", completed);
    }
}
