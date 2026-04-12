<template>
  <div class="progress-page fade-in">
    <section class="progress-grid">
      <article class="panel hero-panel">
        <div class="panel-grid"></div>
        <div class="hero-copy">
          <div class="panel-kicker">
            <trophy-outlined />
            <span>学习进度</span>
          </div>
          <h1>我的学习进度</h1>
          <p>追踪漏洞模块完成情况，查看整体掌握程度，并根据当前节奏调整学习路径。</p>
        </div>

        <div class="hero-metrics">
          <div class="metric-card" v-for="card in metricCards" :key="card.label">
            <div class="metric-icon">
              <component :is="card.icon" />
            </div>
            <div class="metric-body">
              <strong>{{ card.value }}</strong>
              <span>{{ card.label }}</span>
            </div>
          </div>
        </div>
      </article>

      <article class="panel status-panel">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">状态概览</span>
            <h2>学习状态</h2>
          </div>
          <span class="status-chip">{{ getProgressLabel(stats.percentage) }}</span>
        </div>

        <div class="status-stack">
          <div class="status-item">
            <span>完成率</span>
            <strong>{{ stats.percentage.toFixed(1) }}%</strong>
          </div>
          <div class="status-item">
            <span>剩余模块</span>
            <strong>{{ remainingCount }}</strong>
          </div>
          <div class="status-item">
            <span>当前建议</span>
            <strong>{{ progressTip }}</strong>
          </div>
        </div>
      </article>

      <article class="panel overview-panel">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">整体完成</span>
            <h2>整体进度</h2>
          </div>
          <a-button type="primary" @click="loadAll">
            <reload-outlined />
            刷新数据
          </a-button>
        </div>

        <div class="progress-ring">
          <a-progress
            type="circle"
            :percent="Number(stats.percentage.toFixed(1))"
            :stroke-color="{
              '0%': '#2b6fff',
              '100%': '#69f3ff'
            }"
            :trail-color="'rgba(255,255,255,0.06)'"
            :width="220"
          >
            <template #format="percent">
              <div class="ring-inner">
                <strong>{{ percent }}%</strong>
                <span>已掌握</span>
              </div>
            </template>
          </a-progress>
        </div>

        <div class="overview-copy">
          已完成 <span>{{ stats.completedCount }}</span> / {{ stats.totalCount }} 个漏洞模块
        </div>

        <div class="tip-card">
          <bulb-outlined />
          <span>{{ progressDescription }}</span>
        </div>
      </article>

      <article class="panel timeline-panel">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">完成记录</span>
            <h2>学习记录</h2>
          </div>
          <span class="panel-note">{{ progressList.length }} 条记录</span>
        </div>

        <a-spin :spinning="loading">
          <a-empty v-if="progressList.length === 0" description="暂无学习记录，开始你的第一个漏洞实验。">
            <a-button type="primary" @click="$router.push('/home')">
              <rocket-outlined />
              开始学习
            </a-button>
          </a-empty>

          <div v-else class="timeline-list">
            <div
              v-for="(item, index) in progressList"
              :key="`${item.vulnModule}-${index}`"
              class="timeline-item"
            >
              <div class="timeline-dot" :class="{ completed: item.completed }">
                <check-outlined v-if="item.completed" />
                <clock-circle-outlined v-else />
              </div>

              <div class="timeline-body">
                <div class="timeline-top">
                  <strong>{{ getModuleName(item.vulnModule) }}</strong>
                  <span class="state-pill" :class="item.completed ? 'done' : 'todo'">
                    {{ item.completed ? '已完成' : '未完成' }}
                  </span>
                </div>
                <p>{{ item.completed ? `完成时间：${formatDate(item.completedAt)}` : '该模块尚未完成，可继续进入实验复盘。' }}</p>
              </div>
            </div>
          </div>
        </a-spin>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  TrophyOutlined,
  CheckCircleOutlined,
  FireOutlined,
  PercentageOutlined,
  CheckOutlined,
  ClockCircleOutlined,
  RocketOutlined,
  BulbOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import axios from 'axios'

const loading = ref(false)
const stats = ref({
  completedCount: 0,
  totalCount: 25,
  percentage: 0
})
const progressList = ref([])

