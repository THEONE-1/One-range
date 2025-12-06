<template>
  <vuln-template
    title="表达式计算器"
    subtitle="Expression Calculator"
    level="严重"
    :icon="ThunderboltOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        本系统提供了一个强大的<strong>表达式计算器</strong>，支持执行各种数学表达式和业务逻辑计算。
      </a-typography-paragraph>
      <a-typography-paragraph>
        功能特点：
      </a-typography-paragraph>
      <ul>
        <li>支持复杂的数学表达式计算</li>
        <li>支持变量和函数调用</li>
        <li>支持字符串处理和格式化</li>
        <li>实时计算并返回结果</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        示例表达式：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        2 + 3 * 4
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        "Hello" + " World"
      </a-typography-paragraph>
    </template>

    <template #test-form>
      <div class="code-exec-container">
        <a-card title="表达式计算器" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            请输入要计算的表达式，系统将实时计算结果。
          </a-typography-paragraph>
          <a-typography-paragraph style="color: #999; font-size: 12px; margin-top: 8px;">
            提示：支持数学运算、字符串操作等常见表达式
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="表达式"
            name="code"
            :rules="[{ required: true, message: '请输入表达式' }]"
          >
            <a-textarea
              v-model:value="formState.code"
              :rows="4"
              size="large"
              placeholder="例如：2 + 3 * 4 或 &quot;Hello&quot; + &quot; World&quot;"
            />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><code-outlined /></template>
                计算
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
        <strong>后端实现代码：</strong>
      </a-typography-paragraph>
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="sourceCode" style="margin: 0;">
          <pre class="code-block"><code>@RestController
