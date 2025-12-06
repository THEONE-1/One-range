<template>
  <vuln-template
    title="运维诊断 - Runtime.exec()"
    subtitle="Remote Code Execution via Runtime"
    level="严重"
    :icon="ThunderboltOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        场景：提供给运维的“远程诊断”接口，直接把输入命令透传到 Runtime.exec()。
      </a-typography-paragraph>
      <a-typography-paragraph>
        风险：未做校验的运维命令会被直接执行，可读取文件、横向移动。
      </a-typography-paragraph>
      <ul>
        <li>执行任意系统命令</li>
        <li>读取敏感文件内容</li>
        <li>修改系统配置</li>
        <li>完全控制服务器</li>
      </ul>
      <a-typography-paragraph>
        <code>Runtime.getRuntime().exec()</code> 是常见执行方式，本示例保持弱配置复现风险。
      </a-typography-paragraph>
    </template>

    <template #payload>
      <a-typography-paragraph>
        <strong>示例诊断命令（Windows）：</strong>
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>whoami</a-typography-paragraph>
      <a-typography-paragraph code copyable>ipconfig</a-typography-paragraph>
      <a-typography-paragraph code copyable>dir</a-typography-paragraph>
      
      <a-typography-paragraph style="margin-top: 16px">
        <strong>示例诊断命令（Linux/Mac）：</strong>
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>whoami</a-typography-paragraph>
      <a-typography-paragraph code copyable>id</a-typography-paragraph>
      <a-typography-paragraph code copyable>ls -la</a-typography-paragraph>

      <a-alert
        message="真实系统应限制命令白名单或提供只读诊断接口"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <div class="system-admin-container">
        <a-card title="远程诊断命令" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            提供给运维的命令执行入口，用于查看主机状态（示例：whoami、ifconfig、netstat -an）。
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="诊断命令"
            name="cmd"
            :rules="[{ required: true, message: '请输入系统命令' }]"
          >
            <a-input
              v-model:value="formState.cmd"
              placeholder="如：whoami 或 127.0.0.1 && id"
              size="large"
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

    <template #input-hint>
      <a-alert message="测试建议" type="warning" show-icon style="margin-bottom: 12px;" />
      <p><strong>Windows系统测试命令：</strong></p>
      <ul style="padding-left: 20px; margin-bottom: 12px;">
        <li><a-typography-text code copyable>whoami</a-typography-text> - 查看当前用户</li>
        <li><a-typography-text code copyable>ipconfig</a-typography-text> - 查看网络配置</li>
        <li><a-typography-text code copyable>dir</a-typography-text> - 列出目录文件</li>
      </ul>
      <p><strong>Linux/Mac系统测试命令：</strong></p>
      <ul style="padding-left: 20px; margin-bottom: 0;">
        <li><a-typography-text code copyable>whoami</a-typography-text> - 查看当前用户</li>
        <li><a-typography-text code copyable>id</a-typography-text> - 查看用户ID信息</li>
        <li><a-typography-text code copyable>ls -la</a-typography-text> - 列出详细文件列表</li>
      </ul>
    </template>

    <template #result v-if="result">
      <div class="result-content">
        <pre style="color: #52c41a; margin: 0;">{{ result }}</pre>
      </div>
      <a-alert
        message="命令成功执行表明存在RCE漏洞，攻击者可执行任意系统命令"
        type="info"
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
@RequestMapping("/home/rce")
public class RuntimeExec {
    @RequestMapping("/runtime")
    public String RuntimeExec(String cmd) {
        StringBuilder sb = new StringBuilder();
        try {
            // 漏洞：直接将用户输入传递给Runtime.exec()，没有验证
            // Runtime.exec()会调用shell执行命令，攻击者可以使用命令分隔符注入
            Process proc = Runtime.getRuntime().exec(cmd);
            
            // 读取命令输出
            InputStream fis = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(fis, "GBK");
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\\n");
            }
        } catch (IOException e) {
            sb.append("错误: ").append(e.toString());
        }
        return sb.toString();
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：Runtime.exec()直接将用户输入作为命令执行，攻击者可以使用命令分隔符（&&、|、;、`等）注入任意命令。Runtime.exec()在某些系统上会调用shell，增加了命令注入的风险。"
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
        <li><strong>避免执行系统命令：</strong>尽量使用Java API替代系统命令</li>
        <li><strong>白名单验证：</strong>严格限制可执行的命令和参数，使用白名单机制</li>
        <li><strong>参数化执行：</strong>使用ProcessBuilder并分离命令和参数，不要拼接字符串</li>
        <li><strong>输入验证：</strong>验证命令格式，过滤命令分隔符和特殊字符</li>
        <li><strong>最小权限运行：</strong>使用低权限用户运行应用</li>
        <li><strong>沙箱隔离：</strong>使用容器或虚拟化技术隔离应用</li>
        <li><strong>命令过滤：</strong>过滤危险命令（rm、del、format等）</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        String cmd = "ping " + userInput;
        Process proc = Runtime.getRuntime().exec(cmd);
// 攻击者可以输入: 127.0.0.1 && whoami</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：使用Java API替代系统命令
        // 使用InetAddress验证IP，而不是ping命令
        try {
            InetAddress addr = InetAddress.getByName(ip);
            boolean reachable = addr.isReachable(5000);
            return "IP " + ip + " 可达性: " + reachable;
        } catch (Exception e) {
            return "无效的IP地址";
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：使用白名单和参数分离
        // 1. 定义允许的命令白名单
        private static final String[] ALLOWED_COMMANDS = {"ping", "nslookup"};
private static final String[] DANGEROUS_CHARS = {"&&", "||", "|", ";", "\`", "$", "(", ")", "<", ">"};
        
        @RequestMapping("/runtime")
        public String RuntimeExec(String cmd) {
            // 2. 验证命令格式
            if (!isSafeCommand(cmd)) {
                throw new SecurityException("不允许的命令");
            }
            
            // 3. 分离命令和参数
    String[] parts = cmd.split("\\\\s+", 2);
            String command = parts[0];
            String arg = parts.length > 1 ? parts[1] : "";
            
            // 4. 验证命令是否在白名单中
            if (!Arrays.asList(ALLOWED_COMMANDS).contains(command)) {
                throw new SecurityException("命令不在白名单中");
            }
            
            // 5. 验证参数格式（例如IP地址）
            if (command.equals("ping") && !isValidIp(arg)) {
                throw new SecurityException("无效的IP地址");
            }
            
            // 6. 使用ProcessBuilder，参数分离
            ProcessBuilder pb = new ProcessBuilder(command, arg);
            Process proc = pb.start();
            // 读取输出...
        }
        
        private boolean isSafeCommand(String cmd) {
            for (String dangerous : DANGEROUS_CHARS) {
                if (cmd.contains(dangerous)) {
                    return false;
                }
            }
            return true;
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：使用ProcessBuilder参数数组
        @RequestMapping("/runtime")
        public String RuntimeExec(String ip) {
            // 验证IP格式
            if (!ip.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                throw new IllegalArgumentException("无效的IP地址格式");
            }
            
            // 使用ProcessBuilder，命令和参数分离
            ProcessBuilder pb = new ProcessBuilder("ping", "-c", "1", ip);
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            // 读取输出...
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：使用安全管理器限制权限
        SecurityManager securityManager = new SecurityManager() {
            @Override
            public void checkExec(String cmd) {
                // 只允许特定命令
                if (!cmd.startsWith("/bin/ping")) {
                    throw new SecurityException("不允许的命令: " + cmd);
                }
            }
        };
System.setSecurityManager(securityManager);</code></pre>
      </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  ThunderboltOutlined,
  PlayCircleOutlined,
  RedoOutlined
} from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/home/rce")
public class RuntimeExec {
    @RequestMapping("/runtime")
    public String RuntimeExec(String cmd) {
        StringBuilder sb = new StringBuilder();
        try {
            // 漏洞：直接将用户输入传递给Runtime.exec()，没有验证
            // Runtime.exec()会调用shell执行命令，攻击者可以使用命令分隔符注入
            Process proc = Runtime.getRuntime().exec(cmd);
            
            // 读取命令输出
            InputStream fis = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(fis, "GBK");
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\\n");
            }
        } catch (IOException e) {
            sb.append("错误: ").append(e.toString());
        }
        return sb.toString();
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
String cmd = "ping " + userInput;
Process proc = Runtime.getRuntime().exec(cmd);
// 攻击者可以输入: 127.0.0.1 && whoami`

const fixCode2 = `// 正确示例1：使用Java API替代系统命令
// 使用InetAddress验证IP，而不是ping命令
try {
    InetAddress addr = InetAddress.getByName(ip);
    boolean reachable = addr.isReachable(5000);
    return "IP " + ip + " 可达性: " + reachable;
} catch (Exception e) {
    return "无效的IP地址";
}`

const fixCode3 = `// 正确示例2：使用白名单和参数分离
// 1. 定义允许的命令白名单
private static final String[] ALLOWED_COMMANDS = {"ping", "nslookup"};
private static final String[] DANGEROUS_CHARS = {"&&", "||", "|", ";", "\`", "$", "(", ")", "<", ">"};

@RequestMapping("/runtime")
public String RuntimeExec(String cmd) {
    // 2. 验证命令格式
    if (!isSafeCommand(cmd)) {
        throw new SecurityException("不允许的命令");
    }
    
    // 3. 分离命令和参数
    String[] parts = cmd.split("\\\\s+", 2);
    String command = parts[0];
    String arg = parts.length > 1 ? parts[1] : "";
    
    // 4. 验证命令是否在白名单中
    if (!Arrays.asList(ALLOWED_COMMANDS).contains(command)) {
        throw new SecurityException("命令不在白名单中");
    }
    
    // 5. 验证参数格式（例如IP地址）
    if (command.equals("ping") && !isValidIp(arg)) {
        throw new SecurityException("无效的IP地址");
    }
    
    // 6. 使用ProcessBuilder，参数分离
    ProcessBuilder pb = new ProcessBuilder(command, arg);
    Process proc = pb.start();
    // 读取输出...
}

private boolean isSafeCommand(String cmd) {
    for (String dangerous : DANGEROUS_CHARS) {
        if (cmd.contains(dangerous)) {
            return false;
        }
    }
    return true;
}`

const fixCode4 = `// 正确示例3：使用ProcessBuilder参数数组
@RequestMapping("/runtime")
public String RuntimeExec(String ip) {
    // 验证IP格式
    if (!ip.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
        throw new IllegalArgumentException("无效的IP地址格式");
    }
    
    // 使用ProcessBuilder，命令和参数分离
    ProcessBuilder pb = new ProcessBuilder("ping", "-c", "1", ip);
    pb.redirectErrorStream(true);
    Process proc = pb.start();
    // 读取输出...
}`

const fixCode5 = `// 正确示例4：使用安全管理器限制权限
SecurityManager securityManager = new SecurityManager() {
    @Override
    public void checkExec(String cmd) {
        // 只允许特定命令
        if (!cmd.startsWith("/bin/ping")) {
            throw new SecurityException("不允许的命令: " + cmd);
        }
    }
};
System.setSecurityManager(securityManager);`

const formState = reactive({
  cmd: 'whoami'
})

const handleSubmit = async (values) => {
  loading.value = true
  try {
    const response = await axios.get('/home/rce/runtime', {
      params: { cmd: formState.cmd }
    })
    result.value = response.data
    message.success('命令执行完成')
  } catch (error) {
    message.error('执行失败：' + (error.response?.data || error.message))
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formState.cmd = 'whoami'
  result.value = ''
}
</script>

