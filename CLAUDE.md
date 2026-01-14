# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

One 安全靶场是一个专业的Web安全漏洞实验平台，包含26个漏洞演示模块，用于安全学习和研究。

**重要提醒**：本项目故意包含已知漏洞，仅供教育用途。禁止在生产环境部署，禁止用于非法用途。

## 技术栈

- **前端**：Vue 3 + Ant Design Vue + Vite + Vue Router + Pinia
- **后端**：Spring Boot 2.4.1 + MyBatis + MySQL 8.0
- **部署**：Docker Compose（三容器架构：frontend/backend/mysql）
- **Java版本**：1.8

## 开发命令

### 前端开发
```bash
cd frontend
npm install          # 安装依赖
npm run dev          # 启动开发服务器（http://localhost:5173）
npm run build        # 构建生产版本
npm run preview      # 预览生产构建
```

### 后端开发
```bash
# 使用Maven Wrapper
./mvnw clean package              # 构建项目（Linux/Mac）
mvnw.cmd clean package            # 构建项目（Windows）
./mvnw spring-boot:run            # 运行后端服务

# 或使用Maven
mvn clean package
mvn spring-boot:run
```

### Docker部署
```bash
docker-compose up -d              # 启动所有服务
docker-compose up -d backend mysql # 仅启动后端和数据库（用于前端开发）
docker-compose logs -f            # 查看日志
docker-compose restart            # 重启服务
docker-compose down               # 停止服务
docker-compose down -v            # 停止并删除数据卷
```

## 架构说明

### 后端架构（Spring Boot）

采用经典三层架构：

```
Controller（控制器层）
  ├── AdminController - 用户认证、登录、注册、验证码
  ├── LearningProgressController - 学习进度管理
  ├── basevul/ - 基础漏洞演示（19个）
  │   ├── XSSVul, CORSVul, RedirectVul, SSRFVul, UnauthVul, XFFVul
  │   ├── file/ - UploadVul, DownloadVul
  │   ├── rce/ - RuntimeExec, ProcessBuilderExec
  │   ├── sqli/ - JDBC
  │   └── xxe/ - XXE, Student
  ├── componentsvul/ - 组件漏洞（6个）
  │   ├── FastJson, Jackson, Log4j
  │   ├── actuators/WEPS
  │   └── shiro/ - Config, MyRealm, Shiro
  ├── deserialize/ - 反序列化漏洞（1个）
  │   └── ObjectInputStream
    ↓
Service（服务层）
  ├── AdminService / AdminServiceImpl - 用户管理（登录、注册）
  ├── LearningProgressService / LearningProgressServiceImpl - 学习进度管理
  ├── InjectService / InjectServiceImpl
  └── XssService / XssServiceImpl
    ↓
DAO（数据访问层）
  ├── AdminDao - 用户数据访问
  ├── LearningProgressDao - 学习进度数据访问
  ├── InjectDao
  └── XssDao
    ↓
MyBatis Mapper（XML映射）
  └── src/main/resources/mapper/*.xml
```

**关键配置**：
- `application.yml` - 数据库连接、端口（8000）、Session超时（6小时）、Actuator端点暴露
- `CorsConfig.java` - CORS跨域配置
- `MvcConfig.java` - MVC拦截器配置

### 前端架构（Vue 3）

```
src/
├── main.js - 应用入口（Vue实例、Router、Pinia）
├── App.vue - 根组件
├── router/index.js - 路由配置（26个漏洞页面路由 + 认证守卫）
├── views/
│   ├── Login.vue - 登录页面
│   ├── Register.vue - 用户注册页面
│   ├── Layout.vue - 主布局（侧边栏 + 内容区）
│   ├── Home.vue - 首页
│   ├── Progress.vue - 学习进度页面
│   └── vulns/ - 25个漏洞演示组件
│       ├── XSSReflect.vue, XSSStore.vue, XSSDOM.vue
│       ├── SQLIntBased.vue, SQLErrorBased.vue, SQLTimeBased.vue, SQLBoolBased.vue
│       ├── RCERuntime.vue, RCEProcessBuilder.vue
│       ├── FileUpload.vue, FileDownload.vue
│       ├── XXEXMLReader.vue, XXESAXReader.vue, XXESAXBuilder.vue
│       ├── SSRF.vue, XFF.vue, CORS.vue, Redirect.vue, Unauth.vue
│       ├── Fastjson.vue, Jackson.vue, Shiro.vue, Log4j.vue
│       ├── Actuators.vue
│       └── ReadObject.vue
└── components/
    └── VulnTemplate.vue - 漏洞页面通用模板组件（包含完成标记功能）
```

**路由守卫**：所有非登录/注册页面需要 `localStorage.getItem('username')` 验证。

**API通信**：使用 Axios，后端地址通过 Vite 代理配置（`vite.config.js`）。

### 数据库架构

- **数据库名**：`one_range`
- **初始化脚本**：`one_range.sql`（Docker启动时自动导入）
- **主要表**：
  - `users` - 用户账户表（包含管理员和普通用户，默认管理员：admin/admin）
  - `learning_progress` - 学习进度表（记录用户完成的漏洞模块）
  - `xss` - XSS存储型漏洞数据
  - 其他表根据漏洞模块需求

## 端口配置

| 服务 | 开发模式 | Docker模式 |
|------|---------|-----------|
| 前端 | 5173 (Vite) | 3000 (Nginx) |
| 后端 | 8000 | 8000 |
| MySQL | 3306/3307 | 3307→3306 |

