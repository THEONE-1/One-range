<template>
  <vuln-template
    title="任意文件下载"
    subtitle="File Download"
    level="中危"
    :icon="CloudDownloadOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>任意文件下载漏洞</strong>是指攻击者可以通过构造特殊的文件路径,下载服务器上的任意文件。
      </a-typography-paragraph>
      <a-typography-paragraph>
        文件下载漏洞的危害：
      </a-typography-paragraph>
      <ul>
        <li>下载配置文件获取敏感信息(数据库密码等)</li>
        <li>下载源码分析业务逻辑</li>
        <li>获取其他用户上传的私密文件</li>
        <li>可能导致更严重的安全问题</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的文件下载payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        ../../../etc/passwd
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">路径穿越读取Linux系统文件</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">
        ..\..\..\..\windows\win.ini
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">路径穿越读取Windows系统文件</span>
      </a-space>

      <a-typography-paragraph code copyable style="margin-top: 12px">
        application.yml
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">尝试下载Spring Boot配置文件</span>
      </a-space>

      <a-alert
        message="必须对文件路径进行严格的过滤和验证！"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <div class="file-download-container">
        <a-card title="文件下载功能" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            输入文件名或路径，下载服务器上的文件。
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="文件路径"
            name="filename"
            :rules="[{ required: true, message: '请输入文件路径' }]"
          >
            <a-input
              v-model:value="formState.filename"
              size="large"
              placeholder="例如：document.pdf 或 uploads/image.jpg"
              prefix="📁"
            />
            <div style="margin-top: 8px; color: #8c8c8c; font-size: 12px;">
              输入要下载的文件名或相对路径
            </div>
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><cloud-download-outlined /></template>
                下载文件
              </a-button>
              <a-button size="large" @click="handleReset">
                <template #icon><redo-outlined /></template>
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </div>
    </template>

    <template #input-hint>
      <a-alert message="测试建议" type="warning" show-icon style="margin-bottom: 12px;" />
      <p><strong>路径穿越测试payload：</strong></p>
      <ul style="padding-left: 20px; margin-bottom: 0;">
        <li><a-typography-text code copyable>../../../etc/passwd</a-typography-text> - Linux系统文件</li>
        <li><a-typography-text code copyable>..\..\..\..\windows\win.ini</a-typography-text> - Windows系统文件</li>
        <li><a-typography-text code copyable>application.yml</a-typography-text> - Spring配置文件</li>
        <li><a-typography-text code copyable>pom.xml</a-typography-text> - Maven配置文件</li>
      </ul>
    </template>

    <template #result v-if="result">
      <div class="result-content">
        <pre style="margin: 0;">{{ result }}</pre>
      </div>
    </template>

    <template #source-code>
      <a-typography-paragraph>
        <strong>后端源码（存在漏洞）：</strong>
      </a-typography-paragraph>
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="sourceCode" style="margin: 0;">
          <pre class="code-block"><code>@RestController
@RequestMapping("/home/file")
public class DownloadVul {
    @RequestMapping("/download")
    public String readFile(String filename, String check, HttpServletResponse response) {
        // 漏洞：安全检测是可选的，默认不启用
        if (check != null && check.equals("true")) {
            if (Security.checkTraversal(filename)) {
                return "请勿输入非法文件名!";
            }
        }
        
        // 漏洞：直接拼接用户输入的文件名，没有路径规范化
        String filePath = System.getProperty("user.dir") + 
            "/src/main/resources/static/upload/" + filename;
        
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return "文件不存在: " + filename;
            }
            
            // 漏洞：没有验证文件路径是否在允许的目录内
            // 攻击者可以使用../进行路径遍历，访问任意文件
            InputStream fis = Files.newInputStream(file.toPath());
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            
            response.addHeader("Content-Disposition", "attachment;filename=" + filename);
            response.getOutputStream().write(buffer);
            
