<template>
  <div class="progress-container fade-in">
    <!-- 进度统计卡片 -->
    <a-row :gutter="[24, 24]">
      <a-col :span="24">
        <div class="progress-header">
          <div class="header-background"></div>
          <div class="header-icon">
            <trophy-outlined />
          </div>
          <div class="header-content">
            <h1>我的学习进度</h1>
            <p>记录你的安全学习成长轨迹</p>
          </div>
        </div>
      </a-col>

      <!-- 统计卡片 -->
      <a-col :xs="24" :sm="12" :md="8">
        <div class="stat-card slide-in-left" style="animation-delay: 0.1s;">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <check-circle-outlined />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.completedCount }}</div>
            <div class="stat-label">已完成模块</div>
          </div>
          <div class="stat-badge success">
            <check-outlined />
          </div>
        </div>
      </a-col>

      <a-col :xs="24" :sm="12" :md="8">
        <div class="stat-card slide-in-left" style="animation-delay: 0.2s;">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <fire-outlined />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalCount }}</div>
            <div class="stat-label">总模块数</div>
          </div>
          <div class="stat-badge total">
            <database-outlined />
          </div>
        </div>
      </a-col>

      <a-col :xs="24" :sm="12" :md="8">
        <div class="stat-card slide-in-left" style="animation-delay: 0.3s;">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <percentage-outlined />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.percentage.toFixed(1) }}%</div>
            <div class="stat-label">完成率</div>
          </div>
          <div class="stat-badge percentage">
            <rocket-outlined />
          </div>
        </div>
      </a-col>

      <!-- 进度条 -->
      <a-col :span="24">
        <a-card title="整体进度" :bordered="false" class="progress-card slide-in-right">
          <template #extra>
            <a-tag :color="getProgressColor(stats.percentage)" style="font-size: 14px; padding: 4px 12px;">
              {{ getProgressLabel(stats.percentage) }}
            </a-tag>
          </template>
          <a-progress
            :percent="stats.percentage"
            :stroke-color="{
              '0%': '#667eea',
              '100%': '#764ba2',
            }"
            :stroke-width="24"
            status="active"
          />
          <div class="progress-text">
            已完成 <span class="highlight">{{ stats.completedCount }}</span> / {{ stats.totalCount }} 个漏洞模块
          </div>
          <div class="progress-tips">
            <bulb-outlined style="color: #faad14; margin-right: 8px;" />
            <span v-if="stats.percentage < 30">刚刚起步，继续加油！</span>
            <span v-else-if="stats.percentage < 60">进展不错，保持学习节奏！</span>
            <span v-else-if="stats.percentage < 90">已经掌握大部分内容，再接再厉！</span>
            <span v-else-if="stats.percentage < 100">即将完成全部学习，最后冲刺！</span>
            <span v-else>恭喜你完成所有模块学习！🎉</span>
          </div>
        </a-card>
      </a-col>

      <!-- 模块列表 -->
      <a-col :span="24">
        <a-card title="学习记录" :bordered="false" class="list-card">
          <template #extra>
            <a-space>
              <a-button type="link" @click="loadProgressList">
                <reload-outlined /> 刷新
              </a-button>
            </a-space>
          </template>
          <a-spin :spinning="loading">
            <a-empty v-if="progressList.length === 0" description="暂无学习记录，开始你的第一个漏洞学习吧！">
              <a-button type="primary" @click="$router.push('/home')">
                <rocket-outlined /> 开始学习
              </a-button>
            </a-empty>
            <a-list v-else :data-source="progressList" :pagination="{ pageSize: 10, showSizeChanger: true, showTotal: (total) => `共 ${total} 条记录` }">
              <template #renderItem="{ item, index }">
                <a-list-item class="list-item-animated" :style="{ animationDelay: `${index * 0.05}s` }">
                  <a-list-item-meta>
                    <template #avatar>
                      <a-avatar :style="{ backgroundColor: item.completed ? '#52c41a' : '#d9d9d9', fontSize: '18px' }">
                        <template #icon>
                          <check-outlined v-if="item.completed" />
                          <clock-circle-outlined v-else />
                        </template>
                      </a-avatar>
                    </template>
                    <template #title>
                      <span :style="{ color: item.completed ? '#52c41a' : '#595959', fontWeight: 600 }">
                        {{ getModuleName(item.vulnModule) }}
                      </span>
                    </template>
                    <template #description>
                      <a-space>
                        <span v-if="item.completed">
                          <calendar-outlined style="margin-right: 4px;" />
                          完成时间：{{ formatDate(item.completedAt) }}
                        </span>
                        <span v-else style="color: #8c8c8c;">
                          <clock-circle-outlined style="margin-right: 4px;" />
                          未完成
                        </span>
                      </a-space>
                    </template>
                  </a-list-item-meta>
                  <template #actions>
                    <a-tag :color="item.completed ? 'success' : 'default'" style="font-size: 13px; padding: 4px 12px;">
                      {{ item.completed ? '✓ 已完成' : '○ 未完成' }}
                    </a-tag>
                  </template>
                </a-list-item>
              </template>
            </a-list>
          </a-spin>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  TrophyOutlined,
  CheckCircleOutlined,
  FireOutlined,
  PercentageOutlined,
  CheckOutlined,
  ClockCircleOutlined,
  DatabaseOutlined,
  RocketOutlined,
  BulbOutlined,
  ReloadOutlined,
  CalendarOutlined
} from '@ant-design/icons-vue'
import axios from 'axios'