## 添加新漏洞模块的步骤

1. **后端**：
   - 在 `src/main/java/com/one/range/controller/` 对应分类目录创建Controller
   - 使用 `@RestController` 或 `@Controller` + `@ResponseBody`
   - 添加漏洞演示接口（通常使用 `@PostMapping` 或 `@GetMapping`）
   - 如需数据库操作，创建对应的 Service、DAO、Mapper XML

2. **前端**：
   - 在 `frontend/src/views/vulns/` 创建Vue组件（参考 `VulnTemplate.vue`）
   - **重要**：必须传入 `moduleKey` 属性到 `VulnTemplate` 组件，用于学习进度跟踪
   - 在 `frontend/src/router/index.js` 添加路由配置
   - 在 `Layout.vue` 侧边栏菜单添加导航项

3. **测试**：
   - 启动后端和数据库：`docker-compose up -d backend mysql`
   - 启动前端开发服务器：`cd frontend && npm run dev`
   - 访问 http://localhost:5173 测试新模块

## 新功能说明（v2.1.0）

### 用户管理功能

**后端实现**：
- `AdminController.register()` - 用户注册接口（`/admin/register`）
- `AdminService.register()` - 注册业务逻辑，包含用户名重复检查
- `AdminDao.register()` - 用户数据插入，默认角色为 `user`
- 支持管理员（role=administrator）和普通用户（role=user）

**前端实现**：
- [Register.vue](frontend/src/views/Register.vue) - 用户注册页面
- 注册表单包含：用户名、密码、确认密码、验证码
- 注册成功后自动跳转到登录页面
- 登录页面添加"立即注册"链接

**路由配置**：
- `/register` - 注册页面路由
- 路由守卫已更新，支持注册页面免登录访问

### 学习进度管理功能

**后端实现**：
- `LearningProgressController` - 学习进度管理控制器
  - `POST /api/progress/complete` - 标记模块为已完成
  - `GET /api/progress/stats` - 获取用户进度统计（已完成数、总数、完成率）
  - `GET /api/progress/list` - 获取用户所有学习记录
  - `GET /api/progress/check` - 检查某个模块是否已完成
- `LearningProgressService` - 学习进度业务逻辑
- `LearningProgressDao` - 学习进度数据访问
- `learning_progress` 表 - 存储用户学习进度（user_id + vuln_module 唯一索引）

**前端实现**：
- [Progress.vue](frontend/src/views/Progress.vue) - 学习进度展示页面
  - 显示已完成模块数、总模块数、完成率
  - 进度条可视化展示
  - 学习记录列表（包含完成时间）
- [VulnTemplate.vue](frontend/src/components/VulnTemplate.vue) - 漏洞模板组件增强
  - 添加"标记完成"按钮
  - 自动检查模块完成状态
  - 完成后按钮变为"已完成"状态
  - **使用方法**：在漏洞页面组件中传入 `moduleKey` 属性

**使用示例**：
```vue
<VulnTemplate
  title="XSS反射型"
  subtitle="Cross-Site Scripting - Reflected"
  level="高危"
  :icon="BugOutlined"
  moduleKey="xss_reflect"
>
  <!-- 漏洞内容 -->
</VulnTemplate>
```

**模块标识规范**：
- XSS: `xss_reflect`, `xss_store`, `xss_dom`
- SQL注入: `sql_int`, `sql_error`, `sql_time`, `sql_bool`
- RCE: `rce_runtime`, `rce_processbuilder`
- 文件操作: `file_upload`, `file_download`
- XXE: `xxe_xmlreader`, `xxe_saxreader`, `xxe_saxbuilder`
- 其他: `ssrf`, `xff`, `cors`, `redirect`, `unauth`
- 组件漏洞: `fastjson`, `jackson`, `shiro`, `log4j`, `actuators`
- 反序列化: `readobject`

## 故意使用的漏洞版本

以下依赖版本故意使用存在已知漏洞的版本，**请勿升级**：

- Fastjson 1.2.24 - RCE漏洞
- Jackson 2.11.0 - 反序列化漏洞
- Shiro 1.2.4 - 反序列化漏洞
- Log4j 2.8.2 - JNDI注入漏洞
- Spring Boot 2.4.1 - 包含多个组件漏洞

## 代码规范

- **后端**：遵循Spring Boot标准项目结构，包名使用 `com.one.range.*`
- **前端**：Vue 3 Composition API或Options API均可，组件使用PascalCase命名
- **注释**：漏洞演示代码需添加中文注释说明漏洞原理和利用方式
- **安全提示**：新增漏洞模块需在前端页面添加"仅供学习使用"警告

## 常见问题

### 数据库连接失败
- 检查MySQL容器是否启动：`docker-compose ps`
- 检查端口占用：`netstat -an | grep 3307`
- 查看后端日志：`docker-compose logs backend`

### 前端无法访问后端API
- 检查 `vite.config.js` 中的代理配置
- 确认后端运行在 http://localhost:8000
- 检查CORS配置（`CorsConfig.java`）

### Maven构建失败
- 使用清华大学镜像源（已在 `pom.xml` 配置）
- 清理缓存：`./mvnw clean`
- 检查Java版本：`java -version`（需要1.8）

## 相关文档

- [README.md](README.md) - 项目介绍和快速开始
- [QUICKSTART.md](QUICKSTART.md) - 详细快速开始指南
- [DEPLOY.md](DEPLOY.md) - 完整部署说明
- [one_range.sql](one_range.sql) - 数据库初始化脚本
