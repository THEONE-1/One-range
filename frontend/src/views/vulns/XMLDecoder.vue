<template>
  <vuln-template
    title="XMLDecoder反序列化"
    subtitle="XMLDecoder Deserialization"
    level="严重"
    :icon="FileZipOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>XMLDecoder反序列化漏洞</strong>是指使用XMLDecoder解析不可信的XML数据时可能触发代码执行。
      </a-typography-paragraph>
      <a-typography-paragraph>
        XMLDecoder反序列化漏洞的危害：
      </a-typography-paragraph>
      <ul>
        <li>通过构造恶意XML触发代码执行</li>
        <li>远程代码执行</li>
        <li>完全控制服务器</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的XMLDecoder攻击payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        &lt;java&gt;&lt;void method=&quot;start&quot;&gt;&lt;object class=&quot;java.lang.Runtime&quot;&gt;&lt;void method=&quot;exec&quot;&gt;&lt;string&gt;calc&lt;/string&gt;&lt;/void&gt;&lt;/object&gt;&lt;/void&gt;&lt;/java&gt;
      </a-typography-paragraph>
    </template>

    <template #test-form>
      <div class="xml-process-container">
        <a-card title="XML反序列化服务" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            输入XML格式的数据，系统将使用XMLDecoder为您反序列化。
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
              placeholder="输入XML格式的数据"
            />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><file-zip-outlined /></template>
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
    </template>

    <template #source-code>
      <a-typography-paragraph>
        <strong>后端源码（存在漏洞）：</strong>
      </a-typography-paragraph>
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="sourceCode" style="margin: 0;">
          <pre class="code-block"><code>@Controller
