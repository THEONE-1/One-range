package com.one.range.controller.componentsvul.actuators;


import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@EnableAutoConfiguration
@RequestMapping("/home/actuators")
public class WEPS {

    /**
     * Actuator 端点访问接口
     * 演示 Spring Boot Actuator 信息泄露漏洞
     */
    @GetMapping("/index")
    public String actuatorEndpoint(@RequestParam String endpoint) {
        try {
            // 构造 Actuator 端点 URL
            String actuatorUrl = "http://localhost:8000/actuator/" + endpoint;

            // 使用 RestTemplate 访问 Actuator 端点
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(actuatorUrl, String.class);

            return response;
        } catch (Exception e) {
            return "访问失败：" + e.getMessage() + "\n\n提示：请确保输入的端点名称正确，常见端点包括：\n" +
                   "- env（环境变量）\n" +
                   "- health（健康检查）\n" +
                   "- info（应用信息）\n" +
                   "- beans（Spring Beans）\n" +
                   "- mappings（URL映射）\n" +
                   "- configprops（配置属性）";
        }
    }

    @RequestMapping("/weps")
    public String wepsRce(String id) {
        int total = 100;
        String message = String.format("You've read %s books, and there are %d left", id, total - Integer.valueOf(id));
        return message;
    }

}
