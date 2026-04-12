<template>
  <div class="auth-page register-page">
    <div class="auth-noise"></div>
    <section class="auth-shell">
      <div class="auth-hero">
        <div class="hero-grid"></div>
        <div class="hero-rings">
          <span class="ring ring-a"></span>
          <span class="ring ring-b"></span>
        </div>

        <div class="hero-topline">
          <security-scan-outlined />
          <span>One靶场</span>
        </div>

        <div class="hero-copy">
          <h1>One靶场</h1>
          <p>Java 安全学习平台</p>
        </div>

        <div class="hero-metrics">
          <div class="metric-card" v-for="metric in heroMetrics" :key="metric.label">
            <strong>{{ metric.value }}</strong>
            <span>{{ metric.label }}</span>
          </div>
        </div>

        <div class="feature-list">
          <div class="feature-card" v-for="feature in features" :key="feature.title">
            <component :is="feature.icon" class="feature-icon" />
            <div class="feature-copy">
              <strong>{{ feature.title }}</strong>
              <p>{{ feature.description }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="auth-panel">
        <div class="panel-header">
          <div class="panel-badge">注册入口</div>
          <h2>创建新账户</h2>
          <p>填写账号信息，开启漏洞实验、进度记录与安全复盘流程。</p>
        </div>

        <a-alert
          v-if="errorMsg"
          :message="errorMsg"
          type="error"
          show-icon
          closable
          @close="errorMsg = ''"
          class="auth-alert"
        />

        <a-form
          :model="formState"
          @finish="handleRegister"
          class="auth-form"
        >
          <a-form-item
            name="username"
            :rules="[
              { required: true, message: '请输入用户名' },
              { min: 3, max: 20, message: '用户名长度为3-20个字符' }
            ]"
          >
            <a-input
              v-model:value="formState.username"
              size="large"
              placeholder="请输入用户名（3-20个字符）"
            >
              <template #prefix>
                <user-outlined class="input-icon" />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item
            name="password"
            :rules="[
              { required: true, message: '请输入密码' },
              { min: 6, message: '密码至少6个字符' }
            ]"
          >
            <a-input-password
              v-model:value="formState.password"
              size="large"
              placeholder="请输入密码（至少6个字符）"
            >
              <template #prefix>
                <lock-outlined class="input-icon" />
              </template>
            </a-input-password>
          </a-form-item>

          <a-form-item
            name="confirmPassword"
            :rules="[
              { required: true, message: '请确认密码' },
              { validator: validateConfirmPassword }
            ]"
          >
            <a-input-password
              v-model:value="formState.confirmPassword"
              size="large"
              placeholder="请再次输入密码"
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
              >
                <template #prefix>
                  <safety-certificate-outlined class="input-icon" />
                </template>
              </a-input>

              <div class="captcha-wrapper">
                <img
                  :src="captchaUrl"
                  class="captcha-img"
                  alt="验证码"
                />
                <button class="captcha-refresh" type="button" @click="refreshCaptcha" aria-label="刷新验证码">
                  <reload-outlined />
                </button>
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
              class="auth-submit"
            >
              <span v-if="!loading">注 册</span>
              <span v-else>注册中...</span>
            </a-button>
          </a-form-item>
        </a-form>

        <div class="panel-footer">
          <div class="footer-link-row">
            <a @click="goToLogin" class="switch-link">
              <login-outlined />
              <span>已有账号？立即登录</span>
            </a>
          </div>

          <div class="notice-card info-tone">
            <info-circle-outlined />
            <span>注册即表示您同意遵守平台使用规范，并将以学习研究为唯一用途。</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  UserOutlined,
  LockOutlined,
  SafetyCertificateOutlined,
  SecurityScanOutlined,
  UserAddOutlined,
  TrophyOutlined,
  RocketOutlined,
  ReloadOutlined,
  LoginOutlined,
  InfoCircleOutlined
} from '@ant-design/icons-vue'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)
const errorMsg = ref('')
const captchaUrl = ref('/captcha?t=' + Date.now())

