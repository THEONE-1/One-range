<template>
  <vuln-template
    title="Shiro反序列化"
    subtitle="Shiro Deserialization RCE"
    level="严重"
    :icon="SafetyOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>Apache Shiro</strong>是Java安全框架,其RememberMe功能存在反序列化漏洞(Shiro-550)。
      </a-typography-paragraph>
      <a-typography-paragraph>
        Shiro反序列化漏洞的危害：
      </a-typography-paragraph>
      <ul>
        <li>默认AES密钥泄露导致Cookie可被伪造</li>
        <li>通过rememberMe Cookie触发反序列化</li>
        <li>远程代码执行</li>
        <li>完全控制服务器</li>
      </ul>
      <a-typography-paragraph>
        漏洞版本：Apache Shiro &lt; 1.2.4
      </a-typography-paragraph>
    </template>

    <template #payload>
      <a-typography-paragraph>
        Shiro漏洞利用流程：
      </a-typography-paragraph>
      <a-typography-paragraph>
        1. 使用已知AES密钥加密恶意序列化数据
      </a-typography-paragraph>
      <a-typography-paragraph>
        2. 将加密后的数据放入rememberMe Cookie
      </a-typography-paragraph>
      <a-typography-paragraph>
        3. 服务端解密并反序列化触发RCE
      </a-typography-paragraph>
      
      <a-typography-paragraph style="margin-top: 12px">
        <strong>默认密钥：</strong>
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        kPH+bIxk5D2deZiIxcaaaA==
      </a-typography-paragraph>

      <a-alert
        message="更换默认密钥,升级到安全版本！"
        type="error"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <div class="auth-container">
        <a-card title="用户认证服务" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            验证rememberMe Cookie，用于保持用户登录状态。
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="RememberMe Cookie"
            name="payload"
            :rules="[{ required: true, message: '请输入Cookie值' }]"
          >
            <a-input
              v-model:value="formState.payload"
              size="large"
              placeholder="输入rememberMe Cookie值（Base64编码）"
              prefix="🍪"
            />
            <div style="margin-top: 8px; color: #8c8c8c; font-size: 12px;">
              系统将验证并解析Cookie中的用户信息
            </div>
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><safety-outlined /></template>
                验证Cookie
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
      <p><strong>Shiro漏洞利用步骤：</strong></p>
      <ol style="padding-left: 20px; margin-bottom: 0;">
        <li>使用已知的AES密钥 <code>kPH+bIxk5D2deZiIxcaaaA==</code></li>
        <li>生成恶意序列化对象(如CommonsCollections链)</li>
        <li>使用AES-CBC模式加密</li>
        <li>Base64编码后设置到rememberMe Cookie</li>
        <li>需要使用专用工具如shiro_attack生成payload</li>
      </ol>
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
@RequestMapping("/home/shiro")
public class Shiro {
    @PostMapping("/login")
    public String login(String username, String password, String rememberMe) {
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        
        // 漏洞：如果rememberMe为true，Shiro会将用户信息序列化后加密存储在Cookie中
        if (rememberMe.equals("true")) {
            token.setRememberMe(true);
        }
        
        try {
            subject.login(token);
            return "登录成功";
        } catch (Exception e) {
            return "认证失败";
        }
    }
    
