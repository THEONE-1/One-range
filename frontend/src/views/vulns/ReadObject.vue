<template>
  <vuln-template
    title="Java反序列化 - 对象读取"
    subtitle="对象读取反序列化"
    level="严重"
    :icon="BlockOutlined"
    moduleKey="readobject"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>Java反序列化漏洞</strong>是指通过构造恶意的序列化数据,在反序列化时触发代码执行。
      </a-typography-paragraph>
      <a-typography-paragraph>
        Java反序列化的危害：
      </a-typography-paragraph>
      <ul>
        <li>通过Gadget链执行任意代码</li>
        <li>远程代码执行</li>
        <li>完全控制服务器</li>
        <li>是Java安全中最危险的漏洞之一</li>
      </ul>
      <a-typography-paragraph>
        常见的Gadget链：Commons Collections、CommonsIO、Spring等。
      </a-typography-paragraph>
    </template>

    <template #payload>
      <a-typography-paragraph>
        利用工具：
      </a-typography-paragraph>
      <a-typography-paragraph>
        • ysoserial - Java反序列化payload生成工具
      </a-typography-paragraph>
      <a-typography-paragraph code copyable style="margin-top: 12px">
        java -jar ysoserial.jar CommonsCollections1 "calc" | base64
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">生成CommonsCollections1链的payload</span>
      </a-space>

      <a-alert
        message="避免反序列化不可信数据,使用白名单机制！"
        type="error"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <div class="deserialize-container">
        <a-card title="数据反序列化服务" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            输入序列化的数据（Base64编码），系统将为您反序列化并恢复对象。
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="序列化数据（Base64）"
            name="payload"
            :rules="[{ required: true, message: '请输入序列化数据' }]"
          >
            <a-textarea
              v-model:value="formState.payload" :rows="6"
              size="large"
              placeholder="输入Base64编码的序列化数据"
            />
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><block-outlined /></template>
                反序列化
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
          <pre class="code-block"><code>@Controller
