<template>
  <div class="user-management-container">
    <a-row :gutter="[24, 24]">
      <a-col :span="24">
        <div class="page-header">
          <div class="header-icon">
            <team-outlined />
          </div>
          <div class="header-content">
            <h1>用户管理</h1>
            <p>管理系统用户账号</p>
          </div>
        </div>
      </a-col>

      <a-col :span="24">
        <a-card title="用户列表" :bordered="false">
          <a-table
            :columns="columns"
            :data-source="users"
            :loading="loading"
            :pagination="{ pageSize: 10 }"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'role'">
                <a-tag :color="record.role === 'administrator' ? 'red' : 'blue'">
                  {{ record.role === 'administrator' ? '管理员' : '普通用户' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button
                    type="primary"
                    danger
                    size="small"
                    @click="handleDelete(record)"
                    :disabled="record.role === 'administrator'"
                  >
                    删除
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { TeamOutlined } from '@ant-design/icons-vue'
import axios from 'axios'

const loading = ref(false)
const users = ref([])

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80
  },
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username'
  },
  {
    title: '角色',
    dataIndex: 'role',
    key: 'role'
  },
  {
    title: '操作',
    key: 'action',
    width: 150
  }
]

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
.user-management-container {
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  padding: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
  margin-bottom: 24px;
}

.header-icon {
  font-size: 64px;
  margin-right: 24px;
  opacity: 0.9;
}

.header-content h1 {
  margin: 0;
  font-size: 32px;
  font-weight: 600;
  color: white;
}

.header-content p {
  margin: 8px 0 0;
  font-size: 16px;
  opacity: 0.9;
  color: white;
}

:deep(.ant-card) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

:deep(.ant-card-head) {
  border-bottom: 1px solid #f0f0f0;
  font-weight: 600;
}

:deep(.ant-table) {
  font-size: 14px;
}

:deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
}
</style>
