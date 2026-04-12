<template>
  <a-layout class="layout-container">
    <a-layout-header class="layout-header">
      <div class="header-shell">
        <div class="header-left">
          <div class="brand-panel" @click="router.push('/home')">
            <div class="brand-mark">
              <security-scan-outlined />
            </div>
            <div class="brand-copy">
              <span class="brand-kicker">安全靶场</span>
              <span class="brand-title">One 安全靶场</span>
            </div>
          </div>

          <div class="header-menu-wrap">
            <a-menu
              v-model:selectedKeys="selectedKeys"
              mode="horizontal"
              theme="dark"
              class="header-menu"
              @click="handleMenuClick"
            >
              <a-menu-item key="/home">
                <home-outlined />
                <span>首页</span>
              </a-menu-item>

              <a-menu-item key="/progress">
                <trophy-outlined />
                <span>学习进度</span>
              </a-menu-item>

              <a-menu-item key="/users" v-if="isAdmin">
                <team-outlined />
                <span>用户管理</span>
              </a-menu-item>

              <a-sub-menu key="basic">
                <template #icon><bug-outlined /></template>
                <template #title>基础漏洞</template>

                <a-sub-menu key="xss">
                  <template #title>XSS</template>
                  <a-menu-item key="/xss/reflect">反射型XSS</a-menu-item>
                  <a-menu-item key="/xss/store">存储型XSS</a-menu-item>
                  <a-menu-item key="/xss/dom">DOM型XSS</a-menu-item>
                </a-sub-menu>

                <a-sub-menu key="sqli">
                  <template #title>SQL注入</template>
                  <a-menu-item key="/sqli/jdbc_int_based">整型注入</a-menu-item>
                  <a-menu-item key="/sqli/jdbc_error_based">报错注入</a-menu-item>
                  <a-menu-item key="/sqli/jdbc_blind_time_based">时间盲注</a-menu-item>
                  <a-menu-item key="/sqli/jdbc_blind_bool_based">布尔盲注</a-menu-item>
                </a-sub-menu>

                <a-sub-menu key="rce">
                  <template #title>远程代码执行</template>
                  <a-menu-item key="/rce/runtime">Runtime</a-menu-item>
                  <a-menu-item key="/rce/processbuilder">ProcessBuilder</a-menu-item>
                </a-sub-menu>

                <a-sub-menu key="file">
                  <template #title>文件操作</template>
                  <a-menu-item key="/file/upload">文件上传</a-menu-item>
                  <a-menu-item key="/file/download">文件下载</a-menu-item>
                </a-sub-menu>

                <a-sub-menu key="xxe">
                  <template #title>XXE</template>
                  <a-menu-item key="/xxe/xmlreader">XMLReader</a-menu-item>
                  <a-menu-item key="/xxe/saxreader">SAXReader</a-menu-item>
                  <a-menu-item key="/xxe/saxbuilder">SAXBuilder</a-menu-item>
                </a-sub-menu>

                <a-menu-item key="/ssrf">SSRF</a-menu-item>
                <a-menu-item key="/xff">XFF伪造</a-menu-item>
                <a-menu-item key="/cors">CORS</a-menu-item>
                <a-menu-item key="/redirect">URL重定向</a-menu-item>
                <a-menu-item key="/unauth/info">未授权访问</a-menu-item>
              </a-sub-menu>

              <a-sub-menu key="component">
                <template #icon><appstore-outlined /></template>
                <template #title>组件漏洞</template>
                <a-menu-item key="/fastjson">Fastjson</a-menu-item>
                <a-menu-item key="/jackson">Jackson</a-menu-item>
                <a-menu-item key="/shiro">Shiro</a-menu-item>
                <a-menu-item key="/log4j">Log4j</a-menu-item>
                <a-menu-item key="/actuators/index">Actuators</a-menu-item>
              </a-sub-menu>

              <a-sub-menu key="deserialize">
                <template #icon><code-outlined /></template>
                <template #title>反序列化</template>
                <a-menu-item key="/deserialize/readobject">ReadObject</a-menu-item>
              </a-sub-menu>
            </a-menu>
          </div>
        </div>

        <div class="header-right">
          <div class="header-status">
            <span class="status-dot"></span>
            <span>实验环境在线</span>
          </div>

          <a-space :size="14">
            <a-tooltip title="刷新">
              <reload-outlined @click="handleRefresh" class="header-icon" />
            </a-tooltip>

            <a-dropdown>
              <a-avatar class="user-avatar">
                <template #icon><user-outlined /></template>
              </a-avatar>
              <template #overlay>
                <a-menu>
                  <a-menu-item>
                    <user-outlined /> {{ username }}
                  </a-menu-item>
                  <a-menu-divider />
                  <a-menu-item @click="openPasswordModal">
                    修改密码
                  </a-menu-item>
                  <a-menu-item @click="handleLogout">
                    <logout-outlined /> 退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </a-space>
        </div>
      </div>
    </a-layout-header>

    <a-layout-content class="layout-content">
      <div class="content-shell">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </a-layout-content>

    <a-layout-footer class="layout-footer">
      One 安全靶场 v2.0.0 © 2026 | 安全学习平台
    </a-layout-footer>

    <a-modal
      v-model:open="passwordModalVisible"
      title="修改密码"
      ok-text="确认修改"
      cancel-text="取消"
      :confirm-loading="passwordSubmitting"
      @ok="handleChangePassword"
      @cancel="resetPasswordForm"
    >
      <a-form layout="vertical">
        <a-form-item label="当前密码" required>
          <a-input-password
            v-model:value="passwordForm.old_password"
            placeholder="请输入当前密码"
            autocomplete="current-password"
          />
        </a-form-item>
        <a-form-item label="新密码" required>
          <a-input-password
            v-model:value="passwordForm.new_password"
            placeholder="请输入新密码"
            autocomplete="new-password"
          />
        </a-form-item>
        <a-form-item label="确认新密码" required>
          <a-input-password
            v-model:value="passwordForm.again_password"
            placeholder="请再次输入新密码"
            autocomplete="new-password"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-layout>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  HomeOutlined,
  BugOutlined,
  AppstoreOutlined,
  CodeOutlined,
  UserOutlined,
  LogoutOutlined,
  ReloadOutlined,
  SecurityScanOutlined,
  TrophyOutlined,
  TeamOutlined
} from '@ant-design/icons-vue'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const selectedKeys = ref([route.path])
const username = computed(() => localStorage.getItem('username') || '管理员')
const isAdmin = ref(false)
const passwordModalVisible = ref(false)
const passwordSubmitting = ref(false)
const passwordForm = reactive({
  old_password: '',
  new_password: '',
  again_password: ''
})

