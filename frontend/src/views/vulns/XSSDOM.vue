<template>
  <vuln-template
    title="DOM型 XSS 漏洞"
    subtitle="Cross-Site Scripting - DOM Type"
    level="高危"
    :icon="BugOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>DOM型XSS (DOM-based XSS)</strong> 是一种完全在客户端发生的XSS攻击类型，恶意脚本通过修改DOM环境来执行。
      </a-typography-paragraph>
      <a-typography-paragraph>
        DOM型XSS的特点：
      </a-typography-paragraph>
      <ul>
        <li>攻击完全在客户端浏览器中发生</li>
        <li>不经过服务器端处理</li>
        <li>通过JavaScript操作DOM实现攻击</li>
        <li>常见于URL参数、location.hash、document.write等场景</li>
        <li>难以被WAF等传统防护手段检测</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的DOM型XSS攻击载荷：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        #&lt;img src=x onerror=alert('DOM XSS')&gt;
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        #&lt;script&gt;alert(document.cookie)&lt;/script&gt;
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        #&lt;svg onload=alert('XSS')&gt;
      </a-typography-paragraph>
      <a-alert
        message="请在安全环境下进行测试，切勿用于非法用途！"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <!-- 模拟真实的用户个人资料页面 -->
      <div style="background: #fff; padding: 24px; border-radius: 8px; margin-bottom: 24px;">
        <h3 style="margin-bottom: 16px; color: #1890ff;">👤 个人资料设置</h3>
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="用户名"
            name="username"
            :rules="[{ required: true, message: '请输入用户名' }]"
          >
            <a-input
              v-model:value="formState.username"
              placeholder="请输入您的用户名"
              size="large"
            />
          </a-form-item>
          
          <a-form-item
            label="个人简介"
            name="bio"
          >
            <a-textarea
              v-model:value="formState.bio"
              :rows="3"
              placeholder="介绍一下自己..."
              size="large"
            />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><save-outlined /></template>
                保存设置
              </a-button>
              <a-button size="large" @click="handleReset">
                <template #icon><redo-outlined /></template>
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>
      
      <!-- 显示用户资料预览 -->
      <div v-if="userProfile" style="background: #f5f5f5; padding: 24px; border-radius: 8px; margin-bottom: 24px;">
        <h4 style="margin-bottom: 16px;">📋 资料预览</h4>
        <div style="background: #fff; padding: 20px; border-radius: 4px;">
          <div style="margin-bottom: 16px;">
            <strong style="color: #666;">用户名：</strong>
            <span ref="usernameDisplay" style="color: #1890ff; font-size: 16px;"></span>
          </div>
          <div>
            <strong style="color: #666;">个人简介：</strong>
            <div ref="bioDisplay" style="margin-top: 8px; color: #333; line-height: 1.6;"></div>
          </div>
        </div>
      </div>

      <!-- URL参数演示区域 -->
      <div style="background: #fff; padding: 24px; border-radius: 8px; margin-bottom: 24px;">
        <h4 style="margin-bottom: 16px; color: #1890ff;">🔗 URL参数演示</h4>
        <a-typography-paragraph>
          当前URL参数：<code>{{ currentHash }}</code>
        </a-typography-paragraph>
        <a-typography-paragraph style="color: #999; font-size: 12px; margin-top: 8px;">
          提示：尝试在URL后添加 #&lt;img src=x onerror=alert('XSS')&gt; 来测试DOM型XSS
        </a-typography-paragraph>
        <a-space style="margin: 8px 0 16px 0;">
          <a-button size="small" type="primary" @click="applySampleHash">
            一键填充示例 hash
          </a-button>
          <a-button size="small" @click="applyCookieLeak">
            一键注入 document.cookie
          </a-button>
        </a-space>
        <div ref="hashDisplay" style="margin-top: 16px; padding: 12px; background: #f9f9f9; border-radius: 4px; min-height: 40px;"></div>
      </div>
    </template>

    <template #input-hint>
      <a-alert message="测试建议" type="warning" show-icon style="margin-bottom: 12px;" />
      <p><strong>DOM型XSS测试方法：</strong></p>
      <ul style="padding-left: 20px; margin-bottom: 0;">
        <li>在URL后添加 <a-typography-text code copyable>#&lt;img src=x onerror=alert('XSS')&gt;</a-typography-text></li>
        <li>在用户名输入框输入 <a-typography-text code copyable>&lt;script&gt;alert('XSS')&lt;/script&gt;</a-typography-text></li>
        <li>在个人简介输入框输入 <a-typography-text code copyable>&lt;svg onload=alert(document.cookie)&gt;</a-typography-text></li>
      </ul>
    </template>

    <template #source-code>
      <a-typography-paragraph>
        <strong>前端源码（存在漏洞）：</strong>
      </a-typography-paragraph>
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="sourceCode" style="margin: 0;">
          <pre class="code-block"><code>// 漏洞代码示例1：直接使用innerHTML
