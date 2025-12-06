<template>
  <vuln-template
    title="Spring Boot Actuator"
    subtitle="Information Disclosure"
    level="中危"
    :icon="DashboardOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>Spring Boot Actuator</strong>提供了生产就绪的功能，用于监控和管理应用程序。
      </a-typography-paragraph>
      <a-typography-paragraph>
        Actuator信息泄露的危害：
      </a-typography-paragraph>
      <ul>
        <li>泄露敏感配置信息</li>
        <li>暴露应用内部状态</li>
        <li>可能导致进一步攻击</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的Actuator端点：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        /actuator/env
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        /actuator/health
      </a-typography-paragraph>
    </template>

    <template #test-form>
      <div class="actuator-container">
        <a-card title="Spring Boot Actuator监控" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            访问Spring Boot Actuator监控端点，查看应用运行状态和配置信息。
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="端点路径"
            name="endpoint"
            :rules="[{ required: true, message: '请输入端点路径' }]"
          >
            <a-input
              v-model:value="formState.endpoint"
              size="large"
              placeholder="例如：env、health、info、beans"
              prefix="📊"
            />
            <div style="margin-top: 8px; color: #8c8c8c; font-size: 12px;">
              输入Actuator端点名称，系统将访问 /actuator/{endpoint}
            </div>
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><dashboard-outlined /></template>
                访问端点
              </a-button>
              <a-button size="large" @click="handleReset">
                <template #icon><redo-outlined /></template>
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>
    </template>

    <template #result v-if="result">
      <div class="result-content">
        <pre style="margin: 0;">{{ result }}</pre>
      </div>
      <a-alert
        v-if="result && (result.includes('password') || result.includes('secret') || result.includes('key'))"
        message="如果返回了敏感信息（密码、密钥等），说明Actuator端点泄露了敏感配置。"
        type="warning"
        show-icon
        style="margin-top: 16px"
      />
    </template>

    <template #source-code>
      <a-typography-paragraph>
        <strong>后端源码（存在漏洞）：</strong>
      </a-typography-paragraph>
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="sourceCode" style="margin: 0;">
          <pre class="code-block"><code>// Spring Boot Actuator默认配置
// 漏洞场景：当应用程序启用Actuator时

// application.properties或application.yml配置：
management.endpoints.web.exposure.include=*
// 或者默认配置暴露了所有端点

// 漏洞端点：
// /actuator/env - 暴露环境变量和配置信息（可能包含密码、密钥等）
// /actuator/health - 暴露应用健康状态
// /actuator/info - 暴露应用信息
// /actuator/beans - 暴露所有Spring Bean
// /actuator/mappings - 暴露所有URL映射
// /actuator/configprops - 暴露配置属性
// /actuator/heapdump - 生成堆转储文件
// /actuator/shutdown - 关闭应用（如果启用）

