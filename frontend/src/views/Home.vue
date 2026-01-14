<template>
  <div class="home-container fade-in">
    <a-row :gutter="[24, 24]">
      <!-- 欢迎Banner -->
      <a-col :span="24">
        <div class="welcome-banner">
          <div class="banner-background"></div>
          <div class="banner-content">
            <div class="banner-icon">
              <security-scan-outlined />
            </div>
            <div class="banner-text">
              <h1 class="banner-title">欢迎来到 One 安全靶场</h1>
              <p class="banner-subtitle">专业的Java Web安全漏洞学习与实战平台</p>
              <div class="banner-stats">
                <div class="stat-item">
                  <div class="stat-number">25</div>
                  <div class="stat-label">漏洞场景</div>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <div class="stat-number">6</div>
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
          <a-col :xs="12" :sm="8" :md="6" :lg="4" v-for="(item, index) in quickLinks" :key="item.key">
            <div class="quick-link-card slide-in-left" :style="{ animationDelay: `${index * 0.05}s` }" @click="router.push(item.path)">
              <div class="quick-link-icon" :style="{ background: item.gradient }">
                <component :is="item.icon" />
              </div>
              <div class="quick-link-title">{{ item.title }}</div>
              <div class="quick-link-badge" :class="getBadgeClass(item.badge)">{{ item.badge }}</div>
            </div>
          </a-col>
        </a-row>
      </a-col>

      <!-- 漏洞类型统计卡片 -->
      <a-col :xs="24" :sm="8" v-for="(stat, index) in vulnStats" :key="stat.name">
        <div class="stat-card slide-in-right" :style="{ borderTopColor: stat.color, animationDelay: `${index * 0.1}s` }">
          <div class="stat-card-icon" :style="{ background: stat.gradient }">
            <component :is="stat.icon" />
          </div>
          <div class="stat-card-info">
            <div class="stat-card-count">{{ stat.count }}</div>
            <div class="stat-card-label">{{ stat.name }}</div>
          </div>
          <div class="stat-card-badge">
            <arrow-right-outlined />
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
          <a-col :xs="24" :md="8" v-for="(feature, index) in features" :key="feature.title">
            <div class="feature-card fade-in" :style="{ animationDelay: `${index * 0.15}s` }">
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
            <a-col :xs="24" :sm="12" :md="6" v-for="(info, index) in systemInfo" :key="info.label">
              <div class="info-item fade-in" :style="{ animationDelay: `${index * 0.1}s` }">
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
  ApiOutlined,
  ArrowRightOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const username = computed(() => localStorage.getItem('username') || 'Admin')

const getBadgeClass = (badge) => {
  const badgeMap = {
    '严重': 'badge-critical',
    '高危': 'badge-high',
    '中危': 'badge-medium',
    '低危': 'badge-low'
  }
  return badgeMap[badge] || 'badge-default'
}

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
  { name: '基础漏洞', count: 19, icon: BugOutlined, color: '#f5222d', gradient: 'linear-gradient(135deg, #f5222d 0%, #ff7875 100%)' },
  { name: '组件漏洞', count: 6, icon: AppstoreOutlined, color: '#fa8c16', gradient: 'linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%)' },
  { name: '反序列化', count: 1, icon: CodeOutlined, color: '#1890ff', gradient: 'linear-gradient(135deg, #1890ff 0%, #69c0ff 100%)' }
]

const features = [
  {
    title: '基础Web漏洞',
    icon: BugOutlined,
    gradient: 'linear-gradient(135deg, #f5222d 0%, #ff7875 100%)',
    items: ['XSS跨站脚本', 'SQL注入攻击', '远程代码执行', 'XXE/SSRF/CORS']
  },
  {
    title: 'Java组件漏洞',
    icon: AppstoreOutlined,
    gradient: 'linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%)',
    items: ['Fastjson RCE', 'Shiro反序列化', 'Log4j漏洞', 'Actuator信息泄露']
  },
  {
    title: 'Java安全技术',
    icon: CodeOutlined,
    gradient: 'linear-gradient(135deg, #1890ff 0%, #69c0ff 100%)',
    items: ['Java反序列化', 'Jackson漏洞', 'Jolokia漏洞', '文件上传下载']
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
  border-radius: 20px;
  padding: 56px;
  color: #fff;
  box-shadow: 0 12px 48px rgba(102, 126, 234, 0.35);
  position: relative;
  overflow: hidden;
}

.banner-background {
  position: absolute;
  top: -50%;
  right: -10%;
  width: 700px;
  height: 700px;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulse 6s ease-in-out infinite;
}

.welcome-banner::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -10%;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulse 8s ease-in-out infinite;
}

.banner-content {
  display: flex;
  align-items: center;
  gap: 40px;
  margin-bottom: 28px;
  position: relative;
  z-index: 1;
}

.banner-icon {
  width: 120px;
  height: 120px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
  flex-shrink: 0;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
  animation: pulse 3s ease-in-out infinite;
}

