<template>
  <vuln-template
    title="SQL注入 - MyBatis LIKE"
    subtitle="MyBatis LIKE"
    level="高危"
    :icon="DatabaseOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>MyBatis LIKE注入</strong>是指在使用LIKE模糊查询时,使用${}拼接导致SQL注入。
      </a-typography-paragraph>
      <a-typography-paragraph>
        MyBatis LIKE注入的特点：
      </a-typography-paragraph>
      <ul>
        <li>${}会直接拼接SQL,不做转义</li>
        <li>LIKE场景容易被忽视</li>
        <li>可导致数据泄露</li>
        <li>应使用#{}或CONCAT函数</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        MyBatis LIKE注入payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        %' or 1=1 --
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">闭合引号并注入恒真条件</span>
      </a-space>

      <a-alert
        message="使用#{}而不是${},或使用CONCAT函数拼接%！"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <div class="user-search-container">
        <a-card title="用户搜索功能" :bordered="false" style="margin-bottom: 24px;">
          <a-typography-paragraph>
            输入用户名关键字，系统将为您搜索匹配的用户信息。
          </a-typography-paragraph>
        </a-card>
        
        <a-form :model="formState" @finish="handleSubmit" layout="vertical">
          <a-form-item
            label="用户名关键字"
            name="username"
            :rules="[{ required: true, message: '请输入用户名关键字' }]"
          >
            <a-input
              v-model:value="formState.username"
              size="large"
              placeholder="例如：admin、test"
              prefix="🔍"
            />
            <div style="margin-top: 8px; color: #8c8c8c; font-size: 12px;">
              支持模糊搜索，系统会自动添加%通配符
            </div>
          </a-form-item>

          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit" size="large" :loading="loading">
                <template #icon><search-outlined /></template>
                搜索用户
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
      <a-alert
        v-if="result && result.includes('用户名')"
        message="如果返回了多条数据或异常数据，说明存在LIKE注入漏洞。"
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
    @RequestMapping("/like")
    public String like(String username) {
        // 漏洞：直接将用户输入传递给Service，没有验证
        Admin admin = injectService.likeSearch(username);
        return "用户名: " + admin.getUsername() + ", 密码: " + admin.getPassword();
    }
}

// DAO层 - 使用@Select注解
@Mapper
public interface InjectDao {
    // 漏洞：使用${username}直接拼接，%${username}%会被直接替换
    // 攻击者可以输入: %' or 1=1 -- 来闭合引号并注入SQL
    @Select("Select * from users where username like '%${username}%'")
    Admin likeSearch(@Param("username") String username);
    