    @RequestMapping(value = "/getkey")
    public String getShiroKey() {
        // 漏洞：使用默认的AES密钥
        // Shiro默认使用硬编码的密钥kPH+bIxk5D2deZiIxcaaaA==
        byte[] key = new CookieRememberMeManager().getCipherKey();
        return "shiro key: \\n" + new String(Base64.getEncoder().encode(key));
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：1. Shiro使用默认的AES密钥（kPH+bIxk5D2deZiIxcaaaA==）加密rememberMe Cookie；2. 攻击者可以使用已知密钥加密恶意序列化数据，设置到rememberMe Cookie；3. Shiro解密并反序列化Cookie时触发RCE。这是Shiro-550漏洞。"
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
        <li><strong>升级版本：</strong>升级到Apache Shiro 1.2.5或更高版本</li>
        <li><strong>更换密钥：</strong>不使用默认AES密钥，生成强随机密钥</li>
        <li><strong>禁用rememberMe：</strong>如非必要，禁用rememberMe功能</li>
        <li><strong>使用安全Cookie：</strong>设置HttpOnly和Secure标志</li>
        <li><strong>添加签名验证：</strong>对Cookie添加HMAC签名验证</li>
        <li><strong>移除Gadget链：</strong>移除Commons Collections等危险依赖</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        // 使用默认密钥，攻击者可以使用已知密钥伪造Cookie
        CookieRememberMeManager rememberMeManager = new CookieRememberMeManager();
// 默认密钥是硬编码的：kPH+bIxk5D2deZiIxcaaaA==</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：配置自定义密钥（推荐）
        import org.apache.shiro.mgt.SecurityManager;
        import org.apache.shiro.web.mgt.CookieRememberMeManager;
        import java.util.Base64;
        import javax.crypto.KeyGenerator;
        
        @Bean
        public CookieRememberMeManager rememberMeManager() {
            CookieRememberMeManager manager = new CookieRememberMeManager();
            
            // 生成强随机密钥
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            byte[] key = keyGenerator.generateKey().getEncoded();
            
            // 设置自定义密钥
            manager.setCipherKey(key);
            
            return manager;
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：在shiro.ini中配置自定义密钥
        [main]
        # 生成随机密钥：使用KeyGenerator生成128位密钥，然后Base64编码
        securityManager.rememberMeManager.cipherKey=YOUR_CUSTOM_BASE64_KEY_HERE
        
        # 或者禁用rememberMe功能
# securityManager.rememberMeManager = null</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：在Spring Boot中配置
        @Configuration
        public class ShiroConfig {
            @Bean
            public CookieRememberMeManager rememberMeManager() {
                CookieRememberMeManager manager = new CookieRememberMeManager();
                
                // 从配置文件读取密钥（不要硬编码）
                String cipherKey = environment.getProperty("shiro.cipher.key");
                if (cipherKey == null || cipherKey.isEmpty()) {
                    // 生成新密钥
                    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
                    keyGenerator.init(128);
                    byte[] key = keyGenerator.generateKey().getEncoded();
                    cipherKey = Base64.getEncoder().encodeToString(key);
                }
                
                manager.setCipherKey(Base64.getDecoder().decode(cipherKey));
                return manager;
            }
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：禁用rememberMe功能
        @PostMapping("/login")
        public String login(String username, String password) {
            Subject subject = SecurityUtils.getSubject();
            UsernamePasswordToken token = new UsernamePasswordToken(username, password);
            // 不设置rememberMe，禁用该功能
            // token.setRememberMe(false);
            
            subject.login(token);
            return "登录成功";
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode6" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例5：移除危险依赖
        // 移除Commons Collections等包含Gadget链的依赖
        // 如果必须使用，升级到安全版本
        &lt;dependency&gt;
            &lt;groupId&gt;org.apache.commons&lt;/groupId&gt;
            &lt;artifactId&gt;commons-collections4&lt;/artifactId&gt;
            &lt;version&gt;4.4&lt;/version&gt;
&lt;/dependency&gt;</code></pre>
      </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { SafetyOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/home/shiro")
public class Shiro {
    @PostMapping("/login")
    public String login(String username, String password, String rememberMe) {
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        
        // 漏洞：如果rememberMe为true，Shiro会将用户信息序列化后加密存储在Cookie中
        if (rememberMe.equals("true")) {
            token.setRememberMe(true);
        }
        
        try {
            subject.login(token);
            return "登录成功";
        } catch (Exception e) {
            return "认证失败";
        }
    }
    
    @RequestMapping(value = "/getkey")
    public String getShiroKey() {
        // 漏洞：使用默认的AES密钥
        // Shiro默认使用硬编码的密钥kPH+bIxk5D2deZiIxcaaaA==
        byte[] key = new CookieRememberMeManager().getCipherKey();
        return "shiro key: \\n" + new String(Base64.getEncoder().encode(key));
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
// 使用默认密钥，攻击者可以使用已知密钥伪造Cookie
CookieRememberMeManager rememberMeManager = new CookieRememberMeManager();
// 默认密钥是硬编码的：kPH+bIxk5D2deZiIxcaaaA==`

const fixCode2 = `// 正确示例1：配置自定义密钥（推荐）
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import java.util.Base64;
import javax.crypto.KeyGenerator;

@Bean
public CookieRememberMeManager rememberMeManager() {
    CookieRememberMeManager manager = new CookieRememberMeManager();
    
    // 生成强随机密钥
    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    keyGenerator.init(128);
    byte[] key = keyGenerator.generateKey().getEncoded();
    
    // 设置自定义密钥
    manager.setCipherKey(key);
    
    return manager;
}`

const fixCode3 = `// 正确示例2：在shiro.ini中配置自定义密钥
[main]
# 生成随机密钥：使用KeyGenerator生成128位密钥，然后Base64编码
securityManager.rememberMeManager.cipherKey=YOUR_CUSTOM_BASE64_KEY_HERE

# 或者禁用rememberMe功能
# securityManager.rememberMeManager = null`

const fixCode4 = `// 正确示例3：在Spring Boot中配置
@Configuration
public class ShiroConfig {
    @Bean
    public CookieRememberMeManager rememberMeManager() {
        CookieRememberMeManager manager = new CookieRememberMeManager();
        
        // 从配置文件读取密钥（不要硬编码）
        String cipherKey = environment.getProperty("shiro.cipher.key");
        if (cipherKey == null || cipherKey.isEmpty()) {
            // 生成新密钥
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            byte[] key = keyGenerator.generateKey().getEncoded();
            cipherKey = Base64.getEncoder().encodeToString(key);
        }
        
        manager.setCipherKey(Base64.getDecoder().decode(cipherKey));
        return manager;
    }
}`

const fixCode5 = `// 正确示例4：禁用rememberMe功能
@PostMapping("/login")
public String login(String username, String password) {
    Subject subject = SecurityUtils.getSubject();
    UsernamePasswordToken token = new UsernamePasswordToken(username, password);
    // 不设置rememberMe，禁用该功能
    // token.setRememberMe(false);
    
    subject.login(token);
    return "登录成功";
}`

const fixCode6 = `// 正确示例5：移除危险依赖
// 移除Commons Collections等包含Gadget链的依赖
// 如果必须使用，升级到安全版本
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-collections4</artifactId>
    <version>4.4</version>
</dependency>`

const formState = reactive({
  payload: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/shiro', {
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
  formState.payload = ''
  result.value = ''
}
</script>