            return "文件下载成功: " + filename;
        } catch (Exception e) {
            return "下载失败: " + e.toString();
        }
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：1. 安全检测是可选的，默认不启用；2. 直接拼接用户输入的文件名，没有路径规范化；3. 没有验证文件路径是否在允许的目录内；4. 攻击者可以使用../进行路径遍历，访问任意文件（如/etc/passwd、application.yml等）"
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
        <li><strong>路径规范化：</strong>使用File.getCanonicalPath()获取规范路径</li>
        <li><strong>白名单校验：</strong>只允许下载特定目录下的文件</li>
        <li><strong>过滤特殊字符：</strong>过滤..、/、\等路径穿越字符</li>
        <li><strong>文件名映射：</strong>使用ID映射文件，不直接使用文件名</li>
        <li><strong>权限控制：</strong>检查用户是否有权限下载该文件</li>
        <li><strong>路径验证：</strong>验证规范化后的路径是否在允许的目录内</li>
      </ul>
      
      <a-typography-paragraph code copyable style="margin-top: 16px;">
        // 错误示例(易受攻击)
        @RequestMapping("/download")
        public String readFile(String filename) {
            String filePath = UPLOAD_DIR + filename;
            // 攻击者可以输入: ../../../etc/passwd
            File file = new File(filePath);
            // 下载文件...
        }
      </a-typography-paragraph>
      
      <a-typography-paragraph code copyable>
        // 正确示例1：路径规范化和验证
        private static final String UPLOAD_DIR = 
            System.getProperty("user.dir") + "/uploads/";
        
        @RequestMapping("/download")
        public String readFile(String filename, HttpServletResponse response) {
            try {
                // 1. 验证文件名格式
                if (filename == null || filename.isEmpty()) {
                    throw new IllegalArgumentException("文件名不能为空");
                }
                
                // 2. 过滤路径遍历字符
                if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                    throw new SecurityException("文件名包含非法字符");
                }
                
                // 3. 构建文件路径
                File file = new File(UPLOAD_DIR, filename);
                
                // 4. 获取规范路径
                String canonicalPath = file.getCanonicalPath();
                String canonicalBaseDir = new File(UPLOAD_DIR).getCanonicalPath();
                
                // 5. 验证文件路径是否在允许的目录内
                if (!canonicalPath.startsWith(canonicalBaseDir)) {
                    throw new SecurityException("不允许访问该文件");
                }
                
                // 6. 验证文件是否存在
                if (!file.exists() || !file.isFile()) {
                    throw new FileNotFoundException("文件不存在");
                }
                
                // 7. 下载文件
                InputStream fis = Files.newInputStream(file.toPath());
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                
                response.addHeader("Content-Disposition", 
                    "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
                response.getOutputStream().write(buffer);
                
                return "文件下载成功";
            } catch (Exception e) {
                return "下载失败: " + e.getMessage();
            }
        }
      </a-typography-paragraph>
      
      <a-typography-paragraph code copyable>
        // 正确示例2：使用文件名映射（ID映射）
        private static final Map&lt;String, String&gt; FILE_MAP = new HashMap&lt;&gt;();
        
        @RequestMapping("/download")
        public String readFile(String fileId, HttpServletResponse response) {
            // 1. 从映射表获取实际文件名
            String filename = FILE_MAP.get(fileId);
            if (filename == null) {
                throw new SecurityException("无效的文件ID");
            }
            
            // 2. 验证文件名格式
            if (!filename.matches("^[a-zA-Z0-9._-]+\\.(jpg|png|pdf)$")) {
                throw new SecurityException("无效的文件名");
            }
            
            // 3. 构建文件路径（使用映射的文件名）
            File file = new File(UPLOAD_DIR, filename);
            String canonicalPath = file.getCanonicalPath();
            String canonicalBaseDir = new File(UPLOAD_DIR).getCanonicalPath();
            
            // 4. 验证路径
            if (!canonicalPath.startsWith(canonicalBaseDir)) {
                throw new SecurityException("不允许访问该文件");
            }
            
            // 下载文件...
        }
      </a-typography-paragraph>
      
      <a-typography-paragraph code copyable>
        // 正确示例3：白名单验证
        private static final String[] ALLOWED_FILES = {
            "document1.pdf",
            "image1.jpg",
            "report.xlsx"
        };
        
        @RequestMapping("/download")
        public String readFile(String filename) {
            // 验证文件名是否在白名单中
            boolean isAllowed = false;
            for (String allowedFile : ALLOWED_FILES) {
                if (allowedFile.equals(filename)) {
                    isAllowed = true;
                    break;
                }
            }
            
            if (!isAllowed) {
                throw new SecurityException("不允许下载该文件");
            }
            
            // 下载文件...
        }
      </a-typography-paragraph>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { CloudDownloadOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/home/file")
public class DownloadVul {
    @RequestMapping("/download")
    public String readFile(String filename, String check, HttpServletResponse response) {
        // 漏洞：安全检测是可选的，默认不启用
        if (check != null && check.equals("true")) {
            if (Security.checkTraversal(filename)) {
                return "请勿输入非法文件名!";
            }
        }
        
        // 漏洞：直接拼接用户输入的文件名，没有路径规范化
        String filePath = System.getProperty("user.dir") + 
            "/src/main/resources/static/upload/" + filename;
        
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return "文件不存在: " + filename;
            }
            
            // 漏洞：没有验证文件路径是否在允许的目录内
            // 攻击者可以使用../进行路径遍历，访问任意文件
            InputStream fis = Files.newInputStream(file.toPath());
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            
            response.addHeader("Content-Disposition", "attachment;filename=" + filename);
            response.getOutputStream().write(buffer);
            
            return "文件下载成功: " + filename;
        } catch (Exception e) {
            return "下载失败: " + e.toString();
        }
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
@RequestMapping("/download")
public String readFile(String filename) {
    String filePath = UPLOAD_DIR + filename;
    // 攻击者可以输入: ../../../etc/passwd
    File file = new File(filePath);
    // 下载文件...
}`

const fixCode2 = `// 正确示例1：路径规范化和验证
private static final String UPLOAD_DIR = 
    System.getProperty("user.dir") + "/uploads/";

@RequestMapping("/download")
public String readFile(String filename, HttpServletResponse response) {
    try {
        // 1. 验证文件名格式
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        // 2. 过滤路径遍历字符
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\\\")) {
            throw new SecurityException("文件名包含非法字符");
        }
        
        // 3. 构建文件路径
        File file = new File(UPLOAD_DIR, filename);
        
        // 4. 获取规范路径
        String canonicalPath = file.getCanonicalPath();
        String canonicalBaseDir = new File(UPLOAD_DIR).getCanonicalPath();
        
        // 5. 验证文件路径是否在允许的目录内
        if (!canonicalPath.startsWith(canonicalBaseDir)) {
            throw new SecurityException("不允许访问该文件");
        }
        
        // 6. 验证文件是否存在
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("文件不存在");
        }
        
        // 7. 下载文件
        InputStream fis = Files.newInputStream(file.toPath());
        byte[] buffer = new byte[fis.available()];
        fis.read(buffer);
        
        response.addHeader("Content-Disposition", 
            "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
        response.getOutputStream().write(buffer);
        
        return "文件下载成功";
    } catch (Exception e) {
        return "下载失败: " + e.getMessage();
    }
}`

const fixCode3 = `// 正确示例2：使用文件名映射（ID映射）
private static final Map<String, String> FILE_MAP = new HashMap<>();

