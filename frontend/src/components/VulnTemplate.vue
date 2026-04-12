<template>
  <div class="vuln-container">
    <!-- 页面头部 -->
    <div class="vuln-header">
      <div class="header-content">
        <div class="header-icon">
          <component :is="icon" />
        </div>
        <div class="header-text">
          <h1 class="header-title">{{ title }}</h1>
          <p class="header-subtitle">{{ subtitle }}</p>
        </div>
        <div class="header-actions">
          <a-tag :color="levelColor" class="level-tag">{{ level }}</a-tag>
          <a-button
            :type="isCompleted ? 'default' : 'primary'"
            :icon="isCompleted ? h(CheckCircleOutlined) : h(CheckOutlined)"
            @click="handleMarkComplete"
            :loading="markLoading"
            class="complete-btn"
          >
            {{ isCompleted ? '已完成' : '标记完成' }}
          </a-button>
        </div>
      </div>
    </div>

    <a-row :gutter="[24, 24]" style="margin-top: 24px">
      <!-- 测试表单 - 放在最上面 -->
      <a-col :span="24">
        <div class="test-card">
          <div class="card-header">
            <div class="card-title">
              <experiment-outlined class="title-icon" />
              <span>漏洞测试</span>
            </div>
          </div>
          <div class="card-body">
            <slot name="test-form"></slot>
            
            <!-- 输入提示 - 可折叠 -->
            <a-collapse v-if="$slots['input-hint']" v-model:activeKey="hintKeys" class="hint-collapse" :bordered="false">
              <a-collapse-panel key="hint">
                <template #header>
                  <div class="hint-header">
                    <bulb-outlined class="hint-icon" />
                    <span>测试提示</span>
                  </div>
                </template>
                <div class="hint-content">
                  <slot name="input-hint"></slot>
                </div>
              </a-collapse-panel>
            </a-collapse>
          </div>
        </div>
      </a-col>

      <!-- 测试结果 -->
      <a-col :span="24" v-if="$slots.result">
        <div class="result-card">
          <div class="card-header">
            <div class="card-title">
              <code-outlined class="title-icon" />
              <span>执行结果</span>
            </div>
          </div>
          <div class="card-body">
            <slot name="result"></slot>
          </div>
        </div>
      </a-col>

      <!-- 帮助信息 - 折叠面板 -->
      <a-col :span="24">
        <a-collapse v-model:activeKey="activeKeys" class="help-collapse" :bordered="false">
          <!-- 漏洞原理 -->
          <a-collapse-panel key="1">
            <template #header>
              <div class="collapse-header">
                <book-outlined class="collapse-icon" />
                <span class="collapse-title">漏洞原理</span>
                <span class="collapse-tag">展开内容</span>
              </div>
            </template>
            <div class="collapse-content">
              <slot name="description"></slot>
            </div>
          </a-collapse-panel>

          <!-- 攻击示例 -->
          <a-collapse-panel key="2">
            <template #header>
              <div class="collapse-header">
                <bug-outlined class="collapse-icon" />
                <span class="collapse-title">攻击示例</span>
                <span class="collapse-tag">展开内容</span>
              </div>
            </template>
            <div class="collapse-content">
              <slot name="payload"></slot>
            </div>
          </a-collapse-panel>

          <!-- 后端源码 -->
          <a-collapse-panel key="3" v-if="$slots['source-code']">
            <template #header>
              <div class="collapse-header">
                <code-outlined class="collapse-icon" />
                <span class="collapse-title">后端源码</span>
                <span class="collapse-tag">展开内容</span>
              </div>
            </template>
            <div class="collapse-content">
              <slot name="source-code"></slot>
            </div>
          </a-collapse-panel>

          <!-- 修复方案 -->
          <a-collapse-panel key="4">
            <template #header>
              <div class="collapse-header">
                <tool-outlined class="collapse-icon" />
                <span class="collapse-title">修复方案</span>
                <span class="collapse-tag">展开内容</span>
              </div>
            </template>
            <div class="collapse-content">
              <slot name="fix">
                <a-alert
                  message="修复建议"
                  description="根据具体漏洞类型，建议采取对应的安全防护措施，如输入验证、输出编码、使用参数化查询等。"
                  type="success"
                  show-icon
                />
              </slot>
            </div>
          </a-collapse-panel>
        </a-collapse>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, h } from 'vue'