@RequestMapping("/home/deserialize")
public class ObjectInputStream {
    @RequestMapping("/readobject")
    public String objectInputStream(String content, Model model) {
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            // 处理Base64编码中的空格问题
            content = content.replace(" ", "+");
            
            // 解码Base64
            byte[] bytes = decoder.decodeBuffer(content);
            
            // 漏洞：直接反序列化用户输入的字节流
            // ObjectInputStream会反序列化任意类，攻击者可以使用Gadget链触发RCE
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(stream);
            in.readObject(); // 反序列化，可能触发恶意代码执行
            in.close();
            
            model.addAttribute("results", "执行成功！");
        } catch (Exception e) {
            model.addAttribute("results", e.toString());
        }
        return "deserialize/readobject";
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：ObjectInputStream.readObject()会反序列化任意类。攻击者可以使用ysoserial等工具生成恶意序列化数据（如CommonsCollections链），通过反序列化触发远程代码执行。"
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
        <li><strong>避免反序列化不可信数据：</strong>不要反序列化用户输入的数据</li>
        <li><strong>使用白名单：</strong>使用ObjectInputFilter限制可反序列化的类</li>
        <li><strong>使用替代方案：</strong>使用JSON、XML等格式替代Java序列化</li>
        <li><strong>升级JDK：</strong>使用JDK 9+的ObjectInputFilter功能</li>
        <li><strong>移除Gadget链：</strong>移除Commons Collections等危险依赖</li>
        <li><strong>输入验证：</strong>验证序列化数据的来源和格式</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        ObjectInputStream in = new ObjectInputStream(stream);
Object obj = in.readObject(); // 反序列化任意类，危险！</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：使用ObjectInputFilter（JDK 9+）
        @RequestMapping("/readobject")
        public String objectInputStream(String content) {
            try {
                byte[] bytes = Base64.getDecoder().decode(content);
                ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
                
                ObjectInputStream in = new ObjectInputStream(stream);
                
                // 设置过滤器，只允许特定类
                ObjectInputFilter filter = ObjectInputFilter.Config.createFilter(
                    "com.example.model.*;!*"
                );
                in.setObjectInputFilter(filter);
                
                Object obj = in.readObject();
                return "反序列化成功";
            } catch (Exception e) {
                return "错误: " + e.getMessage();
            }
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：使用JSON替代Java序列化（推荐）
        import com.fasterxml.jackson.databind.ObjectMapper;
        
        @RequestMapping("/readobject")
        public String objectInputStream(String content) {
            ObjectMapper mapper = new ObjectMapper();
            // JSON反序列化更安全，不会执行任意代码
            User user = mapper.readValue(content, User.class);
            return "解析成功: " + user.getName();
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：自定义ObjectInputStream，重写resolveClass
        public class SafeObjectInputStream extends ObjectInputStream {
            private static final String[] ALLOWED_CLASSES = {
                "com.example.model.User",
                "com.example.model.Order"
            };
            
            public SafeObjectInputStream(InputStream in) throws IOException {
                super(in);
            }
            
            @Override
            protected Class&lt;?&gt; resolveClass(ObjectStreamClass desc) 
                    throws IOException, ClassNotFoundException {
                String className = desc.getName();
                
                // 验证类名是否在白名单中
                boolean isAllowed = false;
                for (String allowed : ALLOWED_CLASSES) {
                    if (className.equals(allowed)) {
                        isAllowed = true;
                        break;
                    }
                }
                
                if (!isAllowed) {
                    throw new SecurityException("不允许反序列化类: " + className);
                }
                
                return super.resolveClass(desc);
            }
        }
        
        // 使用自定义的SafeObjectInputStream
        SafeObjectInputStream in = new SafeObjectInputStream(stream);
Object obj = in.readObject();</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：移除危险依赖
        // 移除Commons Collections等包含Gadget链的依赖
// 如果必须使用，升级到安全版本</code></pre>
      </a-typography-paragraph>
      </div>
      
      <a-typography-paragraph code copyable>
        &lt;dependency&gt;
            &lt;groupId&gt;org.apache.commons&lt;/groupId&gt;
            &lt;artifactId&gt;commons-collections4&lt;/artifactId&gt;
            &lt;version&gt;4.4&lt;/version&gt; &lt;!-- 使用安全版本 --&gt;
        &lt;/dependency&gt;
      </a-typography-paragraph>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { BlockOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@Controller
@RequestMapping("/home/deserialize")
public class ObjectInputStream {
    @RequestMapping("/readobject")
    public String objectInputStream(String content, Model model) {
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            // 处理Base64编码中的空格问题
            content = content.replace(" ", "+");
            
            // 解码Base64
            byte[] bytes = decoder.decodeBuffer(content);
            
            // 漏洞：直接反序列化用户输入的字节流
            // ObjectInputStream会反序列化任意类，攻击者可以使用Gadget链触发RCE
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(stream);
            in.readObject(); // 反序列化，可能触发恶意代码执行
            in.close();
            
            model.addAttribute("results", "执行成功！");
        } catch (Exception e) {
            model.addAttribute("results", e.toString());
        }
        return "deserialize/readobject";
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
ObjectInputStream in = new ObjectInputStream(stream);
Object obj = in.readObject(); // 反序列化任意类，危险！`

const fixCode2 = `// 正确示例1：使用ObjectInputFilter（JDK 9+）
@RequestMapping("/readobject")
public String objectInputStream(String content) {
    try {
        byte[] bytes = Base64.getDecoder().decode(content);
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        
        ObjectInputStream in = new ObjectInputStream(stream);
        
        // 设置过滤器，只允许特定类
        ObjectInputFilter filter = ObjectInputFilter.Config.createFilter(
            "com.example.model.*;!*"
        );
        in.setObjectInputFilter(filter);
        
        Object obj = in.readObject();
        return "反序列化成功";
    } catch (Exception e) {
        return "错误: " + e.getMessage();
    }
}`

const fixCode3 = `// 正确示例2：使用JSON替代Java序列化（推荐）
import com.fasterxml.jackson.databind.ObjectMapper;

@RequestMapping("/readobject")
public String objectInputStream(String content) {
    ObjectMapper mapper = new ObjectMapper();
    // JSON反序列化更安全，不会执行任意代码
    User user = mapper.readValue(content, User.class);
    return "解析成功: " + user.getName();
}`

const fixCode4 = `// 正确示例3：自定义ObjectInputStream，重写resolveClass
public class SafeObjectInputStream extends ObjectInputStream {
    private static final String[] ALLOWED_CLASSES = {
        "com.example.model.User",
        "com.example.model.Order"
    };
    
    public SafeObjectInputStream(InputStream in) throws IOException {
        super(in);
    }
    
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) 
            throws IOException, ClassNotFoundException {
        String className = desc.getName();
        
        // 验证类名是否在白名单中
        boolean isAllowed = false;
        for (String allowed : ALLOWED_CLASSES) {
            if (className.equals(allowed)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw new SecurityException("不允许反序列化类: " + className);
        }
        
        return super.resolveClass(desc);
    }
}

// 使用自定义的SafeObjectInputStream
SafeObjectInputStream in = new SafeObjectInputStream(stream);
Object obj = in.readObject();`

const fixCode5 = `// 正确示例4：移除危险依赖
// 移除Commons Collections等包含Gadget链的依赖
// 如果必须使用，升级到安全版本`

const formState = reactive({
  payload: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/deserialize/readobject', {
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
  formState.payload = ''
  result.value = ''
}
</script>