@RequestMapping("/api/calculator")
public class ExpressionCalculator {
    @PostMapping("/evaluate")
    public String evaluateExpression(String expression) {
        // 使用Groovy引擎执行用户输入的表达式
        GroovyShell shell = new GroovyShell();
        try {
            Object result = shell.evaluate(expression);
            return "计算结果: " + result.toString();
        } catch (Exception e) {
            return "计算错误: " + e.getMessage();
        }
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="安全提示：直接执行用户输入的表达式存在安全风险，应该对输入进行严格验证和过滤"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #fix>
      <a-typography-paragraph>
        <strong>修复建议：</strong>
      </a-typography-paragraph>
      <ul style="padding-left: 20px; line-height: 2;">
        <li><strong>禁止代码执行：</strong>不要允许用户输入并执行代码</li>
        <li><strong>使用Groovy安全模式：</strong>配置Groovy的安全策略，限制可执行的类和方法</li>
        <li><strong>沙箱隔离：</strong>使用Groovy沙箱限制代码执行权限</li>
        <li><strong>白名单验证：</strong>如果必须使用，只允许预定义的代码模板</li>
        <li><strong>输入验证：</strong>严格验证用户输入，禁止危险关键字</li>
        <li><strong>替换方案：</strong>使用模板引擎（如Velocity、FreeMarker）而不是代码执行</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        GroovyShell shell = new GroovyShell();
        shell.evaluate(userInput);
// 攻击者可以输入: "whoami".execute().text</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：禁止代码执行（最佳方案）
        // 不要提供代码执行功能，使用其他方式实现需求
// 例如：使用配置化的规则引擎，而不是代码执行</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：使用Groovy沙箱
        import groovy.security.GroovyCodeSource;
        import java.security.CodeSource;
        
        @GetMapping("/groovy")
        public String groovyExec(String cmd) {
            // 创建安全策略
            SecureASTCustomizer secureAST = new SecureASTCustomizer();
            secureAST.setClosuresAllowed(false); // 禁止闭包
            secureAST.setMethodDefinitionAllowed(false); // 禁止方法定义
            
            // 配置允许的导入
            secureAST.setImportsWhitelist(Arrays.asList("java.lang.Math"));
            secureAST.setStaticImportsWhitelist(Arrays.asList());
            secureAST.setStaticStarImportsWhitelist(Arrays.asList());
            
            CompilerConfiguration config = new CompilerConfiguration();
            config.addCompilationCustomizers(secureAST);
            
            GroovyShell shell = new GroovyShell(config);
            try {
                Object result = shell.evaluate(cmd);
                return result.toString();
            } catch (Exception e) {
                return "执行失败: " + e.getMessage();
            }
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：使用模板引擎替代
        // 使用Velocity或FreeMarker模板引擎，而不是Groovy代码执行
        import org.apache.velocity.Template;
        import org.apache.velocity.VelocityContext;
        
        VelocityContext context = new VelocityContext();
        context.put("variable", value);
        Template template = velocityEngine.getTemplate("template.vm");
// 模板只能访问预定义的变量，不能执行任意代码</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：输入验证和关键字过滤
        private static final String[] DANGEROUS_KEYWORDS = {
            "execute", "exec", "Process", "Runtime", "File", 
            "getClass", "class", "eval", "import", "package"
        };
        
        @GetMapping("/groovy")
        public String groovyExec(String cmd) {
            // 验证输入，禁止危险关键字
            String lowerCmd = cmd.toLowerCase();
            for (String keyword : DANGEROUS_KEYWORDS) {
                if (lowerCmd.contains(keyword.toLowerCase())) {
                    throw new SecurityException("代码包含危险关键字: " + keyword);
                }
            }
            
            // 限制代码长度
            if (cmd.length() > 100) {
                throw new SecurityException("代码过长");
            }
            
            // 使用沙箱执行（参考示例2）
    // ...</code></pre>
      </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { ThunderboltOutlined, CodeOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/api/calculator")
public class ExpressionCalculator {
    @PostMapping("/evaluate")
    public String evaluateExpression(String expression) {
        // 使用Groovy引擎执行用户输入的表达式
        GroovyShell shell = new GroovyShell();
        try {
            Object result = shell.evaluate(expression);
            return "计算结果: " + result.toString();
        } catch (Exception e) {
            return "计算错误: " + e.getMessage();
        }
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
GroovyShell shell = new GroovyShell();
shell.evaluate(userInput);
// 攻击者可以输入: "whoami".execute().text`

const fixCode2 = `// 正确示例1：禁止代码执行（最佳方案）
// 不要提供代码执行功能，使用其他方式实现需求
// 例如：使用配置化的规则引擎，而不是代码执行`

const fixCode3 = `// 正确示例2：使用Groovy沙箱
import groovy.security.GroovyCodeSource;
import java.security.CodeSource;

@GetMapping("/groovy")
public String groovyExec(String cmd) {
    // 创建安全策略
    SecureASTCustomizer secureAST = new SecureASTCustomizer();
    secureAST.setClosuresAllowed(false); // 禁止闭包
    secureAST.setMethodDefinitionAllowed(false); // 禁止方法定义
    
    // 配置允许的导入
    secureAST.setImportsWhitelist(Arrays.asList("java.lang.Math"));
    secureAST.setStaticImportsWhitelist(Arrays.asList());
    secureAST.setStaticStarImportsWhitelist(Arrays.asList());
    
    CompilerConfiguration config = new CompilerConfiguration();
    config.addCompilationCustomizers(secureAST);
    
    GroovyShell shell = new GroovyShell(config);
    try {
        Object result = shell.evaluate(cmd);
        return result.toString();
    } catch (Exception e) {
        return "执行失败: " + e.getMessage();
    }
}`

const fixCode4 = `// 正确示例3：使用模板引擎替代
// 使用Velocity或FreeMarker模板引擎，而不是Groovy代码执行
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

VelocityContext context = new VelocityContext();
context.put("variable", value);
Template template = velocityEngine.getTemplate("template.vm");
// 模板只能访问预定义的变量，不能执行任意代码`

const fixCode5 = `// 正确示例4：输入验证和关键字过滤
private static final String[] DANGEROUS_KEYWORDS = {
    "execute", "exec", "Process", "Runtime", "File", 
    "getClass", "class", "eval", "import", "package"
};

@GetMapping("/groovy")
public String groovyExec(String cmd) {
    // 验证输入，禁止危险关键字
    String lowerCmd = cmd.toLowerCase();
    for (String keyword : DANGEROUS_KEYWORDS) {
        if (lowerCmd.contains(keyword.toLowerCase())) {
            throw new SecurityException("代码包含危险关键字: " + keyword);
        }
    }
    
    // 限制代码长度
    if (cmd.length() > 100) {
        throw new SecurityException("代码过长");
    }
    
    // 使用沙箱执行（参考示例2）
    // ...`

const formState = reactive({
  code: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/rce/groovy', {
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
  formState.code = ''
  result.value = ''
}
</script>
