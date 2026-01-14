package com.one.range.controller.basevul;

import com.one.range.model.Admin;
import com.one.range.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;


@RestController
@RequestMapping("/unauth")
public class UnauthVul {
    @Autowired
    @SuppressWarnings("all")
    AdminService adminService;

    @RequestMapping(value = "/userinfo", produces = "text/plain;charset=UTF-8")
    public String adminInfo(String username) {
        ArrayList<Admin> userInfo = new ArrayList<>();
        // 未授权访问漏洞：没有进行权限验证，任何人都可以查询用户信息
        if (username == null || username.equals("")) {
            return "请输入用户名！";
        }
        if (username.equals("all")) {
            userInfo = adminService.getAllInfo();
        } else {
            Admin admin = adminService.getInfoByUserName(username);
            if (admin == null) {
                return "用户不存在!";
            }
            userInfo.add(admin);
        }
        // 格式化返回用户信息
        StringBuilder result = new StringBuilder();
        result.append("查询结果:\n");
        for (Admin admin : userInfo) {
            result.append("用户名: ").append(admin.getUsername())
                  .append(", 密码: ").append(admin.getPassword())
                  .append(", 角色: ").append(admin.getRole())
                  .append(", 权限: ").append(admin.getPerms()).append("\n");
        }
        return result.toString();
    }
}