const moduleNameMap = {
  xss_reflect: 'XSS反射型',
  xss_store: 'XSS存储型',
  xss_dom: 'XSS DOM型',
  sql_int: 'SQL注入-整型',
  sql_error: 'SQL注入-报错',
  sql_time: 'SQL注入-时间盲注',
  sql_bool: 'SQL注入-布尔盲注',
  mybatis_orderby: 'MyBatis-OrderBy注入',
  mybatis_in: 'MyBatis-In注入',
  rce_runtime: 'RCE-Runtime',
  rce_processbuilder: 'RCE-ProcessBuilder',
  rce_processimpl: 'RCE-ProcessImpl',
  rce_loadjs: 'RCE-LoadJs',
  rce_groovy: 'RCE-Groovy',
  file_upload: '文件上传',
  file_download: '文件下载',
  xxe_xmlreader: 'XXE-XMLReader',
  xxe_saxreader: 'XXE-SAXReader',
  xxe_saxbuilder: 'XXE-SAXBuilder',
  xxe_documentbuilder: 'XXE-DocumentBuilder',
  xxe_unmarshaller: 'XXE-Unmarshaller',
  ssrf: 'SSRF',
  xff: 'XFF伪造',
  cors: 'CORS跨域',
  redirect: 'URL重定向',
  unauth: '未授权访问',
  fastjson: 'Fastjson反序列化',
  jackson: 'Jackson反序列化',
  shiro: 'Shiro反序列化',
  log4j: 'Log4j RCE',
  xstream: 'XStream反序列化',
  actuators: 'Actuator信息泄露',
  jolokia: 'Jolokia RCE',
  readobject: 'ReadObject反序列化',
  rmi: 'RMI反序列化',
  xmldecoder: 'XMLDecoder反序列化',
  yaml: 'SnakeYaml反序列化'
}

const getModuleName = (moduleKey) => moduleNameMap[moduleKey] || moduleKey

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getProgressLabel = (percentage) => {
  if (percentage < 30) return '初学者'
  if (percentage < 60) return '进阶中'
  if (percentage < 90) return '熟练者'
  if (percentage < 100) return '精通者'
  return '大师级'
}

const remainingCount = computed(() => Math.max(stats.value.totalCount - stats.value.completedCount, 0))

const progressTip = computed(() => {
  if (stats.value.percentage < 30) return '从基础漏洞开始'
  if (stats.value.percentage < 60) return '保持连续训练'
  if (stats.value.percentage < 90) return '补齐剩余高阶模块'
  if (stats.value.percentage < 100) return '完成最后冲刺'
  return '进入复盘与加固'
})

const progressDescription = computed(() => {
  if (stats.value.percentage < 30) return '刚刚起步，建议优先完成 XSS、SQL 注入和文件操作等基础模块。'
  if (stats.value.percentage < 60) return '进展稳定，可以开始把 XXE、SSRF、RCE 等链路型漏洞串起来理解。'
  if (stats.value.percentage < 90) return '已经掌握大部分内容，下一步建议聚焦 Fastjson、Shiro、Log4j 等组件专题。'
  if (stats.value.percentage < 100) return '距离全模块完成只差最后几步，适合结合源码和修复方案做集中复盘。'
  return '你已经完成全部模块，可以回到详情页按攻击链和修复思路做系统性复盘。'
})

const metricCards = computed(() => [
  { label: '已完成模块', value: stats.value.completedCount, icon: CheckCircleOutlined },
  { label: '总模块数', value: stats.value.totalCount, icon: FireOutlined },
  { label: '完成率', value: `${stats.value.percentage.toFixed(1)}%`, icon: PercentageOutlined }
])

const loadProgressStats = async () => {
  try {
    const response = await axios.get('/api/progress/stats')
    let result = response.data
    if (typeof result === 'string') {
      result = JSON.parse(result)
    }
    if (result.success) {
      stats.value = {
        completedCount: result.completedCount || 0,
        totalCount: result.totalCount || 25,
        percentage: result.percentage || 0
      }
    }
  } catch (error) {
    console.error('加载进度统计失败:', error)
  }
}

const loadProgressList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/progress/list')
    let result = response.data
    if (typeof result === 'string') {
      result = JSON.parse(result)
    }

    if (result.success) {
      progressList.value = result.data || []
    } else {
      message.error(result.message || '加载进度列表失败')
    }
  } catch (error) {
    console.error('加载进度列表失败:', error)
    message.error('加载进度列表失败')
  } finally {
    loading.value = false
  }
}

const loadAll = async () => {
  await Promise.all([loadProgressStats(), loadProgressList()])
}

onMounted(() => {
  loadAll()
})
</script>

<style scoped>
.progress-page {
  width: 100%;
}

