<template>
  <a-layout class="layout-container">
    <a-layout-header class="layout-header">
      <div class="header-left">
        <div class="logo">
          <security-scan-outlined :style="{ fontSize: '28px', color: '#fff' }" />
          <span>One 靶场</span>
        </div>
        
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          theme="dark"
          :style="{ 
            background: 'transparent', 
            borderBottom: 'none',
            lineHeight: '64px',
            flex: 1
          }"
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

      <div class="header-right">
        <a-space :size="16">
          <a-tooltip title="刷新">
            <reload-outlined @click="handleRefresh" class="header-icon" />
          </a-tooltip>
          
          <a-dropdown>
            <a-avatar :style="{ backgroundColor: '#3A7BD5', cursor: 'pointer' }">
              <template #icon><user-outlined /></template>
            </a-avatar>
            <template #overlay>
              <a-menu>
                <a-menu-item>
                  <user-outlined /> {{ username }}
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item @click="handleLogout">
                  <logout-outlined /> 退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </a-space>
      </div>
    </a-layout-header>

    <a-layout-content class="layout-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </a-layout-content>

    <a-layout-footer class="layout-footer">
      One 安全靶场 v2.0.0 © 2026 | 仅供学习研究使用
    </a-layout-footer>
  </a-layout>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
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
const username = computed(() => localStorage.getItem('username') || 'Admin')
const isAdmin = ref(false)

// 检查用户是否是管理员
const checkAdminRole = async () => {
  try {
    const response = await axios.get('/admin/users')
    let result = response.data
    if (typeof result === 'string') {
      result = JSON.parse(result)
    }
    // 如果能成功获取用户列表，说明是管理员
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
}

.layout-header {
  background: linear-gradient(90deg, #3A7BD5 0%, #5B9FE3 100%);
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  height: 64px;
  line-height: 64px;
}

.header-left {
  display: flex;
  align-items: center;
  flex: 1;
  overflow: hidden;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  margin-right: 30px;
  white-space: nowrap;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-icon {
  font-size: 18px;
  color: #fff;
  cursor: pointer;
  transition: color 0.3s;
}

.header-icon:hover {
  color: #FFB74D;
}

.layout-content {
  margin: 24px;
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px - 70px);
}

.layout-footer {
  text-align: center;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  color: #95a5a6;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

:deep(.ant-menu-horizontal) {
  border-bottom: none !important;
}

:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-item),
:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-submenu) {
  color: rgba(255, 255, 255, 0.85);
}

:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-item-selected) {
  background-color: rgba(255, 255, 255, 0.15) !important;
  color: #fff !important;
}

:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-item:hover),
:deep(.ant-menu-dark.ant-menu-horizontal > .ant-menu-submenu:hover) {
  background-color: rgba(255, 255, 255, 0.1) !important;
  color: #fff !important;
}

:deep(.ant-menu-submenu-popup) {
  top: 64px !important;
}
</style>

