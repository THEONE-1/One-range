<template>
  <div class="dashboard-page fade-in">
    <section class="dashboard-grid">
      <article class="panel hero-panel">
        <div class="panel-glow"></div>
        <div class="hero-copy">
          <div class="hero-heading">
            <h1>One 安全靶场</h1>
          </div>

          <div class="hero-actions">
            <a-button type="primary" size="large" @click="goTo('/xss/reflect')">
              进入热门实验
            </a-button>
            <a-button size="large" @click="goTo('/progress')">
              查看学习进度
            </a-button>
          </div>
        </div>

        <div class="hero-stats">
          <div class="metric-card" v-for="metric in heroMetrics" :key="metric.label">
            <div class="metric-value">{{ metric.value }}</div>
            <div class="metric-label">{{ metric.label }}</div>
          </div>
        </div>
      </article>

      <article class="panel telemetry-panel">
        <div class="panel-heading">
          <div>
            <span class="panel-kicker">实验态势</span>
            <h2>实验态势</h2>
          </div>
          <span class="panel-chip">在线</span>
        </div>

        <div class="telemetry-list">
          <div class="telemetry-item" v-for="item in telemetryItems" :key="item.label">
            <div class="telemetry-icon">
              <component :is="item.icon" />
            </div>
            <div class="telemetry-body">
              <div class="telemetry-top">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
              <p>{{ item.description }}</p>
              <div class="signal-bar">
                <span :style="{ width: item.ratio }"></span>
              </div>
            </div>
          </div>
        </div>
      </article>

      <article class="panel modules-panel">
        <div class="panel-heading">
          <div>
            <span class="panel-kicker">漏洞矩阵</span>
            <h2>漏洞模块矩阵</h2>
          </div>
          <span class="panel-note">单色线性图标 / 霓虹青交互</span>
        </div>

        <div class="module-grid">
          <button
            v-for="module in modules"
            :key="module.title"
            class="module-card"
            type="button"
            @click="goTo(module.path)"
          >
            <div class="module-top">
              <div class="module-icon">
                <component :is="module.icon" />
              </div>
              <span class="risk-pill" :class="getRiskClass(module.risk)">{{ module.risk }}</span>
            </div>
            <div class="module-title">{{ module.title }}</div>
            <div class="module-desc">{{ module.description }}</div>
            <div class="module-meta">
              <span>{{ module.count }}</span>
              <span>{{ module.pathLabel }}</span>
            </div>
          </button>
        </div>
      </article>

      <article class="panel path-panel">
        <div class="panel-heading">
          <div>
            <span class="panel-kicker">学习路径</span>
            <h2>进阶路线</h2>
          </div>
        </div>

        <div class="path-list">
          <div class="path-item" v-for="lane in learningLanes" :key="lane.title">
            <div class="path-index">{{ lane.step }}</div>
            <div class="path-content">
              <strong>{{ lane.title }}</strong>
              <p>{{ lane.description }}</p>
            </div>
          </div>
        </div>
      </article>

      <article class="panel stream-panel">
        <div class="panel-heading">
          <div>
            <span class="panel-kicker">学习维度</span>
            <h2>学习维度</h2>
          </div>
        </div>

        <div class="stream-list">
          <div class="stream-card" v-for="stream in streams" :key="stream.title">
            <component :is="stream.icon" class="stream-icon" />
            <div class="stream-text">
              <strong>{{ stream.title }}</strong>
              <p>{{ stream.description }}</p>
            </div>
          </div>
        </div>
      </article>

      <article class="panel command-panel">
        <div class="panel-heading">
          <div>
            <span class="panel-kicker">平台信号</span>
            <h2>平台信号</h2>
          </div>
        </div>

        <div class="signal-list">
          <div class="signal-item" v-for="info in systemSignals" :key="info.label">
            <span>{{ info.label }}</span>
            <strong>{{ info.value }}</strong>
          </div>
        </div>

        <div class="command-footer">
          <div class="footer-alert">
            <exclamation-circle-outlined />
            <span>仅供安全学习与研究，请勿用于非法用途。</span>
          </div>
          <a-button block @click="goTo('/fastjson')">
            打开组件漏洞专区
          </a-button>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  ExclamationCircleOutlined,
  BugOutlined,
  DatabaseOutlined,
  ThunderboltOutlined,
  FileTextOutlined,
  GlobalOutlined,
  ApiOutlined,
  SafetyOutlined,
  CodeOutlined,
  RadarChartOutlined,
  RocketOutlined,
  CheckCircleOutlined,
  NodeIndexOutlined,
  EyeOutlined,
  DeploymentUnitOutlined,
  PartitionOutlined
} from '@ant-design/icons-vue'

