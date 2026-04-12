<template>
  <div class="users-page fade-in">
    <section class="users-grid">
      <article class="panel hero-panel">
        <div class="panel-grid"></div>
        <div class="hero-copy">
          <div class="panel-kicker">
            <team-outlined />
            <span>用户管理</span>
          </div>
          <h1>用户管理</h1>
          <p>统一查看账号分布、管理员权限和当前用户池，保持教学环境的账号治理处于可控状态。</p>
        </div>

        <div class="hero-metrics">
          <div class="metric-card" v-for="metric in metricCards" :key="metric.label">
            <div class="metric-icon">
              <component :is="metric.icon" />
            </div>
            <div class="metric-body">
              <strong>{{ metric.value }}</strong>
              <span>{{ metric.label }}</span>
            </div>
          </div>
        </div>
      </article>

      <article class="panel insight-panel">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">控制面板</span>
            <h2>控制面板</h2>
          </div>
          <a-button type="primary" @click="loadUsers">
            <reload-outlined />
            刷新用户
          </a-button>
        </div>

        <div class="insight-list">
          <div class="insight-item">
            <span>总用户数</span>
            <strong>{{ users.length }}</strong>
          </div>
          <div class="insight-item">
            <span>管理员账户</span>
            <strong>{{ adminCount }}</strong>
          </div>
          <div class="insight-item">
            <span>普通用户</span>
            <strong>{{ normalCount }}</strong>
          </div>
          <div class="insight-item">
            <span>当前状态</span>
            <strong>管理模式</strong>
          </div>
        </div>

        <div class="notice-card">
          <safety-outlined />
          <span>管理员账号禁止删除，普通用户删除操作仍需二次确认。</span>
        </div>
      </article>

      <article class="panel table-panel">
        <div class="panel-head">
          <div>
            <span class="panel-kicker">用户列表</span>
            <h2>用户列表</h2>
          </div>
          <span class="panel-note">共 {{ users.length }} 个账号</span>
        </div>

        <a-table
          :columns="columns"
          :data-source="users"
          :loading="loading"
          :pagination="{ pageSize: 10, showSizeChanger: true }"
          row-key="id"
          class="users-table"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'role'">
              <span class="role-pill" :class="record.role === 'administrator' ? 'admin' : 'user'">
                {{ record.role === 'administrator' ? '管理员' : '普通用户' }}
              </span>
            </template>

            <template v-else-if="column.key === 'action'">
              <a-button
                danger
                size="small"
                class="delete-btn"
                @click="handleDelete(record)"
                :disabled="record.role === 'administrator'"
              >
                删除
              </a-button>
            </template>
          </template>
        </a-table>
      </article>
    </section>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  TeamOutlined,
  UserOutlined,
  SafetyOutlined,
  CrownOutlined,
  ReloadOutlined
} from '@ant-design/icons-vue'
import axios from 'axios'

const loading = ref(false)
const users = ref([])

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 90
  },
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username'
  },
  {
    title: '角色',
    dataIndex: 'role',
    key: 'role',
    width: 160
  },
  {
    title: '操作',
    key: 'action',
    width: 140
  }
]

const adminCount = computed(() => users.value.filter((item) => item.role === 'administrator').length)
const normalCount = computed(() => users.value.length - adminCount.value)

const metricCards = computed(() => [
  { label: '账户总数', value: users.value.length, icon: TeamOutlined },
  { label: '管理员', value: adminCount.value, icon: CrownOutlined },
  { label: '普通用户', value: normalCount.value, icon: UserOutlined }
])

