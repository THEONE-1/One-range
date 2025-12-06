package com.one.range.controller.basevul;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/home/xff")
public class XFFVul {

    @RequestMapping("")
    public String xffVul(HttpServletRequest request, String xff) {
        String ip = request.getRemoteAddr();
        // 如果xff参数为true，则从X-Forwarded-For头获取IP
        if (xff != null && xff.equals("true")) {
            String xffHeader = request.getHeader("X-Forwarded-For");
            if (xffHeader != null && !xffHeader.trim().isEmpty()) {
                // 如果X-Forwarded-For有多个IP（逗号分隔），取第一个
                ip = xffHeader.split(",")[0].trim();
            }
        }
        if (ip != null && ip.equals("10.0.0.1")) {
            return "你的ip为: " + ip + ", 访问成功。";
        }
        return "你的ip为: " + ip + ", 本资源仅允许 10.0.0.1 访问。";
    }
}
