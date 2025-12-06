<template>
  <vuln-template
    title="XML导入服务 - XMLReader"
    subtitle="XML External Entity"
    level="高危"
    :icon="FileTextOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        这是一个“电子发票 / 配置文件”XML导入接口，后端使用 XMLReader 解析并展示摘要。
      </a-typography-paragraph>
      <a-typography-paragraph>
        场景特点：
      </a-typography-paragraph>
      <ul>
        <li>用户上传 XML 单据/配置，直接解析</li>
        <li>允许携带 DOCTYPE，可能解析外部实体</li>
        <li>一旦可控，能读取文件、SSRF 或探测内网</li>
      </ul>
      <a-typography-paragraph>
        XMLReader 是常见的 XML 解析器，本示例保持弱配置以复现风险。
      </a-typography-paragraph>
    </template>

    <template #payload>
      <a-typography-paragraph>
        示例“发票”XML（可替换路径触发文件读取）：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        &lt;?xml version="1.0"?&gt;
        &lt;!DOCTYPE invoice [ &lt;!ENTITY xxe SYSTEM "file:///etc/passwd"&gt; ]&gt;
        &lt;invoice&gt;&amp;xxe;&lt;/invoice&gt;
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">模拟导入时读取本地敏感文件</span>
      </a-space>

      <a-alert
        message="真实场景应禁用DTD/外部实体或使用安全解析配置"
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
        message="如果返回了文件内容（如/etc/passwd），说明存在XXE漏洞，XML解析器加载了外部实体。"
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
    @PostMapping(value = "/xmlreader")
    public String XMLReader(@RequestBody String content, String entity) {
        try {
            // 漏洞：安全检测是可选的，默认不启用
            if (entity != null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }
            
            // 漏洞：创建XMLReader时没有禁用外部实体
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            // 修复代码被注释掉了：
            // xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            
            // 解析XML，如果XML包含外部实体引用，会加载外部资源
            xmlReader.setContentHandler(handler);
            xmlReader.parse(new InputSource(new StringReader(content)));
            
            return "解析结果: " + result.toString();
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：XMLReader默认允许加载外部实体，攻击者可以通过DOCTYPE声明和ENTITY引用加载本地文件（file://）或远程资源（http://），导致文件读取或SSRF攻击。"
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
        <li><strong>禁用DOCTYPE：</strong>设置disallow-doctype-decl特性为true，禁止DOCTYPE声明</li>
        <li><strong>禁用外部实体：</strong>设置external-general-entities和external-parameter-entities为false</li>
        <li><strong>禁用外部DTD：</strong>设置load-external-dtd为false</li>
        <li><strong>使用安全解析器：</strong>使用配置了安全特性的XML解析器</li>
        <li><strong>输入验证：</strong>在解析前检查XML是否包含DOCTYPE声明</li>
        <li><strong>白名单验证：</strong>只允许预定义的XML结构</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.parse(new InputSource(new StringReader(content)));
// 攻击者可以输入包含外部实体的XML</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：禁用DOCTYPE和外部实体
        @PostMapping(value = "/xmlreader")
        public String XMLReader(@RequestBody String content) {
            try {
                XMLReader xmlReader = XMLReaderFactory.createXMLReader();
                
                // 禁用DOCTYPE声明
                xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                
                // 禁用外部通用实体
                xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
                
                // 禁用外部参数实体
                xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                
                // 禁用外部DTD
                xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                
                ContentHandler handler = new ContentHandler() { /* ... */ };
                xmlReader.setContentHandler(handler);
                xmlReader.parse(new InputSource(new StringReader(content)));
                
                return "解析结果: " + result.toString();
            } catch (Exception e) {
                return "错误: " + e.getMessage();
            }
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：输入验证（检查DOCTYPE）
        @PostMapping(value = "/xmlreader")
        public String XMLReader(@RequestBody String content) {
            // 检查是否包含DOCTYPE声明
    if (content.contains("&lt;!DOCTYPE") || content.contains("&lt;!ENTITY")) {
                throw new SecurityException("不允许DOCTYPE或ENTITY声明");
            }
            
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            // 设置安全特性...
            xmlReader.parse(new InputSource(new StringReader(content)));
            return "解析结果";
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：使用XMLFilter限制解析
        import org.xml.sax.XMLFilter;
        import org.xml.sax.helpers.XMLFilterImpl;
        
        XMLReader parent = XMLReaderFactory.createXMLReader();
        XMLFilter filter = new XMLFilterImpl(parent) {
            @Override
            public void startDTD(String name, String publicId, String systemId) {
                throw new SAXException("DOCTYPE声明被禁止");
            }
        };
        filter.setContentHandler(handler);
filter.parse(new InputSource(new StringReader(content)));</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：使用安全的XML解析库
        // 使用OWASP Java Encoder或Apache Commons Configuration等
// 这些库默认禁用外部实体</code></pre>
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
    @PostMapping(value = "/xmlreader")
    public String XMLReader(@RequestBody String content, String entity) {
        try {
            // 漏洞：安全检测是可选的，默认不启用
            if (entity != null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }
            
            // 漏洞：创建XMLReader时没有禁用外部实体
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            // 修复代码被注释掉了：
            // xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            
            // 解析XML，如果XML包含外部实体引用，会加载外部资源
            xmlReader.setContentHandler(handler);
            xmlReader.parse(new InputSource(new StringReader(content)));
            
            return "解析结果: " + result.toString();
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
XMLReader xmlReader = XMLReaderFactory.createXMLReader();
xmlReader.parse(new InputSource(new StringReader(content)));
// 攻击者可以输入包含外部实体的XML`

const fixCode2 = `// 正确示例1：禁用DOCTYPE和外部实体
@PostMapping(value = "/xmlreader")
public String XMLReader(@RequestBody String content) {
    try {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        
        // 禁用DOCTYPE声明
        xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        
        // 禁用外部通用实体
        xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        
        // 禁用外部参数实体
        xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        
        // 禁用外部DTD
        xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        
        ContentHandler handler = new ContentHandler() { /* ... */ };
        xmlReader.setContentHandler(handler);
        xmlReader.parse(new InputSource(new StringReader(content)));
        
        return "解析结果: " + result.toString();
    } catch (Exception e) {
        return "错误: " + e.getMessage();
    }
}`

const fixCode3 = `// 正确示例2：输入验证（检查DOCTYPE）
@PostMapping(value = "/xmlreader")
public String XMLReader(@RequestBody String content) {
    // 检查是否包含DOCTYPE声明
    if (content.contains("<!DOCTYPE") || content.contains("<!ENTITY")) {
        throw new SecurityException("不允许DOCTYPE或ENTITY声明");
    }
    
    XMLReader xmlReader = XMLReaderFactory.createXMLReader();
    // 设置安全特性...
    xmlReader.parse(new InputSource(new StringReader(content)));
    return "解析结果";
}`

const fixCode4 = `// 正确示例3：使用XMLFilter限制解析
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLFilterImpl;

XMLReader parent = XMLReaderFactory.createXMLReader();
XMLFilter filter = new XMLFilterImpl(parent) {
    @Override
    public void startDTD(String name, String publicId, String systemId) {
        throw new SAXException("DOCTYPE声明被禁止");
    }
};
filter.setContentHandler(handler);
filter.parse(new InputSource(new StringReader(content)));`

const fixCode5 = `// 正确示例4：使用安全的XML解析库
// 使用OWASP Java Encoder或Apache Commons Configuration等
// 这些库默认禁用外部实体`

const formState = reactive({
  content: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.post('/home/xxe/xmlreader', formState.content, {
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