const router = useRouter()

const goTo = (path) => {
  router.push(path)
}

const getRiskClass = (risk) => {
  const riskMap = {
    '严重': 'risk-critical',
    '高危': 'risk-high',
    '中危': 'risk-medium'
  }
  return riskMap[risk] || 'risk-default'
}

const heroMetrics = [
  { label: '漏洞模块', value: '26+' },
  { label: '真实链路', value: '100%' },
  { label: '重点场景', value: '8' },
  { label: '学习状态', value: '同步中' }
]

const telemetryItems = [
  {
    label: '基础漏洞覆盖',
    value: '19 模块',
    ratio: '84%',
    description: '从输入校验到执行链路，覆盖常见 Web 攻击面。',
    icon: RadarChartOutlined
  },
  {
    label: '组件漏洞实验',
    value: '6 模块',
    ratio: '68%',
    description: '聚焦 Fastjson、Shiro、Log4j 与 Jackson 等典型场景。',
    icon: DeploymentUnitOutlined
  },
  {
    label: '实验可视状态',
    value: '实时联动',
    ratio: '92%',
    description: '首页入口、详情页模板与学习进度页共享统一视觉风格。',
    icon: EyeOutlined
  }
]

const modules = [
  {
    title: 'XSS',
    description: '从反射型到 DOM 型，观察输入到渲染的攻击通路。',
    count: '3 个场景',
    risk: '高危',
    path: '/xss/reflect',
    pathLabel: '前端注入',
    icon: BugOutlined
  },
  {
    title: 'SQL 注入',
    description: '整型、报错与盲注路线完整串联数据库攻击面。',
    count: '4 个场景',
    risk: '高危',
    path: '/sqli/jdbc_int_based',
    pathLabel: '数据层攻击',
    icon: DatabaseOutlined
  },
  {
    title: 'RCE',
    description: '通过 Runtime 与 ProcessBuilder 理解命令执行风险。',
    count: '2 个场景',
    risk: '严重',
    path: '/rce/runtime',
    pathLabel: '执行链路',
    icon: ThunderboltOutlined
  },
  {
    title: 'XXE',
    description: 'XMLReader、SAXReader 与 SAXBuilder 多解析器对照。',
    count: '3 个场景',
    risk: '高危',
    path: '/xxe/xmlreader',
    pathLabel: '解析器攻击',
    icon: FileTextOutlined
  },
  {
    title: 'SSRF',
    description: '围绕服务端请求转发构造内部资产探测路径。',
    count: '1 个场景',
    risk: '高危',
    path: '/ssrf',
    pathLabel: '网络边界',
    icon: GlobalOutlined
  },
  {
    title: 'Fastjson',
    description: '典型组件反序列化链路，适合作为高阶入口模块。',
    count: '1 个场景',
    risk: '严重',
    path: '/fastjson',
    pathLabel: '组件利用',
    icon: ApiOutlined
  },
  {
    title: 'Shiro',
    description: '认证授权组件中的反序列化与配置误用风险。',
    count: '1 个场景',
    risk: '严重',
    path: '/shiro',
    pathLabel: '认证体系',
    icon: SafetyOutlined
  },
  {
    title: '反序列化',
    description: '基于 ReadObject 理解对象边界与危险方法触发。',
    count: '1 个场景',
    risk: '中危',
    path: '/deserialize/readobject',
    pathLabel: '对象边界',
    icon: CodeOutlined
  }
]