import { message } from 'ant-design-vue'
import {
  BookOutlined,
  BugOutlined,
  ExperimentOutlined,
  CodeOutlined,
  ToolOutlined,
  BulbOutlined,
  CheckOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'
import axios from 'axios'

const props = defineProps({
  title: String,
  subtitle: String,
  level: {
    type: String,
    default: '高危'
  },
  icon: Object,
  moduleKey: {
    type: String,
    required: true
  }
})

const activeKeys = ref([])
const hintKeys = ref([])
const isCompleted = ref(false)
const markLoading = ref(false)

const levelColor = computed(() => {
  const colors = {
    '严重': 'red',
    '高危': 'red',
    '中危': 'orange',
    '低危': 'blue'
  }
  return colors[props.level] || 'default'
})

const checkModuleCompleted = async () => {
  try {
    const response = await axios.get('/api/progress/check', {
      params: { vulnModule: props.moduleKey }
    })
    let result = response.data
    if (typeof result === 'string') {
      result = JSON.parse(result)
    }

    console.log(`[${props.moduleKey}] 检查完成状态:`, result)

    if (result.success) {
      isCompleted.value = result.completed || false
      console.log(`[${props.moduleKey}] 完成状态:`, isCompleted.value)
    }
  } catch (error) {
    console.error('检查完成状态失败:', error)
  }
}

const handleMarkComplete = async () => {
  if (isCompleted.value) {
    message.info('该模块已标记为完成')
    return
  }

  markLoading.value = true
  try {
    const params = new URLSearchParams()
    params.append('vulnModule', props.moduleKey)

    console.log(`[${props.moduleKey}] 标记完成请求:`, props.moduleKey)

    const response = await axios.post('/api/progress/complete', params, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })

    let result = response.data
    if (typeof result === 'string') {
      result = JSON.parse(result)
    }

    console.log(`[${props.moduleKey}] 标记完成响应:`, result)

    if (result.success) {
      isCompleted.value = true
      message.success('已标记为完成！')
    } else {
      message.error(result.message || '标记失败')
    }
  } catch (error) {
    console.error('标记完成失败:', error)
    message.error('标记失败，请重试')
  } finally {
    markLoading.value = false
  }
}

onMounted(() => {
  checkModuleCompleted()
})
</script>

<style scoped>
.vuln-container {
  width: 100%;
}

.vuln-header {
  position: relative;
  overflow: hidden;
  padding: 34px;
  border-radius: 28px;
  border: 1px solid rgba(105, 243, 255, 0.16);
  background:
    radial-gradient(circle at top right, rgba(105, 243, 255, 0.18), transparent 28%),
    linear-gradient(135deg, rgba(16, 28, 44, 0.92) 0%, rgba(4, 10, 18, 0.92) 100%);
  box-shadow: 0 28px 70px rgba(0, 0, 0, 0.28);
}

.vuln-header::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(var(--grid-line) 1px, transparent 1px),
    linear-gradient(90deg, var(--grid-line) 1px, transparent 1px);
  background-size: 36px 36px;
  opacity: 0.4;
  pointer-events: none;
}

.header-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 84px;
  height: 84px;
  border-radius: 24px;
  border: 1px solid rgba(105, 243, 255, 0.16);
  background: rgba(105, 243, 255, 0.08);
  backdrop-filter: blur(16px);
  color: var(--accent);
  font-size: 38px;
  flex-shrink: 0;
}

.header-text {
  flex: 1;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.complete-btn {
  height: 42px;
  padding: 0 22px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 999px;
}

.complete-btn:hover {
  transform: translateY(-1px);
}

.header-title {
  margin: 0 0 10px;
  font-size: clamp(28px, 4vw, 40px);
  line-height: 1.05;
  color: var(--text-primary);
}

.header-subtitle {
  margin: 0;
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-secondary);
}

