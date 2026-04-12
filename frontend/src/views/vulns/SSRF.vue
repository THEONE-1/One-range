<template>
  <vuln-template
    title="SSRF"
    subtitle="服务端请求伪造"
    level="高危"
    :icon="GlobalOutlined"
    moduleKey="ssrf"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>服务端请求伪造</strong> 是一种由攻击者构造请求、再由服务端代为发起请求的安全漏洞。
      </a-typography-paragraph>
      <a-typography-paragraph>
        SSRF漏洞的危害：
      </a-typography-paragraph>
      <ul>
        <li>探测内网服务和端口</li>
        <li>访问内网敏感接口</li>
        <li>读取本地文件(file://协议)</li>
        <li>攻击内网其他服务</li>
        <li>绕过防火墙访问限制</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的SSRF攻击payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        http://127.0.0.1:8080
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">访问本机服务</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">
        http://192.168.1.1
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">探测内网IP</span>
      </a-space>

      <a-typography-paragraph code copyable style="margin-top: 12px">
        file:///etc/passwd
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">使用file协议读取本地文件</span>
      </a-space>

      <a-alert
        message="必须对URL进行白名单校验,禁止访问内网地址！"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <div class="url-preview-container">
        <a-card title="URL预览功能" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            输入一个URL地址，系统将为您预览该URL的内容或加载图片。
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="URL地址"
            name="url"
            :rules="[{ required: true, message: '请输入URL地址' }]"
          >
            <a-input
              v-model:value="formState.url"
              size="large"
              placeholder="例如：https://www.example.com/image.jpg"
              prefix="🌐"
            />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><eye-outlined /></template>
                预览URL
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

    <template #input-hint>
      <a-alert message="测试建议" type="warning" show-icon style="margin-bottom: 12px;" />
      <p><strong>建议测试payload：</strong></p>
      <ul style="padding-left: 20px; margin-bottom: 0;">
        <li><a-typography-text code copyable>http://127.0.0.1:8080</a-typography-text> - 访问本机服务</li>
        <li><a-typography-text code copyable>http://192.168.1.1</a-typography-text> - 探测内网地址</li>
        <li><a-typography-text code copyable>http://localhost/admin</a-typography-text> - 访问本地管理页面</li>
        <li><a-typography-text code copyable>file:///etc/passwd</a-typography-text> - 尝试读取本地文件</li>
      </ul>
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
          <pre class="code-block"><code>@RestController
@RequestMapping("/home/ssrf")
public class SSRFVul {
    @RequestMapping("")
    public String urlConnection(@RequestParam String url, 
                                String isHttp, String isIntranet) {
        if (url.equals("")) {
            return "请输入url";
        }
        
        // 漏洞：安全检测是可选的，默认情况下不进行检测
        if (isHttp != null && isHttp.equals("true")) {
            if (!Security.isHttp(url)) {
                return "不允许非http/https协议!!!";
            }
        }
        if (isIntranet != null && isIntranet.equals("true")) {
            if (Security.isIntranet(url)) {
                return "不允许访问内网!!!";
            }
        }
        
        // 漏洞：直接将用户输入的URL传递给HTTP.URLConnection()
        // 没有默认的安全检测，攻击者可以访问内网或使用file://协议
        String results = HTTP.URLConnection(url);
        return results;
    }
}</code></pre>
        </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：1. 安全检测是可选的，默认不启用；2. 直接将用户输入的URL传递给HTTP请求，没有验证协议和IP地址；3. 可能允许file://、gopher://等危险协议；4. 可能访问内网地址"
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
        <li><strong>URL白名单：</strong>严格限制允许访问的域名和协议，使用白名单机制</li>
        <li><strong>禁止内网访问：</strong>默认过滤私有IP段(10.x, 172.16.x, 192.168.x, 127.x)</li>
        <li><strong>禁用危险协议：</strong>只允许http/https，禁止file、gopher、dict等协议</li>
        <li><strong>DNS重绑定防护：</strong>在请求前后都检查解析的IP地址，防止DNS重绑定攻击</li>
        <li><strong>统一出口：</strong>使用专用的HTTP客户端，配置安全策略</li>
        <li><strong>IP验证：</strong>解析URL的IP地址，验证是否为内网IP</li>
        <li><strong>重定向限制：</strong>禁止跟随重定向，或限制重定向次数</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        @RequestMapping("")
        public String urlConnection(String url) {
            // 直接使用用户输入的URL，没有验证
            String results = HTTP.URLConnection(url);
            return results;
        }
        // 攻击者可以输入: file:///etc/passwd 或 http://127.0.0.1:8080</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：URL白名单验证
        private static final String[] ALLOWED_DOMAINS = {
            "https://api.example.com",
            "https://cdn.example.com"
        };
        
        @RequestMapping("")
        public String urlConnection(String url) {
            // 1. 验证URL格式
            URL urlObj;
            try {
                urlObj = new URL(url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("无效的URL格式");
            }
            
            // 2. 验证协议（只允许http/https）
            String protocol = urlObj.getProtocol().toLowerCase();
            if (!protocol.equals("http") && !protocol.equals("https")) {
                throw new SecurityException("只允许http/https协议");
            }
            
            // 3. 验证域名是否在白名单中
            String host = urlObj.getHost();
            boolean isAllowed = false;
            for (String domain : ALLOWED_DOMAINS) {
                try {
                    URL allowedUrl = new URL(domain);
                    if (host.equals(allowedUrl.getHost())) {
                        isAllowed = true;
                        break;
                    }
                } catch (MalformedURLException e) {
                    continue;
                }
            }
            
            if (!isAllowed) {
                throw new SecurityException("域名不在白名单中");
            }
            
            // 4. 解析IP地址，防止DNS重绑定
            InetAddress address = InetAddress.getByName(host);
            String ip = address.getHostAddress();
            
            // 5. 验证IP是否为内网IP
            if (isInternalIP(ip)) {
                throw new SecurityException("禁止访问内网地址");
            }
            
            // 6. 使用安全的HTTP客户端
            String results = HTTP.URLConnection(url);
            return results;
        }</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：内网IP检测函数
        private boolean isInternalIP(String ip) {
            if (ip == null || ip.isEmpty()) {
                return true;
            }
            
            // 127.0.0.0/8
            if (ip.startsWith("127.")) {
                return true;
            }
            
            // 10.0.0.0/8
            if (ip.startsWith("10.")) {
                return true;
            }
            
            // 172.16.0.0/12
            if (ip.startsWith("172.")) {
                String[] parts = ip.split("\\.");
                if (parts.length >= 2) {
                    int second = Integer.parseInt(parts[1]);
                    if (second >= 16 && second <= 31) {
                        return true;
                    }
                }
            }
            
            // 192.168.0.0/16
            if (ip.startsWith("192.168.")) {
                return true;
            }
            
            // 0.0.0.0/8
            if (ip.startsWith("0.")) {
                return true;
            }
            
            return false;
        }</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：使用OkHttp配置安全策略
        import okhttp3.OkHttpClient;
        import okhttp3.Request;
        import okhttp3.Response;
        
        OkHttpClient client = new OkHttpClient.Builder()
            .followRedirects(false) // 禁止跟随重定向
            .build();
        
        Request request = new Request.Builder()
            .url(url)
            .build();
        
        Response response = client.newCall(request).execute();
        return response.body().string();</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：DNS重绑定防护
        @RequestMapping("")
        public String urlConnection(String url) {
            URL urlObj = new URL(url);
            String host = urlObj.getHost();
            
            // 第一次DNS解析
            InetAddress address1 = InetAddress.getByName(host);
            String ip1 = address1.getHostAddress();
            
            if (isInternalIP(ip1)) {
                throw new SecurityException("禁止访问内网地址");
            }
            
            // 执行请求
            String results = HTTP.URLConnection(url);
            
            // 请求后再次DNS解析，防止DNS重绑定
            InetAddress address2 = InetAddress.getByName(host);
            String ip2 = address2.getHostAddress();
            
            if (!ip1.equals(ip2)) {
                throw new SecurityException("检测到DNS重绑定攻击");
            }
            
            return results;
        }</code></pre>
        </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { GlobalOutlined, EyeOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/home/ssrf")
public class SSRFVul {
    @RequestMapping("")
    public String urlConnection(@RequestParam String url, 
                                String isHttp, String isIntranet) {
        if (url.equals("")) {
            return "请输入url";
        }
        
        // 漏洞：安全检测是可选的，默认情况下不进行检测
        if (isHttp != null && isHttp.equals("true")) {
            if (!Security.isHttp(url)) {
                return "不允许非http/https协议!!!";
            }
        }
        if (isIntranet != null && isIntranet.equals("true")) {
            if (Security.isIntranet(url)) {
                return "不允许访问内网!!!";
            }
        }
        
        // 漏洞：直接将用户输入的URL传递给HTTP.URLConnection()
        // 没有默认的安全检测，攻击者可以访问内网或使用file://协议
        String results = HTTP.URLConnection(url);
        return results;
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
        @RequestMapping("")
        public String urlConnection(String url) {
            // 直接使用用户输入的URL，没有验证
            String results = HTTP.URLConnection(url);
            return results;
        }
        // 攻击者可以输入: file:///etc/passwd 或 http://127.0.0.1:8080`

const fixCode2 = `// 正确示例1：URL白名单验证
        private static final String[] ALLOWED_DOMAINS = {
            "https://api.example.com",
            "https://cdn.example.com"
        };
        
        @RequestMapping("")
        public String urlConnection(String url) {
            // 1. 验证URL格式
            URL urlObj;
            try {
                urlObj = new URL(url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("无效的URL格式");
            }
            
            // 2. 验证协议（只允许http/https）
            String protocol = urlObj.getProtocol().toLowerCase();
            if (!protocol.equals("http") && !protocol.equals("https")) {
                throw new SecurityException("只允许http/https协议");
            }
            
            // 3. 验证域名是否在白名单中
            String host = urlObj.getHost();
            boolean isAllowed = false;
            for (String domain : ALLOWED_DOMAINS) {
                try {
                    URL allowedUrl = new URL(domain);
                    if (host.equals(allowedUrl.getHost())) {
                        isAllowed = true;
                        break;
                    }
                } catch (MalformedURLException e) {
                    continue;
                }
            }
            
            if (!isAllowed) {
                throw new SecurityException("域名不在白名单中");
            }
            
            // 4. 解析IP地址，防止DNS重绑定
            InetAddress address = InetAddress.getByName(host);
            String ip = address.getHostAddress();
            
            // 5. 验证IP是否为内网IP
            if (isInternalIP(ip)) {
                throw new SecurityException("禁止访问内网地址");
            }
            
            // 6. 使用安全的HTTP客户端
            String results = HTTP.URLConnection(url);
            return results;
        }`

const fixCode3 = `// 正确示例2：内网IP检测函数
        private boolean isInternalIP(String ip) {
            if (ip == null || ip.isEmpty()) {
                return true;
            }
            
            // 127.0.0.0/8
            if (ip.startsWith("127.")) {
                return true;
            }
            
            // 10.0.0.0/8
            if (ip.startsWith("10.")) {
                return true;
            }
            
            // 172.16.0.0/12
            if (ip.startsWith("172.")) {
                String[] parts = ip.split("\\.");
                if (parts.length >= 2) {
                    int second = Integer.parseInt(parts[1]);
                    if (second >= 16 && second <= 31) {
                        return true;
                    }
                }
            }
            
            // 192.168.0.0/16
            if (ip.startsWith("192.168.")) {
                return true;
            }
            
            // 0.0.0.0/8
            if (ip.startsWith("0.")) {
                return true;
            }
            
            return false;
        }`

const fixCode4 = `// 正确示例3：使用OkHttp配置安全策略
        import okhttp3.OkHttpClient;
        import okhttp3.Request;
        import okhttp3.Response;
        
        OkHttpClient client = new OkHttpClient.Builder()
            .followRedirects(false) // 禁止跟随重定向
            .build();
        
        Request request = new Request.Builder()
            .url(url)
            .build();
        
        Response response = client.newCall(request).execute();
        return response.body().string();`

const fixCode5 = `// 正确示例4：DNS重绑定防护
        @RequestMapping("")
        public String urlConnection(String url) {
            URL urlObj = new URL(url);
            String host = urlObj.getHost();
            
            // 第一次DNS解析
            InetAddress address1 = InetAddress.getByName(host);
            String ip1 = address1.getHostAddress();
            
            if (isInternalIP(ip1)) {
                throw new SecurityException("禁止访问内网地址");
            }
            
            // 执行请求
            String results = HTTP.URLConnection(url);
            
            // 请求后再次DNS解析，防止DNS重绑定
            InetAddress address2 = InetAddress.getByName(host);
            String ip2 = address2.getHostAddress();
            
            if (!ip1.equals(ip2)) {
                throw new SecurityException("检测到DNS重绑定攻击");
            }
            
            return results;
        }`

const formState = reactive({
  url: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/ssrf', {
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
  formState.url = ''
  result.value = ''
}
</script>
