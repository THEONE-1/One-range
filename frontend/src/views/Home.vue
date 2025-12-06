<template>
  <div class="home-container">
    <a-row :gutter="[24, 24]">
      <!-- 欢迎Banner -->
      <a-col :span="24">
        <div class="welcome-banner">
          <div class="banner-content">
            <div class="banner-icon">
              <security-scan-outlined />
            </div>
            <div class="banner-text">
              <h1 class="banner-title">欢迎来到 One 安全靶场</h1>
              <p class="banner-subtitle">专业的Java Web安全漏洞学习与实战平台</p>
              <div class="banner-stats">
                <div class="stat-item">
                  <div class="stat-number">39+</div>
                  <div class="stat-label">漏洞场景</div>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <div class="stat-number">8+</div>
                  <div class="stat-label">组件漏洞</div>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <div class="stat-number">100%</div>
                  <div class="stat-label">真实环境</div>
                </div>
              </div>
            </div>
          </div>
          <div class="banner-alert">
            <exclamation-circle-outlined style="margin-right: 8px;" />
            本平台仅供安全学习和研究使用，请勿用于非法用途
          </div>
        </div>
      </a-col>

      <!-- 快捷入口 -->
      <a-col :span="24">
        <div class="section-header">
          <h2>
            <fire-outlined style="color: #ff4d4f; margin-right: 8px;" />
            热门漏洞快速入口
          </h2>
          <p>选择一个漏洞类型开始你的安全学习之旅</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col :xs="12" :sm="8" :md="6" :lg="4" v-for="item in quickLinks" :key="item.key">
            <div class="quick-link-card" @click="router.push(item.path)">
              <div class="quick-link-icon" :style="{ background: item.gradient }">
                <component :is="item.icon" />
              </div>
              <div class="quick-link-title">{{ item.title }}</div>
              <div class="quick-link-badge">{{ item.badge }}</div>
            </div>
          </a-col>
        </a-row>
      </a-col>

      <!-- 漏洞类型统计卡片 -->
      <a-col :xs="24" :sm="8" v-for="stat in vulnStats" :key="stat.name">
        <div class="stat-card" :style="{ borderTopColor: stat.color }">
          <div class="stat-card-icon" :style="{ background: stat.gradient }">
            <component :is="stat.icon" />
          </div>
          <div class="stat-card-info">
            <div class="stat-card-count">{{ stat.count }}</div>
            <div class="stat-card-label">{{ stat.name }}</div>
          </div>
        </div>
      </a-col>

      <!-- 功能特性 -->
      <a-col :span="24">
        <div class="section-header">
          <h2>
            <star-outlined style="color: #faad14; margin-right: 8px;" />
            平台特性
          </h2>
          <p>涵盖主流Web安全漏洞和Java组件安全</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :md="8" v-for="feature in features" :key="feature.title">
            <div class="feature-card">
              <div class="feature-icon" :style="{ background: feature.gradient }">
                <component :is="feature.icon" />
              </div>
              <h3 class="feature-title">{{ feature.title }}</h3>
              <ul class="feature-list">
                <li v-for="item in feature.items" :key="item">
                  <check-circle-outlined style="color: #52c41a; margin-right: 8px;" />
                  {{ item }}
                </li>
              </ul>
            </div>
          </a-col>
        </a-row>
      </a-col>
      
      <!-- 系统信息 -->
      <a-col :span="24">
        <div class="info-card">
          <a-row :gutter="16">
            <a-col :xs="24" :sm="12" :md="6" v-for="info in systemInfo" :key="info.label">
              <div class="info-item">
                <div class="info-icon">
                  <component :is="info.icon" />
                </div>
                <div class="info-content">
                  <div class="info-label">{{ info.label }}</div>
                  <div class="info-value">{{ info.value }}</div>
                </div>
              </div>
            </a-col>
          </a-row>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  SecurityScanOutlined,
  ExclamationCircleOutlined,
  FireOutlined,
  StarOutlined,
  CheckCircleOutlined,
  BugOutlined,
  AppstoreOutlined,
  CodeOutlined,
  DatabaseOutlined,
  ThunderboltOutlined,
  CloudUploadOutlined,
  FileTextOutlined,
  GlobalOutlined,
  SafetyOutlined,
  UserOutlined,
  RocketOutlined,
  CloudServerOutlined,
  ApiOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const username = computed(() => localStorage.getItem('username') || 'Admin')