.level-tag {
  font-size: 13px;
  padding: 6px 16px;
  border-radius: 999px;
  font-weight: 600;
}

.test-card,
.result-card {
  overflow: hidden;
  border-radius: 24px;
  border: 1px solid rgba(105, 243, 255, 0.12);
  background: linear-gradient(180deg, rgba(11, 24, 38, 0.84) 0%, rgba(6, 12, 21, 0.92) 100%);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.24);
  transition: transform 0.25s ease, border-color 0.25s ease;
}

.test-card:hover,
.result-card:hover {
  transform: translateY(-3px);
  border-color: rgba(105, 243, 255, 0.24);
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid rgba(105, 243, 255, 0.1);
  background: rgba(105, 243, 255, 0.04);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.title-icon {
  font-size: 18px;
  color: var(--accent);
}

.card-body {
  padding: 24px;
}

.help-collapse {
  background: transparent;
}

.help-collapse :deep(.ant-collapse-item) {
  margin-bottom: 16px;
  overflow: hidden;
  border: 1px solid rgba(105, 243, 255, 0.1);
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(11, 24, 38, 0.82) 0%, rgba(6, 12, 21, 0.9) 100%);
  box-shadow: 0 20px 48px rgba(0, 0, 0, 0.22);
  transition: transform 0.25s ease, border-color 0.25s ease;
}

.help-collapse :deep(.ant-collapse-item:hover) {
  transform: translateY(-2px);
  border-color: rgba(105, 243, 255, 0.22);
}

.help-collapse :deep(.ant-collapse-header) {
  padding: 20px 24px;
  background: rgba(105, 243, 255, 0.05);
}

.help-collapse :deep(.ant-collapse-content) {
  border-top: 1px solid rgba(105, 243, 255, 0.08);
  background: transparent;
}

.help-collapse :deep(.ant-collapse-content-box) {
  padding: 24px;
}