.progress-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 20px;
}

.panel {
  position: relative;
  overflow: hidden;
  border-radius: 28px;
  border: 1px solid rgba(105, 243, 255, 0.12);
  background: linear-gradient(180deg, rgba(12, 24, 38, 0.84) 0%, rgba(6, 12, 21, 0.92) 100%);
  box-shadow: 0 26px 80px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(18px);
}

.hero-panel {
  grid-column: span 8;
  padding: 30px;
}

.status-panel {
  grid-column: span 4;
  padding: 28px;
}

.overview-panel {
  grid-column: span 4;
  padding: 28px;
}

.timeline-panel {
  grid-column: span 8;
  padding: 28px;
}

.panel-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(105, 243, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(105, 243, 255, 0.05) 1px, transparent 1px);
  background-size: 44px 44px;
  opacity: 0.34;
}

.panel-kicker {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--accent);
  font-size: 12px;
  letter-spacing: 0.22em;
  text-transform: uppercase;
}

.hero-copy,
.hero-metrics {
  position: relative;
  z-index: 1;
}

.hero-copy h1 {
  margin: 18px 0 16px;
  font-size: clamp(36px, 4vw, 56px);
  line-height: 1;
  letter-spacing: -0.04em;
  color: var(--text-primary);
}

.hero-copy p {
  max-width: 680px;
  color: var(--text-secondary);
  line-height: 1.9;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 26px;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 22px;
  border: 1px solid rgba(105, 243, 255, 0.1);
  background: rgba(7, 16, 27, 0.56);
}

.metric-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 18px;
  background: rgba(105, 243, 255, 0.08);
  color: var(--accent);
  font-size: 22px;
}

.metric-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.metric-body strong {
  font-size: 28px;
  color: var(--text-primary);
}

.metric-body span {
  color: var(--text-muted);
  font-size: 13px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.panel-head h2 {
  margin-top: 8px;
  font-size: 26px;
  color: var(--text-primary);
}

.status-chip,
.panel-note {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid rgba(105, 243, 255, 0.16);
  background: rgba(105, 243, 255, 0.08);
  color: var(--accent);
  font-size: 12px;
  white-space: nowrap;
}

.status-stack,
.timeline-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.status-item,
.timeline-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 18px;
  border-radius: 22px;
  border: 1px solid rgba(105, 243, 255, 0.08);
  background: rgba(7, 16, 27, 0.54);
}

.status-item {
  justify-content: space-between;
  align-items: center;
}

.status-item span {
  color: var(--text-muted);
}

.status-item strong {
  color: var(--text-primary);
}

.progress-ring {
  display: flex;
  justify-content: center;
  margin: 8px 0 20px;
}

.ring-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.ring-inner strong {
  font-size: 34px;
  color: var(--text-primary);
}

.ring-inner span {
  color: var(--text-muted);
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.overview-copy {
  text-align: center;
  color: var(--text-secondary);
  line-height: 1.8;
}

.overview-copy span {
  color: var(--accent);
  font-size: 22px;
  font-weight: 700;
  margin: 0 4px;
}

.tip-card {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-top: 18px;
  padding: 16px;
  border-radius: 20px;
  border: 1px solid rgba(255, 200, 87, 0.14);
  background: rgba(255, 200, 87, 0.08);
  color: #ffe6ad;
  line-height: 1.8;
}

.timeline-dot {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.06);
  color: var(--text-muted);
}

.timeline-dot.completed {
  background: rgba(56, 242, 180, 0.12);
  color: var(--success);
}

.timeline-body {
  flex: 1;
}

.timeline-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.timeline-top strong {
  font-size: 17px;
  color: var(--text-primary);
}

.timeline-body p {
  margin-top: 8px;
  color: var(--text-secondary);
  line-height: 1.8;
}

.state-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  white-space: nowrap;
}

.state-pill.done {
  background: rgba(56, 242, 180, 0.12);
  color: #c9ffe9;
}

.state-pill.todo {
  background: rgba(255, 255, 255, 0.06);
  color: var(--text-muted);
}

@media (max-width: 1200px) {
  .hero-panel,
  .status-panel,
  .overview-panel,
  .timeline-panel {
    grid-column: span 12;
  }
}

@media (max-width: 768px) {
  .progress-grid {
    gap: 16px;
  }

  .hero-panel,
  .status-panel,
  .overview-panel,
  .timeline-panel {
    padding: 20px;
    border-radius: 22px;
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }

  .panel-head,
  .timeline-top {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
