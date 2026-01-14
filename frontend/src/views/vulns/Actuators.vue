<template>
  <vuln-template
    title="Spring Boot Actuator"
    subtitle="Information Disclosure"
    level="中危"
    :icon="DashboardOutlined"
    moduleKey="actuators"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>Spring Boot Actuator</strong>提供了生产就绪的功能，用于监控和管理应用程序。
      </a-typography-paragraph>
      <a-typography-paragraph>
        Actuator信息泄露的危害：
      </a-typography-paragraph>
      <ul>
        <li>泄露敏感配置信息（数据库密码、API密钥等）</li>
        <li>暴露应用内部状态和架构</li>
        <li>可能导致进一步攻击（RCE、权限提升等）</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的Actuator端点：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        /actuator/env - 环境变量和配置
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        /actuator/health - 健康检查
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        /actuator/beans - Spring Beans列表
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        /actuator/mappings - URL映射
      </a-typography-paragraph>
    </template>

    <template #test-form>
      <div class="actuator-container">
        <a-card title="Spring Boot Actuator 端点探测" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            访问Spring Boot Actuator监控端点，查看应用运行状态和配置信息。
          </a-typography-paragraph>
          <a-alert
            message="实战提示"
            description="在真实渗透测试中，Actuator端点可能泄露数据库密码、JWT密钥、API密钥等敏感信息，甚至可以通过/actuator/env POST请求修改配置导致RCE。"
            type="info"
            show-icon
            style="margin-top: 12px"
          />
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
              placeholder="例如：env、health、info、beans、mappings"
              prefix="📊"
            />
            <div style="margin-top: 8px; color: #8c8c8c; font-size: 12px;">
              输入Actuator端点名称，系统将访问 /actuator/{endpoint}
            </div>
          </a-form-item>

          <a-form-item label="常用端点快捷访问">
            <a-space wrap>
              <a-button @click="quickAccess('env')" size="small">
                <template #icon>🔐</template>
                env（环境变量）
              </a-button>
              <a-button @click="quickAccess('health')" size="small">
                <template #icon>💚</template>
                health（健康检查）
              </a-button>
              <a-button @click="quickAccess('info')" size="small">
                <template #icon>ℹ️</template>
                info（应用信息）
              </a-button>
              <a-button @click="quickAccess('beans')" size="small">
                <template #icon>🫘</template>
                beans（Spring Beans）
              </a-button>
              <a-button @click="quickAccess('mappings')" size="small">
                <template #icon>🗺️</template>
                mappings（URL映射）
              </a-button>
              <a-button @click="quickAccess('configprops')" size="small">
                <template #icon>⚙️</template>
                configprops（配置属性）
              </a-button>
              <a-button @click="quickAccess('metrics')" size="small">
                <template #icon>📈</template>
                metrics（性能指标）
              </a-button>
              <a-button @click="quickAccess('loggers')" size="small">
                <template #icon>📝</template>
                loggers（日志配置）
              </a-button>
            </a-space>
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
        <a-card title="端点响应结果" :bordered="false">
          <a-tabs v-model:activeKey="activeTab">
            <a-tab-pane key="formatted" tab="格式化视图">
              <div class="json-viewer">
                <pre style="margin: 0; max-height: 600px; overflow: auto;">{{ formattedResult }}</pre>
              </div>
            </a-tab-pane>
            <a-tab-pane key="raw" tab="原始数据">
              <div class="raw-viewer">
                <pre style="margin: 0; max-height: 600px; overflow: auto;">{{ result }}</pre>
              </div>
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </div>
      <a-alert
        v-if="hasSensitiveInfo"
        message="⚠️ 检测到敏感信息"
        description="响应中包含敏感关键词（password、secret、key、token等），这可能导致严重的安全风险。在生产环境中，应该禁用或严格限制Actuator端点的访问。"
        type="error"
        show-icon
        style="margin-top: 16px"
      />
      <a-alert
        v-else
        message="✅ 端点访问成功"
        description="成功获取Actuator端点数据。在真实场景中，攻击者可以通过这些信息了解应用架构、发现隐藏接口、获取敏感配置等。"
        type="success"
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
          <pre class="code-block"><code>// application.yml 配置
management:
  endpoints:
    web:
      exposure:
        include: "*"  # ❌ 危险：暴露所有端点

// 漏洞端点说明：
// /actuator/env - 暴露环境变量和配置信息（可能包含密码、密钥等）
// /actuator/health - 暴露应用健康状态和依赖信息
// /actuator/info - 暴露应用信息
// /actuator/beans - 暴露所有Spring Bean（了解应用架构）
// /actuator/mappings - 暴露所有URL映射（发现隐藏接口）
// /actuator/configprops - 暴露配置属性
// /actuator/heapdump - 生成堆转储文件（可能包含敏感数据）
// /actuator/shutdown - 关闭应用（如果启用）

// 攻击场景：
// 1. 访问 /actuator/env 获取数据库密码、API密钥
// 2. 通过 /actuator/env POST请求修改配置（Spring Cloud环境）
// 3. 访问 /actuator/beans 了解应用结构
// 4. 访问 /actuator/mappings 发现隐藏的API端点
// 5. 下载 /actuator/heapdump 分析内存数据</code></pre>
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
          <pre class="code-block"><code>// ❌ 错误示例（易受攻击）
