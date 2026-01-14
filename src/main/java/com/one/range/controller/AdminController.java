package com.one.range.controller;

import com.wf.captcha.utils.CaptchaUtil;
import com.one.range.model.Admin;
import com.one.range.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

@Controller
public class AdminController {
    @Autowired
    @SuppressWarnings("all")
    AdminService adminService;

    @RequestMapping({"/", "/index"})
    @ResponseBody
    public String index() {
        return "{\"message\":\"One Security Range API v2.0.0\",\"frontend\":\"Vue 3 + Ant Design Vue\"}";
    }

    @GetMapping("/admin/logout")
    @ResponseBody
    public String logout(HttpSession session) {
        session.invalidate();
        return "注销成功，请重新登录！";
    }

    @PostMapping("/admin/login")
    @ResponseBody
    public String login(String username, String password, String captcha,
                        HttpSession session, HttpServletRequest request) {
        
        if (!CaptchaUtil.ver(captcha, request)) {
            CaptchaUtil.clear(request);
            return "{\"success\":false,\"message\":\"验证码不正确\"}";
        }
        
        Admin admin = adminService.login(username, password);
        if (admin != null) {
            session.setAttribute("username", username);
            return "{\"success\":true,\"message\":\"登录成功\",\"username\":\"" + username + "\"}";
        } else {
            return "{\"success\":false,\"message\":\"用户名或密码错误\"}";
        }
    }

    @GetMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
        CaptchaUtil.out(request, response);
    }

    @PostMapping("/admin/chpwd")
    @ResponseBody
    public String changePassword(@RequestBody Map<String, String> map, HttpSession session) {
        String old_password = map.get("old_password");
        String new_password = map.get("new_password");
        String again_password = map.get("again_password");
        String username = (String) session.getAttribute("username");
        if (old_password == null || new_password == null || again_password == null) {
            return "输入不能为空!";
        }
        if (old_password.equals(new_password)) {
            return "新密码不能与旧密码一致!";
        }
        if (!new_password.equals(again_password)) {
            return "新密码两次输入不一致!";
        }
        Admin admin = adminService.login(username, old_password);
        if (admin != null) {
            if (adminService.updatePWD(username, new_password) != 0) {
                session.invalidate();
                return "密码修改成功!";
            } else {
                return "密码修改失败!";
            }
        } else {
            return "旧密码输入错误!";
        }
    }

    @GetMapping("/admin/info")
    @ResponseBody
    public String adminInfo(HttpSession session) {
        String username = (String) session.getAttribute("username");
        Date day = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return String.format("{\"username\":\"%s\",\"date\":\"%s\",\"os\":\"%s\",\"java\":\"%s\"}",
            username, df.format(day), System.getProperty("os.name"), System.getProperty("java.version"));
    }

    @PostMapping("/admin/register")
    @ResponseBody
    public String register(String username, String password, String confirmPassword, String captcha,
                          HttpServletRequest request) {

        // 验证验证码
        if (!CaptchaUtil.ver(captcha, request)) {
            CaptchaUtil.clear(request);
            return "{\"success\":false,\"message\":\"验证码不正确\"}";
        }

        // 验证输入
        if (username == null || username.trim().isEmpty()) {
            return "{\"success\":false,\"message\":\"用户名不能为空\"}";
        }
        if (password == null || password.trim().isEmpty()) {
            return "{\"success\":false,\"message\":\"密码不能为空\"}";
        }
        if (!password.equals(confirmPassword)) {
            return "{\"success\":false,\"message\":\"两次密码输入不一致\"}";
        }

        // 检查用户名是否已存在
        if (adminService.checkUsernameExists(username)) {
            return "{\"success\":false,\"message\":\"用户名已存在\"}";
        }

        // 注册用户
        int result = adminService.register(username, password);
        if (result > 0) {
            return "{\"success\":true,\"message\":\"注册成功，请登录\"}";
        } else {
            return "{\"success\":false,\"message\":\"注册失败，请稍后重试\"}";
        }
    }

    @GetMapping("/admin/users")
    @ResponseBody
    public String getAllUsers(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "{\"success\":false,\"message\":\"请先登录\"}";
        }

        Admin currentUser = adminService.getInfoByUserName(username);
        if (currentUser == null || !"administrator".equals(currentUser.getRole())) {
            return "{\"success\":false,\"message\":\"权限不足，仅管理员可访问\"}";
        }

        ArrayList<Admin> users = adminService.getAllInfo();
        StringBuilder json = new StringBuilder("{\"success\":true,\"data\":[");
        for (int i = 0; i < users.size(); i++) {
            Admin user = users.get(i);
            if (i > 0) json.append(",");
            json.append(String.format("{\"id\":%d,\"username\":\"%s\",\"role\":\"%s\"}",
                    user.getId(), user.getUsername(), user.getRole() != null ? user.getRole() : "user"));
        }
        json.append("]}");
        return json.toString();
    }

    @PostMapping("/admin/deleteUser")
    @ResponseBody
    public String deleteUser(@RequestBody Map<String, String> map, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "{\"success\":false,\"message\":\"请先登录\"}";
        }

        Admin currentUser = adminService.getInfoByUserName(username);
        if (currentUser == null || !"administrator".equals(currentUser.getRole())) {
            return "{\"success\":false,\"message\":\"权限不足，仅管理员可访问\"}";
        }

        String userIdStr = map.get("userId");
        if (userIdStr == null) {
            return "{\"success\":false,\"message\":\"用户ID不能为空\"}";
        }

        try {
            Integer userId = Integer.parseInt(userIdStr);

            // 不能删除自己
            if (userId.equals(currentUser.getId())) {
                return "{\"success\":false,\"message\":\"不能删除当前登录的管理员账号\"}";
            }

            // 检查要删除的用户是否存在
            Admin targetUser = adminService.getInfoById(userId);
            if (targetUser == null) {
                return "{\"success\":false,\"message\":\"用户不存在\"}";
            }

            // 不能删除其他管理员
            if ("administrator".equals(targetUser.getRole())) {
                return "{\"success\":false,\"message\":\"不能删除管理员账号\"}";
            }

            int result = adminService.deleteUser(userId);
            if (result > 0) {
                return "{\"success\":true,\"message\":\"删除成功\"}";
            } else {
                return "{\"success\":false,\"message\":\"删除失败\"}";
            }
        } catch (NumberFormatException e) {
            return "{\"success\":false,\"message\":\"用户ID格式错误\"}";
        }
    }
}

