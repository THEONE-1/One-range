<template>
  <vuln-template
    title="XML导入服务 - SAXReader"
    subtitle="XML External Entity"
    level="高危"
    :icon="FileTextOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        场景：电子发票/配置文件上传接口，后端用 SAXReader 解析并展示摘要。
      </a-typography-paragraph>
      <a-typography-paragraph>
        风险：允许 DOCTYPE 时可解析外部实体，可能读取文件或做 SSRF。
      </a-typography-paragraph>
    </template>

    <template #payload>
      <a-typography-paragraph>
        示例“发票”XML（可替换路径测试文件读取）：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        &lt;?xml version="1.0"?&gt;
        &lt;!DOCTYPE invoice [ &lt;!ENTITY xxe SYSTEM "file:///etc/passwd"&gt; ]&gt;
        &lt;invoice&gt;&amp;xxe;&lt;/invoice&gt;
      </a-typography-paragraph>
      <a-alert
        message="真实系统应禁用DTD/外部实体或使用安全解析配置"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <div class="xml-process-container">
        <a-card title="发票/配置导入" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            粘贴上传的 XML 单据/配置，系统直接解析并展示内容。
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="XML数据"
            name="content"
            :rules="[{ required: true, message: '请输入XML数据' }]"
          >
            <a-textarea
              v-model:value="formState.content" :rows="6"
              size="large"
              placeholder="示例：包含发票/配置字段的XML，可尝试带DOCTYPE"
            />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><file-text-outlined /></template>
                解析XML
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
        v-if="result && result.includes('解析结果')"
        message="如果返回了文件内容（如/etc/passwd），说明存在XXE漏洞，SAXReader加载了外部实体。"
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
@RequestMapping("/home/xxe")
public class XXE {
    @PostMapping(value = "/saxreader")
    public String SAXReader(@RequestBody String content, String entity) {
        try {
            // 漏洞：安全检测是可选的，默认不启用
            if (entity != null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }
            
            // 漏洞：创建SAXReader时没有禁用外部实体
            SAXReader sax = new SAXReader();
            // 修复代码被注释掉了：
            // sax.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            
            // 解析XML，如果XML包含外部实体引用，会加载外部资源
            org.dom4j.Document document = sax.read(new InputSource(new StringReader(content)));
            String text = document.getRootElement().getText();
            return "解析结果: " + text;
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：SAXReader（dom4j库）默认允许加载外部实体，攻击者可以通过DOCTYPE声明和ENTITY引用加载本地文件或远程资源。"
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
        <li><strong>禁用外部实体：</strong>设置SAXReader的setFeature禁用外部实体</li>
        <li><strong>使用安全配置：</strong>配置SAXReader的安全特性</li>
        <li><strong>输入验证：</strong>在解析前检查XML是否包含DOCTYPE声明</li>
        <li><strong>升级dom4j版本：</strong>使用较新版本的dom4j，默认禁用外部实体</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例：禁用外部实体
        @PostMapping(value = "/saxreader")
        public String SAXReader(@RequestBody String content) {
            try {
                SAXReader sax = new SAXReader();
                
                // 禁用DOCTYPE声明
                sax.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                
                // 禁用外部实体
                sax.setFeature("http://xml.org/sax/features/external-general-entities", false);
                sax.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                
                org.dom4j.Document document = sax.read(new InputSource(new StringReader(content)));
                String text = document.getRootElement().getText();
                return "解析结果: " + text;
            } catch (Exception e) {
                return "错误: " + e.getMessage();
            }
}</code></pre>
      </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { FileTextOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/home/xxe")
public class XXE {
    @PostMapping(value = "/saxreader")
    public String SAXReader(@RequestBody String content, String entity) {
        try {
            // 漏洞：安全检测是可选的，默认不启用
            if (entity != null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }
            
            // 漏洞：创建SAXReader时没有禁用外部实体
            SAXReader sax = new SAXReader();
            // 修复代码被注释掉了：
            // sax.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            
            // 解析XML，如果XML包含外部实体引用，会加载外部资源
            org.dom4j.Document document = sax.read(new InputSource(new StringReader(content)));
            String text = document.getRootElement().getText();
            return "解析结果: " + text;
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}`

const fixCode1 = `// 正确示例：禁用外部实体
@PostMapping(value = "/saxreader")
public String SAXReader(@RequestBody String content) {
    try {
        SAXReader sax = new SAXReader();
        
        // 禁用DOCTYPE声明
        sax.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        
        // 禁用外部实体
        sax.setFeature("http://xml.org/sax/features/external-general-entities", false);
        sax.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        
        org.dom4j.Document document = sax.read(new InputSource(new StringReader(content)));
        String text = document.getRootElement().getText();
        return "解析结果: " + text;
    } catch (Exception e) {
        return "错误: " + e.getMessage();
    }
}`

const formState = reactive({
  content: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.post('/home/xxe/saxreader', formState.content, {
      headers: {
        'Content-Type': 'application/xml'
      }
    })
    result.value = response.data
    message.success('测试完成')
  } catch (error) {
    const errorMsg = error.response?.data || error.message
    result.value = typeof errorMsg === 'string' ? errorMsg : JSON.stringify(errorMsg)
    message.error('测试失败：' + result.value)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formState.content = ''
  result.value = ''
}
</script>
