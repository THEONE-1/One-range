<template>
  <vuln-template
    title="未授权访问"
    subtitle="Unauthorized Access"
    level="中危"
    :icon="UnlockOutlined"
    moduleKey="unauth"
  >
    <template #description>
      <a-typography-paragraph>
        <strong>未授权访问</strong>是指由于权限配置不当,攻击者无需认证即可访问敏感资源或执行特权操作。
      </a-typography-paragraph>
      <a-typography-paragraph>
        未授权访问的危害：
      </a-typography-paragraph>
      <ul>
        <li>访问管理后台功能</li>
        <li>查看敏感数据和配置</li>
        <li>执行管理员操作</li>
        <li>可能导致系统完全失陷</li>
      </ul>
    </template>

    <template #payload>
      <a-typography-paragraph>
        常见的未授权访问路径：
      </a-typography-paragraph>
      <a-typography-paragraph code copyable>
        /admin
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">管理后台</span>
      </a-space>
      
      <a-typography-paragraph code copyable style="margin-top: 12px">
        /api/user/list
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">敏感API接口</span>
      </a-space>

      <a-typography-paragraph code copyable style="margin-top: 12px">
        /actuator/env
      </a-typography-paragraph>
      <a-space direction="vertical" style="width: 100%">
        <span style="color: #95a5a6">Spring Boot Actuator端点</span>
      </a-space>

      <a-alert
        message="必须对所有敏感资源和接口进行严格的权限控制！"
        type="warning"
        show-icon
        style="margin-top: 12px"
      />
    </template>

    <template #test-form>
      <!-- 模拟真实用户信息查询页面 -->
      <div style="background: #fff; padding: 24px; border-radius: 8px; margin-bottom: 24px;">
        <h3 style="margin-bottom: 16px; color: #1890ff;">👤 用户信息查询（未授权访问）</h3>
        <a-alert
          message="此接口未进行权限验证，任何人都可以查询用户信息"
          type="warning"
          show-icon
          style="margin-bottom: 16px"
        />
        <a-form :model="formState" @finish="handleSubmit" layout="inline" style="width: 100%">
          <a-form-item
            name="username"
            style="flex: 1; margin-right: 8px;"
          >
            <a-input
              v-model:value="formState.username"
              placeholder="请输入用户名（如：admin）或输入 all 查询所有用户"
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
          <pre style="margin: 0; white-space: pre-wrap;">{{ result }}</pre>
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
@RequestMapping("/unauth")
public class UnauthVul {
    @Autowired
    AdminService adminService;
    
