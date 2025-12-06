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
}
