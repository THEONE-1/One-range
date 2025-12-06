<template>
  <vuln-template
    title="X-Forwarded-For伪造"
    subtitle="HTTP Header Forgery"
    level="低危"
    :icon="SwapOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>X-Forwarded-For</strong> 是HTTP请求头,用于标识客户端的真实IP地址。但这个头可以被伪造。
      </a-typography-paragraph>
      <a-typography-paragraph>
        XFF伪造的危害：
      </a-typography-paragraph>
      <ul>
        <li>绕过IP限制和黑名单</li>
        <li>伪造来源IP进行攻击</li>
        <li>绕过访问控制策略</li>
        <li>可能导致审计日志失真</li>
      </ul>
      <a-typography-paragraph>
        应用程序不应该直接信任X-Forwarded-For头的值。
      </a-typography-paragraph>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的XFF伪造payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        127.0.0.1
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">伪造成本地访问</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">
        192.168.1.1
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">伪造成内网IP</span>
      </a-space>

      <a-typography-paragraph code copyable style="margin-top: 12px">
        8.8.8.8
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">伪造成可信IP地址</span>
      </a-space>

      <a-alert
        message="不要直接信任X-Forwarded-For头,应使用服务器获取的真实IP！"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <div class="restricted-resource-container">
        <a-card title="🔒 受限资源访问" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            这是一个受IP限制的敏感资源，只有特定IP地址（10.0.0.1）才能访问。系统会检查您的IP地址来判断是否允许访问。
          </a-typography-paragraph>
          <a-alert
            message="访问限制：此资源仅允许IP地址 10.0.0.1 访问"
            type="warning"
            show-icon
            style="margin-top: 16px"
          />
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="您的IP地址"
            name="xff"
            :rules="[{ required: true, message: '请输入IP地址' }]"
          >
            <a-input
              v-model:value="formState.xff"
              size="large"
              placeholder="例如：10.0.0.1"
              prefix="🌐"
            />
            <div style="margin-top: 8px; color: #8c8c8c; font-size: 12px;">
              提示：如果您通过代理访问，可以设置 X-Forwarded-For 头来指定您的真实IP
            </div>
          </a-form-item>

          <a-form-item>
            <a-checkbox v-model:checked="formState.useProxy">
              我通过代理访问（设置 X-Forwarded-For 头）
            </a-checkbox>
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><unlock-outlined /></template>
                尝试访问受限资源
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
        <div style="background: #fff; padding: 16px; border-radius: 4px; border-left: 4px solid #1890ff;">
          <pre style="margin: 0; white-space: pre-wrap; color: #1890ff;">{{ result }}</pre>
        </div>
        <a-alert
          v-if="result.includes('访问成功')"
          message="成功绕过IP限制！说明系统直接信任了X-Forwarded-For头，存在安全漏洞。"
          type="success"
          show-icon
          style="margin-top: 16px"
        />
        <a-alert
          v-else
          message="访问被拒绝。尝试设置X-Forwarded-For头为10.0.0.1来绕过限制。"
          type="info"
          show-icon
          style="margin-top: 16px"
        />
      </div>
    </template>

    <template #source-code>
      <a-typography-paragraph>
        <strong>后端源码（存在漏洞）：</strong>
      </a-typography-paragraph>
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="sourceCode" style="margin: 0;">
          <pre class="code-block"><code>@RestController