    @RequestMapping("/userinfo")
    public String adminInfo(String username) {
        ArrayList&lt;Admin&gt; userInfo = new ArrayList&lt;&gt;();
        
        // 漏洞：没有进行权限验证，任何人都可以查询用户信息
        // 没有检查用户是否登录，没有检查用户权限
        if (username == null || username.equals("")) {
            return "请输入用户名！";
        }
        
        if (username.equals("all")) {
            // 漏洞：允许查询所有用户信息，没有权限控制
            userInfo = adminService.getAllInfo();
        } else {
            Admin admin = adminService.getInfoByUserName(username);
            if (admin == null) {
                return "用户不存在!";
            }
            userInfo.add(admin);
        }
        
        // 返回敏感信息（用户名、密码、角色、权限）
        StringBuilder result = new StringBuilder();
        result.append("查询结果:\\n");
        for (Admin admin : userInfo) {
            result.append("用户名: ").append(admin.getUsername())
                  .append(", 密码: ").append(admin.getPassword())
                  .append(", 角色: ").append(admin.getRole())
                  .append(", 权限: ").append(admin.getPerms()).append("\\n");
        }
        return result.toString();
    }
}</code></pre>
        </a-typography-paragraph>
      </div>
      <a-alert
        message="漏洞点：1. 没有进行身份认证，未登录用户也可以访问；2. 没有进行权限验证，普通用户也可以查询管理员信息；3. 返回了敏感信息（密码、权限等）；4. 允许查询所有用户信息（username=all）"
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
        <li><strong>身份认证：</strong>要求用户必须登录才能访问，使用Session或JWT验证</li>
        <li><strong>权限验证：</strong>验证用户是否有权限查询其他用户信息</li>
        <li><strong>最小权限原则：</strong>用户只能查询自己的信息，或经过授权的信息</li>
        <li><strong>敏感信息脱敏：</strong>不返回密码等敏感信息，或进行脱敏处理</li>
        <li><strong>禁止批量查询：</strong>禁止查询所有用户信息的功能</li>
        <li><strong>使用Spring Security：</strong>使用Spring Security框架统一管理认证和授权</li>
        <li><strong>审计日志：</strong>记录所有敏感操作，便于审计</li>
      </ul>
      
      <div style="margin-top: 16px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode1" style="margin: 0;">
          <pre class="code-block"><code>// 错误示例(易受攻击)
        @RequestMapping("/userinfo")
        public String adminInfo(String username) {
            // 没有认证和授权检查
            Admin admin = adminService.getInfoByUserName(username);
            return "用户名: " + admin.getUsername() + 
                   ", 密码: " + admin.getPassword();
        }</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode2" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例1：身份认证和权限验证
        @RequestMapping("/userinfo")
        public String adminInfo(String username, HttpSession session) {
            // 1. 验证用户是否登录
            String currentUser = (String) session.getAttribute("username");
            if (currentUser == null) {
                throw new SecurityException("请先登录");
            }
            
            // 2. 验证权限（只有管理员可以查询其他用户信息）
            Admin currentAdmin = adminService.getInfoByUserName(currentUser);
            if (currentAdmin == null || !currentAdmin.getRole().equals("admin")) {
                // 普通用户只能查询自己的信息
                if (!currentUser.equals(username)) {
                    throw new SecurityException("无权查询其他用户信息");
                }
            }
            
            // 3. 禁止查询所有用户
            if (username.equals("all")) {
                throw new SecurityException("不允许查询所有用户");
            }
            
            // 4. 查询用户信息
            Admin admin = adminService.getInfoByUserName(username);
            if (admin == null) {
                return "用户不存在";
            }
            
            // 5. 脱敏处理，不返回密码
            return "用户名: " + admin.getUsername() + 
                   ", 角色: " + admin.getRole();
        }</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode3" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例2：使用Spring Security注解
        import org.springframework.security.access.prepost.PreAuthorize;
        
        @RestController
        @RequestMapping("/unauth")
        public class UnauthVul {
            @RequestMapping("/userinfo")
            @PreAuthorize("hasRole('ADMIN')") // 只有管理员可以访问
            public String adminInfo(String username) {
                // 只有管理员可以访问此接口
                Admin admin = adminService.getInfoByUserName(username);
                return "用户名: " + admin.getUsername();
            }
            
            @RequestMapping("/myinfo")
            @PreAuthorize("isAuthenticated()") // 已登录用户
            public String myInfo(Principal principal) {
                // 用户只能查询自己的信息
                String username = principal.getName();
                Admin admin = adminService.getInfoByUserName(username);
                return "用户名: " + admin.getUsername();
            }
        }</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode4" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例3：使用拦截器统一验证
        @Component
        public class AuthInterceptor implements HandlerInterceptor {
            @Override
            public boolean preHandle(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   Object handler) {
                HttpSession session = request.getSession();
                String username = (String) session.getAttribute("username");
                
                if (username == null) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    return false;
                }
                
                // 检查权限
                Admin admin = adminService.getInfoByUserName(username);
                if (admin == null || !admin.getRole().equals("admin")) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    return false;
                }
                
                return true;
            }
        }</code></pre>
        </a-typography-paragraph>
      </div>
      
      <div style="margin-top: 12px; position: relative;">
        <a-typography-paragraph copyable :copy-text="fixCode5" style="margin: 0;">
          <pre class="code-block"><code>// 正确示例4：敏感信息脱敏
        @RequestMapping("/userinfo")
        public String adminInfo(String username, HttpSession session) {
            // 认证和授权检查...
            
            Admin admin = adminService.getInfoByUserName(username);
            
            // 脱敏处理
            StringBuilder result = new StringBuilder();
            result.append("用户名: ").append(admin.getUsername()).append("\\n");
            result.append("角色: ").append(admin.getRole()).append("\\n");
            // 不返回密码
            // result.append("密码: ").append("***"); // 或返回脱敏后的密码
            
            return result.toString();
        }

        // 正确示例5：使用DTO返回，不返回敏感字段
        public class UserInfoDTO {
            private String username;
            private String role;
            // 不包含password字段
            
            // getter/setter...
        }
        
        @RequestMapping("/userinfo")
        public UserInfoDTO adminInfo(String username) {
            // 认证和授权检查...
            
            Admin admin = adminService.getInfoByUserName(username);
            UserInfoDTO dto = new UserInfoDTO();
            dto.setUsername(admin.getUsername());
            dto.setRole(admin.getRole());
            // 不设置password
            
            return dto;
        }</code></pre>
        </a-typography-paragraph>
      </div>
    </template>
  </vuln-template>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { UnlockOutlined, PlayCircleOutlined, RedoOutlined, SearchOutlined } from '@ant-design/icons-vue'
import VulnTemplate from '@/components/VulnTemplate.vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')

const sourceCode = `@RestController
@RequestMapping("/unauth")
public class UnauthVul {
    @Autowired
    AdminService adminService;
    
    @RequestMapping("/userinfo")
    public String adminInfo(String username) {
        ArrayList<Admin> userInfo = new ArrayList<>();
        
        // 漏洞：没有进行权限验证，任何人都可以查询用户信息
        // 没有检查用户是否登录，没有检查用户权限
        if (username == null || username.equals("")) {
            return "请输入用户名！";
        }
        
        if (username.equals("all")) {
            // 漏洞：允许查询所有用户信息，没有权限控制
            userInfo = adminService.getAllInfo();
        } else {
            Admin admin = adminService.getInfoByUserName(username);
            if (admin == null) {
                return "用户不存在!";
            }
            userInfo.add(admin);
        }
        
        // 返回敏感信息（用户名、密码、角色、权限）
        StringBuilder result = new StringBuilder();
        result.append("查询结果:\\n");
        for (Admin admin : userInfo) {
            result.append("用户名: ").append(admin.getUsername())
                  .append(", 密码: ").append(admin.getPassword())
                  .append(", 角色: ").append(admin.getRole())
                  .append(", 权限: ").append(admin.getPerms()).append("\\n");
        }
        return result.toString();
    }
}`

