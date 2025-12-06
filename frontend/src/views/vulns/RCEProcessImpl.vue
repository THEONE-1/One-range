<template>
  <vuln-template
    title="底层进程调用 - ProcessImpl"
    subtitle="RCE via ProcessImpl"
    level="严重"
    :icon="ThunderboltOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        场景：为“系统维护”提供的底层进程接口，直接用反射调用 ProcessImpl.start。
      </a-typography-paragraph>
      <a-typography-paragraph>
        风险：用户输入被当作命令数组第一元素，可拼接分隔符执行任意命令。
      </a-typography-paragraph>
      <ul>
        <li>执行任意系统命令</li>
        <li>读取敏感文件内容</li>
        <li>修改系统配置</li>
        <li>完全控制服务器</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        <strong>示例维护命令（Windows）：</strong>
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>whoami</a-typography-paragraph>
      <a-typography-paragraph code copyable>ipconfig</a-typography-paragraph>
      
      <a-typography-paragraph style="margin-top: 16px">
        <strong>示例维护命令（Linux/Mac）：</strong>
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>whoami</a-typography-paragraph>
      <a-typography-paragraph code copyable>id</a-typography-paragraph>
    </template>

    <template #test-form>
      <div class="system-admin-container">
        <a-card title="系统管理工具 - ProcessImpl" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            使用ProcessImpl底层接口执行系统命令进行服务器维护。
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="系统命令"
            name="cmd"
            :rules="[{ required: true, message: '请输入系统命令' }]"
          >
            <a-input
              v-model:value="formState.cmd"
              size="large"
              placeholder="输入要执行的系统命令"
              prefix="⚡"
            />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><thunderbolt-outlined /></template>
                执行命令
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
@RequestMapping("/home/rce")
public class ProcessImplExec {
    @RequestMapping("/processimpl")
    public String processImplExec(String cmd) {
        try {
            // 漏洞：使用反射调用ProcessImpl.start()方法，直接执行用户输入的命令
            Class&lt;?&gt; clazz = Class.forName("java.lang.ProcessImpl");
            Method method = clazz.getDeclaredMethod("start", 
                String[].class, Map.class, String.class, 
                ProcessBuilder.Redirect[].class, boolean.class);
            method.setAccessible(true);
            
            // 漏洞：直接将用户输入作为命令数组的第一个元素
            Process e = (Process) method.invoke(null, 
                new String[]{cmd}, null, null, null, false);
            
            // 读取命令输出...
            return infoStream.toString();
        } catch (Exception ex) {
            return "错误: " + ex.toString();
        }
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="真实系统应避免暴露通用命令入口，使用白名单/参数分离或直接禁用"
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
        <li><strong>避免使用反射：</strong>不要使用反射绕过Java安全机制</li>
        <li><strong>避免执行系统命令：</strong>尽量使用Java API替代系统命令</li>
        <li><strong>白名单验证：</strong>严格限制可执行的命令和参数</li>
        <li><strong>参数分离：</strong>将命令和参数分离，不要拼接字符串</li>
        <li><strong>输入验证：</strong>验证命令格式，过滤命令分隔符</li>
        <li><strong>最小权限运行：</strong>使用低权限用户运行应用</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        Class&lt;?&gt; clazz = Class.forName("java.lang.ProcessImpl");
        Method method = clazz.getDeclaredMethod("start", ...);
        method.setAccessible(true);
        Process e = (Process) method.invoke(null, new String[]{cmd}, ...);
// 攻击者可以输入: whoami && ls</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：使用ProcessBuilder（推荐）
@RequestMapping("/processimpl")
public String processImplExec(String cmd) {
    // 验证命令格式
    if (!isSafeCommand(cmd)) {
        throw new SecurityException("不允许的命令");
    }
    
    // 分离命令和参数
    String[] parts = cmd.split("\\\\s+", 2);
    String command = parts[0];
    String arg = parts.length > 1 ? parts[1] : "";
    
    // 使用ProcessBuilder，参数分离
    ProcessBuilder pb = new ProcessBuilder(command, arg);
    Process proc = pb.start();
    // 读取输出...
}</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：使用Java API替代
// 不要执行系统命令，使用Java API实现功能
// 例如：使用InetAddress而不是ping命令
InetAddress addr = InetAddress.getByName(ip);
boolean reachable = addr.isReachable(5000);</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：白名单验证
private static final String[] ALLOWED_COMMANDS = {"ping", "nslookup"};

@RequestMapping("/processimpl")
public String processImplExec(String cmd) {
    String[] parts = cmd.split("\\\\s+", 2);
    String command = parts[0];
    
    // 验证命令是否在白名单中
    if (!Arrays.asList(ALLOWED_COMMANDS).contains(command)) {
        throw new SecurityException("命令不在白名单中");
    }
    
    // 验证参数格式
    if (parts.length > 1 && !isValidArg(command, parts[1])) {
        throw new SecurityException("无效的参数");
    }
    
    // 使用ProcessBuilder执行
    ProcessBuilder pb = new ProcessBuilder(parts);
    Process proc = pb.start();
    // 读取输出...
}</code></pre>
        </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { ThunderboltOutlined, PlayCircleOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/home/rce")
public class ProcessImplExec {
    @RequestMapping("/processimpl")
    public String processImplExec(String cmd) {
        try {
            // 漏洞：使用反射调用ProcessImpl.start()方法，直接执行用户输入的命令
            Class<?> clazz = Class.forName("java.lang.ProcessImpl");
            Method method = clazz.getDeclaredMethod("start", 
                String[].class, Map.class, String.class, 
                ProcessBuilder.Redirect[].class, boolean.class);
            method.setAccessible(true);
            
            // 漏洞：直接将用户输入作为命令数组的第一个元素
            Process e = (Process) method.invoke(null, 
                new String[]{cmd}, null, null, null, false);
            
            // 读取命令输出...
            return infoStream.toString();
        } catch (Exception ex) {
            return "错误: " + ex.toString();
        }
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
Class<?> clazz = Class.forName("java.lang.ProcessImpl");
Method method = clazz.getDeclaredMethod("start", ...);
method.setAccessible(true);
Process e = (Process) method.invoke(null, new String[]{cmd}, ...);
// 攻击者可以输入: whoami && ls`

const fixCode2 = `// 正确示例1：使用ProcessBuilder（推荐）
        @RequestMapping("/processimpl")
        public String processImplExec(String cmd) {
            // 验证命令格式
            if (!isSafeCommand(cmd)) {
                throw new SecurityException("不允许的命令");
            }
            
            // 分离命令和参数
            String[] parts = cmd.split("\\s+", 2);
            String command = parts[0];
            String arg = parts.length > 1 ? parts[1] : "";
            
            // 使用ProcessBuilder，参数分离
            ProcessBuilder pb = new ProcessBuilder(command, arg);
            Process proc = pb.start();
            // 读取输出...
}`
      
const fixCode3 = `// 正确示例2：使用Java API替代
        // 不要执行系统命令，使用Java API实现功能
        // 例如：使用InetAddress而不是ping命令
        InetAddress addr = InetAddress.getByName(ip);
boolean reachable = addr.isReachable(5000);`
      
const fixCode4 = `// 正确示例3：白名单验证
        private static final String[] ALLOWED_COMMANDS = {"ping", "nslookup"};
        
        @RequestMapping("/processimpl")
        public String processImplExec(String cmd) {
            String[] parts = cmd.split("\\s+", 2);
            String command = parts[0];
            
            // 验证命令是否在白名单中
            if (!Arrays.asList(ALLOWED_COMMANDS).contains(command)) {
                throw new SecurityException("命令不在白名单中");
            }
            
            // 验证参数格式
            if (parts.length > 1 && !isValidArg(command, parts[1])) {
                throw new SecurityException("无效的参数");
            }
            
            // 使用ProcessBuilder执行
            ProcessBuilder pb = new ProcessBuilder(parts);
            Process proc = pb.start();
            // 读取输出...
}`

const formState = reactive({
  cmd: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/rce/processimpl', {
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
  formState.cmd = ''
  result.value = ''
}
</script>