.collapse-header {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.collapse-icon {
  font-size: 18px;
  color: var(--accent);
}

.collapse-title {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.collapse-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 7px 14px;
  border-radius: 999px;
  border: 1px solid rgba(105, 243, 255, 0.28);
  background: rgba(105, 243, 255, 0.14);
  color: #d7fdff;
  font-size: 12px;
  font-weight: 600;
}

.collapse-content {
  font-size: 14px;
  line-height: 1.85;
  color: #d8edf8;
}

:deep(.result-content) {
  max-height: 500px;
  overflow-y: auto;
  padding: 20px;
  border: 1px solid rgba(105, 243, 255, 0.1);
  border-radius: 18px;
  background: rgba(4, 12, 20, 0.88);
  color: #cffaff;
  font-family: 'Cascadia Code', 'Consolas', 'Courier New', monospace;
}

:deep(pre) {
  margin: 0;
  color: var(--text-primary);
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.hint-collapse {
  margin-top: 20px;
  background: transparent;
}

.hint-collapse :deep(.ant-collapse-item) {
  overflow: hidden;
  border: 1px solid rgba(105, 243, 255, 0.28);
  border-radius: 18px;
  background: rgba(105, 243, 255, 0.1);
}

.hint-collapse :deep(.ant-collapse-header) {
  padding: 14px 16px;
  background: rgba(105, 243, 255, 0.05);
}

.hint-collapse :deep(.ant-collapse-content) {
  border-top: 1px solid rgba(105, 243, 255, 0.22);
  background: rgba(6, 15, 25, 0.82);
}

.hint-collapse :deep(.ant-collapse-content-box) {
  padding: 16px;
}

.hint-header {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #e7fcff;
  font-weight: 600;
}

.hint-icon {
  color: var(--accent);
}

.hint-content {
  color: #d8edf8;
  line-height: 1.85;
}

.hint-content :deep(.ant-alert) {
  margin-bottom: 14px;
}

.hint-content :deep(code) {
  display: inline-block;
  margin: 0 4px 4px 0;
  padding: 3px 8px;
  border-radius: 10px;
  background: rgba(105, 243, 255, 0.12);
  border: 1px solid rgba(105, 243, 255, 0.18);
  color: #effcff;
  font-size: 12px;
}

.card-body :deep(ul),
.collapse-content :deep(ul),
.card-body :deep(ol),
.collapse-content :deep(ol) {
  padding-left: 20px;
  color: #d8edf8;
  line-height: 1.9;
}

.card-body :deep(li),
.collapse-content :deep(li) {
  margin-bottom: 8px;
}

:deep(.collapse-content strong),
:deep(.hint-content strong),
:deep(.collapse-content h3),
:deep(.collapse-content h4),
:deep(.hint-content h3),
:deep(.hint-content h4) {
  color: var(--text-primary);
}

:deep(.collapse-content p),
:deep(.collapse-content li),
:deep(.hint-content p),
:deep(.hint-content li) {
  color: #d8edf8;
}

.lab-surface {
  padding: 24px;
  border-radius: 20px;
  border: 1px solid rgba(105, 243, 255, 0.12);
  background: linear-gradient(180deg, rgba(8, 18, 30, 0.9) 0%, rgba(5, 11, 20, 0.96) 100%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.02);
}

.lab-surface + .lab-surface,
.lab-surface + .lab-empty,
.lab-empty + .lab-surface {
  margin-top: 18px;
}

.lab-surface-muted {
  background: rgba(7, 16, 27, 0.7);
}

.lab-surface-title {
  margin: 0 0 16px;
  color: var(--text-primary);
  font-size: 18px;
  font-weight: 600;
}

.lab-surface-title--accent {
  color: var(--accent);
}

.lab-label {
  margin: 0 0 8px;
  color: var(--text-muted);
  font-size: 13px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.lab-inline-form {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  width: 100%;
}

.lab-inline-form > :deep(.ant-form-item:first-child) {
  flex: 1;
  margin-right: 0 !important;
}

.lab-result-box {
  padding: 16px;
  border-radius: 16px;
  border: 1px solid rgba(105, 243, 255, 0.12);
  background: rgba(4, 12, 20, 0.92);
}

.lab-result-box--accent {
  border-left: 4px solid var(--accent);
}

.lab-result-box :deep(pre) {
  margin: 0;
  color: #bff8ff;
  white-space: pre-wrap;
}

.lab-caption {
  margin-top: 10px;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.7;
}

.lab-empty {
  padding: 24px;
  border-radius: 20px;
  border: 1px dashed rgba(105, 243, 255, 0.16);
  background: rgba(7, 16, 27, 0.62);
  color: var(--text-muted);
  text-align: center;
}

.lab-note {
  margin-top: 8px;
  color: var(--text-muted);
  font-size: 12px;
  line-height: 1.7;
}

.collapse-content :deep(.ant-typography code),
:deep(.ant-typography code) {
  display: block !important;
  width: 100% !important;
  margin: 0 !important;
  padding: 16px !important;
  overflow-x: auto !important;
  white-space: pre !important;
  word-wrap: normal !important;
  word-break: normal !important;
  tab-size: 4 !important;
  box-sizing: border-box !important;
  border: 1px solid rgba(105, 243, 255, 0.12) !important;
  border-radius: 14px !important;
  background: rgba(4, 12, 20, 0.92) !important;
  color: #cffaff !important;
  font-family: 'Cascadia Code', 'Consolas', 'Courier New', monospace !important;
  font-size: 13px !important;
  line-height: 1.65 !important;
  text-align: left !important;
}

.collapse-content :deep(.ant-typography pre code),
:deep(.ant-typography pre code) {
  display: block !important;
  white-space: pre !important;
  overflow-x: auto !important;
}

:deep(.ant-typography-copy) {
  margin-top: 8px;
}

@media (max-width: 768px) {
  .vuln-header {
    padding: 24px;
    border-radius: 22px;
  }

  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .header-title {
    font-size: 26px;
  }

  .lab-surface,
  .lab-empty {
    padding: 18px;
    border-radius: 16px;
  }

  .lab-inline-form {
    flex-direction: column;
  }
}
</style>

