package com.one.range.filter;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * One安全靶场 - MVC配置
 * 纯API模式，配合Vue前端使用
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    // API模式，无需拦截器和视图配置
}
