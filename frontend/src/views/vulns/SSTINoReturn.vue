<template>
  <vuln-template
    title="SSTI - 无回显"
    subtitle="SSTI Without Return"
    level="高危"
    :icon="FileMarkdownOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <!-- 漏洞描述内容 -->
      </a-typography-paragraph>
    </template>

    <template #payload>
      <a-typography-paragraph>
        <!-- 攻击载荷示例 -->
      </a-typography-paragraph>
    </template>

    <template #test-form>
      <a-form :model="formState" @finish="handleSubmit" layout="vertical">
        <a-form-item
          label="输入模板表达式"
          name="content"
          :rules="[{ required: true, message: '请输入内容' }]"
        >
          <a-input
            v-model:value="formState.content"
            size="large"
            placeholder="请输入测试内容"
          />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit" size="large" :loading="loading">
              <template #icon><play-circle-outlined /></template>
              执行测试
            </a-button>
            <a-button size="large" @click="handleReset">
              <template #icon><redo-outlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </template>

    <template #result v-if="result">
      <div class="result-content">
        <pre style="margin: 0;">{{ result }}</pre>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { FileMarkdownOutlined, PlayCircleOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const formState = reactive({
  content: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/ssti/noreturn', {
      params: formState
    })
    result.value = response.data
    message.success('测试完成')
  } catch (error) {
    message.error('测试失败：' + (error.response?.data || error.message))
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formState.content = ''
  result.value = ''
}
</script>
