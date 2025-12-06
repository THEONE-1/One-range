<template>
  <vuln-template
    title="XML导入服务 - DocumentBuilder"
    subtitle="XML External Entity"
    level="高危"
    :icon="FileTextOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        场景：电子发票/配置文件上传接口，后端用 DocumentBuilder 解析并展示摘要。
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
        message="如果返回了文件内容，说明存在XXE漏洞，DocumentBuilder加载了外部实体。"
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
    @PostMapping(value = "/documentbuilder")
    public String DocumentBuilder(@RequestBody String content, String entity) {
        try {
            // 漏洞：安全检测是可选的
            if (entity != null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }
            
            // 漏洞：创建DocumentBuilderFactory时没有禁用外部实体
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 修复代码被注释掉了：
            // factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(content)));
            
            String textContent = document.getDocumentElement().getTextContent();
            return "解析结果: " + textContent;
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：DocumentBuilderFactory默认允许加载外部实体，攻击者可以通过DOCTYPE声明加载本地文件或远程资源。"
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
        <li><strong>禁用DOCTYPE：</strong>设置disallow-doctype-decl特性为true</li>
        <li><strong>禁用外部实体：</strong>设置external-general-entities和external-parameter-entities为false</li>
        <li><strong>禁用外部DTD：</strong>设置load-external-dtd为false</li>
        <li><strong>输入验证：</strong>检查XML是否包含DOCTYPE声明</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例：禁用外部实体
        @PostMapping(value = "/documentbuilder")
        public String DocumentBuilder(@RequestBody String content) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                
                // 禁用DOCTYPE声明
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                
                // 禁用外部通用实体
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                
                // 禁用外部参数实体
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                
                // 禁用外部DTD
                factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                
                // 设置为不可扩展
                factory.setExpandEntityReferences(false);
                
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(new InputSource(new StringReader(content)));
                
                String textContent = document.getDocumentElement().getTextContent();
                return "解析结果: " + textContent;
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
    @PostMapping(value = "/documentbuilder")
    public String DocumentBuilder(@RequestBody String content, String entity) {
        try {
            // 漏洞：安全检测是可选的
            if (entity != null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }
            
            // 漏洞：创建DocumentBuilderFactory时没有禁用外部实体
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 修复代码被注释掉了：
            // factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(content)));
            
            String textContent = document.getDocumentElement().getTextContent();
            return "解析结果: " + textContent;
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}`

const fixCode1 = `// 正确示例：禁用外部实体
@PostMapping(value = "/documentbuilder")
public String DocumentBuilder(@RequestBody String content) {
    try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        
        // 禁用DOCTYPE声明
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        
        // 禁用外部通用实体
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        
        // 禁用外部参数实体
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        
        // 禁用外部DTD
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        
        // 设置为不可扩展
        factory.setExpandEntityReferences(false);
        
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(content)));
        
        String textContent = document.getDocumentElement().getTextContent();
        return "解析结果: " + textContent;
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
    const response = await axios.post('/home/xxe/documentbuilder', formState.content, {
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
