<template>
  <vuln-template
    title="存储型 XSS 漏洞"
    subtitle="Cross-Site Scripting - Stored Type"
    level="高危"
    :icon="BugOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>存储型XSS (Stored XSS)</strong> 是一种将恶意脚本永久存储在目标服务器数据库中的XSS攻击类型。
      </a-typography-paragraph>
      <a-typography-paragraph>
        存储型XSS的特点：
      </a-typography-paragraph>
      <ul>
        <li>恶意脚本存储在服务器数据库中</li>
        <li>每次用户访问包含该数据的页面时都会执行</li>
        <li>影响范围更广，不需要诱使用户点击特定链接</li>
        <li>常见于留言板、评论系统、用户资料等场景</li>
        <li>危害性通常比反射型XSS更大</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的存储型XSS攻击载荷：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        &lt;script&gt;alert('XSS')&lt;/script&gt;
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        &lt;img src=x onerror=alert(document.cookie)&gt;
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        &lt;svg onload=alert('Stored XSS')&gt;
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        &lt;iframe src=javascript:alert(1)&gt;
      </a-typography-paragraph>
      <a-alert
        message="请在安全环境下进行测试，切勿用于非法用途！"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <!-- 模拟真实留言板 -->
      <div style="background: #fff; padding: 24px; border-radius: 8px; margin-bottom: 24px;">
        <h3 style="margin-bottom: 16px; color: #1890ff;">💬 留言板</h3>
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            name="content"
            :rules="[{ required: true, message: '请输入留言内容' }]"
          >
            <a-textarea
              v-model:value="formState.content"
              :rows="4"
              placeholder="请输入您的留言..."
              size="large"
            />
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><message-outlined /></template>
                发表留言
              </a-button>
              <a-button size="large" @click="handleReset">
                <template #icon><redo-outlined /></template>
                清空输入
              </a-button>
              <a-button danger size="large" @click="handleClear">
                <template #icon><delete-outlined /></template>
                清空所有留言
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>
      
      <!-- 显示留言列表 -->
      <div v-if="result && result !== '暂无留言内容'" style="background: #f5f5f5; padding: 24px; border-radius: 8px;">
        <h4 style="margin-bottom: 16px;">📋 留言列表</h4>
        <div v-html="result" ref="xssResult"></div>
      </div>
      <div v-else-if="result === '暂无留言内容'" style="background: #f5f5f5; padding: 24px; border-radius: 8px; text-align: center; color: #999;">
        暂无留言，快来发表第一条留言吧！
      </div>
    </template>

    <template #input-hint>
      <a-alert message="测试建议" type="warning" show-icon style="margin-bottom: 12px;" />
      <p><strong>建议测试payload：</strong></p>
      <ul style="padding-left: 20px; margin-bottom: 0;">
        <li><a-typography-text code copyable>&lt;script&gt;alert('XSS')&lt;/script&gt;</a-typography-text> - 基础弹窗测试</li>
        <li><a-typography-text code copyable>&lt;img src=x onerror=alert(document.cookie)&gt;</a-typography-text> - 图片标签注入</li>
        <li><a-typography-text code copyable>&lt;svg onload=alert('Stored XSS')&gt;</a-typography-text> - SVG标签注入</li>
        <li><a-typography-text code copyable>&lt;iframe src=javascript:alert(1)&gt;</a-typography-text> - iframe注入</li>
      </ul>
    </template>


    <template #source-code>
      <a-typography-paragraph>
        <strong>后端源码（存在漏洞）：</strong>
      </a-typography-paragraph>
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="codeContent" style="margin: 0;">
          <pre class="code-block"><code>@RestController
