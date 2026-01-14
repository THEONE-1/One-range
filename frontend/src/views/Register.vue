<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <div class="logo">
          <security-scan-outlined :style="{ fontSize: '48px', color: '#fff' }" />
        </div>
        <h1>用户注册</h1>
        <p>One Security Range</p>
      </div>

      <a-form
        :model="formState"
        @finish="handleRegister"
        class="register-form"
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
            placeholder="用户名（3-20个字符）"
          >
            <template #prefix>
              <user-outlined />
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
            placeholder="密码（至少6个字符）"
          >
            <template #prefix>
              <lock-outlined />
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
            placeholder="确认密码"
          >
            <template #prefix>
              <lock-outlined />
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
              placeholder="验证码"
              style="flex: 1"
            >
              <template #prefix>
                <safety-certificate-outlined />
              </template>
            </a-input>
            <img
              :src="captchaUrl"
              @click="refreshCaptcha"
              class="captcha-img"
              alt="验证码"
            />
          </div>
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            block
            :loading="loading"
          >
            注 册
          </a-button>
        </a-form-item>

        <a-form-item>
          <div class="register-footer-links">
            <a @click="goToLogin">已有账号？立即登录</a>
          </div>
        </a-form-item>
      </a-form>

      <div class="register-footer">
        <a-alert
          v-if="errorMsg"
          :message="errorMsg"
          type="error"
          show-icon
          closable
          @close="errorMsg = ''"
        />
      </div>
    </div>

    <div class="particles" id="particles-js"></div>
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
  SecurityScanOutlined
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

const refreshCaptcha = () => {
  captchaUrl.value = '/captcha?t=' + Date.now()
}

const validateConfirmPassword = (rule, value) => {
  if (value && value !== formState.password) {
    return Promise.reject('两次密码输入不一致')
  }
  return Promise.resolve()
}

const handleRegister = async (values) => {
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

    // 后端返回的是JSON字符串,需要解析
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
.register-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.particles {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 0;
}

.register-box {
  width: 420px;
  padding: 48px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  z-index: 1;
  backdrop-filter: blur(10px);
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #3A7BD5 0%, #5B9FE3 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(58, 123, 213, 0.4);
}

.register-header h1 {
  margin: 0;
  font-size: 32px;
  font-weight: 600;
  color: #2c3e50;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.register-header p {
  margin: 8px 0 0;
  color: #95a5a6;
  font-size: 14px;
}

.register-form {
  margin-top: 24px;
}

.captcha-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-img {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  transition: all 0.3s;
}

.captcha-img:hover {
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.register-footer {
  margin-top: 16px;
}

.register-footer-links {
  text-align: center;
}

.register-footer-links a {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s;
}

.register-footer-links a:hover {
  color: #764ba2;
  text-decoration: underline;
}

:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-weight: 600;
  height: 48px;
  font-size: 16px;
}

:deep(.ant-btn-primary:hover) {
  background: linear-gradient(135deg, #5568d3 0%, #6a3f8f 100%);
}

:deep(.ant-input-affix-wrapper) {
  border-radius: 8px;
}

:deep(.ant-input) {
  border-radius: 8px;
}
</style>