# application.properties
management.endpoints.web.exposure.include=*
# 暴露所有端点，包括env、beans等敏感端点</code></pre>
        </a-typography-paragraph>
      </div>

      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// ✅ 正确示例1：只暴露必要端点
# application.properties
# 只暴露health和info端点
management.endpoints.web.exposure.include=health,info

# 禁用敏感端点
management.endpoint.env.enabled=false
management.endpoint.beans.enabled=false
management.endpoint.mappings.enabled=false
management.endpoint.configprops.enabled=false
management.endpoint.shutdown.enabled=false
management.endpoint.heapdump.enabled=false</code></pre>
        </a-typography-paragraph>
      </div>

      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// ✅ 正确示例2：使用Spring Security保护
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
          <pre class="code-block"><code>// ✅ 正确示例3：网络隔离
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
          <pre class="code-block"><code>// ✅ 正确示例4：生产环境禁用Actuator
# application-prod.properties
# 生产环境完全禁用Actuator
management.endpoints.enabled-by-default=false

# 或者只启用health端点
management.endpoints.enabled-by-default=false
management.endpoint.health.enabled=true
management.endpoints.web.exposure.include=health

// ✅ 正确示例5：敏感信息脱敏
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
import { reactive, ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import { DashboardOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')
const activeTab = ref('formatted')

const sourceCode = `// application.yml 配置
management:
  endpoints:
    web:
      exposure:
        include: "*"  # ❌ 危险：暴露所有端点

// 漏洞端点说明：
// /actuator/env - 暴露环境变量和配置信息（可能包含密码、密钥等）
// /actuator/health - 暴露应用健康状态和依赖信息
// /actuator/info - 暴露应用信息
// /actuator/beans - 暴露所有Spring Bean（了解应用架构）
// /actuator/mappings - 暴露所有URL映射（发现隐藏接口）
// /actuator/configprops - 暴露配置属性
// /actuator/heapdump - 生成堆转储文件（可能包含敏感数据）
// /actuator/shutdown - 关闭应用（如果启用）

// 攻击场景：
// 1. 访问 /actuator/env 获取数据库密码、API密钥
// 2. 通过 /actuator/env POST请求修改配置（Spring Cloud环境）
// 3. 访问 /actuator/beans 了解应用结构
// 4. 访问 /actuator/mappings 发现隐藏的API端点
// 5. 下载 /actuator/heapdump 分析内存数据`

const fixCode1 = `// ❌ 错误示例（易受攻击）
# application.properties
management.endpoints.web.exposure.include=*
# 暴露所有端点，包括env、beans等敏感端点`

const fixCode2 = `// ✅ 正确示例1：只暴露必要端点
# application.properties
# 只暴露health和info端点
management.endpoints.web.exposure.include=health,info

# 禁用敏感端点
management.endpoint.env.enabled=false
management.endpoint.beans.enabled=false
management.endpoint.mappings.enabled=false
management.endpoint.configprops.enabled=false
management.endpoint.shutdown.enabled=false
management.endpoint.heapdump.enabled=false`

const fixCode3 = `// ✅ 正确示例2：使用Spring Security保护
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

const fixCode4 = `// ✅ 正确示例3：网络隔离
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

const fixCode5 = `// ✅ 正确示例4：生产环境禁用Actuator
# application-prod.properties
# 生产环境完全禁用Actuator
management.endpoints.enabled-by-default=false

# 或者只启用health端点
management.endpoints.enabled-by-default=false
management.endpoint.health.enabled=true
management.endpoints.web.exposure.include=health

// ✅ 正确示例5：敏感信息脱敏
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

// 格式化JSON结果
const formattedResult = computed(() => {
  if (!result.value) return ''
  try {
    const parsed = JSON.parse(result.value)
    return JSON.stringify(parsed, null, 2)
  } catch (e) {
    return result.value
  }
})

// 检测敏感信息
const hasSensitiveInfo = computed(() => {
  if (!result.value) return false
  const lowerResult = result.value.toLowerCase()
  const sensitiveKeywords = ['password', 'secret', 'key', 'token', 'credential', 'apikey', 'api_key']
  return sensitiveKeywords.some(keyword => lowerResult.includes(keyword))
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/actuators/index', {
      params: formState
    })
    result.value = typeof response.data === 'string' ? response.data : JSON.stringify(response.data)
    message.success('端点访问成功')
  } catch (error) {
    message.error('访问失败：' + (error.response?.data || error.message))
    result.value = ''
  } finally {
    loading.value = false
  }
}

const quickAccess = (endpoint) => {
  formState.endpoint = endpoint
  handleSubmit()
}

const handleReset = () => {
  formState.endpoint = ''
  result.value = ''
  activeTab.value = 'formatted'
}
</script>

<style scoped>
.actuator-container {
  padding: 0;
}

.json-viewer,
.raw-viewer {
  background: #f5f5f5;
  border-radius: 4px;
  padding: 16px;
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.result-content {
  margin-top: 16px;
}
</style>