const username = getUrlParameter('username');
document.getElementById('username').innerHTML = username; // 危险！

// 漏洞代码示例2：使用location.hash
const hash = location.hash.substring(1);
document.getElementById('content').innerHTML = hash; // 危险！

// 漏洞代码示例3：使用document.write
const userInput = getUrlParameter('input');
document.write('&lt;div&gt;' + userInput + '&lt;/div&gt;'); // 危险！

// 漏洞代码示例4：使用eval
const code = location.search.split('code=')[1];
eval(code); // 非常危险！</code></pre>
        </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：直接使用innerHTML、location.hash、document.write等危险函数将用户输入写入DOM，导致恶意脚本被执行"
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
        <li><strong>使用textContent：</strong>使用textContent而不是innerHTML，自动转义HTML</li>
        <li><strong>输入验证：</strong>对URL参数和用户输入进行严格验证和过滤</li>
        <li><strong>避免危险函数：</strong>避免使用innerHTML、document.write、eval等危险函数</li>
        <li><strong>使用安全的DOM操作：</strong>使用createElement、appendChild等安全的DOM API</li>
        <li><strong>内容安全策略(CSP)：</strong>设置CSP头限制脚本执行</li>
        <li><strong>使用DOMPurify：</strong>如果必须使用HTML，使用DOMPurify等库进行清理</li>
        <li><strong>URL参数验证：</strong>对location.hash、location.search等URL参数进行验证</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
const username = getUrlParameter('username');
document.getElementById('username').innerHTML = username; // 会执行脚本</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：使用textContent
const username = getUrlParameter('username');
document.getElementById('username').textContent = username; // 安全，自动转义</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：输入验证和过滤
function sanitizeInput(input) {
    // 移除HTML标签
    const div = document.createElement('div');
    div.textContent = input;
    return div.innerHTML;
}

const username = getUrlParameter('username');
const safeUsername = sanitizeInput(username);
document.getElementById('username').innerHTML = safeUsername;</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：使用DOMPurify清理HTML
import DOMPurify from 'dompurify';

const userInput = getUrlParameter('input');
const cleanHTML = DOMPurify.sanitize(userInput, {
    ALLOWED_TAGS: ['b', 'i', 'em', 'strong'],
    ALLOWED_ATTR: []
});
document.getElementById('content').innerHTML = cleanHTML;</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：安全的DOM操作
const username = getUrlParameter('username');
const element = document.getElementById('username');
element.textContent = username; // 使用textContent

// 或者使用createElement
const div = document.createElement('div');
div.textContent = username;
element.appendChild(div);</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode6" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例5：URL参数验证
function getSafeHash() {
    const hash = location.hash.substring(1);
    // 验证hash格式，只允许字母数字和部分符号
    if (!/^[a-zA-Z0-9\s\-_]+$/.test(hash)) {
        return '';
    }
    return hash;
}

