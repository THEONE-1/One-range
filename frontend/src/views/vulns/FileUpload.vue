<template>
  <vuln-template
    title="文件上传漏洞"
    subtitle="文件上传风险"
    level="中危"
    :icon="CloudUploadOutlined"
    moduleKey="file_upload"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>文件上传漏洞</strong>是指应用程序没有对上传的文件进行充分的安全检查,导致攻击者可以上传恶意文件。
      </a-typography-paragraph>
      <a-typography-paragraph>
        文件上传漏洞的危害：
      </a-typography-paragraph>
      <ul>
        <li>上传WebShell获取服务器控制权</li>
        <li>上传恶意脚本文件(jsp, php等)</li>
        <li>绕过文件类型检测</li>
        <li>可能导致服务器被完全控制</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的绕过方法：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        shell.jsp
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">直接上传WebShell</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">
        shell.jsp;.jpg
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">使用分号绕过黑名单</span>
      </a-space>

      <a-typography-paragraph code copyable style="margin-top: 12px">
        shell.jsp%00.jpg
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">使用%00截断绕过检测</span>
      </a-space>

      <a-alert
        message="必须对上传文件的类型、大小、内容进行严格检查！"
        type="error"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <div class="file-upload-container">
        <a-card title="文件上传功能" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            上传您的文件到服务器，支持图片、文档等多种格式。
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="选择文件"
            name="file"
            :rules="[{ required: true, message: '请选择文件' }]"
          >
            <a-upload
              v-model:file-list="fileList"
              :before-upload="beforeUpload"
              :max-count="1"
            >
              <a-button size="large">
                <upload-outlined /> 选择文件
              </a-button>
            </a-upload>
            <div style="margin-top: 8px; color: #8c8c8c; font-size: 12px;">
              支持常见文件格式：jpg, png, pdf, doc, txt等
            </div>
          </a-form-item>

          <a-form-item>
            <a-checkbox v-model:checked="formState.check">
              启用文件类型检测（安全模式）
            </a-checkbox>
          </a-form-item>
          
          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><cloud-upload-outlined /></template>
                上传文件
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
      <p><strong>文件上传绕过技巧：</strong></p>
      <ul style="padding-left: 20px; margin-bottom: 0;">
        <li>修改文件扩展名：<code>shell.jsp</code> → <code>shell.jsp;.jpg</code></li>
        <li>双写绕过：<code>shell.jjspsp</code> (过滤jsp后变成shell.jsp)</li>
        <li>大小写绕过：<code>shell.JsP</code></li>
        <li>空字节绕过：<code>shell.jsp%00.jpg</code></li>
        <li>MIME类型伪造：修改Content-Type为image/jpeg</li>
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
public class UploadVul {
    private static final String UPLOADED_FOLDER = 
        System.getProperty("user.dir") + "/src/main/resources/static/upload/";
    
