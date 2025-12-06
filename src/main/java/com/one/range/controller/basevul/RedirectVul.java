package com.one.range.controller.basevul;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/home/redirect")
public class RedirectVul {
    @RequestMapping("")
    public String redirect(String url, HttpServletResponse response) {
        try {
            if (url != null && !url.trim().isEmpty()) {
                response.sendRedirect(url);
                return "重定向到: " + url;
            }
            return "URL不能为空";
        } catch (IOException e) {
            return "重定向失败: " + e.getMessage();
        }
    }
}
