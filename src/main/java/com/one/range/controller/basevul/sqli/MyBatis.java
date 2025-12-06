package com.one.range.controller.basevul.sqli;

import com.one.range.model.Admin;
import com.one.range.service.InjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/home/sqli/mybatis")
public class MyBatis {
    @Autowired
    @SuppressWarnings("all")
    InjectService injectService;

    @RequestMapping("/orderby")
    public String orderBy(String field) {
        ArrayList<Admin> adminList;
        try {
            adminList = injectService.orderBy(field);
            if (adminList == null || adminList.isEmpty()) {
                return "查询结果为空";
            }
            StringBuilder result = new StringBuilder();
            result.append("查询结果:\n");
            for (Admin admin : adminList) {
                result.append("用户名: ").append(admin.getUsername())
                      .append(", 密码: ").append(admin.getPassword()).append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "错误: " + e.toString();
        }
    }

    @RequestMapping("/like")
    public String like(String username) {
        try {
            Admin admin = injectService.likeSearch(username);
            if (admin == null) {
                return "查询结果为空";
            }
            return "用户名: " + admin.getUsername() + ", 密码: " + admin.getPassword();
        } catch (Exception e) {
            e.printStackTrace();
            return "错误: " + e.toString();
        }
    }

    @RequestMapping("/in")
    public String in(String ids) {
        try {
            ArrayList<Admin> adminList = injectService.in(ids);
            if (adminList == null || adminList.isEmpty()) {
                return "查询结果为空";
            }
            StringBuilder result = new StringBuilder();
            result.append("查询结果:\n");
            for (Admin admin : adminList) {
                result.append("用户名: ").append(admin.getUsername())
                      .append(", 密码: ").append(admin.getPassword()).append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "错误: " + e.toString();
        }
    }

}
