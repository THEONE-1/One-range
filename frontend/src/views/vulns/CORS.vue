<template>
  <vuln-template
    title="CORS跨域"
    subtitle="Cross-Origin Resource Sharing"
    level="中危"
    :icon="ShareAltOutlined"
    moduleKey="cors"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>CORS (Cross-Origin Resource Sharing)</strong> 跨域资源共享是一种基于HTTP头的机制,允许服务器标识除了它自己以外的其它来源。
      </a-typography-paragraph>
      <a-typography-paragraph>
        CORS漏洞的危害：
      </a-typography-paragraph>
      <ul>
        <li>当Access-Control-Allow-Origin配置不当时可能导致安全问题</li>
        <li>攻击者可以从恶意网站读取敏感数据</li>
        <li>可能导致用户隐私泄露</li>
        <li>结合其他漏洞可能造成更大危害</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的CORS配置不当：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        Origin: https://evil.com
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">如果服务器直接返回该Origin,说明存在漏洞</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">
        Access-Control-Allow-Origin: *
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">允许任何来源访问,配置过于宽松</span>
      </a-space>

      <a-alert
        message="请在测试环境中验证CORS配置的安全性！"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <div class="api-call-container">
        <a-card title="跨域API调用" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            模拟从不同来源的网站调用本API接口，测试CORS跨域资源共享配置。
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="请求来源 (Origin)"
            name="origin"
            :rules="[{ required: true, message: '请输入Origin' }]"
          >
            <a-input
              v-model:value="formState.origin"
              size="large"
              placeholder="例如：https://example.com"
              prefix="🔗"
            />
            <div style="margin-top: 8px; color: #8c8c8c; font-size: 12px;">
              模拟从该来源发起的跨域请求
            </div>
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><api-outlined /></template>
                发起跨域请求
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
        v-if="result && result.includes('登录用户名')"
        message="如果返回了用户敏感信息，说明CORS配置不当，允许了任意Origin访问。"
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
@RequestMapping("/home/cors")
public class CORSVul {
    @GetMapping("")
    public String corsVul(HttpServletRequest request, 
                          HttpServletResponse response, 
                          HttpSession httpSession) {
        // 漏洞：直接将请求头中的Origin设置为Access-Control-Allow-Origin
        // 攻击者可以伪造Origin头，导致任意域名都可以访问
        String origin = request.getHeader("origin");
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", 
            "GET,POST,PUT,DELETE,OPTIONS");
        
