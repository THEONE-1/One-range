<template>
  <vuln-template
    title="网络连通性诊断 - ProcessBuilder"
    subtitle="RCE via ProcessBuilder"
    level="严重"
    :icon="ThunderboltOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        场景：提供“连通性诊断”接口，使用 ProcessBuilder 调用 ping 检查目标节点。
      </a-typography-paragraph>
      <a-typography-paragraph>
        风险：用户输入被直接拼到系统命令，可拼接分隔符实现命令注入。
      </a-typography-paragraph>
      <ul>
        <li>命令行 ping 参数可被追加 &&、| 等分隔符</li>
        <li>可执行任意系统命令，读取/改写服务器</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        示例（连通性测试 / 注入）：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        127.0.0.1
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">正常ping测试</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">
        127.0.0.1 &amp;&amp; whoami
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">Linux/Win 命令注入（使用 &&）</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">
        127.0.0.1 | dir
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">管道注入</span>
      </a-space>

      <a-alert
        message="真实系统应使用参数分离或改用探测 API，而非拼接命令"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <a-form :model="formState" @finish="handleSubmit" layout="vertical">
        <a-form-item
          label="诊断目标"
          name="ip"
          :rules="[{ required: true, message: '请输入IP地址' }]"
        >
          <a-input
            v-model:value="formState.ip"
            size="large"
            placeholder="示例：8.8.8.8 或 127.0.0.1"
          />
        </a-form-item>

        <a-form-item>
          <a-checkbox v-model:checked="formState.safe">
            启用安全检测
          </a-checkbox>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" size="large" :loading="loading">
              <template #icon><play-circle-outlined /></template>
              执行测试
            </a-button>
            <a-button size="large" @click="handleReset">
              <template #icon><redo-outlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </template>

    <template #result v-if="result">
      <div class="result-content">
        <pre style="margin: 0;">{{ result }}</pre>
      </div>
      <a-alert
        v-if="result && !result.includes('错误')"
        message="命令成功执行表明存在RCE漏洞，攻击者可执行任意系统命令"
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
@RequestMapping("/home/rce")
public class ProcessBuilderExec {
    @RequestMapping("/processbuilder")
    public String ProcessBuilderExec(String ip, String safe) {
        // 检测操作系统
        String os = System.getProperty("os.name").toLowerCase();
        String[] cmdList;
        
        if (os.contains("win")) {
            // 漏洞：直接将用户输入拼接到命令中
            cmdList = new String[]{"cmd", "/c", "ping -n 1 " + ip};
        } else {
            // 漏洞：使用sh -c执行命令，用户输入直接拼接
            cmdList = new String[]{"sh", "-c", "ping -c 1 " + ip + " 2>&1 || echo 'Ping test for: " + ip + "'"};
        }
        
        // 使用ProcessBuilder执行命令
        ProcessBuilder pb = new ProcessBuilder(cmdList);
        Process process = pb.start();
        // 读取命令输出...
        return output;
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：直接将用户输入的ip参数拼接到系统命令中，攻击者可以使用命令分隔符（如 &&、|、;）注入任意命令"
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
        <li><strong>输入验证：</strong>验证IP地址格式，只允许合法的IP地址</li>
        <li><strong>命令过滤：</strong>过滤命令分隔符（&&、|、;、`、$等）和特殊字符</li>
        <li><strong>最小权限运行：</strong>使用低权限用户运行应用</li>
        <li><strong>沙箱隔离：</strong>使用容器或虚拟化技术隔离应用</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        String[] cmdList = new String[]{"cmd", "/c", "ping -n 1 " + ip};
        ProcessBuilder pb = new ProcessBuilder(cmdList);
pb.start();</code></pre>
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
          <pre class="code-block"><code>// 正确示例2：如果必须使用命令，使用白名单和参数分离
        // 1. 验证IP格式
        if (!ip.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
            throw new SecurityException("无效的IP地址格式");
        }
        
        // 2. 验证IP范围（可选）
        String[] parts = ip.split("\\.");
        for (String part : parts) {
            int num = Integer.parseInt(part);
            if (num < 0 || num > 255) {
                throw new SecurityException("IP地址超出范围");
            }
        }
        
        // 3. 使用参数分离，不要拼接字符串
        String[] cmdList;
        if (os.contains("win")) {
            cmdList = new String[]{"ping", "-n", "1", ip}; // 参数分离
        } else {
            cmdList = new String[]{"ping", "-c", "1", ip}; // 参数分离
        }
        
        ProcessBuilder pb = new ProcessBuilder(cmdList);
pb.start();</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：使用命令过滤
        private static final String[] DANGEROUS_CHARS = {"&&", "||", "|", ";", "`", "$", "(", ")", "<", ">"};
        
        public boolean isSafeInput(String input) {
            for (String dangerous : DANGEROUS_CHARS) {
                if (input.contains(dangerous)) {
                    return false;
                }
            }
            return true;
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
public class ProcessBuilderExec {
    @RequestMapping("/processbuilder")
    public String ProcessBuilderExec(String ip, String safe) {
        // 检测操作系统
        String os = System.getProperty("os.name").toLowerCase();
        String[] cmdList;
        
        if (os.contains("win")) {
            // 漏洞：直接将用户输入拼接到命令中
            cmdList = new String[]{"cmd", "/c", "ping -n 1 " + ip};
        } else {
            // 漏洞：使用sh -c执行命令，用户输入直接拼接
            cmdList = new String[]{"sh", "-c", "ping -c 1 " + ip + " 2>&1 || echo 'Ping test for: " + ip + "'"};
        }
        
        // 使用ProcessBuilder执行命令
        ProcessBuilder pb = new ProcessBuilder(cmdList);
        Process process = pb.start();
        // 读取命令输出...
        return output;
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
String[] cmdList = new String[]{"cmd", "/c", "ping -n 1 " + ip};
ProcessBuilder pb = new ProcessBuilder(cmdList);
pb.start();`

const fixCode2 = `// 正确示例1：使用Java API替代系统命令
// 使用InetAddress验证IP，而不是ping命令
try {
    InetAddress addr = InetAddress.getByName(ip);
    boolean reachable = addr.isReachable(5000);
    return "IP " + ip + " 可达性: " + reachable;
} catch (Exception e) {
    return "无效的IP地址";
}`

const fixCode3 = `// 正确示例2：如果必须使用命令，使用白名单和参数分离
// 1. 验证IP格式
if (!ip.matches("^([0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
    throw new SecurityException("无效的IP地址格式");
}

// 2. 验证IP范围（可选）
String[] parts = ip.split("\\.");
for (String part : parts) {
    int num = Integer.parseInt(part);
    if (num < 0 || num > 255) {
        throw new SecurityException("IP地址超出范围");
    }
}

// 3. 使用参数分离，不要拼接字符串
String[] cmdList;
if (os.contains("win")) {
    cmdList = new String[]{"ping", "-n", "1", ip}; // 参数分离
} else {
    cmdList = new String[]{"ping", "-c", "1", ip}; // 参数分离
}

ProcessBuilder pb = new ProcessBuilder(cmdList);
pb.start();`

const fixCode4 = `// 正确示例3：使用命令过滤
private static final String[] DANGEROUS_CHARS = {"&&", "||", "|", ";", "\`", "$", "(", ")", "&lt;", "&gt;"};

public boolean isSafeInput(String input) {
    for (String dangerous : DANGEROUS_CHARS) {
        if (input.contains(dangerous)) {
            return false;
        }
    }
    return true;
}`

const formState = reactive({
  ip: '',
  safe: false
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const params = {
      ip: formState.ip
    }
    if (formState.safe) {
      params.safe = 'true'
    }
    const response = await axios.get('/home/rce/processbuilder', {
      params: params
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
  formState.ip = ''
  formState.safe = false
  result.value = ''
}
</script>