@RequestMapping("/home/xff")
public class XFFVul {
    @RequestMapping("")
    public String xffVul(HttpServletRequest request, String xff) {
        String ip = request.getRemoteAddr();
        // 漏洞：直接信任X-Forwarded-For头，没有验证
        if (xff != null && xff.equals("true")) {
            String xffHeader = request.getHeader("X-Forwarded-For");
            if (xffHeader != null && !xffHeader.trim().isEmpty()) {
                // 直接使用用户提供的X-Forwarded-For值，可被伪造
                ip = xffHeader.split(",")[0].trim();
            }
        }
        if (ip != null && ip.equals("10.0.0.1")) {
            return "你的ip为: " + ip + ", 访问成功。";
        }
        return "你的ip为: " + ip + ", 本资源仅允许 10.0.0.1 访问。";
    }
}</code></pre>
        </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：直接信任X-Forwarded-For头，攻击者可以伪造IP地址绕过访问限制"
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
        <li><strong>不信任X-Forwarded-For头：</strong>优先使用request.getRemoteAddr()获取真实IP</li>
        <li><strong>代理IP白名单：</strong>如果必须使用X-Forwarded-For，只信任可信代理的IP</li>
        <li><strong>IP验证：</strong>验证IP地址格式，防止注入特殊字符</li>
        <li><strong>多重验证：</strong>结合多个HTTP头（X-Real-IP、X-Forwarded-For等）进行验证</li>
        <li><strong>日志记录：</strong>记录原始IP和X-Forwarded-For值，便于审计</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip.equals("10.0.0.1")) {
            // 允许访问
        }</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例(优先使用真实IP)
        public String getClientIp(HttpServletRequest request) {
            String ip = request.getRemoteAddr(); // 优先使用真实IP
            
            // 只有在可信代理环境下才使用X-Forwarded-For
            if (isTrustedProxy(request)) {
                String xff = request.getHeader("X-Forwarded-For");
                if (xff != null && isValidIp(xff)) {
                    // 取第一个IP（客户端真实IP）
                    ip = xff.split(",")[0].trim();
                }
            }
            
            // 验证IP格式
            if (!isValidIp(ip)) {
                throw new SecurityException("无效的IP地址");
            }
            
            return ip;
        }
        
        private boolean isValidIp(String ip) {
            return ip.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$");
        }</code></pre>
        </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { SwapOutlined, UnlockOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/home/xff")
public class XFFVul {
    @RequestMapping("")
    public String xffVul(HttpServletRequest request, String xff) {
        String ip = request.getRemoteAddr();
        // 漏洞：直接信任X-Forwarded-For头，没有验证
        if (xff != null && xff.equals("true")) {
            String xffHeader = request.getHeader("X-Forwarded-For");
            if (xffHeader != null && !xffHeader.trim().isEmpty()) {
                // 直接使用用户提供的X-Forwarded-For值，可被伪造
                ip = xffHeader.split(",")[0].trim();
            }
        }
        if (ip != null && ip.equals("10.0.0.1")) {
            return "你的ip为: " + ip + ", 访问成功。";
        }
        return "你的ip为: " + ip + ", 本资源仅允许 10.0.0.1 访问。";
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip.equals("10.0.0.1")) {
            // 允许访问
        }`

const fixCode2 = `// 正确示例(优先使用真实IP)
        public String getClientIp(HttpServletRequest request) {
            String ip = request.getRemoteAddr(); // 优先使用真实IP
            
            // 只有在可信代理环境下才使用X-Forwarded-For
            if (isTrustedProxy(request)) {
                String xff = request.getHeader("X-Forwarded-For");
                if (xff != null && isValidIp(xff)) {
                    // 取第一个IP（客户端真实IP）
                    ip = xff.split(",")[0].trim();
                }
            }
            
            // 验证IP格式
            if (!isValidIp(ip)) {
                throw new SecurityException("无效的IP地址");
            }
            
            return ip;
        }
        
        private boolean isValidIp(String ip) {
            return ip.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$");
        }`

const formState = reactive({
  xff: '',
  useProxy: true
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const headers = {}
    const params = {}
    
    if (formState.useProxy) {
      params.xff = 'true'
      headers['X-Forwarded-For'] = formState.xff
    }
    
    const response = await axios.get('/home/xff', {
      params: params,
      headers: headers
    })
    result.value = response.data
    message.success('访问完成')
  } catch (error) {
    message.error('访问失败：' + (error.response?.data || error.message))
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formState.xff = ''
  formState.useProxy = true
  result.value = ''
}
</script>
