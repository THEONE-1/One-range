<template>
  <vuln-template
    title="登录后跳转 / 营销跳转"
    subtitle="URL Redirection"
    level="中危"
    :icon="LinkOutlined"
    moduleKey="redirect"
  >
    <template #description>
      <a-typography-paragraph>
        场景：登录成功后根据 returnUrl 跳转，或营销落地页根据 link 参数跳转。
      </a-typography-paragraph>
      <a-typography-paragraph>
        风险点：
      </a-typography-paragraph>
      <ul>
        <li>returnUrl 可被篡改，跳转到钓鱼站</li>
        <li>营销链接可被替换为恶意域名</li>
        <li>借用站点信誉转发恶意链接</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        可疑的跳转参数示例：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        https://evil.com/login
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">伪装为登录后跳转地址</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">
        //evil.com
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">使用协议相对URL绕过检测</span>
      </a-space>

      <a-typography-paragraph code copyable style="margin-top: 12px">
        javascript:alert(1)
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">结合javascript伪协议执行代码</span>
      </a-space>

      <a-alert
        message="真实系统应：白名单校验或仅允许站内相对路径"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <a-form :model="formState" @finish="handleSubmit" layout="vertical">
        <a-form-item
          label="跳转目标"
          name="url"
          :rules="[{ required: true, message: '请输入内容' }]"
        >
          <a-input
            v-model:value="formState.url"
            size="large"
            placeholder="示例：https://example.com/dashboard 或 //evil.com/phish"
          />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" size="large" :loading="loading">
              <template #icon><play-circle-outlined /></template>
              执行测试
            </a-button>
            <a-button size="large" @click="handleReset">
              <template #icon><redo-outlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </template>

    <template #result v-if="result">
      <div class="result-content">
        <pre style="margin: 0;">{{ result }}</pre>
      </div>
      <a-alert
        v-if="result && result.includes('重定向')"
        message="如果页面发生了重定向，说明存在URL重定向漏洞。攻击者可以利用此漏洞进行钓鱼攻击。"
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
          <pre class="code-block"><code>@RestController
