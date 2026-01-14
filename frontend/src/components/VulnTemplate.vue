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
                    <bulb-outlined style="color: #faad14; margin-right: 8px;" />
                    <span>💡 需要测试提示？点击查看建议</span>
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
                <book-outlined class="collapse-icon" style="color: #3A7BD5" />
                <span class="collapse-title">漏洞原理</span>
                <a-tag color="blue" size="small">点击查看</a-tag>
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
                <bug-outlined class="collapse-icon" style="color: #f5222d" />
                <span class="collapse-title">攻击示例</span>
                <a-tag color="red" size="small">点击查看</a-tag>
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
                <code-outlined class="collapse-icon" style="color: #722ed1" />
                <span class="collapse-title">后端源码</span>
                <a-tag color="purple" size="small">点击查看</a-tag>
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
                <tool-outlined class="collapse-icon" style="color: #52c41a" />
                <span class="collapse-title">修复方案</span>
                <a-tag color="green" size="small">点击查看</a-tag>
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

/* 页面头部 */
.vuln-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 40px;
  color: #fff;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
  position: relative;
  overflow: hidden;
}

.vuln-header::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  border-radius: 50%;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 24px;
  position: relative;
  z-index: 1;
}

.header-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  flex-shrink: 0;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.header-text {
  flex: 1;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.complete-btn {
  height: 40px;
  padding: 0 24px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.complete-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.header-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #fff;
}

.header-subtitle {
  font-size: 16px;
  margin: 0;
  color: rgba(255, 255, 255, 0.85);
}

.level-tag {
  font-size: 14px;
  padding: 4px 16px;
  border-radius: 20px;
  font-weight: 600;
}

/* 卡片通用样式 */
.test-card,
.result-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s;
}

.test-card:hover,
.result-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.test-card {
  border-top: 4px solid #FFB74D;
}

.result-card {
  border-top: 4px solid #52c41a;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.title-icon {
  font-size: 20px;
  color: #FFB74D;
}

.result-card .title-icon {
  color: #52c41a;
}

.card-body {
  padding: 24px;
}

/* 折叠面板 */
.help-collapse {
  background: transparent;
}

.help-collapse :deep(.ant-collapse-item) {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 16px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s;
}

.help-collapse :deep(.ant-collapse-item:hover) {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.help-collapse :deep(.ant-collapse-header) {
  padding: 20px 24px;
  background: #fafafa;
}

.help-collapse :deep(.ant-collapse-content) {
  border-top: 1px solid #f0f0f0;
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
}

.collapse-title {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.collapse-content {
  font-size: 14px;
  line-height: 1.8;
  color: #5a6c7d;
}

/* 结果内容样式 */
:deep(.result-content) {
  background: #f6f8fa;
  padding: 20px;
  border-radius: 8px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  max-height: 500px;
  overflow-y: auto;
  border: 1px solid #e8e8e8;
}

:deep(pre) {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-size: 13px;
  line-height: 1.6;
  color: #2c3e50;
}

/* 输入提示折叠 */
.hint-collapse {
  margin-top: 20px;
  background: transparent;
}

.hint-collapse :deep(.ant-collapse-item) {
  background: #fff7e6;
  border: 1px dashed #faad14;
  border-radius: 8px;
  overflow: hidden;
}

.hint-collapse :deep(.ant-collapse-header) {
  padding: 12px 16px;
  background: transparent;
}

.hint-collapse :deep(.ant-collapse-content) {
  border-top: 1px dashed #faad14;
  background: #fffbf0;
}

.hint-collapse :deep(.ant-collapse-content-box) {
  padding: 16px;
}

.hint-header {
  display: flex;
  align-items: center;
  color: #d48806;
  font-weight: 500;
}

.hint-content {
  color: #5a6c7d;
  line-height: 1.8;
}

/* 代码块样式 - 保持格式 */
.collapse-content :deep(.ant-typography code),
:deep(.ant-typography code) {
  display: block !important;
  white-space: pre !important;
  overflow-x: auto !important;
  padding: 16px !important;
  background: #f6f8fa !important;
  border-radius: 6px !important;
  border: 1px solid #e1e4e8 !important;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace !important;
  font-size: 13px !important;
  line-height: 1.6 !important;
  color: #24292e !important;
  margin: 0 !important;
  word-wrap: normal !important;
  word-break: normal !important;
  tab-size: 4 !important;
  text-align: left !important;
  width: 100% !important;
  box-sizing: border-box !important;
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

/* 响应式 */
@media (max-width: 768px) {
  .vuln-header {
    padding: 24px;
  }
  
  .header-content {
    flex-direction: column;
    text-align: center;
  }
  
  .header-title {
    font-size: 24px;
  }
  
  .level-tag {
    align-self: center;
  }
}
</style>

