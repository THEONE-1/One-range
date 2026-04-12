<template>
  <vuln-template
    title="反射型 XSS 漏洞"
    subtitle="反射型脚本注入"
    level="高危"
    :icon="BugOutlined"
    moduleKey="xss_reflect"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>跨站脚本攻击</strong> 是一种代码注入攻击，攻击者通过在目标网站注入恶意脚本，使其在用户浏览器中执行。
      </a-typography-paragraph>
      <a-typography-paragraph>
        反射型XSS的特点：
      </a-typography-paragraph>
      <ul>
        <li>恶意脚本通过URL参数传递</li>
        <li>服务端直接将用户输入反射到页面</li>
        <li>通常需要诱使用户点击恶意链接</li>
        <li>不会存储在服务器数据库中</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的XSS攻击载荷：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        &lt;script&gt;alert('XSS')&lt;/script&gt;
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        &lt;img src=x onerror=alert(1)&gt;
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        &lt;svg onload=alert(document.cookie)&gt;
      </a-typography-paragraph>
      <a-alert
        message="请在安全环境下进行测试，切勿用于非法用途！"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <!-- 模拟真实搜索页面 -->
      <div class="lab-surface">
        <h3 class="lab-surface-title lab-surface-title--accent">网站搜索</h3>
        <a-form :model="formState" @finish="handleSubmit" layout="inline" class="lab-inline-form">
          <a-form-item
            name="content"
          >
            <a-input
              v-model:value="formState.content"
              placeholder="请输入搜索关键词..."
              size="large"
            />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" html-type="submit" size="large" :loading="loading">
              <template #icon><search-outlined /></template>
              搜索
            </a-button>
          </a-form-item>
        </a-form>
      </div>
      
      <!-- 显示搜索结果 -->
      <div v-if="result" class="lab-surface lab-surface-muted">
        <h4 class="lab-surface-title">搜索结果</h4>
        <p class="lab-label">您搜索的关键词是</p>
        <div class="lab-result-box lab-result-box--accent" v-html="result" ref="xssResult"></div>
        <p class="lab-caption">共找到 0 条相关结果</p>
      </div>
    </template>

    <template #input-hint>
      <a-alert message="测试建议" type="warning" show-icon style="margin-bottom: 12px;" />
      <p><strong>建议测试payload：</strong></p>
      <ul style="padding-left: 20px; margin-bottom: 0;">
        <li><a-typography-text code copyable>&lt;script&gt;alert('XSS')&lt;/script&gt;</a-typography-text> - 基础弹窗测试</li>
        <li><a-typography-text code copyable>&lt;img src=x onerror=alert(1)&gt;</a-typography-text> - 图片标签注入</li>
        <li><a-typography-text code copyable>&lt;svg onload=alert(document.cookie)&gt;</a-typography-text> - SVG标签注入</li>
        <li><a-typography-text code copyable>&lt;iframe src=javascript:alert(1)&gt;</a-typography-text> - iframe注入</li>
      </ul>
    </template>


    <template #source-code>
      <a-typography-paragraph>
        <strong>后端源码（存在漏洞）：</strong>
      </a-typography-paragraph>
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="sourceCode" style="margin: 0;">
          <pre class="code-block"><code>@RestController