const loading = ref(false)
const stats = ref({
  completedCount: 0,
  totalCount: 25, // 实际漏洞模块总数
  percentage: 0
})
const progressList = ref([])

// 模块名称映射
const moduleNameMap = {
  'xss_reflect': 'XSS反射型',
  'xss_store': 'XSS存储型',
  'xss_dom': 'XSS DOM型',
  'sql_int': 'SQL注入-整型',
  'sql_error': 'SQL注入-报错',
  'sql_time': 'SQL注入-时间盲注',
  'sql_bool': 'SQL注入-布尔盲注',
  'mybatis_orderby': 'MyBatis-OrderBy注入',
  'mybatis_in': 'MyBatis-In注入',
  'rce_runtime': 'RCE-Runtime',
  'rce_processbuilder': 'RCE-ProcessBuilder',
  'rce_processimpl': 'RCE-ProcessImpl',
  'rce_loadjs': 'RCE-LoadJs',
  'rce_groovy': 'RCE-Groovy',
  'file_upload': '文件上传',
  'file_download': '文件下载',
  'xxe_xmlreader': 'XXE-XMLReader',
  'xxe_saxreader': 'XXE-SAXReader',
  'xxe_saxbuilder': 'XXE-SAXBuilder',
  'xxe_documentbuilder': 'XXE-DocumentBuilder',
  'xxe_unmarshaller': 'XXE-Unmarshaller',
  'ssrf': 'SSRF',
  'xff': 'XFF伪造',
  'cors': 'CORS跨域',
  'redirect': 'URL重定向',
  'unauth': '未授权访问',
  'fastjson': 'Fastjson反序列化',
  'jackson': 'Jackson反序列化',
  'shiro': 'Shiro反序列化',
  'log4j': 'Log4j RCE',
  'xstream': 'XStream反序列化',
  'actuators': 'Actuator信息泄露',
  'jolokia': 'Jolokia RCE',
  'readobject': 'ReadObject反序列化',
  'rmi': 'RMI反序列化',
  'xmldecoder': 'XMLDecoder反序列化',
  'yaml': 'SnakeYaml反序列化'
}

const getModuleName = (moduleKey) => {
  return moduleNameMap[moduleKey] || moduleKey
}

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

const getProgressColor = (percentage) => {
  if (percentage < 30) return 'red'
  if (percentage < 60) return 'orange'
  if (percentage < 90) return 'blue'
  if (percentage < 100) return 'cyan'
  return 'green'
}

