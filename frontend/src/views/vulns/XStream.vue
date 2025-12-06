<template>
  <vuln-template
    title="XStream反序列化"
    subtitle="XStream Deserialization"
    level="严重"
    :icon="ApiOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>XStream</strong>是Java的XML序列化库,存在多个远程代码执行漏洞。
      </a-typography-paragraph>
      <a-typography-paragraph>
        XStream反序列化漏洞的危害：
      </a-typography-paragraph>
      <ul>
        <li>通过构造恶意XML触发反序列化</li>
        <li>利用各种Gadget链执行代码</li>
        <li>远程代码执行</li>
        <li>影响大量使用XStream的应用</li>
      </ul>
      <a-typography-paragraph>
        漏洞版本：XStream &lt; 1.4.18
      </a-typography-paragraph>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的XStream攻击payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        &lt;java.util.PriorityQueue&gt;&lt;comparator class='sun.awt.datatransfer.DataTransferer$IndexOrderComparator'&gt;...
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">利用PriorityQueue触发RCE</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">
        &lt;sorted-set&gt;&lt;string&gt;foo&lt;/string&gt;&lt;dynamic-proxy&gt;...
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">利用动态代理触发漏洞</span>
      </a-space>

      <a-alert
        message="使用白名单机制,升级到安全版本！"
        type="error"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <div class="xml-process-container">
        <a-card title="XML数据处理服务" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            输入XML格式的数据，系统将使用XStream库为您解析和处理。
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
    </template>

    <template #source-code>
      <a-typography-paragraph>
        <strong>后端源码（存在漏洞）：</strong>
      </a-typography-paragraph>
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="sourceCode" style="margin: 0;">
          <pre class="code-block"><code>@RestController
@RequestMapping("/home/xstream")
public class Xstream {
    @RequestMapping("")
    public String vul(@RequestBody String content) {
        try {
            // 漏洞：创建XStream时没有配置安全策略
            // XStream默认允许反序列化任意类，攻击者可以通过构造恶意XML触发RCE
            XStream xs = new XStream();
            xs.fromXML(content);
            return "XStream Vul";
        } catch (Exception e) {
            return e.toString();
        }
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：XStream默认允许反序列化任意类，攻击者可以通过构造恶意XML（如使用java.util.PriorityQueue、动态代理等）触发反序列化漏洞，实现远程代码执行。"
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
        <li><strong>升级版本：</strong>升级到XStream 1.4.18或更高版本</li>
        <li><strong>使用白名单：</strong>配置XStream只允许反序列化特定类</li>
        <li><strong>禁用危险类型：</strong>禁用动态代理、PriorityQueue等危险类型</li>
        <li><strong>使用安全配置：</strong>使用XStream的安全配置模式</li>
        <li><strong>输入验证：</strong>检查XML是否包含危险类名</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        XStream xs = new XStream();
xs.fromXML(content); // 允许反序列化任意类</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：使用白名单（推荐）
        @RequestMapping("")
        public String vul(@RequestBody String content) {
            XStream xs = new XStream();
            
            // 设置白名单，只允许反序列化特定类
            xs.allowTypes(new Class[]{
                com.example.model.User.class,
                com.example.model.Order.class
            });
            
            // 或者使用包名白名单
            xs.allowTypesByWildcard(new String[]{
                "com.example.model.*"
            });
            
            Object obj = xs.fromXML(content);
            return "解析成功";
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：禁用危险类型
        @RequestMapping("")
        public String vul(@RequestBody String content) {
            XStream xs = new XStream();
            
            // 禁用动态代理
            xs.denyTypes(new Class[]{
                java.lang.reflect.Proxy.class,
                java.lang.reflect.InvocationHandler.class
            });
            
            // 禁用危险集合类
            xs.denyTypes(new String[]{
                "java.util.PriorityQueue",
                "java.beans.EventHandler"
            });
            
            Object obj = xs.fromXML(content);
            return "解析成功";
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：使用安全配置（XStream 1.4.18+）
        @RequestMapping("")
        public String vul(@RequestBody String content) {
            // 使用安全配置，默认禁用危险类型
            XStream xs = new XStream();
            xs.addPermission(NoTypePermission.NONE); // 默认禁止所有类型
            xs.allowTypes(new Class[]{User.class, Order.class}); // 只允许特定类
            
            Object obj = xs.fromXML(content);
            return "解析成功";
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：升级到安全版本
        // 在pom.xml中升级XStream版本
        &lt;dependency&gt;
            &lt;groupId&gt;com.thoughtworks.xstream&lt;/groupId&gt;
            &lt;artifactId&gt;xstream&lt;/artifactId&gt;
            &lt;version&gt;1.4.20&lt;/version&gt;
&lt;/dependency&gt;</code></pre>
      </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { ApiOutlined, FileTextOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/home/xstream")
public class Xstream {
    @RequestMapping("")
    public String vul(@RequestBody String content) {
        try {
            // 漏洞：创建XStream时没有配置安全策略
            // XStream默认允许反序列化任意类，攻击者可以通过构造恶意XML触发RCE
            XStream xs = new XStream();
            xs.fromXML(content);
            return "XStream Vul";
        } catch (Exception e) {
            return e.toString();
        }
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
XStream xs = new XStream();
xs.fromXML(content); // 允许反序列化任意类`

const fixCode2 = `// 正确示例1：使用白名单（推荐）
@RequestMapping("")
public String vul(@RequestBody String content) {
    XStream xs = new XStream();
    
    // 设置白名单，只允许反序列化特定类
    xs.allowTypes(new Class[]{
        com.example.model.User.class,
        com.example.model.Order.class
    });
    
    // 或者使用包名白名单
    xs.allowTypesByWildcard(new String[]{
        "com.example.model.*"
    });
    
    Object obj = xs.fromXML(content);
    return "解析成功";
}`

const fixCode3 = `// 正确示例2：禁用危险类型
@RequestMapping("")
public String vul(@RequestBody String content) {
    XStream xs = new XStream();
    
    // 禁用动态代理
    xs.denyTypes(new Class[]{
        java.lang.reflect.Proxy.class,
        java.lang.reflect.InvocationHandler.class
    });
    
    // 禁用危险集合类
    xs.denyTypes(new String[]{
        "java.util.PriorityQueue",
        "java.beans.EventHandler"
    });
    
    Object obj = xs.fromXML(content);
    return "解析成功";
}`

const fixCode4 = `// 正确示例3：使用安全配置（XStream 1.4.18+）
@RequestMapping("")
public String vul(@RequestBody String content) {
    // 使用安全配置，默认禁用危险类型
    XStream xs = new XStream();
    xs.addPermission(NoTypePermission.NONE); // 默认禁止所有类型
    xs.allowTypes(new Class[]{User.class, Order.class}); // 只允许特定类
    
    Object obj = xs.fromXML(content);
    return "解析成功";
}`

const fixCode5 = `// 正确示例4：升级到安全版本
// 在pom.xml中升级XStream版本
<dependency>
    <groupId>com.thoughtworks.xstream</groupId>
    <artifactId>xstream</artifactId>
    <version>1.4.20</version>
</dependency>`

const formState = reactive({
  content: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/xstream', {
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
