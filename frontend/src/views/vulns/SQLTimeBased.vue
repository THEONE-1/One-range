<template>
  <vuln-template
    title="SQL注入 - 时间盲注"
    subtitle="SQL Injection - Time Based Blind"
    level="高危"
    :icon="DatabaseOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>时间盲注 (Time Based Blind SQL Injection)</strong> 是一种通过时间延迟来判断SQL语句是否执行的盲注技术。
      </a-typography-paragraph>
      <a-typography-paragraph>
        时间盲注的特点：
      </a-typography-paragraph>
      <ul>
        <li>页面没有明显的回显和错误信息</li>
        <li>通过sleep()函数延迟响应时间来判断</li>
        <li>需要逐字符猜测数据，速度较慢</li>
        <li>使用if条件判断和sleep函数组合</li>
        <li>可以绕过很多WAF防护</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的时间盲注payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>1 and sleep(5)</a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">延迟5秒，测试是否存在注入</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">1 and if(1=1,sleep(5),0)</a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">条件为真时延迟5秒</span>
      </a-space>

      <a-typography-paragraph code copyable style="margin-top: 12px">1 and if(ascii(substring(database(),1,1))>100,sleep(5),0)</a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">判断数据库名第一个字符的ASCII值</span>
      </a-space>

      <a-alert
        message="时间盲注需要耐心等待，每次判断都需要等待延迟时间！"
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
        <a-alert
          v-if="responseTime > 3000"
          :message="`响应时间: ${responseTime}ms，可能存在时间盲注漏洞`"
          type="warning"
          show-icon
          style="margin-top: 16px"
        />
      </div>
    </template>

    <template #input-hint>
      <a-alert message="攻击示例流程" type="warning" show-icon style="margin-bottom: 12px;" />
      <p><strong>完整攻击流程：</strong></p>
      <ol style="padding-left: 20px; margin-bottom: 0; line-height: 2;">
        <li><strong>步骤1：</strong>测试注入点 - 输入 <a-typography-text code copyable>1 and sleep(5)</a-typography-text>，如果响应延迟5秒，说明存在注入</li>
        <li><strong>步骤2：</strong>判断数据库名长度 - 输入 <a-typography-text code copyable>1 and if(length(database())=8,sleep(5),0)</a-typography-text>，逐步测试长度</li>
        <li><strong>步骤3：</strong>逐字符获取数据库名 - 输入 <a-typography-text code copyable>1 and if(ascii(substring(database(),1,1))=111,sleep(5),0)</a-typography-text>，使用二分法快速判断</li>
        <li><strong>步骤4：</strong>获取表名数量 - 输入 <a-typography-text code copyable>1 and if((select count(*) from information_schema.tables where table_schema=database())=2,sleep(5),0)</a-typography-text></li>
        <li><strong>步骤5：</strong>获取表名 - 使用substring和ascii函数逐字符获取表名</li>
        <li><strong>步骤6：</strong>获取列名 - 同样使用逐字符方式获取</li>
        <li><strong>步骤7：</strong>获取数据 - 逐字符获取用户名和密码</li>
      </ol>
      <p style="margin-top: 12px;"><strong>技巧：</strong>使用二分法可以快速缩小字符范围，例如先判断是否大于128，再判断是否大于64，以此类推。</p>
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
    @GetMapping("/blind_time_based")
    public String blind_time_based(String id) {
        try {
            Connection conn = DriverManager.getConnection(db_url, db_user, db_pass);
            // 漏洞：SQL语句是拼接的，攻击者可以注入sleep()函数
            String sql = "select * from users where id = " + id;
            // 错误：先拼接SQL再创建PreparedStatement，无法防止注入
            PreparedStatement st = conn.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            
            if (rs.next()) {
                return "查询成功！";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "查询失败！";
    }
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：SQL语句是字符串拼接的，攻击者可以注入sleep()、benchmark()等时间延迟函数进行盲注攻击。即使没有回显，也可以通过响应时间判断SQL是否执行成功。"
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
        <li><strong>输入验证：</strong>对用户输入进行类型和格式验证，严格限制输入范围</li>
        <li><strong>函数过滤：</strong>在应用层过滤sleep、benchmark等时间延迟函数</li>
        <li><strong>数据库权限：</strong>限制数据库用户权限，禁止执行sleep、benchmark等函数</li>
        <li><strong>响应时间监控：</strong>监控异常长的响应时间，可能是盲注攻击</li>
        <li><strong>WAF防护：</strong>部署Web应用防火墙检测和拦截盲注攻击</li>
        <li><strong>SQL审计：</strong>记录所有SQL查询，检测异常模式</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        String sql = "SELECT * FROM users WHERE id = " + id;
        PreparedStatement st = conn.prepareStatement(sql);
        // 攻击者可以输入: 1 and sleep(5)
ResultSet rs = st.executeQuery();</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：使用?占位符
        String sql = "SELECT * FROM users WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, Integer.parseInt(id)); // 参数会被转义，无法注入函数
ResultSet rs = pstmt.executeQuery();</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：输入验证和类型转换
        // 1. 验证输入格式（只允许数字）
        if (!id.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("ID必须是数字");
        }
        
        // 2. 转换为整数（防止字符串拼接）
        int userId = Integer.parseInt(id);
        
        // 3. 验证范围
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
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：数据库权限限制
        -- 创建只读用户，禁止执行sleep、benchmark等函数
        CREATE USER 'app_user'@'localhost' IDENTIFIED BY 'password';
        GRANT SELECT ON database_name.* TO 'app_user'@'localhost';
        REVOKE EXECUTE ON *.* FROM 'app_user'@'localhost';
-- 限制用户只能执行SELECT查询，不能执行函数</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：响应时间监控
        long startTime = System.currentTimeMillis();
        ResultSet rs = pstmt.executeQuery();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 如果响应时间异常长（超过3秒），记录日志并告警
        if (duration > 3000) {
            logger.warn("SQL查询响应时间异常: " + duration + "ms, SQL: " + sql);
            // 可以触发告警或限制该用户
}</code></pre>
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
const responseTime = ref(0)

const sourceCode = `@RestController
@RequestMapping("/home/sqli/jdbc")
public class JDBC {
    @GetMapping("/blind_time_based")
    public String blind_time_based(String id) {
        try {
            Connection conn = DriverManager.getConnection(db_url, db_user, db_pass);
            // 漏洞：SQL语句是拼接的，攻击者可以注入sleep()函数
            String sql = "select * from users where id = " + id;
            // 错误：先拼接SQL再创建PreparedStatement，无法防止注入
            PreparedStatement st = conn.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            
            if (rs.next()) {
                return "查询成功！";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "查询失败！";
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
String sql = "SELECT * FROM users WHERE id = " + id;
PreparedStatement st = conn.prepareStatement(sql);
// 攻击者可以输入: 1 and sleep(5)
ResultSet rs = st.executeQuery();`

const fixCode2 = `// 正确示例1：使用?占位符
String sql = "SELECT * FROM users WHERE id = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setInt(1, Integer.parseInt(id)); // 参数会被转义，无法注入函数
ResultSet rs = pstmt.executeQuery();`

const fixCode3 = `// 正确示例2：输入验证和类型转换
// 1. 验证输入格式（只允许数字）
if (!id.matches("^[0-9]+$")) {
    throw new IllegalArgumentException("ID必须是数字");
}

// 2. 转换为整数（防止字符串拼接）
int userId = Integer.parseInt(id);

// 3. 验证范围
if (userId < 1 || userId > 10000) {
    throw new IllegalArgumentException("ID超出范围");
}

// 4. 使用参数化查询
String sql = "SELECT * FROM users WHERE id = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setInt(1, userId);
ResultSet rs = pstmt.executeQuery();`

const fixCode4 = `// 正确示例3：数据库权限限制
-- 创建只读用户，禁止执行sleep、benchmark等函数
CREATE USER 'app_user'@'localhost' IDENTIFIED BY 'password';
GRANT SELECT ON database_name.* TO 'app_user'@'localhost';
REVOKE EXECUTE ON *.* FROM 'app_user'@'localhost';
-- 限制用户只能执行SELECT查询，不能执行函数`

const fixCode5 = `// 正确示例4：响应时间监控
long startTime = System.currentTimeMillis();
ResultSet rs = pstmt.executeQuery();
long endTime = System.currentTimeMillis();
long duration = endTime - startTime;

// 如果响应时间异常长（超过3秒），记录日志并告警
if (duration > 3000) {
    logger.warn("SQL查询响应时间异常: " + duration + "ms, SQL: " + sql);
    // 可以触发告警或限制该用户
}`

const formState = reactive({
  id: ''
})

const handleSubmit = async () => {
  loading.value = true
  responseTime.value = 0
  try {
    const startTime = Date.now()
    const response = await axios.get('/home/sqli/jdbc/blind_time_based', {
      params: formState
    })
    const endTime = Date.now()
    responseTime.value = endTime - startTime
    result.value = response.data + `\n响应时间: ${responseTime.value}ms`
    if (responseTime.value > 3000) {
      message.warning('响应时间较长，可能存在时间盲注')
    } else {
      message.success('查询完成')
    }
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
