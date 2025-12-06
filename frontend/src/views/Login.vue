<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <security-scan-outlined :style="{ fontSize: '48px', color: '#fff' }" />
        </div>
        <h1>One 安全靶场</h1>
        <p>One Security Range</p>
      </div>
      
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
            placeholder="用户名"
          >
            <template #prefix>
              <user-outlined />
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
            placeholder="密码"
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
            登 录
          </a-button>
        </a-form-item>
      </a-form>

      <div class="login-footer">
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
import { reactive, ref, onMounted } from 'vue'
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

onMounted(() => {
  // 初始化粒子背景（可选）
})
</script>

<style scoped>
.login-container {
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

.login-box {
  width: 420px;
  padding: 48px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  z-index: 1;
  backdrop-filter: blur(10px);
}

.login-header {
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

.login-header h1 {
  margin: 0;
  font-size: 32px;
  font-weight: 600;
  color: #2c3e50;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.login-header p {
  margin: 8px 0 0;
  color: #95a5a6;
  font-size: 14px;
}

.login-form {
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

.login-footer {
  margin-top: 16px;
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