const formState = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  captcha: ''
})

const heroMetrics = [
  { label: '实验模块', value: '26+' },
  { label: '学习记录', value: '可追踪' },
  { label: '组件专题', value: '6' }
]

const features = [
  {
    title: '快速创建账户',
    description: '以最少输入步骤完成接入，直接进入漏洞学习工作流。',
    icon: UserAddOutlined
  },
  {
    title: '记录学习成果',
    description: '通过学习进度页追踪模块完成情况与训练状态。',
    icon: TrophyOutlined
  },
  {
    title: '进入实战矩阵',
    description: '覆盖基础漏洞、组件漏洞与反序列化等多条主线。',
    icon: RocketOutlined
  }
]

const refreshCaptcha = () => {
  captchaUrl.value = '/captcha?t=' + Date.now()
}

const validateConfirmPassword = (rule, value) => {
  if (value && value !== formState.password) {
    return Promise.reject('两次密码输入不一致')
  }
  return Promise.resolve()
}

const handleRegister = async () => {
  loading.value = true
  errorMsg.value = ''

  try {
    const params = new URLSearchParams()
    params.append('username', formState.username)
    params.append('password', formState.password)
    params.append('confirmPassword', formState.confirmPassword)
    params.append('captcha', formState.captcha)

    const response = await axios.post('/admin/register', params, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })

    let result = response.data
    if (typeof result === 'string') {
      try {
        result = JSON.parse(result)
      } catch {
        if (result.includes('成功') || result.includes('success')) {
          result = { success: true, message: '注册成功' }
        } else {
          result = { success: false, message: result }
        }
      }
    }

    if (result.success === true || result.success === 'true') {
      message.success(result.message || '注册成功，请登录')
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      errorMsg.value = result.message || '注册失败，请重试'
      refreshCaptcha()
    }
  } catch (error) {
    console.error('注册错误:', error)
    errorMsg.value = error.response?.data?.message || error.response?.data || '注册失败，请重试'
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  padding: 24px;
  overflow: hidden;
}

.auth-noise {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 18% 18%, rgba(105, 243, 255, 0.14), transparent 22%),
    radial-gradient(circle at 82% 20%, rgba(52, 110, 255, 0.16), transparent 22%),
    radial-gradient(circle at 70% 76%, rgba(105, 243, 255, 0.08), transparent 18%);
  pointer-events: none;
}

.auth-shell {
  position: relative;
  z-index: 1;
  min-height: calc(100vh - 48px);
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(360px, 460px);
  gap: 20px;
  max-width: 1480px;
  margin: 0 auto;
}

.auth-hero,
.auth-panel {
  position: relative;
  overflow: hidden;
  border-radius: 32px;
  border: 1px solid rgba(105, 243, 255, 0.14);
  background: linear-gradient(180deg, rgba(12, 24, 38, 0.84) 0%, rgba(6, 12, 21, 0.92) 100%);
  backdrop-filter: blur(18px);
  box-shadow: 0 28px 80px rgba(0, 0, 0, 0.32);
}

.auth-hero {
  padding: 36px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.hero-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(105, 243, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(105, 243, 255, 0.05) 1px, transparent 1px);
  background-size: 46px 46px;
  opacity: 0.36;
  animation: grid-shift 18s linear infinite;
}

.hero-rings {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.ring {
  position: absolute;
  border: 1px solid rgba(105, 243, 255, 0.14);
  border-radius: 50%;
}

.ring-a {
  width: 320px;
  height: 320px;
  top: -80px;
  right: -20px;
}

.ring-b {
  width: 220px;
  height: 220px;
  bottom: -60px;
  left: 14%;
}

.hero-topline,
.hero-copy,
.hero-metrics,
.feature-list {
  position: relative;
  z-index: 1;
}

.hero-topline {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--accent);
  font-size: 13px;
  letter-spacing: 0.24em;
}