    @PostMapping("/upload")
    public String uFile(@RequestParam("file") MultipartFile file, 
                       String check, HttpServletRequest request) {
        if (file.isEmpty()) {
            return "请选择要上传的文件!";
        }
        
        String fileName = file.getOriginalFilename();
        
        // 漏洞：安全检测是可选的，默认不启用
        if (check != null && check.equals("true")) {
            // 只检查扩展名，可以被绕过
            String suffix = fileName.substring(fileName.lastIndexOf("."));
            String[] safeSuffixes = {".jpg", ".png", ".jpeg", ".gif", ".bmp", ".ico"};
            boolean flag = false;
            for (String s : safeSuffixes) {
                if (suffix.toLowerCase().equals(s)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                return "不允许的文件类型";
            }
        }
        
        // 漏洞：直接使用原文件名，没有重命名
        // 漏洞：存储在Web可访问目录，可能被执行
        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER + fileName);
            Files.write(path, bytes);
            
            return "上传成功，访问地址: " + basePath + "/upload/" + fileName;
        } catch (Exception e) {
            return "上传失败: " + e.toString();
        }
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：1. 安全检测是可选的，默认不启用；2. 只检查扩展名，可以通过双写、大小写等方式绕过；3. 没有检查文件内容（magic bytes）；4. 使用原文件名，可能覆盖系统文件；5. 存储在Web目录，可能被执行"
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
        <li><strong>文件类型白名单：</strong>只允许特定类型的文件上传，使用白名单机制</li>
        <li><strong>文件内容检测：</strong>检查文件头magic bytes，不只看扩展名</li>
        <li><strong>重命名文件：</strong>使用UUID等随机名称，不使用原文件名</li>
        <li><strong>独立存储：</strong>将上传文件存储在Web目录外，或不可执行目录</li>
        <li><strong>病毒扫描：</strong>对上传文件进行病毒扫描</li>
        <li><strong>大小限制：</strong>限制上传文件的大小</li>
        <li><strong>文件名验证：</strong>验证文件名格式，防止路径遍历</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        @PostMapping("/upload")
        public String uFile(@RequestParam("file") MultipartFile file) {
            String fileName = file.getOriginalFilename();
            // 直接使用原文件名，没有验证
            Files.write(Paths.get(UPLOADED_FOLDER + fileName), file.getBytes());
            return "上传成功";
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：文件类型白名单和内容检测
        private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".png", ".jpeg", ".gif"};
        private static final Map&lt;String, byte[]&gt; MAGIC_BYTES = new HashMap&lt;&gt;();
        static {
            MAGIC_BYTES.put(".jpg", new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF});
            MAGIC_BYTES.put(".png", new byte[]{(byte)0x89, 0x50, 0x4E, 0x47});
        }
        
        @PostMapping("/upload")
        public String uFile(@RequestParam("file") MultipartFile file) {
            // 1. 验证文件大小
            if (file.getSize() > 5 * 1024 * 1024) { // 5MB
                throw new SecurityException("文件过大");
            }
            
            // 2. 获取文件扩展名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                throw new SecurityException("无效的文件名");
            }
            
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            
            // 3. 验证扩展名是否在白名单中
            if (!Arrays.asList(ALLOWED_EXTENSIONS).contains(ext)) {
                throw new SecurityException("不允许的文件类型");
            }
            
            // 4. 验证文件内容（magic bytes）
            byte[] fileBytes = file.getBytes();
            byte[] expectedMagic = MAGIC_BYTES.get(ext);
            if (expectedMagic != null) {
                boolean isValid = true;
                for (int i = 0; i &lt; expectedMagic.length && i &lt; fileBytes.length; i++) {
                    if (fileBytes[i] != expectedMagic[i]) {
                        isValid = false;
                        break;
                    }
                }
                if (!isValid) {
                    throw new SecurityException("文件内容与扩展名不匹配");
                }
            }
            
            // 5. 生成新的文件名（UUID）
            String newFilename = UUID.randomUUID().toString() + ext;
            
            // 6. 验证文件名，防止路径遍历
            Path uploadPath = Paths.get(UPLOADED_FOLDER).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(newFilename).normalize();
            if (!filePath.startsWith(uploadPath)) {
                throw new SecurityException("无效的文件路径");
            }
            
            // 7. 保存文件
            Files.write(filePath, fileBytes);
            
            return "上传成功: " + newFilename;
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：存储在非Web目录
        private static final String UPLOADED_FOLDER = 
            System.getProperty("user.dir") + "/uploads/"; // 不在Web目录
        
        // 或者使用配置的独立存储路径
        @Value("${upload.path}")
