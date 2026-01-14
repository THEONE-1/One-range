<template>
  <div class="login-container">
    <!-- 左侧信息展示区 -->
    <div class="left-panel">
      <div class="matrix-bg"></div>
      <div class="glow-orbs">
        <div class="orb orb-1"></div>
        <div class="orb orb-2"></div>
        <div class="orb orb-3"></div>
        <div class="orb orb-4"></div>
        <div class="orb orb-5"></div>
      </div>

      <div class="panel-content">
        <div class="brand-section">
          <div class="brand-icon">
            <security-scan-outlined :style="{ fontSize: '64px' }" />
          </div>
          <h1 class="brand-title">One 安全靶场</h1>
          <p class="brand-subtitle">ONE SECURITY RANGE</p>
          <div class="brand-divider"></div>
          <p class="brand-desc">专业的Web安全漏洞实验平台</p>
        </div>

        <div class="features-section">
          <div class="feature-item">
            <bug-outlined class="feature-icon" />
            <span>真实漏洞环境</span>
          </div>
          <div class="feature-item">
            <code-outlined class="feature-icon" />
            <span>源码级别学习</span>
          </div>
          <div class="feature-item">
            <safety-outlined class="feature-icon" />
            <span>安全防护实践</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录表单区 -->
    <div class="right-panel">
      <div class="login-box">
        <div class="login-header">
          <h2>欢迎回来</h2>
          <p>登录您的账户开始学习</p>
        </div>

        <a-alert
          v-if="errorMsg"
          :message="errorMsg"
          type="error"
          show-icon
          closable
          @close="errorMsg = ''"
          style="margin-bottom: 24px"
        />

        <a-form
          :model="formState"
          @finish="handleLogin"
          class="login-form"
        >
          <a-form-item
            name="username"
            :rules="[{ required: true, message: '请输入用户名' }]"
          >
            <a-input
              v-model:value="formState.username"
              size="large"
              placeholder="请输入用户名"
            >
              <template #prefix>
                <user-outlined class="input-icon" />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item
            name="password"
            :rules="[{ required: true, message: '请输入密码' }]"
          >
            <a-input-password
              v-model:value="formState.password"
              size="large"
              placeholder="请输入密码"
            >
              <template #prefix>
                <lock-outlined class="input-icon" />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item
            name="captcha"
            :rules="[{ required: true, message: '请输入验证码' }]"
          >
            <div class="captcha-row">
              <a-input
                v-model:value="formState.captcha"
                size="large"
                placeholder="请输入验证码"
                style="flex: 1"
              >
                <template #prefix>
                  <safety-certificate-outlined class="input-icon" />
                </template>
              </a-input>
              <div class="captcha-wrapper">
                <img
                  :src="captchaUrl"
                  @click="refreshCaptcha"
                  class="captcha-img"
                  alt="验证码"
                />
                <div class="captcha-refresh" @click="refreshCaptcha">
                  <reload-outlined />
                </div>
              </div>
            </div>
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              block
              :loading="loading"
              class="login-btn"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>登录中...</span>
            </a-button>
          </a-form-item>

          <div class="form-footer">
            <a-divider style="margin: 16px 0">
              <span style="color: #999; font-size: 12px">其他选项</span>
            </a-divider>
            <div class="footer-links">
              <a @click="goToRegister" class="register-link">
                <user-add-outlined />
                <span>还没有账号？立即注册</span>
              </a>
            </div>
          </div>
        </a-form>

        <div class="security-notice">
          <warning-outlined style="margin-right: 8px; color: #faad14" />
          <span>本平台仅供安全学习使用，请勿用于非法用途</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  UserOutlined,
  LockOutlined,
  SafetyCertificateOutlined,
  SecurityScanOutlined,
  BugOutlined,
  CodeOutlined,
  SafetyOutlined,
  ReloadOutlined,
  UserAddOutlined,
  WarningOutlined
} from '@ant-design/icons-vue'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')
const captchaUrl = ref('/captcha?t=' + Date.now())

const formState = reactive({
  username: '',
  password: '',
  captcha: ''
})