@RequestMapping("/home/deserialize")
public class XMLDecoder {
    @RequestMapping("/xmldecoder")
    public String xmlDecoder(String cmd, Model model) {
        StringBuilder results = new StringBuilder();
        String[] strCmd = cmd.split(" ");
        
        // 漏洞：直接使用用户输入的命令构造XML
        // XMLDecoder可以执行XML中指定的Java方法，攻击者可以构造恶意XML执行任意代码
        StringBuilder sb = new StringBuilder()
            .append("&lt;?xml version=\\"1.0\\" encoding=\\"UTF-8\\"?&gt;")
            .append("&lt;java version=\\"1.8.0_151\\" class=\\"java.beans.XMLDecoder\\"&gt;")
            .append("    &lt;object class=\\"java.lang.ProcessBuilder\\"&gt;")
            .append("        &lt;array class=\\"java.lang.String\\" length=\\"" + strCmd.length + "\\"&gt;");
        
        for (int i = 0; i &lt; strCmd.length; i++) {
            sb.append("            &lt;void index=\\"" + i + "\\"&gt;")
              .append("                &lt;string&gt;" + strCmd[i] + "&lt;/string&gt;")
              .append("            &lt;/void&gt;");
        }
        
        sb.append("        &lt;/array&gt;")
          .append("        &lt;void method=\\"start\\" /&gt;")
          .append("    &lt;/object&gt;")
          .append("&lt;/java&gt;");
        
        String xml = sb.toString();
        try {
            // 漏洞：XMLDecoder会执行XML中指定的Java方法
            // 这里构造了ProcessBuilder来执行系统命令
            java.beans.XMLDecoder xmlDecoder = new java.beans.XMLDecoder(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            xmlDecoder.readObject(); // 执行ProcessBuilder.start()
            xmlDecoder.close();
            return "执行成功!";
        } catch (Exception e) {
            return e.toString();
        }
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：XMLDecoder会将XML转换为Java对象并执行其中的方法。攻击者可以构造恶意XML（如使用ProcessBuilder、Runtime.exec等）执行任意系统命令，实现远程代码执行。"
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
        <li><strong>禁止使用XMLDecoder：</strong>不要使用XMLDecoder解析不可信的XML数据</li>
        <li><strong>使用替代方案：</strong>使用JAXB、DOM4J等更安全的XML解析库</li>
        <li><strong>输入验证：</strong>检查XML是否包含危险类名（ProcessBuilder、Runtime等）</li>
        <li><strong>白名单验证：</strong>只允许预定义的XML结构</li>
        <li><strong>沙箱隔离：</strong>在受限环境中执行XMLDecoder</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        XMLDecoder decoder = new XMLDecoder(inputStream);
Object obj = decoder.readObject(); // 可能执行任意代码</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：使用JAXB替代XMLDecoder（推荐）
        import javax.xml.bind.JAXBContext;
        import javax.xml.bind.Unmarshaller;
        
        @RequestMapping("/xmldecoder")
        public String xmlDecoder(String xml) {
            JAXBContext context = JAXBContext.newInstance(User.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            // JAXB只反序列化预定义的类，不会执行任意方法
            User user = (User) unmarshaller.unmarshal(new StringReader(xml));
            return "解析成功: " + user.getName();
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：输入验证
        @RequestMapping("/xmldecoder")
        public String xmlDecoder(String xml) {
            // 检查是否包含危险类名
            String[] dangerousClasses = {
                "ProcessBuilder", "Runtime", "Process", 
                "ScriptEngineManager", "URLClassLoader"
            };
            
            for (String dangerous : dangerousClasses) {
                if (xml.contains(dangerous)) {
                    throw new SecurityException("XML包含危险类: " + dangerous);
                }
            }
            
            // 仍然不推荐使用XMLDecoder
            // 应该使用更安全的XML解析库
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：使用DOM解析器
        import org.w3c.dom.Document;
        import javax.xml.parsers.DocumentBuilder;
        
        @RequestMapping("/xmldecoder")
        public String xmlDecoder(String xml) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            // DOM解析器只解析XML结构，不会执行代码
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            return "解析成功";
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：完全禁止XMLDecoder
        // 在代码审查和静态分析工具中配置规则，禁止使用XMLDecoder
// 如果必须使用，确保XML来源可信，并进行严格验证</code></pre>
      </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { FileZipOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@Controller
@RequestMapping("/home/deserialize")
public class XMLDecoder {
    @RequestMapping("/xmldecoder")
    public String xmlDecoder(String cmd, Model model) {
        StringBuilder results = new StringBuilder();
        String[] strCmd = cmd.split(" ");
        
        // 漏洞：直接使用用户输入的命令构造XML
        // XMLDecoder可以执行XML中指定的Java方法，攻击者可以构造恶意XML执行任意代码
        StringBuilder sb = new StringBuilder()
            .append("<?xml version=\\"1.0\\" encoding=\\"UTF-8\\"?>")
            .append("<java version=\\"1.8.0_151\\" class=\\"java.beans.XMLDecoder\\">")
            .append("    <object class=\\"java.lang.ProcessBuilder\\">")
            .append("        <array class=\\"java.lang.String\\" length=\\"" + strCmd.length + "\\">");
        
        for (int i = 0; i < strCmd.length; i++) {
            sb.append("            <void index=\\"" + i + "\\">")
              .append("                <string>" + strCmd[i] + "</string>")
              .append("            </void>");
        }
        
        sb.append("        </array>")
          .append("        <void method=\\"start\\" />")
          .append("    </object>")
          .append("</java>");
        
        String xml = sb.toString();
        try {
            // 漏洞：XMLDecoder会执行XML中指定的Java方法
            // 这里构造了ProcessBuilder来执行系统命令
            java.beans.XMLDecoder xmlDecoder = new java.beans.XMLDecoder(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            xmlDecoder.readObject(); // 执行ProcessBuilder.start()
            xmlDecoder.close();
            return "执行成功!";
        } catch (Exception e) {
            return e.toString();
        }
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
XMLDecoder decoder = new XMLDecoder(inputStream);
Object obj = decoder.readObject(); // 可能执行任意代码`

const fixCode2 = `// 正确示例1：使用JAXB替代XMLDecoder（推荐）
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

@RequestMapping("/xmldecoder")
public String xmlDecoder(String xml) {
    JAXBContext context = JAXBContext.newInstance(User.class);
    Unmarshaller unmarshaller = context.createUnmarshaller();
    // JAXB只反序列化预定义的类，不会执行任意方法
    User user = (User) unmarshaller.unmarshal(new StringReader(xml));
    return "解析成功: " + user.getName();
}`

const fixCode3 = `// 正确示例2：输入验证
@RequestMapping("/xmldecoder")
public String xmlDecoder(String xml) {
    // 检查是否包含危险类名
    String[] dangerousClasses = {
        "ProcessBuilder", "Runtime", "Process", 
        "ScriptEngineManager", "URLClassLoader"
    };
    
    for (String dangerous : dangerousClasses) {
        if (xml.contains(dangerous)) {
            throw new SecurityException("XML包含危险类: " + dangerous);
        }
    }
    
    // 仍然不推荐使用XMLDecoder
    // 应该使用更安全的XML解析库
}`

const fixCode4 = `// 正确示例3：使用DOM解析器
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;

@RequestMapping("/xmldecoder")
public String xmlDecoder(String xml) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    // DOM解析器只解析XML结构，不会执行代码
    Document document = builder.parse(new InputSource(new StringReader(xml)));
    return "解析成功";
}`

const fixCode5 = `// 正确示例4：完全禁止XMLDecoder
// 在代码审查和静态分析工具中配置规则，禁止使用XMLDecoder
// 如果必须使用，确保XML来源可信，并进行严格验证`

const formState = reactive({
  content: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/deserialize/xmldecoder', {
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
  formState.content = ''
  result.value = ''
}
</script>