private String uploadPath;</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：文件名验证
        private boolean isValidFilename(String filename) {
            // 只允许字母、数字、点、下划线、连字符
            if (!filename.matches("^[a-zA-Z0-9._-]+$")) {
                return false;
            }
            
            // 禁止路径分隔符
    if (filename.contains("/") || filename.contains("\\\\")) {
                return false;
            }
            
            // 禁止路径遍历
            if (filename.contains("..")) {
                return false;
            }
            
            return true;
}</code></pre>
      </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { CloudUploadOutlined, RedoOutlined, UploadOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')
const fileList = ref([])

const sourceCode = `@RestController
@RequestMapping("/home/file")
public class UploadVul {
    private static final String UPLOADED_FOLDER = 
        System.getProperty("user.dir") + "/src/main/resources/static/upload/";
    
    @PostMapping("/upload")
    public String uFile(@RequestParam("file") MultipartFile file, 
                       String check, HttpServletRequest request) {
        if (file.isEmpty()) {
            return "请选择要上传的文件!";
        }
        
        String fileName = file.getOriginalFilename();
        
        // 漏洞：安全检测是可选的，默认不启用
        if (check != null && check.equals("true")) {
            // 只检查扩展名，可以被绕过
            String suffix = fileName.substring(fileName.lastIndexOf("."));
            String[] safeSuffixes = {".jpg", ".png", ".jpeg", ".gif", ".bmp", ".ico"};
            boolean flag = false;
            for (String s : safeSuffixes) {
                if (suffix.toLowerCase().equals(s)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                return "不允许的文件类型";
            }
        }
        
        // 漏洞：直接使用原文件名，没有重命名
        // 漏洞：存储在Web可访问目录，可能被执行
        try {
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER + fileName);
            Files.write(path, bytes);
            
            return "上传成功，访问地址: " + basePath + "/upload/" + fileName;
        } catch (Exception e) {
            return "上传失败: " + e.toString();
        }
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
@PostMapping("/upload")
public String uFile(@RequestParam("file") MultipartFile file) {
    String fileName = file.getOriginalFilename();
    // 直接使用原文件名，没有验证
    Files.write(Paths.get(UPLOADED_FOLDER + fileName), file.getBytes());
    return "上传成功";
}`

const fixCode2 = `// 正确示例1：文件类型白名单和内容检测
private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".png", ".jpeg", ".gif"};
private static final Map<String, byte[]> MAGIC_BYTES = new HashMap<>();
static {
    MAGIC_BYTES.put(".jpg", new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF});
    MAGIC_BYTES.put(".png", new byte[]{(byte)0x89, 0x50, 0x4E, 0x47});
}

@PostMapping("/upload")
public String uFile(@RequestParam("file") MultipartFile file) {
    // 1. 验证文件大小
    if (file.getSize() > 5 * 1024 * 1024) { // 5MB
        throw new SecurityException("文件过大");
    }
    
    // 2. 获取文件扩展名
    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || !originalFilename.contains(".")) {
        throw new SecurityException("无效的文件名");
    }
    
    String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
    
    // 3. 验证扩展名是否在白名单中
    if (!Arrays.asList(ALLOWED_EXTENSIONS).contains(ext)) {
        throw new SecurityException("不允许的文件类型");
    }
    
    // 4. 验证文件内容（magic bytes）
    byte[] fileBytes = file.getBytes();
    byte[] expectedMagic = MAGIC_BYTES.get(ext);
    if (expectedMagic != null) {
        boolean isValid = true;
        for (int i = 0; i < expectedMagic.length && i < fileBytes.length; i++) {
            if (fileBytes[i] != expectedMagic[i]) {
                isValid = false;
                break;
            }
        }
        if (!isValid) {
            throw new SecurityException("文件内容与扩展名不匹配");
        }
    }
    
    // 5. 生成新的文件名（UUID）
    String newFilename = UUID.randomUUID().toString() + ext;
    
    // 6. 验证文件名，防止路径遍历
    Path uploadPath = Paths.get(UPLOADED_FOLDER).toAbsolutePath().normalize();
    Path filePath = uploadPath.resolve(newFilename).normalize();
    if (!filePath.startsWith(uploadPath)) {
        throw new SecurityException("无效的文件路径");
    }
    
    // 7. 保存文件
    Files.write(filePath, fileBytes);
    
    return "上传成功: " + newFilename;
}`

const fixCode3 = `// 正确示例2：存储在非Web目录
private static final String UPLOADED_FOLDER = 
    System.getProperty("user.dir") + "/uploads/"; // 不在Web目录

// 或者使用配置的独立存储路径
@Value("\${upload.path}")
private String uploadPath;`

const fixCode4 = `// 正确示例3：文件名验证
private boolean isValidFilename(String filename) {
    // 只允许字母、数字、点、下划线、连字符
    if (!filename.matches("^[a-zA-Z0-9._-]+$")) {
        return false;
    }
    
    // 禁止路径分隔符
    if (filename.contains("/") || filename.contains("\\\\")) {
        return false;
    }
    
    // 禁止路径遍历
    if (filename.contains("..")) {
        return false;
    }
    
    return true;
}`

const formState = reactive({
  file: '',
  check: false
})

const beforeUpload = (file) => {
  formState.file = file
  return false // 阻止自动上传
}

const handleSubmit = async () => {
  if (!formState.file) {
    message.warning('请选择要上传的文件')
    return
  }
  loading.value = true
  try {
    const formData = new FormData()
    formData.append('file', formState.file)
    if (formState.check) {
      formData.append('check', 'true')
    }
    const response = await axios.post('/home/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
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
  formState.file = ''
  fileList.value = []
  result.value = ''
}
</script>