@RequestMapping("/home/xss")
public class XSSVul {
    @RequestMapping("/reflect")
    public String xssReflect(String content) {
        // 漏洞：直接返回用户输入，没有进行HTML编码
        // 前端使用v-html渲染，导致脚本被执行
        return content != null ? content : "";
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：后端直接返回用户输入，前端使用v-html渲染，导致恶意脚本被执行"
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
        <li><strong>输出编码：</strong>对所有用户输入进行HTML实体编码，使用ESAPI、OWASP Java Encoder等库</li>
        <li><strong>内容安全策略(CSP)：</strong>使用CSP头限制脚本执行</li>
        <li><strong>HttpOnly Cookie：</strong>设置Cookie的HttpOnly属性防止JS读取</li>
        <li><strong>使用安全函数：</strong>避免使用innerHTML、v-html、document.write等危险函数</li>
        <li><strong>输入验证：</strong>对用户输入进行白名单验证，只允许安全字符</li>
        <li><strong>使用框架：</strong>使用安全的模板引擎（如Thymeleaf），自动转义</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        @RequestMapping("/reflect")
        public String xssReflect(String content) {
            return content; // 直接返回，没有编码
}</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 前端：&lt;div v-html="result"&gt;&lt;/div&gt; // 会执行脚本</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：后端进行HTML编码
        import org.owasp.encoder.Encode;
        
        @RequestMapping("/reflect")
        public String xssReflect(String content) {
            // 对用户输入进行HTML编码
            return content != null ? Encode.forHtml(content) : "";
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：使用ESAPI编码
        import org.owasp.esapi.ESAPI;
        
        @RequestMapping("/reflect")
        public String xssReflect(String content) {
            if (content == null) return "";
            // HTML编码，将&lt;script&gt;转换为&amp;lt;script&amp;gt;
            return ESAPI.encoder().encodeForHTML(content);
}</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：前端使用插值而不是v-html
// Vue模板
&lt;div&gt;{{ result }}&lt;/div&gt;
// Vue会自动转义HTML</code></pre>
      </a-typography-paragraph>
      </div>
      
      <a-typography-paragraph code copyable>
        // 或者使用textContent
        element.textContent = userInput; // 安全，不会执行脚本
      </a-typography-paragraph>
      
      <a-typography-paragraph code copyable>
        // 正确示例4：设置CSP头
        @RequestMapping("/reflect")
        public ResponseEntity&lt;String&gt; xssReflect(String content) {
            HttpHeaders headers = new HttpHeaders();
            // 设置CSP，禁止内联脚本
            headers.add("Content-Security-Policy", 
                "default-src 'self'; script-src 'self'; object-src 'none'");
            return new ResponseEntity&lt;&gt;(content, headers, HttpStatus.OK);
        }
      </a-typography-paragraph>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref, nextTick, watch } from 'vue'
import { message } from 'ant-design-vue'
import { BugOutlined, PlayCircleOutlined, RedoOutlined, SearchOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')
const xssResult = ref(null)

const sourceCode = `@RestController
@RequestMapping("/home/xss")
public class XSSVul {
    @RequestMapping("/reflect")
    public String xssReflect(String content) {
        // 漏洞：直接返回用户输入，没有进行HTML编码
        // 前端使用v-html渲染，导致脚本被执行
        return content != null ? content : "";
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
@RequestMapping("/reflect")
public String xssReflect(String content) {
    return content; // 直接返回，没有编码
}`

const fixCode2 = `// 前端：<div v-html="result"></div> // 会执行脚本`

const fixCode3 = `// 正确示例1：后端进行HTML编码
import org.owasp.encoder.Encode;

@RequestMapping("/reflect")
public String xssReflect(String content) {
    // 对用户输入进行HTML编码
    return content != null ? Encode.forHtml(content) : "";
}`

const fixCode4 = `// 正确示例2：使用ESAPI编码
import org.owasp.esapi.ESAPI;

@RequestMapping("/reflect")
public String xssReflect(String content) {
    if (content == null) return "";
    // HTML编码，将<script>转换为&amp;lt;script&amp;gt;
    return ESAPI.encoder().encodeForHTML(content);
}`

const fixCode5 = `// 正确示例3：前端使用插值而不是v-html
// Vue模板
<div>{{ result }}</div>
// Vue会自动转义HTML`

const formState = reactive({
  content: ''
})

// 监听result变化，确保脚本能执行
watch(result, () => {
  nextTick(() => {
    if (xssResult.value && result.value) {
      // 使用innerHTML直接设置，然后手动执行script标签
      xssResult.value.innerHTML = result.value
      const scripts = xssResult.value.querySelectorAll('script')
      scripts.forEach(script => {
        const newScript = document.createElement('script')
        Array.from(script.attributes).forEach(attr => {
          newScript.setAttribute(attr.name, attr.value)
        })
        newScript.textContent = script.textContent
        document.body.appendChild(newScript)
        script.remove()
      })
    }
  })
})

const handleSubmit = async (values) => {
  loading.value = true
  try {
    const response = await axios.get('/home/xss/reflect', {
      params: { content: formState.content }
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
  formState.content = ''
  result.value = ''
}
</script>