const checkAdminRole = async () => {
  try {
    const response = await axios.get('/admin/users')
    let result = response.data
    if (typeof result === 'string') {
      result = JSON.parse(result)
    }
    isAdmin.value = result.success === true
  } catch (error) {
    isAdmin.value = false
  }
}

watch(() => route.path, (newPath) => {
  selectedKeys.value = [newPath]
})

const handleMenuClick = ({ key }) => {
  router.push(key)
}

const handleRefresh = () => {
  window.location.reload()
}

const resetPasswordForm = () => {
  passwordForm.old_password = ''
  passwordForm.new_password = ''
  passwordForm.again_password = ''
  passwordModalVisible.value = false
}

const openPasswordModal = () => {
  resetPasswordForm()
  passwordModalVisible.value = true
}

const handleChangePassword = async () => {
  if (!passwordForm.old_password || !passwordForm.new_password || !passwordForm.again_password) {
    message.error('请完整填写密码信息')
    return
  }

  if (passwordForm.old_password === passwordForm.new_password) {
    message.error('新密码不能与旧密码一致')
    return
  }

  if (passwordForm.new_password !== passwordForm.again_password) {
    message.error('两次输入的新密码不一致')
    return
  }

  passwordSubmitting.value = true
  try {
    const response = await axios.post('/admin/chpwd', {
      old_password: passwordForm.old_password,
      new_password: passwordForm.new_password,
      again_password: passwordForm.again_password
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    })

    let result = response.data
    if (typeof result === 'string') {
      try {
        result = JSON.parse(result)
      } catch {
        result = { success: false, message: result }
      }
    }

    if (result.success) {
      message.success(result.message || '密码修改成功，请重新登录')
      resetPasswordForm()
      localStorage.removeItem('username')
      router.push('/login')
    } else {
      message.error(result.message || '密码修改失败')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    message.error(error.response?.data?.message || error.response?.data || '修改密码失败，请稍后重试')
  } finally {
    passwordSubmitting.value = false
  }
}

const handleLogout = async () => {
  try {
    await axios.get('/admin/logout')
    localStorage.removeItem('username')
    message.success('退出成功')
    router.push('/login')
  } catch (error) {
    localStorage.removeItem('username')
    router.push('/login')
  }
}

onMounted(() => {
  checkAdminRole()
})
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  background: transparent;
}

.layout-header {
  position: sticky;
  top: 0;
  z-index: 20;
  height: auto;
  line-height: normal;
  padding: 18px 24px 0;
  background: transparent;
}