const fixCode1 = `// 错误示例(易受攻击)
        @RequestMapping("/userinfo")
        public String adminInfo(String username) {
            // 没有认证和授权检查
            Admin admin = adminService.getInfoByUserName(username);
            return "用户名: " + admin.getUsername() + 
                   ", 密码: " + admin.getPassword();
        }`

const fixCode2 = `// 正确示例1：身份认证和权限验证
        @RequestMapping("/userinfo")
        public String adminInfo(String username, HttpSession session) {
            // 1. 验证用户是否登录
            String currentUser = (String) session.getAttribute("username");
            if (currentUser == null) {
                throw new SecurityException("请先登录");
            }
            
            // 2. 验证权限（只有管理员可以查询其他用户信息）
            Admin currentAdmin = adminService.getInfoByUserName(currentUser);
            if (currentAdmin == null || !currentAdmin.getRole().equals("admin")) {
                // 普通用户只能查询自己的信息
                if (!currentUser.equals(username)) {
                    throw new SecurityException("无权查询其他用户信息");
                }
            }
            
            // 3. 禁止查询所有用户
            if (username.equals("all")) {
                throw new SecurityException("不允许查询所有用户");
            }
            
            // 4. 查询用户信息
            Admin admin = adminService.getInfoByUserName(username);
            if (admin == null) {
                return "用户不存在";
            }
            
            // 5. 脱敏处理，不返回密码
            return "用户名: " + admin.getUsername() + 
                   ", 角色: " + admin.getRole();
        }`

const fixCode3 = `// 正确示例2：使用Spring Security注解
        import org.springframework.security.access.prepost.PreAuthorize;
        
        @RestController
        @RequestMapping("/unauth")
        public class UnauthVul {
            @RequestMapping("/userinfo")
            @PreAuthorize("hasRole('ADMIN')") // 只有管理员可以访问
            public String adminInfo(String username) {
                // 只有管理员可以访问此接口
                Admin admin = adminService.getInfoByUserName(username);
                return "用户名: " + admin.getUsername();
            }
            
            @RequestMapping("/myinfo")
            @PreAuthorize("isAuthenticated()") // 已登录用户
            public String myInfo(Principal principal) {
                // 用户只能查询自己的信息
                String username = principal.getName();
                Admin admin = adminService.getInfoByUserName(username);
                return "用户名: " + admin.getUsername();
            }
        }`

const fixCode4 = `// 正确示例3：使用拦截器统一验证
        @Component
        public class AuthInterceptor implements HandlerInterceptor {
            @Override
            public boolean preHandle(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   Object handler) {
                HttpSession session = request.getSession();
                String username = (String) session.getAttribute("username");
                
                if (username == null) {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    return false;
                }
                
                // 检查权限
                Admin admin = adminService.getInfoByUserName(username);
                if (admin == null || !admin.getRole().equals("admin")) {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    return false;
                }
                
                return true;
            }
        }`

const fixCode5 = `// 正确示例4：敏感信息脱敏
        @RequestMapping("/userinfo")
        public String adminInfo(String username, HttpSession session) {
            // 认证和授权检查...
            
            Admin admin = adminService.getInfoByUserName(username);
            
            // 脱敏处理
            StringBuilder result = new StringBuilder();
            result.append("用户名: ").append(admin.getUsername()).append("\\n");
            result.append("角色: ").append(admin.getRole()).append("\\n");
            // 不返回密码
            // result.append("密码: ").append("***"); // 或返回脱敏后的密码
            
            return result.toString();
        }

        // 正确示例5：使用DTO返回，不返回敏感字段
        public class UserInfoDTO {
            private String username;
            private String role;
            // 不包含password字段
            
            // getter/setter...
        }
        
        @RequestMapping("/userinfo")
        public UserInfoDTO adminInfo(String username) {
            // 认证和授权检查...
            
            Admin admin = adminService.getInfoByUserName(username);
            UserInfoDTO dto = new UserInfoDTO();
            dto.setUsername(admin.getUsername());
            dto.setRole(admin.getRole());
            // 不设置password
            
            return dto;
        }`

const formState = reactive({
  username: 'admin'
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const response = await axios.get('/unauth/userinfo', {
      params: { username: formState.username },
      responseType: 'text'
    })
    result.value = response.data
    message.success('查询完成')
  } catch (error) {
    const errorMsg = error.response?.data || error.message
    result.value = typeof errorMsg === 'string' ? errorMsg : JSON.stringify(errorMsg)
    message.error('查询失败：' + result.value)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  formState.username = 'admin'
  result.value = ''
}
</script>
