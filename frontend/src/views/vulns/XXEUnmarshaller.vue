<template>
  <vuln-template
    title="XML导入服务 - JAXB Unmarshaller"
    subtitle="XML External Entity"
    level="高危"
    :icon="FileTextOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        场景：电子发票/配置文件上传接口，后端用 JAXB Unmarshaller 反序列化对象。
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
            粘贴上传的 XML 单据/配置，系统直接反序列化并展示内容。
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
                反序列化XML
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
        message="如果返回了文件内容，说明存在XXE漏洞，Unmarshaller加载了外部实体。"
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
    @PostMapping(value = "/unmarshaller")
    public String Unmarshaller(@RequestBody String content, String entity) {
        try {
            // 漏洞：安全检测是可选的
            if (entity != null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }
            
            JAXBContext context = JAXBContext.newInstance(Student.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            
            XMLInputFactory xif = XMLInputFactory.newFactory();
            // 漏洞：修复代码被注释掉了，默认允许加载外部实体
            // xif.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            // xif.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(content));
            Object o = unmarshaller.unmarshal(xsr);
            
            return o != null ? o.toString() : "解析成功";
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：JAXB Unmarshaller配合XMLInputFactory默认允许加载外部实体，攻击者可以通过DOCTYPE声明加载外部资源。"
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
        <li><strong>禁用外部DTD和样式表：</strong>设置XMLConstants.ACCESS_EXTERNAL_DTD和ACCESS_EXTERNAL_STYLESHEET为空字符串</li>
        <li><strong>禁用外部实体支持：</strong>设置IS_SUPPORTING_EXTERNAL_ENTITIES为false</li>
        <li><strong>禁用DTD支持：</strong>设置SUPPORT_DTD为false</li>
        <li><strong>输入验证：</strong>检查XML是否包含DOCTYPE声明</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例：禁用外部实体
        import javax.xml.XMLConstants;
        
        @PostMapping(value = "/unmarshaller")
        public String Unmarshaller(@RequestBody String content) {
            try {
                JAXBContext context = JAXBContext.newInstance(Student.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                
                XMLInputFactory xif = XMLInputFactory.newFactory();
                
                // 禁用外部DTD访问
                xif.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                
                // 禁用外部样式表访问
                xif.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
                
                // 禁用外部实体支持
                xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
                
                // 禁用DTD支持
                xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
                
                XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(content));
                Object o = unmarshaller.unmarshal(xsr);
                
                return o != null ? o.toString() : "解析成功";
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
    @PostMapping(value = "/unmarshaller")
    public String Unmarshaller(@RequestBody String content, String entity) {
        try {
            // 漏洞：安全检测是可选的
            if (entity != null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }
            
            JAXBContext context = JAXBContext.newInstance(Student.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            
            XMLInputFactory xif = XMLInputFactory.newFactory();
            // 漏洞：修复代码被注释掉了，默认允许加载外部实体
            // xif.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            // xif.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(content));
            Object o = unmarshaller.unmarshal(xsr);
            
            return o != null ? o.toString() : "解析成功";
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}`

const fixCode1 = `// 正确示例：禁用外部实体
import javax.xml.XMLConstants;

@PostMapping(value = "/unmarshaller")
public String Unmarshaller(@RequestBody String content) {
    try {
        JAXBContext context = JAXBContext.newInstance(Student.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        
        XMLInputFactory xif = XMLInputFactory.newFactory();
        
        // 禁用外部DTD访问
        xif.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        
        // 禁用外部样式表访问
        xif.setProperty(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        
        // 禁用外部实体支持
        xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        
        // 禁用DTD支持
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        
        XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(content));
        Object o = unmarshaller.unmarshal(xsr);
        
        return o != null ? o.toString() : "解析成功";
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
    const response = await axios.post('/home/xxe/unmarshaller', formState.content, {
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