        // 返回敏感信息
        String username = (String) httpSession.getAttribute("username");
        Admin admin = adminService.getInfoByUserName(username);
        return "登录用户名: " + admin.getUsername() + 
               ", 密码: " + admin.getPassword();
    }
}</code></pre>
        </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：直接将请求头中的Origin设置为Access-Control-Allow-Origin，没有验证Origin是否可信。攻击者可以从恶意网站发送请求，伪造Origin头，从而绕过CORS限制访问敏感数据。"
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
        <li><strong>Origin白名单：</strong>验证Origin是否在允许的白名单中，只返回白名单中的Origin</li>
        <li><strong>固定Origin：</strong>如果只有一个前端域名，直接设置固定的Origin</li>
        <li><strong>避免通配符：</strong>不要使用*作为Access-Control-Allow-Origin，特别是当Allow-Credentials为true时</li>
        <li><strong>验证Origin格式：</strong>验证Origin格式，防止注入特殊字符</li>
        <li><strong>使用Spring CORS配置：</strong>使用Spring的CORS配置类，统一管理CORS策略</li>
        <li><strong>敏感操作限制：</strong>对敏感操作（如用户信息查询）限制CORS</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        String origin = request.getHeader("origin");
        response.setHeader("Access-Control-Allow-Origin", origin);
        // 攻击者可以设置Origin: https://evil.com</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：Origin白名单验证
        private static final String[] ALLOWED_ORIGINS = {
            "https://www.example.com",
            "https://app.example.com"
        };
        
        @GetMapping("")
        public String corsVul(HttpServletRequest request, 
                              HttpServletResponse response) {
            String origin = request.getHeader("origin");
            
            // 验证Origin是否在白名单中
            boolean isAllowed = false;
            for (String allowedOrigin : ALLOWED_ORIGINS) {
                if (allowedOrigin.equals(origin)) {
                    isAllowed = true;
                    break;
                }
            }
            
            if (isAllowed) {
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Credentials", "true");
            } else {
                // 不在白名单中，不设置CORS头
                throw new SecurityException("Origin不在白名单中");
            }
            
            // 返回数据...
        }</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：固定Origin（如果只有一个前端）
        @GetMapping("")
        public String corsVul(HttpServletRequest request, 
                              HttpServletResponse response) {
            // 直接设置固定的Origin，不依赖请求头
            response.setHeader("Access-Control-Allow-Origin", 
                "https://www.example.com");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", 
                "GET,POST,PUT,DELETE,OPTIONS");
            
            // 返回数据...
        }</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：使用Spring CORS配置
        @Configuration
        public class CorsConfig implements WebMvcConfigurer {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("https://www.example.com", 
                                   "https://app.example.com")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        }</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：验证Origin格式
        private boolean isValidOrigin(String origin) {
            if (origin == null || origin.isEmpty()) {
                return false;
            }
            
            // 验证Origin格式（必须是http://或https://开头）
            if (!origin.matches("^https?://[a-zA-Z0-9.-]+(:[0-9]+)?$")) {
                return false;
            }
            
            // 验证是否为允许的域名
            try {
                URL url = new URL(origin);
                String host = url.getHost();
                return Arrays.asList(ALLOWED_DOMAINS).contains(host);
            } catch (MalformedURLException e) {
                return false;
            }
        }

        // 正确示例5：敏感操作不设置CORS
        @GetMapping("/sensitive")
        public String sensitiveData(HttpServletRequest request, 
                                   HttpServletResponse response) {
            // 敏感操作不设置CORS头，只允许同源访问
            // 不设置Access-Control-Allow-Origin
            return sensitiveData;
        }</code></pre>
        </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { ShareAltOutlined, ApiOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/home/cors")
public class CORSVul {
    @GetMapping("")
    public String corsVul(HttpServletRequest request, 
                          HttpServletResponse response, 
                          HttpSession httpSession) {
        // 漏洞：直接将请求头中的Origin设置为Access-Control-Allow-Origin
        // 攻击者可以伪造Origin头，导致任意域名都可以访问
        String origin = request.getHeader("origin");
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", 
            "GET,POST,PUT,DELETE,OPTIONS");
        
        // 返回敏感信息
        String username = (String) httpSession.getAttribute("username");
        Admin admin = adminService.getInfoByUserName(username);
        return "登录用户名: " + admin.getUsername() + 
               ", 密码: " + admin.getPassword();
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
        String origin = request.getHeader("origin");
        response.setHeader("Access-Control-Allow-Origin", origin);
        // 攻击者可以设置Origin: https://evil.com`

const fixCode2 = `// 正确示例1：Origin白名单验证
        private static final String[] ALLOWED_ORIGINS = {
            "https://www.example.com",
            "https://app.example.com"
        };
        
        @GetMapping("")
        public String corsVul(HttpServletRequest request, 
                              HttpServletResponse response) {
            String origin = request.getHeader("origin");
            
            // 验证Origin是否在白名单中
            boolean isAllowed = false;
            for (String allowedOrigin : ALLOWED_ORIGINS) {
                if (allowedOrigin.equals(origin)) {
                    isAllowed = true;
                    break;
                }
            }
            
            if (isAllowed) {
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Credentials", "true");
            } else {
                // 不在白名单中，不设置CORS头
                throw new SecurityException("Origin不在白名单中");
            }
            
            // 返回数据...
        }`

const fixCode3 = `// 正确示例2：固定Origin（如果只有一个前端）
        @GetMapping("")
        public String corsVul(HttpServletRequest request, 
                              HttpServletResponse response) {
            // 直接设置固定的Origin，不依赖请求头
            response.setHeader("Access-Control-Allow-Origin", 
                "https://www.example.com");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", 
                "GET,POST,PUT,DELETE,OPTIONS");
            
            // 返回数据...
        }`

const fixCode4 = `// 正确示例3：使用Spring CORS配置
        @Configuration
        public class CorsConfig implements WebMvcConfigurer {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("https://www.example.com", 
                                   "https://app.example.com")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        }`

const fixCode5 = `// 正确示例4：验证Origin格式
        private boolean isValidOrigin(String origin) {
            if (origin == null || origin.isEmpty()) {
                return false;
            }
            
            // 验证Origin格式（必须是http://或https://开头）
            if (!origin.matches("^https?://[a-zA-Z0-9.-]+(:[0-9]+)?$")) {
                return false;
            }
            
            // 验证是否为允许的域名
            try {
                URL url = new URL(origin);
                String host = url.getHost();
                return Arrays.asList(ALLOWED_DOMAINS).contains(host);
            } catch (MalformedURLException e) {
                return false;
            }
        }

        // 正确示例5：敏感操作不设置CORS
        @GetMapping("/sensitive")
        public String sensitiveData(HttpServletRequest request, 
                                   HttpServletResponse response) {
            // 敏感操作不设置CORS头，只允许同源访问
            // 不设置Access-Control-Allow-Origin
            return sensitiveData;
        }`

const formState = reactive({
  origin: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/cors', {
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
  formState.origin = ''
  result.value = ''
}
</script>