const quickLinks = [
  { key: '1', title: 'XSS', path: '/xss/reflect', icon: BugOutlined, gradient: 'linear-gradient(135deg, #f5222d 0%, #ff7875 100%)', badge: '高危' },
  { key: '2', title: 'SQL注入', path: '/sqli/jdbc_int_based', icon: DatabaseOutlined, gradient: 'linear-gradient(135deg, #fa541c 0%, #ff9c6e 100%)', badge: '高危' },
  { key: '3', title: 'RCE', path: '/rce/runtime', icon: ThunderboltOutlined, gradient: 'linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%)', badge: '严重' },
  { key: '4', title: '文件上传', path: '/file/upload', icon: CloudUploadOutlined, gradient: 'linear-gradient(135deg, #faad14 0%, #ffd666 100%)', badge: '中危' },
  { key: '5', title: 'XXE', path: '/xxe/xmlreader', icon: FileTextOutlined, gradient: 'linear-gradient(135deg, #52c41a 0%, #95de64 100%)', badge: '高危' },
  { key: '6', title: 'SSRF', path: '/ssrf', icon: GlobalOutlined, gradient: 'linear-gradient(135deg, #13c2c2 0%, #5cdbd3 100%)', badge: '高危' },
  { key: '7', title: 'Fastjson', path: '/fastjson', icon: ApiOutlined, gradient: 'linear-gradient(135deg, #1890ff 0%, #69c0ff 100%)', badge: '严重' },
  { key: '8', title: 'Shiro', path: '/shiro', icon: SafetyOutlined, gradient: 'linear-gradient(135deg, #2f54eb 0%, #85a5ff 100%)', badge: '严重' }
]

const vulnStats = [
  { name: '基础漏洞', count: 28, icon: BugOutlined, color: '#f5222d', gradient: 'linear-gradient(135deg, #f5222d 0%, #ff7875 100%)' },
  { name: '组件漏洞', count: 7, icon: AppstoreOutlined, color: '#fa8c16', gradient: 'linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%)' },
  { name: '反序列化', count: 4, icon: CodeOutlined, color: '#1890ff', gradient: 'linear-gradient(135deg, #1890ff 0%, #69c0ff 100%)' }
]

const features = [
  {
    title: '基础Web漏洞',
    icon: BugOutlined,
    gradient: 'linear-gradient(135deg, #f5222d 0%, #ff7875 100%)',
    items: ['XSS跨站脚本', 'SQL注入攻击', '远程代码执行', 'XXE/SSRF/SSTI']
  },
  {
    title: 'Java组件漏洞',
    icon: AppstoreOutlined,
    gradient: 'linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%)',
    items: ['Fastjson RCE', 'Shiro反序列化', 'Log4j漏洞', 'Spring组件']
  },
  {
    title: 'Java安全技术',
    icon: CodeOutlined,
    gradient: 'linear-gradient(135deg, #1890ff 0%, #69c0ff 100%)',
    items: ['Java反序列化', 'RMI远程调用', 'JNDI注入', '表达式注入']
  }
]

const systemInfo = [
  { label: '靶场版本', value: 'v2.0.0', icon: RocketOutlined },
  { label: '前端框架', value: 'Vue 3', icon: CodeOutlined },
  { label: '后端框架', value: 'Spring Boot', icon: CloudServerOutlined },
  { label: '当前用户', value: username.value, icon: UserOutlined }
]
</script>

<style scoped>
.home-container {
  width: 100%;
}

/* 欢迎Banner */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 48px;
  color: #fff;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
  position: relative;
  overflow: hidden;
}

.welcome-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  border-radius: 50%;
}

.banner-content {
  display: flex;
  align-items: center;
  gap: 32px;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.banner-icon {
  width: 100px;
  height: 100px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  flex-shrink: 0;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.banner-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #fff;
}

.banner-subtitle {
  font-size: 18px;
  margin: 0 0 24px 0;
  color: rgba(255, 255, 255, 0.9);
}

.banner-stats {
  display: flex;
  gap: 24px;
  align-items: center;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.3);
}

.banner-alert {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 8px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.95);
  position: relative;
  z-index: 1;
}

/* 分区标题 */
.section-header {
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.section-header p {
  margin: 0;
  color: #95a5a6;
  font-size: 14px;
}

/* 快捷链接卡片 */
.quick-link-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

.quick-link-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--gradient);
  opacity: 0;
  transition: opacity 0.3s;
}

.quick-link-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}

.quick-link-card:hover::before {
  opacity: 1;
}

.quick-link-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #fff;
}

.quick-link-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.quick-link-badge {
  display: inline-block;
  padding: 2px 12px;
  font-size: 12px;
  background: #f0f0f0;
  color: #666;
  border-radius: 12px;
}

/* 统计卡片 */
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-top: 4px solid;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-card-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #fff;
  flex-shrink: 0;
}

.stat-card-count {
  font-size: 36px;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-card-label {
  font-size: 14px;
  color: #95a5a6;
}

/* 特性卡片 */
.feature-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  height: 100%;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.feature-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #fff;
  margin-bottom: 20px;
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 16px 0;
}

.feature-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.feature-list li {
  padding: 8px 0;
  color: #5a6c7d;
  font-size: 14px;
  display: flex;
  align-items: center;
}

/* 信息卡片 */
.info-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.info-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.info-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}

.info-label {
  font-size: 12px;
  color: #95a5a6;
  margin-bottom: 4px;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

@media (max-width: 768px) {
  .welcome-banner {
    padding: 32px 24px;
  }
  
  .banner-content {
    flex-direction: column;
    text-align: center;
  }
  
  .banner-title {
    font-size: 28px;
  }
  
  .banner-stats {
    justify-content: center;
  }
}
</style>