.header-shell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 14px 18px;
  border-radius: 26px;
  border: 1px solid rgba(105, 243, 255, 0.16);
  background:
    linear-gradient(135deg, rgba(12, 23, 36, 0.92) 0%, rgba(6, 12, 21, 0.82) 100%);
  backdrop-filter: blur(18px);
  box-shadow: 0 22px 54px rgba(0, 0, 0, 0.32);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 18px;
  flex: 1;
  min-width: 0;
}

.brand-panel {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 14px;
  border-radius: 20px;
  background: rgba(105, 243, 255, 0.08);
  border: 1px solid rgba(105, 243, 255, 0.14);
  cursor: pointer;
  flex-shrink: 0;
}

.brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 16px;
  background: rgba(105, 243, 255, 0.14);
  color: var(--accent);
  font-size: 22px;
  box-shadow: inset 0 0 0 1px rgba(105, 243, 255, 0.14);
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-kicker {
  font-size: 11px;
  letter-spacing: 0.22em;
  color: var(--text-muted);
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.header-menu-wrap {
  flex: 1;
  min-width: 0;
  overflow-x: auto;
  scrollbar-width: none;
}

.header-menu-wrap::-webkit-scrollbar {
  display: none;
}

.header-menu {
  min-width: max-content;
  background: transparent !important;
  border-bottom: none !important;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
}

.header-status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid rgba(56, 242, 180, 0.2);
  background: rgba(56, 242, 180, 0.08);
  color: rgba(196, 255, 232, 0.9);
  font-size: 13px;
  letter-spacing: 0.04em;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--success);
  box-shadow: 0 0 14px rgba(56, 242, 180, 0.9);
}

.header-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid rgba(105, 243, 255, 0.14);
  background: rgba(105, 243, 255, 0.06);
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.25s ease;
}

.header-icon:hover {
  color: var(--accent);
  border-color: rgba(105, 243, 255, 0.4);
  transform: translateY(-1px);
}

.user-avatar {
  cursor: pointer;
  background: linear-gradient(135deg, rgba(105, 243, 255, 0.88), rgba(50, 212, 255, 0.68)) !important;
  color: #03131c !important;
  box-shadow: 0 12px 24px rgba(50, 212, 255, 0.18);
}

.layout-content {
  padding: 26px 24px 28px;
  background: transparent;
}

.content-shell {
  max-width: 1480px;
  margin: 0 auto;
}

.layout-footer {
  padding: 0 24px 24px;
  text-align: center;
  color: var(--text-muted);
  background: transparent;
  border-top: none;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 11px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.28s ease, transform 0.28s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(14px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-14px);
}

:deep(.ant-menu-horizontal) {
  border-bottom: none !important;
}

:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-item),
:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-submenu) {
  color: var(--text-secondary);
  border-radius: 999px;
  margin-inline: 4px;
  padding-inline: 14px;
}

:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-item-selected),
:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-submenu-selected) {
  background: rgba(105, 243, 255, 0.12) !important;
  color: var(--accent) !important;
}

:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-item:hover),
:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-submenu:hover),
:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-item-active),
:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-submenu-active) {
  background: rgba(105, 243, 255, 0.08) !important;
  color: var(--text-primary) !important;
}

:deep(.ant-menu-dark .ant-menu-submenu-title:hover),
:deep(.ant-menu-dark .ant-menu-item:hover) {
  color: var(--text-primary) !important;
}

:deep(.ant-menu-submenu-popup) {
  top: 84px !important;
}

:deep(.ant-menu-dark.ant-menu-submenu-popup > .ant-menu) {
  background: rgba(8, 18, 30, 0.96) !important;
  border: 1px solid rgba(105, 243, 255, 0.14);
  border-radius: 18px;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.32);
}

:deep(.ant-menu-dark.ant-menu-submenu-popup .ant-menu-item-selected) {
  background: rgba(105, 243, 255, 0.12) !important;
}

@media (max-width: 1180px) {
  .header-shell {
    flex-direction: column;
    align-items: stretch;
  }

  .header-right {
    justify-content: space-between;
  }
}

@media (max-width: 768px) {
  .layout-header {
    padding: 14px 14px 0;
  }

  .layout-content {
    padding: 18px 14px 24px;
  }

  .header-left {
    flex-direction: column;
    align-items: stretch;
  }

  .brand-panel {
    width: 100%;
    justify-content: center;
  }

  .header-right {
    flex-wrap: wrap;
  }

  .header-status {
    order: 2;
    width: 100%;
    justify-content: center;
  }
}
</style>