const safeHash = getSafeHash();
document.getElementById('content').textContent = safeHash;</code></pre>
        </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref, onMounted, watch, nextTick, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import { BugOutlined, SaveOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'

const loading = ref(false)
const userProfile = ref(null)
const usernameDisplay = ref(null)
const bioDisplay = ref(null)
const hashDisplay = ref(null)
const currentHash = ref('')

const sourceCode = `// 漏洞代码示例1：直接使用innerHTML
const username = getUrlParameter('username');
document.getElementById('username').innerHTML = username; // 危险！

// 漏洞代码示例2：使用location.hash
const hash = location.hash.substring(1);
document.getElementById('content').innerHTML = hash; // 危险！

// 漏洞代码示例3：使用document.write
const userInput = getUrlParameter('input');
document.write('<div>' + userInput + '</div>'); // 危险！

// 漏洞代码示例4：使用eval
const code = location.search.split('code=')[1];
eval(code); // 非常危险！`

const fixCode1 = `// 错误示例(易受攻击)
const username = getUrlParameter('username');
document.getElementById('username').innerHTML = username; // 会执行脚本`

const fixCode2 = `// 正确示例1：使用textContent
const username = getUrlParameter('username');
document.getElementById('username').textContent = username; // 安全，自动转义`

const fixCode3 = `// 正确示例2：输入验证和过滤
function sanitizeInput(input) {
    // 移除HTML标签
    const div = document.createElement('div');
    div.textContent = input;
    return div.innerHTML;
}

const username = getUrlParameter('username');
const safeUsername = sanitizeInput(username);
document.getElementById('username').innerHTML = safeUsername;`

const fixCode4 = `// 正确示例3：使用DOMPurify清理HTML
import DOMPurify from 'dompurify';

const userInput = getUrlParameter('input');
const cleanHTML = DOMPurify.sanitize(userInput, {
    ALLOWED_TAGS: ['b', 'i', 'em', 'strong'],
    ALLOWED_ATTR: []
});
document.getElementById('content').innerHTML = cleanHTML;`

const fixCode5 = `// 正确示例4：安全的DOM操作
const username = getUrlParameter('username');
const element = document.getElementById('username');
element.textContent = username; // 使用textContent

// 或者使用createElement
const div = document.createElement('div');
div.textContent = username;
element.appendChild(div);`

const fixCode6 = `// 正确示例5：URL参数验证
function getSafeHash() {
    const hash = location.hash.substring(1);
    // 验证hash格式，只允许字母数字和部分符号
    if (!/^[a-zA-Z0-9\s\-_]+$/.test(hash)) {
        return '';
    }
    return hash;
}

const safeHash = getSafeHash();
document.getElementById('content').textContent = safeHash;`

const formState = reactive({
  username: '',
  bio: ''
})

const applySampleHash = () => {
  const payload = encodeURIComponent("<img src=x onerror=alert('DOM XSS')>")
  window.location.hash = payload
  handleHashChange()
}

const applyCookieLeak = () => {
  // 避免在SFC中出现字面量 script 结束标记，拆分字符串防止提前闭合
  const payload = encodeURIComponent("<scr" + "ipt>alert(document.cookie)</scr" + "ipt>")
  window.location.hash = payload
  handleHashChange()
}

// 处理URL hash变化（DOM型XSS的关键点）
const handleHashChange = () => {
  let hash = window.location.hash.substring(1)
  // 兼容通过查询参数传递payload的场景（例如 ?hash=payload）
  if (!hash) {
    const params = new URLSearchParams(window.location.search)
    hash = params.get('hash') || ''
  }
  currentHash.value = hash || '(无)'
  
  if (hash && hashDisplay.value) {
    // 漏洞：直接使用innerHTML，存在DOM型XSS风险
    hashDisplay.value.innerHTML = decodeURIComponent(hash)
    
    // 手动执行script标签（因为innerHTML不会自动执行script）
    nextTick(() => {
      const scripts = hashDisplay.value.querySelectorAll('script')
      scripts.forEach(script => {
        const newScript = document.createElement('script')
        Array.from(script.attributes).forEach(attr => {
          newScript.setAttribute(attr.name, attr.value)
        })
        newScript.textContent = script.textContent
        document.body.appendChild(newScript)
        script.remove()
      })
    })
  }
}

// 处理表单提交
const handleSubmit = async (values) => {
  loading.value = true
  try {
    // 模拟保存用户资料
    await new Promise(resolve => setTimeout(resolve, 500))
    
    userProfile.value = {
      username: formState.username,
      bio: formState.bio
    }
    
    // 漏洞：直接使用innerHTML，存在DOM型XSS风险
    nextTick(() => {
      if (usernameDisplay.value) {
        usernameDisplay.value.innerHTML = formState.username
      }
      if (bioDisplay.value) {
        bioDisplay.value.innerHTML = formState.bio
      }
      
      // 手动执行script标签
      nextTick(() => {
        const allElements = [usernameDisplay.value, bioDisplay.value]
        allElements.forEach(element => {
          if (element) {
            const scripts = element.querySelectorAll('script')
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
    })
    
    message.success('设置已保存')
  } catch (error) {
    message.error('保存失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formState.username = ''
  formState.bio = ''
  userProfile.value = null
  currentHash.value = ''
  if (hashDisplay.value) {
    hashDisplay.value.innerHTML = ''
  }
}

// 监听hash变化
onMounted(() => {
  handleHashChange()
  window.addEventListener('hashchange', handleHashChange)
})

// 组件卸载时移除监听器
onUnmounted(() => {
  window.removeEventListener('hashchange', handleHashChange)
})
</script>

<style scoped>
.code-block {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
}
</style>