.banner-title {
  font-size: 42px;
  font-weight: 800;
  margin: 0 0 12px 0;
  color: #fff;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  letter-spacing: -0.5px;
}

.banner-subtitle {
  font-size: 19px;
  margin: 0 0 28px 0;
  color: rgba(255, 255, 255, 0.95);
  font-weight: 400;
}

.banner-stats {
  display: flex;
  gap: 32px;
  align-items: center;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 38px;
  font-weight: 800;
  color: #fff;
  line-height: 1;
  margin-bottom: 10px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 500;
}

.stat-divider {
  width: 2px;
  height: 48px;
  background: rgba(255, 255, 255, 0.25);
}

.banner-alert {
  display: flex;
  align-items: center;
  padding: 14px 24px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.98);
  position: relative;
  z-index: 1;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* 分区标题 */
.section-header {
  margin-bottom: 28px;
}

.section-header h2 {
  font-size: 26px;
  font-weight: 700;
  margin: 0 0 10px 0;
  color: #2c3e50;
  display: flex;
  align-items: center;
}

.section-header p {
  margin: 0;
  color: #95a5a6;
  font-size: 15px;
}

/* 快捷链接卡片 */
.quick-link-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
  border: 2px solid transparent;
}

.quick-link-card::before {
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

.quick-link-card:hover {
  transform: translateY(-10px) scale(1.02);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.18);
  border-color: rgba(102, 126, 234, 0.2);
}

.quick-link-card:hover::before {
  transform: scaleX(1);
}

.quick-link-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 18px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;
}

.quick-link-card:hover .quick-link-icon {
  transform: scale(1.15) rotate(5deg);
}

.quick-link-title {
  font-size: 17px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 10px;
}

.quick-link-badge {
  display: inline-block;
  padding: 4px 14px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 14px;
  transition: all 0.3s ease;
}

.badge-critical {
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
}

.badge-high {
  background: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(250, 140, 22, 0.3);
}

.badge-medium {
  background: linear-gradient(135deg, #faad14 0%, #ffd666 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(250, 173, 20, 0.3);
}

.badge-low {
  background: linear-gradient(135deg, #52c41a 0%, #95de64 100%);
  color: #fff;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

.quick-link-card:hover .quick-link-badge {
  transform: scale(1.1);
}

/* 统计卡片 */
.stat-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  display: flex;
  align-items: center;
  gap: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border-top: 5px solid;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.stat-card::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.05) 0%, transparent 70%);
  border-radius: 50%;
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.stat-card-icon {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;
}

.stat-card:hover .stat-card-icon {
  transform: scale(1.1) rotate(-5deg);
}

.stat-card-info {
  flex: 1;
}

.stat-card-count {
  font-size: 42px;
  font-weight: 800;
  color: #2c3e50;
  line-height: 1;
  margin-bottom: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-card-label {
  font-size: 15px;
  color: #95a5a6;
  font-weight: 600;
}

.stat-card-badge {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  opacity: 0.15;
  transition: all 0.3s ease;
}

.stat-card:hover .stat-card-badge {
  opacity: 0.3;
  transform: translateX(8px);
}

/* 特性卡片 */
.feature-card {
  background: #fff;
  border-radius: 16px;
  padding: 36px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  height: 100%;
  border: 2px solid transparent;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  border-color: rgba(102, 126, 234, 0.2);
}

.feature-icon {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: #fff;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s ease;
}

.feature-card:hover .feature-icon {
  transform: scale(1.1) rotate(-5deg);
}

.feature-title {
  font-size: 20px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 20px 0;
}

.feature-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.feature-list li {
  padding: 10px 0;
  color: #5a6c7d;
  font-size: 15px;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
}

.feature-list li:hover {
  color: #2c3e50;
  transform: translateX(4px);
}

/* 信息卡片 */
.info-card {
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.info-item {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #f0f2f5 100%);
  border-radius: 12px;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.info-item:hover {
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%);
  border-color: rgba(102, 126, 234, 0.2);
  transform: translateY(-2px);
}

.info-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.3);
  transition: transform 0.3s ease;
}

.info-item:hover .info-icon {
  transform: scale(1.1) rotate(5deg);
}

.info-label {
  font-size: 13px;
  color: #95a5a6;
  margin-bottom: 6px;
  font-weight: 500;
}

.info-value {
  font-size: 16px;
  font-weight: 700;
  color: #2c3e50;
}

@media (max-width: 768px) {
  .welcome-banner {
    padding: 40px 28px;
  }

  .banner-content {
    flex-direction: column;
    text-align: center;
    gap: 24px;
  }

  .banner-icon {
    width: 96px;
    height: 96px;
    font-size: 48px;
  }

  .banner-title {
    font-size: 32px;
  }

  .banner-subtitle {
    font-size: 16px;
  }

  .banner-stats {
    justify-content: center;
    flex-wrap: wrap;
  }

  .stat-number {
    font-size: 32px;
  }
}
</style>

