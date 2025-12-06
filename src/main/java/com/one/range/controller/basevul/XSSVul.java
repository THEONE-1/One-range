package com.one.range.controller.basevul;

import com.one.range.model.Xss;
import com.one.range.service.XssService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/home/xss")
public class XSSVul {
    final XssService xssService;

    public XSSVul(XssService xssService) {
        this.xssService = xssService;
    }

    @RequestMapping("/reflect")
    public String xssReflect(String content) {
        // 直接返回内容，用于XSS反射测试
        return content != null ? content : "";
    }

    @RequestMapping("/store")
    public String xssInsert(String content, String clear) {
        try {
            // 处理清空操作（clear可能是"true"字符串或boolean值转换的字符串）
            if (clear != null && (clear.equals("true") || clear.equalsIgnoreCase("true"))) {
                xssService.clear();
                return "清除成功";
            }
            // 如果有内容，则添加
            if (content != null && !content.trim().equals("")) {
                xssService.setContent(new Xss(content));
            }
            // 获取并返回所有存储的内容，使用HTML格式以便前端v-html渲染
            List<String> list = xssService.getContent();
            if (list == null || list.isEmpty()) {
                return "暂无留言内容";
            }
            StringBuilder result = new StringBuilder();
            result.append("<div style='line-height: 2;'>");
            for (int i = 0; i < list.size(); i++) {
                result.append("<div style='margin-bottom: 12px; padding: 8px; background: #f5f5f5; border-radius: 4px;'>");
                result.append("<strong>留言").append(i + 1).append(":</strong><br/>");
                result.append(list.get(i)); // 直接输出，不进行HTML转义，用于XSS测试
                result.append("</div>");
            }
            result.append("</div>");
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "错误: " + e.toString();
        }
    }
}
