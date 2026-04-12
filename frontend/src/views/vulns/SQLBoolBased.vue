<template>
  <vuln-template
    title="SQL注入 - 布尔盲注"
    subtitle="布尔盲注"
    level="高危"
    :icon="DatabaseOutlined"
    moduleKey="sql_bool"
  >
    <template #description>
      <a-typography-paragraph>
        场景：用户信息查询接口，返回“存在/不存在”布尔结果。未使用参数化，直接拼接 id 条件。
      </a-typography-paragraph>
      <a-typography-paragraph>
        风险：攻击者可构造布尔表达式，通过返回真假逐位猜测数据（库名、用户名、密码）。
      </a-typography-paragraph>
    </template>

    <template #payload>
      <a-typography-paragraph>示例 payload：</a-typography-paragraph>
      <a-typography-paragraph code copyable>1 and 1=1</a-typography-paragraph>
      <a-typography-paragraph code copyable>1 and 1=2</a-typography-paragraph>
      <a-typography-paragraph code copyable>
        1 and ascii(substr(database(),1,1)) &gt; 100
      </a-typography-paragraph>
      <a-alert
        message="真实系统应使用参数化查询，并避免回显布尔差异"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <div class="lab-surface">
        <h3 class="lab-surface-title lab-surface-title--accent">用户信息查询（布尔型）</h3>
        <a-form :model="formState" @finish="handleSubmit" layout="inline" class="lab-inline-form">
          <a-form-item
            name="id"
            :rules="[{ required: true, message: '请输入用户ID' }]"
          >
            <a-input
              v-model:value="formState.id"
              placeholder="例如：1 "
              size="large"
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

      <div v-if="result" class="lab-surface lab-surface-muted">
        <h4 class="lab-surface-title">查询结果</h4>
        <div class="lab-result-box">
          <pre>{{ result }}</pre>
        </div>
      </div>
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
    @GetMapping("/blind_bool_based")
    public String blind_bool_based(String id) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(db_url, db_user, db_pass);

            // 漏洞：直接拼接用户输入
            String sql = "select * from users where id = " + id;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            boolean exists = rs.next();
            rs.close();
            stmt.close();
            conn.close();

            return exists ? "存在该用户" : "用户不存在";
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}</code></pre>
        </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：未使用参数化，且仅返回布尔结果，方便布尔盲注逐位猜解数据。"
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
        <li>使用 PreparedStatement 参数化查询</li>
        <li>限制/校验 id 参数为纯数字</li>
        <li>错误与结果返回保持一致，避免布尔差异</li>
        <li>必要时增加 WAF/审计</li>
      </ul>
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
    @GetMapping("/blind_bool_based")
    public String blind_bool_based(String id) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(db_url, db_user, db_pass);

            String sql = "select * from users where id = " + id;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            boolean exists = rs.next();
            rs.close();
            stmt.close();
            conn.close();

            return exists ? "存在该用户" : "用户不存在";
        } catch (Exception e) {
            return "错误: " + e.toString();
        }
    }
}`

const formState = reactive({
  id: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/sqli/jdbc/blind_bool_based', {
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