const learningLanes = [
  { step: '01', title: '基础认知', description: '先从 XSS、SQL 注入和文件操作理解输入与边界。' },
  { step: '02', title: '利用构造', description: '切换到 XXE、SSRF、RCE，建立完整攻击链视角。' },
  { step: '03', title: '组件进阶', description: '进入 Fastjson、Shiro、Log4j 等 Java 生态漏洞模块。' },
  { step: '04', title: '复盘与修复', description: '回到源码、修复建议与学习进度，形成闭环。' }
]

const streams = [
  {
    title: '真实漏洞模块',
    description: '按攻击面拆分实验入口，避免大而散的教程堆叠。',
    icon: NodeIndexOutlined
  },
  {
    title: '统一详情模板',
    description: '测试、结果、原理、Payload 与修复建议在同一工作台收拢。',
    icon: CheckCircleOutlined
  },
  {
    title: '结构化导航',
    description: '基础漏洞、组件漏洞、反序列化三条主线保持清晰分层。',
    icon: PartitionOutlined
  }
]

const systemSignals = computed(() => [
  { label: '当前用户', value: localStorage.getItem('username') || 'Admin' },
  { label: '前端框架', value: 'Vue 3 + Vite' },
  { label: '后端框架', value: 'Spring Boot' },
  { label: '实验版本', value: 'v2.0.0' },
  { label: '运行模式', value: '暗色工作台' },
  { label: '访问状态', value: '已登录' }
])
</script>

<style scoped>
.dashboard-page {
  width: 100%;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 20px;
}

.panel {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-lg);
  border: 1px solid rgba(105, 243, 255, 0.14);
  background:
    linear-gradient(180deg, rgba(13, 24, 38, 0.82) 0%, rgba(5, 11, 20, 0.88) 100%);
  box-shadow: var(--panel-shadow);
  backdrop-filter: blur(18px);
}

.panel::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(135deg, rgba(105, 243, 255, 0.04), transparent 38%),
    linear-gradient(0deg, rgba(255, 255, 255, 0.02), transparent 24%);
  pointer-events: none;
}

.hero-panel {
  grid-column: span 7;
  min-height: 360px;
  padding: 30px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.telemetry-panel {
  grid-column: span 5;
  padding: 28px;
}

.modules-panel {
  grid-column: span 8;
  padding: 28px;
}

.path-panel,
.stream-panel,
.command-panel {
  grid-column: span 4;
  padding: 28px;
}

.panel-glow {
  position: absolute;
  top: -40px;
  right: -40px;
  width: 260px;
  height: 260px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(105, 243, 255, 0.24), transparent 68%);
  filter: blur(8px);
  animation: pulse 6s ease-in-out infinite;
}

.panel-kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  color: var(--text-muted);
  font-size: 11px;
  letter-spacing: 0.26em;
  text-transform: uppercase;
}

.panel-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.panel-heading h2 {
  font-size: 26px;
  line-height: 1.1;
  color: var(--text-primary);
}

.panel-chip,
.panel-note {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid rgba(105, 243, 255, 0.18);
  background: rgba(105, 243, 255, 0.08);
  color: var(--accent);
  font-size: 12px;
  white-space: nowrap;
}

.hero-copy {
  position: relative;
  z-index: 1;
}

.hero-heading {
  max-width: 680px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-radius: 999px;
  border: 1px solid rgba(105, 243, 255, 0.18);
  background: rgba(105, 243, 255, 0.08);
  color: var(--accent);
  margin-bottom: 18px;
  font-size: 13px;
}

.hero-heading h1 {
  font-size: clamp(38px, 5vw, 64px);
  line-height: 1;
  letter-spacing: -0.04em;
  margin-bottom: 18px;
  color: var(--text-primary);
}

.hero-heading p {
  max-width: 600px;
  color: var(--text-secondary);
  font-size: 16px;
  line-height: 1.9;
}

.hero-actions {
  display: flex;
  gap: 14px;
  margin-top: 28px;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  position: relative;
  z-index: 1;
}