    // 注释中提到了安全写法，但实际代码使用了不安全的${}
    // 正确安全写法: @Select("Select * from users where username like concat('%',#{username}, '%')")
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：MyBatis中使用${username}直接拼接SQL，${}不会进行转义。攻击者可以输入%' or 1=1 --来闭合引号并注入SQL语句。LIKE场景应该使用#{}配合CONCAT函数。"
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
        <li><strong>使用#{}：</strong>使用#{}而不是${}，参数会被自动转义</li>
        <li><strong>CONCAT函数：</strong>使用CONCAT函数拼接%通配符，而不是字符串拼接</li>
        <li><strong>输入验证：</strong>验证用户输入，过滤特殊字符（单引号、分号等）</li>
        <li><strong>转义处理：</strong>对用户输入中的%和_进行转义，避免被当作通配符</li>
        <li><strong>长度限制：</strong>限制搜索关键字长度，避免过长输入</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        @Select("Select * from users where username like '%${username}%'")
        Admin likeSearch(@Param("username") String username);
// 攻击者可以输入: %' or 1=1 --</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：使用#{}和CONCAT函数
        @Select("Select * from users where username like concat('%',#{username},'%')")
        Admin likeSearch(@Param("username") String username);
// #{username}会被转义，无法注入SQL</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：使用MyBatis XML配置
        &lt;select id="likeSearch" resultType="Admin"&gt;
            SELECT * FROM users 
            WHERE username LIKE CONCAT('%', #{username}, '%')
&lt;/select&gt;</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：转义通配符
        @RequestMapping("/like")
        public String like(String username) {
            // 转义%和_，避免被当作通配符
    username = username.replace("%", "\\\\%")
                      .replace("_", "\\\\_");
            
            Admin admin = injectService.likeSearch(username);
            return "用户名: " + admin.getUsername() + ", 密码: " + admin.getPassword();
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：输入验证
        @RequestMapping("/like")
        public String like(String username) {
            // 验证输入格式（只允许字母、数字、中文、空格）
    if (!username.matches("^[a-zA-Z0-9\\\\u4e00-\\\\u9fa5\\\\s]+$")) {
                throw new IllegalArgumentException("用户名包含非法字符");
            }
            
            // 限制长度
            if (username.length() > 50) {
                throw new IllegalArgumentException("用户名过长");
            }
            
            Admin admin = injectService.likeSearch(username);
            return "用户名: " + admin.getUsername() + ", 密码: " + admin.getPassword();
}</code></pre>
      </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { DatabaseOutlined, SearchOutlined, RedoOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `// Controller层
@RestController
@RequestMapping("/home/sqli/mybatis")
public class MyBatis {
    @RequestMapping("/like")
    public String like(String username) {
        // 漏洞：直接将用户输入传递给Service，没有验证
        Admin admin = injectService.likeSearch(username);
        return "用户名: " + admin.getUsername() + ", 密码: " + admin.getPassword();
    }
}

// DAO层 - 使用@Select注解
@Mapper
public interface InjectDao {
    // 漏洞：使用\${username}直接拼接，%\${username}%会被直接替换
    // 攻击者可以输入: %' or 1=1 -- 来闭合引号并注入SQL
    @Select("Select * from users where username like '%\${username}%'")
    Admin likeSearch(@Param("username") String username);
    
    // 注释中提到了安全写法，但实际代码使用了不安全的\${}
    // 正确安全写法: @Select("Select * from users where username like concat('%',#{username}, '%')")
}`

const fixCode1 = `// 错误示例(易受攻击)
@Select("Select * from users where username like '%\${username}%'")
Admin likeSearch(@Param("username") String username);
// 攻击者可以输入: %' or 1=1 --`

const fixCode2 = `// 正确示例1：使用#{}和CONCAT函数
@Select("Select * from users where username like concat('%',#{username},'%')")
Admin likeSearch(@Param("username") String username);
// #{username}会被转义，无法注入SQL`

const fixCode3 = `// 正确示例2：使用MyBatis XML配置
<select id="likeSearch" resultType="Admin">
    SELECT * FROM users 
    WHERE username LIKE CONCAT('%', #{username}, '%')
</select>`

const fixCode4 = `// 正确示例3：转义通配符
@RequestMapping("/like")
public String like(String username) {
    // 转义%和_，避免被当作通配符
    username = username.replace("%", "\\\\%")
                      .replace("_", "\\\\_");
    
    Admin admin = injectService.likeSearch(username);
    return "用户名: " + admin.getUsername() + ", 密码: " + admin.getPassword();
}`

const fixCode5 = `// 正确示例4：输入验证
@RequestMapping("/like")
public String like(String username) {
    // 验证输入格式（只允许字母、数字、中文、空格）
    if (!username.matches("^[a-zA-Z0-9\\\\u4e00-\\\\u9fa5\\\\s]+$")) {
        throw new IllegalArgumentException("用户名包含非法字符");
    }
    
    // 限制长度
    if (username.length() > 50) {
        throw new IllegalArgumentException("用户名过长");
    }
    
    Admin admin = injectService.likeSearch(username);
    return "用户名: " + admin.getUsername() + ", 密码: " + admin.getPassword();
}`

const formState = reactive({
  username: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/sqli/mybatis/like', {
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
  formState.username = ''
  result.value = ''
}
</script>