@RequestMapping("/home/redirect")
public class RedirectVul {
    @RequestMapping("")
    public String redirect(String url, HttpServletResponse response) {
        try {
            if (url != null && !url.trim().isEmpty()) {
                // 漏洞：直接将用户输入的URL传递给sendRedirect()
                // 没有验证URL是否为可信域名，攻击者可以重定向到恶意网站
                response.sendRedirect(url);
                return "重定向到: " + url;
            }
            return "URL不能为空";
        } catch (IOException e) {
            return "重定向失败: " + e.getMessage();
        }
    }
}</code></pre>
        </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：直接将用户输入的URL传递给sendRedirect()，没有验证URL是否为可信域名。攻击者可以构造恶意URL，将用户重定向到钓鱼网站，进行钓鱼攻击。"
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
        <li><strong>URL白名单：</strong>只允许重定向到预定义的白名单URL</li>
        <li><strong>相对路径：</strong>只允许相对路径重定向，不允许绝对URL</li>
        <li><strong>域名验证：</strong>验证URL的域名是否为可信域名</li>
        <li><strong>协议限制：</strong>只允许http/https协议，禁止javascript:、data:等协议</li>
        <li><strong>重定向确认：</strong>对敏感操作的重定向，要求用户确认</li>
        <li><strong>使用相对路径：</strong>使用相对路径而不是绝对URL</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        @RequestMapping("")
        public String redirect(String url, HttpServletResponse response) {
            response.sendRedirect(url);
            // 攻击者可以输入: https://evil.com/phishing
        }</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：URL白名单验证
        private static final String[] ALLOWED_REDIRECT_URLS = {
            "/home",
            "/login",
            "/dashboard"
        };
        
        @RequestMapping("")
        public String redirect(String url, HttpServletResponse response) {
            if (url == null || url.trim().isEmpty()) {
                return "URL不能为空";
            }
            
            // 验证URL是否在白名单中
            boolean isAllowed = false;
            for (String allowedUrl : ALLOWED_REDIRECT_URLS) {
                if (url.equals(allowedUrl)) {
                    isAllowed = true;
                    break;
                }
            }
            
            if (!isAllowed) {
                throw new SecurityException("不允许的重定向URL");
            }
            
            response.sendRedirect(url);
            return "重定向到: " + url;
        }</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：只允许相对路径
        @RequestMapping("")
        public String redirect(String url, HttpServletResponse response) {
            if (url == null || url.trim().isEmpty()) {
                return "URL不能为空";
            }
            
            // 只允许相对路径（以/开头，不以http://或https://开头）
            if (url.startsWith("http://") || url.startsWith("https://") ||
                url.startsWith("javascript:") || url.startsWith("data:")) {
                throw new SecurityException("只允许相对路径重定向");
            }
            
            // 验证路径格式（防止路径遍历）
            if (!url.startsWith("/") || url.contains("..")) {
                throw new SecurityException("无效的路径格式");
            }
            
            response.sendRedirect(url);
            return "重定向到: " + url;
        }</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：域名白名单验证
        private static final String[] ALLOWED_DOMAINS = {
            "www.example.com",
            "app.example.com"
        };
        
        @RequestMapping("")
        public String redirect(String url, HttpServletResponse response) {
            if (url == null || url.trim().isEmpty()) {
                return "URL不能为空";
            }
            
            try {
                URL urlObj = new URL(url);
                String protocol = urlObj.getProtocol();
                String host = urlObj.getHost();
                
                // 验证协议（只允许http/https）
                if (!protocol.equals("http") && !protocol.equals("https")) {
                    throw new SecurityException("只允许http/https协议");
                }
                
                // 验证域名是否在白名单中
                boolean isAllowed = false;
                for (String domain : ALLOWED_DOMAINS) {
                    if (host.equals(domain) || host.endsWith("." + domain)) {
                        isAllowed = true;
                        break;
                    }
                }
                
                if (!isAllowed) {
                    throw new SecurityException("域名不在白名单中");
                }
                
                response.sendRedirect(url);
                return "重定向到: " + url;
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("无效的URL格式");
            }
        }</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：使用重定向映射表
        private static final Map&lt;String, String&gt; REDIRECT_MAP = new HashMap&lt;&gt;();
        static {
            REDIRECT_MAP.put("home", "/home");
            REDIRECT_MAP.put("login", "/login");
            REDIRECT_MAP.put("dashboard", "/dashboard");
        }
        
        @RequestMapping("")
        public String redirect(String key, HttpServletResponse response) {
            // 使用key而不是直接使用URL
            String url = REDIRECT_MAP.get(key);
            if (url == null) {
                throw new SecurityException("无效的重定向key");
            }
            
            response.sendRedirect(url);
            return "重定向到: " + url;
        }</code></pre>
        </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { LinkOutlined, PlayCircleOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/home/redirect")
public class RedirectVul {
    @RequestMapping("")
    public String redirect(String url, HttpServletResponse response) {
        try {
            if (url != null && !url.trim().isEmpty()) {
                // 漏洞：直接将用户输入的URL传递给sendRedirect()
                // 没有验证URL是否为可信域名，攻击者可以重定向到恶意网站
                response.sendRedirect(url);
                return "重定向到: " + url;
            }
            return "URL不能为空";
        } catch (IOException e) {
            return "重定向失败: " + e.getMessage();
        }
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
        @RequestMapping("")
        public String redirect(String url, HttpServletResponse response) {
            response.sendRedirect(url);
            // 攻击者可以输入: https://evil.com/phishing
        }`

const fixCode2 = `// 正确示例1：URL白名单验证
        private static final String[] ALLOWED_REDIRECT_URLS = {
            "/home",
            "/login",
            "/dashboard"
        };
        
        @RequestMapping("")
        public String redirect(String url, HttpServletResponse response) {
            if (url == null || url.trim().isEmpty()) {
                return "URL不能为空";
            }
            
            // 验证URL是否在白名单中
            boolean isAllowed = false;
            for (String allowedUrl : ALLOWED_REDIRECT_URLS) {
                if (url.equals(allowedUrl)) {
                    isAllowed = true;
                    break;
                }
            }
            
            if (!isAllowed) {
                throw new SecurityException("不允许的重定向URL");
            }
            
            response.sendRedirect(url);
            return "重定向到: " + url;
        }`

const fixCode3 = `// 正确示例2：只允许相对路径
        @RequestMapping("")
        public String redirect(String url, HttpServletResponse response) {
            if (url == null || url.trim().isEmpty()) {
                return "URL不能为空";
            }
            
            // 只允许相对路径（以/开头，不以http://或https://开头）
            if (url.startsWith("http://") || url.startsWith("https://") ||
                url.startsWith("javascript:") || url.startsWith("data:")) {
                throw new SecurityException("只允许相对路径重定向");
            }
            
            // 验证路径格式（防止路径遍历）
            if (!url.startsWith("/") || url.contains("..")) {
                throw new SecurityException("无效的路径格式");
            }
            
            response.sendRedirect(url);
            return "重定向到: " + url;
        }`

const fixCode4 = `// 正确示例3：域名白名单验证
        private static final String[] ALLOWED_DOMAINS = {
            "www.example.com",
            "app.example.com"
        };
        
        @RequestMapping("")
        public String redirect(String url, HttpServletResponse response) {
            if (url == null || url.trim().isEmpty()) {
                return "URL不能为空";
            }
            
            try {
                URL urlObj = new URL(url);
                String protocol = urlObj.getProtocol();
                String host = urlObj.getHost();
                
                // 验证协议（只允许http/https）
                if (!protocol.equals("http") && !protocol.equals("https")) {
                    throw new SecurityException("只允许http/https协议");
                }
                
                // 验证域名是否在白名单中
                boolean isAllowed = false;
                for (String domain : ALLOWED_DOMAINS) {
                    if (host.equals(domain) || host.endsWith("." + domain)) {
                        isAllowed = true;
                        break;
                    }
                }
                
                if (!isAllowed) {
                    throw new SecurityException("域名不在白名单中");
                }
                
                response.sendRedirect(url);
                return "重定向到: " + url;
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("无效的URL格式");
            }
        }`

const fixCode5 = `// 正确示例4：使用重定向映射表
        private static final Map<String, String> REDIRECT_MAP = new HashMap<>();
        static {
            REDIRECT_MAP.put("home", "/home");
            REDIRECT_MAP.put("login", "/login");
            REDIRECT_MAP.put("dashboard", "/dashboard");
        }
        
        @RequestMapping("")
        public String redirect(String key, HttpServletResponse response) {
            // 使用key而不是直接使用URL
            String url = REDIRECT_MAP.get(key);
            if (url == null) {
                throw new SecurityException("无效的重定向key");
            }
            
            response.sendRedirect(url);
            return "重定向到: " + url;
        }`

const formState = reactive({
  url: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    // 直接使用window.location进行重定向测试
    if (formState.url && formState.url.trim()) {
      const redirectUrl = `/home/redirect?url=${encodeURIComponent(formState.url)}`
      window.location.href = redirectUrl
      // 如果重定向失败，3秒后显示结果
      setTimeout(() => {
        result.value = '如果页面没有重定向，说明重定向被阻止或URL无效'
        loading.value = false
      }, 3000)
    } else {
      message.warning('请输入URL')
      loading.value = false
    }
  } catch (error) {
    message.error('测试失败：' + (error.response?.data || error.message))
    loading.value = false
  }
}

const handleReset = () => {
  formState.url = ''
  result.value = ''
}
</script>