.metric-card {
  padding: 18px 18px 16px;
  border-radius: var(--radius-md);
  background: rgba(5, 15, 26, 0.54);
  border: 1px solid rgba(105, 243, 255, 0.1);
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

.metric-label {
  margin-top: 8px;
  color: var(--text-muted);
  font-size: 13px;
}

.telemetry-list,
.path-list,
.stream-list,
.signal-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.telemetry-item,
.stream-card,
.signal-item,
.path-item {
  display: flex;
  gap: 14px;
  padding: 16px;
  border-radius: var(--radius-md);
  background: rgba(6, 15, 25, 0.7);
  border: 1px solid rgba(105, 243, 255, 0.08);
}

.telemetry-icon,
.stream-icon,
.path-index {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 46px;
  height: 46px;
  border-radius: 16px;
  background: rgba(105, 243, 255, 0.08);
  color: var(--accent);
  font-size: 20px;
}

.telemetry-body,
.stream-text,
.path-content {
  flex: 1;
}

.telemetry-top,
.signal-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.telemetry-top span,
.stream-text p,
.path-content p,
.signal-item span {
  color: var(--text-muted);
}

.telemetry-top strong,
.stream-text strong,
.path-content strong,
.signal-item strong {
  color: var(--text-primary);
}

.telemetry-body p,
.stream-text p,
.path-content p {
  margin-top: 6px;
  font-size: 14px;
  line-height: 1.75;
}

.signal-bar {
  margin-top: 12px;
  width: 100%;
  height: 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.04);
  overflow: hidden;
}

.signal-bar span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, rgba(105, 243, 255, 0.4), rgba(105, 243, 255, 0.9));
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.module-card {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 14px;
  padding: 18px;
  border-radius: 24px;
  border: 1px solid rgba(105, 243, 255, 0.1);
  background: rgba(6, 15, 25, 0.66);
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: transform 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
}

.module-card:hover {
  transform: translateY(-4px);
  border-color: rgba(105, 243, 255, 0.3);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.24);
}

.module-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.module-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 18px;
  background: rgba(105, 243, 255, 0.08);
  color: var(--accent);
  font-size: 24px;
}

.risk-pill {
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid transparent;
}

.risk-critical {
  color: #ffc7d3;
  background: rgba(255, 107, 143, 0.12);
  border-color: rgba(255, 107, 143, 0.18);
}

.risk-high {
  color: #ffd4df;
  background: rgba(255, 107, 143, 0.08);
  border-color: rgba(255, 107, 143, 0.14);
}

.risk-medium {
  color: #ffe7ba;
  background: rgba(255, 200, 87, 0.1);
  border-color: rgba(255, 200, 87, 0.16);
}

.risk-default {
  color: var(--text-secondary);
  background: rgba(105, 243, 255, 0.08);
  border-color: rgba(105, 243, 255, 0.12);
}

.module-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.module-desc {
  color: var(--text-secondary);
  line-height: 1.8;
  min-height: 58px;
}

.module-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding-top: 6px;
  color: var(--text-muted);
  font-size: 13px;
  letter-spacing: 0.04em;
}

.path-index {
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.stream-card {
  align-items: flex-start;
}

.stream-icon {
  width: 44px;
  height: 44px;
  font-size: 18px;
}

.signal-item {
  padding: 15px 16px;
}

.command-footer {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.footer-alert {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(255, 200, 87, 0.14);
  background: rgba(255, 200, 87, 0.08);
  color: #ffe6ad;
  line-height: 1.7;
}

@media (max-width: 1200px) {
  .hero-panel,
  .telemetry-panel,
  .modules-panel,
  .path-panel,
  .stream-panel,
  .command-panel {
    grid-column: span 12;
  }

  .module-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .dashboard-grid {
    gap: 16px;
  }

  .hero-panel,
  .telemetry-panel,
  .modules-panel,
  .path-panel,
  .stream-panel,
  .command-panel {
    padding: 20px;
    border-radius: 22px;
  }

  .hero-actions,
  .hero-stats,
  .module-grid {
    grid-template-columns: 1fr;
    flex-direction: column;
  }

  .hero-actions {
    display: flex;
  }

  .hero-heading h1 {
    font-size: 36px;
  }

  .module-grid {
    display: grid;
  }

  .panel-heading {
    flex-direction: column;
  }
}
</style>