@RequestMapping("/download")
public String readFile(String fileId, HttpServletResponse response) {
    // 1. 从映射表获取实际文件名
    String filename = FILE_MAP.get(fileId);
    if (filename == null) {
        throw new SecurityException("无效的文件ID");
    }
    
    // 2. 验证文件名格式
    if (!filename.matches("^[a-zA-Z0-9._-]+\\.(jpg|png|pdf)$")) {
        throw new SecurityException("无效的文件名");
    }
    
    // 3. 构建文件路径（使用映射的文件名）
    File file = new File(UPLOAD_DIR, filename);
    String canonicalPath = file.getCanonicalPath();
    String canonicalBaseDir = new File(UPLOAD_DIR).getCanonicalPath();
    
    // 4. 验证路径
    if (!canonicalPath.startsWith(canonicalBaseDir)) {
        throw new SecurityException("不允许访问该文件");
    }
    
    // 下载文件...
}`

const fixCode4 = `// 正确示例3：白名单验证
private static final String[] ALLOWED_FILES = {
    "document1.pdf",
    "image1.jpg",
    "report.xlsx"
};

@RequestMapping("/download")
public String readFile(String filename) {
    // 验证文件名是否在白名单中
    boolean isAllowed = false;
    for (String allowedFile : ALLOWED_FILES) {
        if (allowedFile.equals(filename)) {
            isAllowed = true;
            break;
        }
    }
    
    if (!isAllowed) {
        throw new SecurityException("不允许下载该文件");
    }
    
    // 下载文件...
}`

const formState = reactive({
  filename: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/file/download', {
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
  formState.filename = ''
  result.value = ''
}
</script>