const getProgressLabel = (percentage) => {
  if (percentage < 30) return '初学者'
  if (percentage < 60) return '进阶中'
  if (percentage < 90) return '熟练者'
  if (percentage < 100) return '精通者'
  return '大师级'
}

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

    console.log('进度列表原始数据:', result)

    if (result.success) {
      progressList.value = result.data || []
      console.log('进度列表处理后:', progressList.value)
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

onMounted(() => {
  loadProgressStats()
  loadProgressList()
})
</script>

<style scoped>
.progress-container {
  padding: 24px;
}

.progress-header {
  display: flex;
  align-items: center;
  padding: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.3);
}

.header-background {
  position: absolute;
  top: -50%;
  right: -10%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulse 4s ease-in-out infinite;
}

.header-icon {
  font-size: 80px;
  margin-right: 32px;
  opacity: 0.95;
  position: relative;
  z-index: 1;
  animation: pulse 2s ease-in-out infinite;
}

.header-content {
  position: relative;
  z-index: 1;
}

.header-content h1 {
  margin: 0;
  font-size: 36px;
  font-weight: 700;
  color: white;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-content p {
  margin: 12px 0 0;
  font-size: 16px;
  opacity: 0.95;
  color: white;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 28px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transform: scaleX(0);
  transition: transform 0.4s ease;
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.stat-card:hover::before {
  transform: scaleX(1);
}

.stat-icon {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: white;
  margin-right: 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1) rotate(5deg);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 40px;
  font-weight: 700;
  color: #262626;
  line-height: 1;
  margin-bottom: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-label {
  font-size: 15px;
  color: #8c8c8c;
  font-weight: 500;
}

.stat-badge {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  opacity: 0.2;
  transition: all 0.3s ease;
}

.stat-card:hover .stat-badge {
  opacity: 0.4;
  transform: rotate(15deg);
}

.stat-badge.success {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
}

.stat-badge.total {
  background: linear-gradient(135deg, #f5222d 0%, #ff7875 100%);
}

.stat-badge.percentage {
  background: linear-gradient(135deg, #1890ff 0%, #69c0ff 100%);
}

.progress-card {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border-radius: 16px;
  overflow: hidden;
}

.progress-text {
  margin-top: 20px;
  text-align: center;
  color: #595959;
  font-size: 16px;
  font-weight: 500;
}

.progress-text .highlight {
  color: #667eea;
  font-size: 20px;
  font-weight: 700;
  margin: 0 4px;
}

.progress-tips {
  margin-top: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #fff7e6 0%, #fffbe6 100%);
  border-radius: 12px;
  text-align: center;
  color: #d48806;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid #ffe58f;
}

.list-card {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border-radius: 16px;
}

.list-item-animated {
  animation: fadeIn 0.5s ease-out;
  transition: all 0.3s ease;
}

:deep(.ant-card) {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

:deep(.ant-card-head) {
  border-bottom: 2px solid #f0f0f0;
  font-weight: 600;
  font-size: 18px;
  padding: 20px 24px;
}

:deep(.ant-card-body) {
  padding: 24px;
}

:deep(.ant-list-item) {
  padding: 20px 0;
  transition: all 0.3s ease;
  border-radius: 12px;
}

:deep(.ant-list-item:hover) {
  background: linear-gradient(135deg, #f6f9ff 0%, #f0f5ff 100%);
  padding-left: 20px;
  padding-right: 20px;
  transform: translateX(4px);
}

:deep(.ant-progress-inner) {
  border-radius: 12px;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.05);
}

:deep(.ant-progress-bg) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

:deep(.ant-empty) {
  padding: 40px 0;
}

:deep(.ant-avatar) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
  .progress-header {
    flex-direction: column;
    text-align: center;
    padding: 32px 24px;
  }

  .header-icon {
    margin-right: 0;
    margin-bottom: 20px;
    font-size: 64px;
  }

  .header-content h1 {
    font-size: 28px;
  }

  .stat-card {
    padding: 20px;
  }

  .stat-icon {
    width: 56px;
    height: 56px;
    font-size: 28px;
  }

  .stat-value {
    font-size: 32px;
  }
}
</style>