// 攻击者可以：
// 1. 访问 /actuator/env 获取敏感配置（数据库密码、API密钥等）
// 2. 通过 /actuator/env POST请求修改配置（可能导致RCE）
// 3. 访问 /actuator/beans 了解应用结构
// 4. 访问 /actuator/mappings 发现隐藏的API端点</code></pre>
        </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：Spring Boot Actuator默认暴露了多个监控端点，可能泄露敏感配置信息（如数据库密码、API密钥等）。攻击者可以通过这些端点获取应用内部信息，甚至修改配置导致远程代码执行。"
        type="error"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #fix>
      <a-typography-paragraph>
        <strong>修复建议：</strong>
      </a-typography-paragraph>
      <ul style="padding-left: 20px; line-height: 2;">
        <li><strong>限制暴露端点：</strong>只暴露必要的端点（如health），不要暴露所有端点</li>
        <li><strong>访问控制：</strong>使用Spring Security保护Actuator端点</li>
        <li><strong>禁用敏感端点：</strong>禁用env、beans、mappings等敏感端点</li>
        <li><strong>网络隔离：</strong>只允许内网访问Actuator端点</li>
        <li><strong>使用HTTPS：</strong>使用HTTPS加密Actuator通信</li>
        <li><strong>生产环境配置：</strong>生产环境禁用或严格限制Actuator</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        # application.properties
        management.endpoints.web.exposure.include=*
        # 暴露所有端点，包括env、beans等敏感端点</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：只暴露必要端点
        # application.properties
        # 只暴露health和info端点
        management.endpoints.web.exposure.include=health,info
        
        # 禁用敏感端点
        management.endpoint.env.enabled=false
        management.endpoint.beans.enabled=false
        management.endpoint.mappings.enabled=false
        management.endpoint.configprops.enabled=false
        management.endpoint.shutdown.enabled=false</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：使用Spring Security保护
        @Configuration
        public class ActuatorSecurityConfig extends WebSecurityConfigurerAdapter {
            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http
                    .requestMatcher(EndpointRequest.toAnyEndpoint())
                    .authorizeRequests()
                    .requestMatchers(EndpointRequest.to("health", "info"))
                        .permitAll() // health和info允许所有人访问
                    .anyRequest()
                        .hasRole("ADMIN") // 其他端点需要管理员权限
                    .and()
                    .httpBasic();
            }
        }</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：网络隔离
        # application.properties
        # 只允许本地访问
        management.server.address=127.0.0.1
        
        # 或者使用Nginx限制访问
        # Nginx配置：
        location /actuator {
            allow 10.0.0.0/8;      # 只允许内网
            deny all;
            proxy_pass http://localhost:8080;
        }</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：生产环境禁用Actuator
        # application-prod.properties
        # 生产环境完全禁用Actuator
        management.endpoints.enabled-by-default=false
        
        # 或者只启用health端点
        management.endpoints.enabled-by-default=false
        management.endpoint.health.enabled=true
        management.endpoints.web.exposure.include=health

        // 正确示例5：敏感信息脱敏
        # application.properties
        # 使用spring-boot-actuator的敏感信息脱敏功能
        management.endpoint.env.show-values=when-authorized
        management.endpoint.env.keys-to-sanitize=password,secret,key,token
        
        # 或者在代码中使用@ConfigurationProperties时标记敏感字段
        @ConfigurationProperties(prefix = "app")
        public class AppConfig {
            @Value("${app.password}")
            private String password; // 会被自动脱敏
        }</code></pre>
        </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { DashboardOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `// Spring Boot Actuator默认配置
// 漏洞场景：当应用程序启用Actuator时

// application.properties或application.yml配置：
management.endpoints.web.exposure.include=*
// 或者默认配置暴露了所有端点

// 漏洞端点：
// /actuator/env - 暴露环境变量和配置信息（可能包含密码、密钥等）
// /actuator/health - 暴露应用健康状态
// /actuator/info - 暴露应用信息
// /actuator/beans - 暴露所有Spring Bean
// /actuator/mappings - 暴露所有URL映射
// /actuator/configprops - 暴露配置属性
// /actuator/heapdump - 生成堆转储文件
// /actuator/shutdown - 关闭应用（如果启用）

// 攻击者可以：
// 1. 访问 /actuator/env 获取敏感配置（数据库密码、API密钥等）
// 2. 通过 /actuator/env POST请求修改配置（可能导致RCE）
// 3. 访问 /actuator/beans 了解应用结构
// 4. 访问 /actuator/mappings 发现隐藏的API端点`

const fixCode1 = `// 错误示例(易受攻击)
        # application.properties
        management.endpoints.web.exposure.include=*
        # 暴露所有端点，包括env、beans等敏感端点`

const fixCode2 = `// 正确示例1：只暴露必要端点
        # application.properties
        # 只暴露health和info端点
        management.endpoints.web.exposure.include=health,info
        
        # 禁用敏感端点
        management.endpoint.env.enabled=false
        management.endpoint.beans.enabled=false
        management.endpoint.mappings.enabled=false
        management.endpoint.configprops.enabled=false
        management.endpoint.shutdown.enabled=false`

const fixCode3 = `// 正确示例2：使用Spring Security保护
        @Configuration
        public class ActuatorSecurityConfig extends WebSecurityConfigurerAdapter {
            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http
                    .requestMatcher(EndpointRequest.toAnyEndpoint())
                    .authorizeRequests()
                    .requestMatchers(EndpointRequest.to("health", "info"))
                        .permitAll() // health和info允许所有人访问
                    .anyRequest()
                        .hasRole("ADMIN") // 其他端点需要管理员权限
                    .and()
                    .httpBasic();
            }
        }`

const fixCode4 = `// 正确示例3：网络隔离
        # application.properties
        # 只允许本地访问
        management.server.address=127.0.0.1
        
        # 或者使用Nginx限制访问
        # Nginx配置：
        location /actuator {
            allow 10.0.0.0/8;      # 只允许内网
            deny all;
            proxy_pass http://localhost:8080;
        }`

const fixCode5 = `// 正确示例4：生产环境禁用Actuator
        # application-prod.properties
        # 生产环境完全禁用Actuator
        management.endpoints.enabled-by-default=false
        
        # 或者只启用health端点
        management.endpoints.enabled-by-default=false
        management.endpoint.health.enabled=true
        management.endpoints.web.exposure.include=health

        // 正确示例5：敏感信息脱敏
        # application.properties
        # 使用spring-boot-actuator的敏感信息脱敏功能
        management.endpoint.env.show-values=when-authorized
        management.endpoint.env.keys-to-sanitize=password,secret,key,token
        
        # 或者在代码中使用@ConfigurationProperties时标记敏感字段
        @ConfigurationProperties(prefix = "app")
        public class AppConfig {
            @Value("${app.password}")
            private String password; // 会被自动脱敏
        }`

const formState = reactive({
  endpoint: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/actuators/index', {
      params: formState
    })
    result.value = response.data
    message.success('测试完成')
  } catch (error) {
    message.error('测试失败：' + (error.response?.data || error.message))
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formState.endpoint = ''
  result.value = ''
}
</script>