.hero-copy {
  max-width: 680px;
  margin-top: 30px;
}

.hero-copy h1 {
  margin: 0 0 18px;
  font-size: clamp(38px, 5vw, 64px);
  line-height: 1;
  letter-spacing: -0.05em;
  color: var(--text-primary);
}

.hero-copy p {
  max-width: 620px;
  color: var(--text-secondary);
  font-size: 16px;
  line-height: 1.9;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 36px;
}

.metric-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px;
  border-radius: 22px;
  border: 1px solid rgba(105, 243, 255, 0.1);
  background: rgba(7, 16, 27, 0.58);
}

.metric-card strong {
  font-size: 26px;
  color: var(--text-primary);
}

.metric-card span {
  color: var(--text-muted);
  font-size: 13px;
}

.feature-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.feature-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 20px;
  border-radius: 24px;
  border: 1px solid rgba(105, 243, 255, 0.1);
  background: rgba(7, 16, 27, 0.54);
}

.feature-icon {
  color: var(--accent);
  font-size: 24px;
}

.feature-copy strong {
  display: block;
  margin-bottom: 8px;
  font-size: 18px;
  color: var(--text-primary);
}

.feature-copy p {
  color: var(--text-secondary);
  line-height: 1.8;
}

.auth-panel {
  padding: 36px 32px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.panel-header {
  margin-bottom: 28px;
}

.panel-badge {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid rgba(105, 243, 255, 0.16);
  background: rgba(105, 243, 255, 0.08);
  color: var(--accent);
  font-size: 12px;
  letter-spacing: 0.18em;
}

.panel-header h2 {
  margin: 18px 0 10px;
  font-size: 34px;
  line-height: 1.05;
  color: var(--text-primary);
}

.panel-header p {
  color: var(--text-secondary);
  line-height: 1.8;
}

.auth-alert {
  margin-bottom: 20px;
}

.auth-form {
  margin-top: 8px;
}

.input-icon {
  color: var(--accent);
}

.captcha-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(184px, 208px);
  gap: 12px;
  align-items: center;
}

.captcha-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.captcha-img {
  display: block;
  flex: 1;
  min-width: 0;
  width: 100%;
  height: 52px;
  border-radius: 16px;
  border: 1px solid rgba(105, 243, 255, 0.16);
  background: rgba(7, 16, 27, 0.9);
  object-fit: cover;
}

.captcha-refresh {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  padding: 0;
  border-radius: 14px;
  background: rgba(5, 13, 21, 0.84);
  border: 1px solid rgba(105, 243, 255, 0.18);
  color: var(--accent);
  cursor: pointer;
  flex-shrink: 0;
}

.auth-submit {
  height: 50px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.18em;
}

.panel-footer {
  margin-top: 8px;
}

.footer-link-row {
  display: flex;
  justify-content: center;
  margin-bottom: 18px;
}

.switch-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--accent);
  font-size: 14px;
}

.notice-card {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 16px;
  border-radius: 20px;
  line-height: 1.75;
}

.info-tone {
  border: 1px solid rgba(105, 243, 255, 0.14);
  background: rgba(105, 243, 255, 0.08);
  color: #d5fbff;
}

:deep(.ant-form-item) {
  margin-bottom: 18px;
}

:deep(.ant-alert) {
  border-radius: 18px;
}

@keyframes grid-shift {
  from {
    transform: translateY(0);
  }
  to {
    transform: translateY(46px);
  }
}

@media (max-width: 1180px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }

  .feature-list {
    grid-template-columns: 1fr;
  }

  .hero-metrics {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .auth-page {
    padding: 14px;
  }

  .auth-shell {
    min-height: calc(100vh - 28px);
  }

  .auth-hero,
  .auth-panel {
    padding: 22px;
    border-radius: 24px;
  }

  .hero-copy h1 {
    font-size: 38px;
  }

  .hero-metrics,
  .feature-list,
  .captcha-row {
    grid-template-columns: 1fr;
  }
}
</style>