@RequestMapping("/home/xss")
public class XSSVul {
    @RequestMapping("/store")
    public String xssInsert(String content, String clear) {
        try {
            if (clear != null && clear.equals("true")) {
                xssService.clear();
                return "清除成功";
            }
            // 漏洞：直接将用户输入存储到数据库，没有进行HTML编码
            if (content != null && !content.trim().equals("")) {
                xssService.setContent(new Xss(content));
            }
            
            // 漏洞：从数据库读取后直接返回，没有进行HTML编码
            List&lt;String&gt; list = xssService.getContent();
            StringBuilder result = new StringBuilder();
            result.append("&lt;div style='line-height: 2;'&gt;");
            for (int i = 0; i &lt; list.size(); i++) {
                result.append("&lt;div&gt;留言").append(i + 1).append(": ");
                result.append(list.get(i)); // 直接输出，不进行HTML转义
                result.append("&lt;/div&gt;");
            }
            result.append("&lt;/div&gt;");
            return result.toString();
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：1. 存储时没有对用户输入进行HTML编码；2. 读取时直接返回HTML，前端使用v-html渲染，导致恶意脚本被执行并持久化存储"
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
        <li><strong>输出编码：</strong>对所有从数据库读取的数据进行HTML实体编码</li>
        <li><strong>输入过滤：</strong>在存储前对用户输入进行过滤和验证</li>
        <li><strong>内容安全策略(CSP)：</strong>使用CSP头限制脚本执行</li>
        <li><strong>HttpOnly Cookie：</strong>设置Cookie的HttpOnly属性防止JS读取</li>
        <li><strong>使用安全函数：</strong>避免使用innerHTML、v-html、document.write等危险函数</li>
        <li><strong>白名单验证：</strong>对用户输入进行白名单验证，只允许安全的HTML标签</li>
        <li><strong>富文本编辑器：</strong>如果必须允许HTML，使用安全的富文本编辑器（如CKEditor）</li>
      </ul>
      
      <a-typography-paragraph code copyable style="margin-top: 16px;">
        // 错误示例(易受攻击)
        @RequestMapping("/store")
        public String xssInsert(String content) {
            // 直接存储，没有编码
            xssService.setContent(new Xss(content));
            
            // 直接返回，没有编码
            List&lt;String&gt; list = xssService.getContent();
            return list.get(0); // 前端使用v-html渲染，会执行脚本
        }
      </a-typography-paragraph>
      
      <a-typography-paragraph code copyable>
        // 正确示例1：后端进行HTML编码
        import org.owasp.encoder.Encode;
        
        @RequestMapping("/store")
        public String xssInsert(String content) {
            if (content == null || content.trim().isEmpty()) {
                return "内容不能为空";
            }
            
            // 存储时编码（可选，但建议在输出时编码）
            // String encodedContent = Encode.forHtml(content);
            // xssService.setContent(new Xss(encodedContent));
            
            // 读取时编码（必须）
            List&lt;String&gt; list = xssService.getContent();
            StringBuilder result = new StringBuilder();
            for (int i = 0; i &lt; list.size(); i++) {
                // 对每个消息进行HTML编码
                String safeContent = Encode.forHtml(list.get(i));
                result.append("留言").append(i + 1).append(": ").append(safeContent).append("\n");
            }
            return result.toString();
        }
      </a-typography-paragraph>
      
      <a-typography-paragraph code copyable>
        // 正确示例2：使用ESAPI编码
        import org.owasp.esapi.ESAPI;
        
        @RequestMapping("/store")
        public String xssInsert(String content) {
            // 输入验证
            if (content == null || content.length() > 1000) {
                throw new IllegalArgumentException("内容长度超出限制");
            }
            
            // 存储原始内容（数据库存储原始数据）
            xssService.setContent(new Xss(content));
            
            // 读取时编码
            List&lt;String&gt; list = xssService.getContent();
            StringBuilder result = new StringBuilder();
            for (String msg : list) {
                // HTML编码，将&lt;script&gt;转换为&amp;lt;script&amp;gt;
                String safeMsg = ESAPI.encoder().encodeForHTML(msg);
                result.append(safeMsg).append("\n");
            }
            return result.toString();
        }
      </a-typography-paragraph>
      
      <a-typography-paragraph code copyable>
        // 正确示例3：前端使用插值而不是v-html
        // Vue模板
      </a-typography-paragraph>
      
      <a-typography-paragraph code copyable>
        &lt;div&gt;{{ result }}&lt;/div&gt;
      </a-typography-paragraph>
      
      <a-typography-paragraph code copyable>
        // Vue会自动转义HTML，安全
      </a-typography-paragraph>
      
      <a-typography-paragraph code copyable>
        // 或者使用textContent
        element.textContent = userInput; // 安全，不会执行脚本
      </a-typography-paragraph>
      
      <a-typography-paragraph code copyable>
        // 正确示例4：如果必须允许HTML，使用DOMPurify清理
        // 前端使用DOMPurify库
        import DOMPurify from 'dompurify';
        
        // 清理HTML，只保留安全的标签
        const cleanHTML = DOMPurify.sanitize(userInput, {
            ALLOWED_TAGS: ['b', 'i', 'em', 'strong', 'a', 'p'],
            ALLOWED_ATTR: ['href']
        });
        element.innerHTML = cleanHTML;
      </a-typography-paragraph>
      
      <a-typography-paragraph code copyable>
        // 正确示例5：设置CSP头
        @RequestMapping("/store")
        public ResponseEntity&lt;String&gt; xssInsert(String content) {
            HttpHeaders headers = new HttpHeaders();
            // 设置CSP，禁止内联脚本和eval
            headers.add("Content-Security-Policy", 
                "default-src 'self'; script-src 'self'; object-src 'none'; " +
                "base-uri 'self'; form-action 'self'");
            return new ResponseEntity&lt;&gt;(result, headers, HttpStatus.OK);
        }
      </a-typography-paragraph>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref, onMounted, nextTick, watch } from 'vue'
import { message } from 'ant-design-vue'
import { BugOutlined, PlayCircleOutlined, RedoOutlined, MessageOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const codeContent = `@RestController
@RequestMapping("/home/xss")
public class XSSVul {
    @RequestMapping("/store")
    public String xssInsert(String content, String clear) {
        try {
            if (clear != null && clear.equals("true")) {
                xssService.clear();
                return "清除成功";
            }
            // 漏洞：直接将用户输入存储到数据库，没有进行HTML编码
            if (content != null && !content.trim().equals("")) {
                xssService.setContent(new Xss(content));
            }
            
            // 漏洞：从数据库读取后直接返回，没有进行HTML编码
            List<String> list = xssService.getContent();
            StringBuilder result = new StringBuilder();
            result.append("<div style='line-height: 2;'>");
            for (int i = 0; i < list.size(); i++) {
                result.append("<div>留言").append(i + 1).append(": ");
                result.append(list.get(i)); // 直接输出，不进行HTML转义
                result.append("</div>");
            }
            result.append("</div>");
            return result.toString();
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}`
const xssResult = ref(null)

const formState = reactive({
  content: '',
  clear: false
})

// 加载存储的XSS内容
const loadStoredContent = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/xss/store', {
      params: {}
    })
    if (response.data && response.data !== '暂无留言内容') {
      result.value = response.data
    }
  } catch (error) {
    // 忽略错误，可能是第一次加载
  } finally {
    loading.value = false
  }
}

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

// 页面加载时自动获取存储的内容
onMounted(() => {
  loadStoredContent()
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const params = {
      content: formState.content
    }
    if (formState.clear) {
      params.clear = 'true'
    }
    const response = await axios.get('/home/xss/store', {
      params: params
    })
    result.value = response.data
    message.success('测试完成')
    formState.content = '' // 提交后清空输入框
  } catch (error) {
    message.error('测试失败：' + (error.response?.data || error.message))
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formState.content = ''
  formState.clear = false
  // 不清空result，保留已存储的内容显示
}

const handleClear = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/xss/store', {
      params: { clear: 'true' }
    })
    result.value = ''
    message.success('所有留言已清空')
  } catch (error) {
    message.error('清空失败：' + (error.response?.data || error.message))
  } finally {
    loading.value = false
  }
}
</script>