const refreshCaptcha = () => {
  captchaUrl.value = '/captcha?t=' + Date.now()
}

const handleLogin = async (values) => {
  loading.value = true
  errorMsg.value = ''

  try {
    const params = new URLSearchParams()
    params.append('username', formState.username)
    params.append('password', formState.password)
    params.append('captcha', formState.captcha)

    const response = await axios.post('/admin/login', params, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })

    // 后端返回的是JSON字符串,需要解析
    let result = response.data
    if (typeof result === 'string') {
      try {
        result = JSON.parse(result)
      } catch {
        // 如果不是JSON,检查是否包含成功标识
        if (result.includes('成功') || result.includes('success')) {
          result = { success: true }
        } else {
          result = { success: false, message: result }
        }
      }
    }

    if (result.success === true || result.success === 'true') {
      localStorage.setItem('username', formState.username)
      message.success('登录成功')
      router.push('/home')
    } else {
      errorMsg.value = result.message || '登录失败，请检查用户名、密码和验证码'
      refreshCaptcha()
    }
  } catch (error) {
    console.error('登录错误:', error)
    errorMsg.value = error.response?.data?.message || error.response?.data || '登录失败，请重试'
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  router.push('/register')
}

onMounted(() => {
  // 初始化粒子背景（可选）
})
</script>

<style scoped>
/* 主容器 */
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  background: #0a0e27;
  overflow: hidden;
}

/* 左侧面板 */
.left-panel {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f1729 0%, #1a1f3a 50%, #0f1729 100%);
  overflow: hidden;
}

/* 矩阵背景效果 */
.matrix-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
    linear-gradient(rgba(0, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  animation: matrix-move 20s linear infinite;
}

@keyframes matrix-move {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(50px);
  }
}

/* 光晕闪烁效果 */
.glow-orbs {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
}

.orb {
  position: absolute;
  width: 4px;
  height: 4px;
  background: rgba(0, 212, 255, 0.8);
  border-radius: 50%;
  box-shadow: 0 0 20px 4px rgba(0, 212, 255, 0.6);
  animation: pulse-orb 3s ease-in-out infinite;
}

.orb-1 {
  top: 15%;
  left: 20%;
  animation-delay: 0s;
}

.orb-2 {
  top: 45%;
  left: 75%;
  animation-delay: 0.8s;
}

.orb-3 {
  top: 70%;
  left: 30%;
  animation-delay: 1.6s;
}

.orb-4 {
  top: 25%;
  left: 85%;
  animation-delay: 2.4s;
}

.orb-5 {
  top: 85%;
  left: 65%;
  animation-delay: 1.2s;
}

@keyframes pulse-orb {
  0%, 100% {
    opacity: 0.2;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.5);
  }
}

/* 面板内容 */
.panel-content {
  position: relative;
  z-index: 1;
  padding: 60px;
  max-width: 600px;
}

/* 品牌区域 */
.brand-section {
  text-align: center;
  margin-bottom: 80px;
}

.brand-icon {
  display: inline-block;
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #00d4ff 0%, #0099ff 100%);
  border-radius: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 30px;
  box-shadow:
    0 0 40px rgba(0, 212, 255, 0.4),
    0 0 80px rgba(0, 212, 255, 0.2),
    inset 0 0 40px rgba(255, 255, 255, 0.1);
  animation: pulse-glow 3s ease-in-out infinite;
  color: #fff;
}

@keyframes pulse-glow {
  0%, 100% {
    box-shadow:
      0 0 40px rgba(0, 212, 255, 0.4),
      0 0 80px rgba(0, 212, 255, 0.2),
      inset 0 0 40px rgba(255, 255, 255, 0.1);
  }
  50% {
    box-shadow:
      0 0 60px rgba(0, 212, 255, 0.6),
      0 0 120px rgba(0, 212, 255, 0.3),
      inset 0 0 60px rgba(255, 255, 255, 0.2);
  }
}

.brand-title {
  font-size: 48px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 10px 0;
  text-shadow: 0 0 20px rgba(0, 212, 255, 0.5);
  letter-spacing: 2px;
}