const loadUsers = async () => {
  loading.value = true
  try {
    const response = await axios.get('/admin/users')
    let result = response.data
    if (typeof result === 'string') {
      result = JSON.parse(result)
    }
    if (result.success) {
      users.value = result.data || []
    } else {
      message.error(result.message || '加载用户列表失败')
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    message.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleDelete = (record) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除用户 "${record.username}" 吗？此操作不可恢复。`,
    okText: '确定',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        const response = await axios.post('/admin/deleteUser', {
          userId: record.id.toString()
        }, {
          headers: {
            'Content-Type': 'application/json'
          }
        })

        let result = response.data
        if (typeof result === 'string') {
          result = JSON.parse(result)
        }

        if (result.success) {
          message.success('删除成功')
          loadUsers()
        } else {
          message.error(result.message || '删除失败')
        }
      } catch (error) {
        console.error('删除用户失败:', error)
        message.error('删除用户失败')
      }
    }
  })
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.users-page {
  width: 100%;
}

.users-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 20px;
}

.panel {
  position: relative;
  overflow: hidden;
  border-radius: 28px;
  border: 1px solid rgba(105, 243, 255, 0.12);
  background: linear-gradient(180deg, rgba(12, 24, 38, 0.84) 0%, rgba(6, 12, 21, 0.92) 100%);
  box-shadow: 0 26px 80px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(18px);
}

.hero-panel {
  grid-column: span 8;
  padding: 30px;
}

.insight-panel {
  grid-column: span 4;
  padding: 28px;
}

.table-panel {
  grid-column: span 12;
  padding: 28px;
}

.panel-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(105, 243, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(105, 243, 255, 0.05) 1px, transparent 1px);
  background-size: 44px 44px;
  opacity: 0.34;
}

.panel-kicker {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid rgba(105, 243, 255, 0.2);
  background: rgba(105, 243, 255, 0.1);
  color: #d7fdff;
  font-size: 12px;
  letter-spacing: 0.22em;
}

.hero-copy,
.hero-metrics {
  position: relative;
  z-index: 1;
}

.hero-copy h1 {
  margin: 18px 0 16px;
  font-size: clamp(36px, 4vw, 56px);
  line-height: 1;
  letter-spacing: -0.04em;
  color: var(--text-primary);
}

.hero-copy p {
  max-width: 680px;
  color: var(--text-secondary);
  line-height: 1.9;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 26px;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 22px;
  border: 1px solid rgba(105, 243, 255, 0.1);
  background: rgba(7, 16, 27, 0.56);
}

.metric-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 18px;
  background: rgba(105, 243, 255, 0.08);
  color: var(--accent);
  font-size: 22px;
}

.metric-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.metric-body strong {
  font-size: 28px;
  color: var(--text-primary);
}

.metric-body span {
  color: var(--text-muted);
  font-size: 13px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.panel-head h2 {
  margin-top: 8px;
  font-size: 26px;
  color: var(--text-primary);
}

.panel-note {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 9px 16px;
  border-radius: 999px;
  border: 1px solid rgba(105, 243, 255, 0.26);
  background: rgba(105, 243, 255, 0.14);
  color: #e3fdff;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.insight-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.insight-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border-radius: 20px;
  border: 1px solid rgba(105, 243, 255, 0.08);
  background: rgba(7, 16, 27, 0.54);
}

.insight-item span {
  color: var(--text-muted);
}

.insight-item strong {
  color: var(--text-primary);
}

.notice-card {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-top: 18px;
  padding: 16px;
  border-radius: 20px;
  border: 1px solid rgba(255, 200, 87, 0.14);
  background: rgba(255, 200, 87, 0.08);
  color: #ffe6ad;
  line-height: 1.8;
}

.users-table :deep(.ant-table-wrapper) {
  background: transparent;
}

.users-table :deep(.ant-pagination) {
  margin-top: 22px;
}

.users-table :deep(.ant-pagination-item),
.users-table :deep(.ant-pagination-prev),
.users-table :deep(.ant-pagination-next) {
  min-width: 36px;
  height: 36px;
  line-height: 34px;
}

.users-table :deep(.ant-pagination-item-active) {
  background: rgba(105, 243, 255, 0.16) !important;
}

.role-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 88px;
  padding: 7px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.role-pill.admin {
  background: rgba(255, 107, 143, 0.18);
  color: #fff0f4;
  border: 1px solid rgba(255, 107, 143, 0.26);
}

.role-pill.user {
  background: rgba(105, 243, 255, 0.14);
  color: #dbfdff;
  border: 1px solid rgba(105, 243, 255, 0.24);
}

.delete-btn {
  border-radius: 999px;
}

@media (max-width: 1200px) {
  .hero-panel,
  .insight-panel,
  .table-panel {
    grid-column: span 12;
  }
}

@media (max-width: 768px) {
  .users-grid {
    gap: 16px;
  }

  .hero-panel,
  .insight-panel,
  .table-panel {
    padding: 20px;
    border-radius: 22px;
  }

  .hero-metrics {
    grid-template-columns: 1fr;
  }

  .panel-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