.brand-subtitle {
  font-size: 16px;
  color: #00d4ff;
  letter-spacing: 4px;
  margin: 0 0 20px 0;
  font-weight: 500;
}

.brand-divider {
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00d4ff, transparent);
  margin: 20px auto;
  box-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
}

.brand-desc {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
  line-height: 1.6;
}

/* 特性区域 */
.features-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: rgba(0, 212, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(0, 212, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
  font-size: 16px;
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(0, 212, 255, 0.1);
  border-color: rgba(0, 212, 255, 0.3);
  transform: translateX(10px);
}

.feature-icon {
  font-size: 24px;
  color: #00d4ff;
}

/* 右侧面板 */
.right-panel {
  width: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 41, 0.95);
  backdrop-filter: blur(20px);
  box-shadow: -10px 0 50px rgba(0, 0, 0, 0.5);
  position: relative;
  z-index: 2;
  border-left: 1px solid rgba(0, 212, 255, 0.2);
}

/* 登录框 */
.login-box {
  width: 100%;
  max-width: 400px;
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 10px 0;
  text-shadow: 0 2px 10px rgba(0, 212, 255, 0.3);
}

.login-header p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

/* 表单样式 */
.login-form {
  margin-top: 24px;
}

.input-icon {
  color: #00d4ff;
}

.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-wrapper {
  position: relative;
  flex-shrink: 0;
}

.captcha-img {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: 8px;
  border: 2px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s;
  display: block;
  background: rgba(255, 255, 255, 0.05);
}

.captcha-img:hover {
  border-color: #00d4ff;
}

.captcha-refresh {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 32px;
  height: 32px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  opacity: 0;
  transition: all 0.3s;
}

.captcha-wrapper:hover .captcha-refresh {
  opacity: 1;
}

.captcha-refresh:hover {
  background: rgba(0, 212, 255, 0.8);
  transform: translate(-50%, -50%) rotate(180deg);
}

/* 登录按钮 */
.login-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #00d4ff 0%, #0099ff 100%);
  border: none;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 212, 255, 0.3);
  transition: all 0.3s;
}

.login-btn:hover {
  background: linear-gradient(135deg, #00bbff 0%, #0088ff 100%);
  box-shadow: 0 6px 20px rgba(0, 212, 255, 0.4);
  transform: translateY(-2px);
}

.login-btn:active {
  transform: translateY(0);
}

/* 表单底部 */
.form-footer {
  margin-top: 24px;
}

.footer-links {
  text-align: center;
}

.register-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #00d4ff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
}

.register-link:hover {
  color: #0099ff;
  text-decoration: underline;
}

/* 安全提示 */
.security-notice {
  margin-top: 30px;
  padding: 12px 16px;
  background: rgba(255, 251, 230, 0.1);
  border: 1px solid rgba(255, 229, 143, 0.3);
  border-radius: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  line-height: 1.5;
}

/* 输入框样式优化 */
:deep(.ant-input-affix-wrapper),
:deep(.ant-input),
:deep(.ant-input-password) {
  border-radius: 8px;
  border: 2px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  transition: all 0.3s;
}

:deep(.ant-input::placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

:deep(.ant-input-affix-wrapper:focus),
:deep(.ant-input-affix-wrapper-focused),
:deep(.ant-input:focus),
:deep(.ant-input-password:focus) {
  border-color: #00d4ff;
  background: rgba(0, 212, 255, 0.05);
  box-shadow: 0 0 0 2px rgba(0, 212, 255, 0.2);
}

:deep(.ant-input-affix-wrapper:hover),
:deep(.ant-input:hover) {
  border-color: rgba(0, 212, 255, 0.5);
  background: rgba(255, 255, 255, 0.08);
}

:deep(.ant-input-password-icon) {
  color: rgba(255, 255, 255, 0.6);
}

:deep(.ant-input-password-icon:hover) {
  color: #00d4ff;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .left-panel {
    display: none;
  }

  .right-panel {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .login-box {
    padding: 30px 20px;
  }

  .login-header h2 {
    font-size: 24px;
  }
}
</style>
