# One 安全靶场

<div align="center">

**专业的 Web 安全漏洞实验平台**

基于 Vue 3 + Ant Design Vue + Spring Boot 构建

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.x-brightgreen.svg)](https://vuejs.org/)

[快速开始](#快速开始) • [开发指南](#开发指南) • [故障排查](#故障排查) • [文档](#文档)

</div>

---

## 📋 目录

- [项目简介](#-项目简介)
- [技术栈](#️-技术栈)
- [快速开始](#-快速开始)
- [开发指南](#-开发指南)
- [项目架构](#-项目架构)
- [配置文件修改指南](#️-配置文件修改指南)
- [功能模块](#-功能模块)
- [常用命令](#-常用命令)
- [故障排查](#-故障排查)
- [端口配置](#-端口配置)
- [文档](#-文档)
- [免责声明](#️-免责声明)

---

## 🎯 项目简介

One 安全靶场是一个专业的 Web 安全漏洞实验平台，包含 **26 个漏洞演示模块**，涵盖 XSS、SQL 注入、RCE、反序列化等常见安全漏洞，用于安全学习和研究。

### ✨ 主要特性

- 🎨 **现代化界面**：基于 Vue 3 + Ant Design Vue，响应式设计
- 🔐 **用户系统**：支持用户注册、登录、权限管理
- 📊 **学习进度**：自动记录学习进度，可视化展示完成情况
- 🐳 **容器化部署**：Docker Compose 一键启动，开箱即用
- 🔥 **热重载开发**：前后端支持热重载，提升开发效率
- 📚 **详细文档**：每个漏洞都有原理说明、攻击示例和修复方案

### ⚠️ 重要提醒

本项目**故意包含已知漏洞**，仅供教育用途。**禁止在生产环境部署，禁止用于非法用途。**

---

## 🛠️ 技术栈

### 前端技术栈
- **框架**：Vue 3（Composition API）
- **UI 库**：Ant Design Vue 4.x
- **构建工具**：Vite 5.x
- **路由**：Vue Router 4.x
- **状态管理**：Pinia
- **HTTP 客户端**：Axios

### 后端技术栈
- **框架**：Spring Boot 2.4.1
- **ORM**：MyBatis
- **数据库**：MySQL 8.0
- **Java 版本**：1.8

### 部署技术
- **容器化**：Docker + Docker Compose
- **Web 服务器**：Nginx（前端）
- **反向代理**：Vite Dev Server（开发模式）

---

## 🚀 快速开始

### 前置要求

- Docker 20.10+
- Docker Compose 2.0+
- （开发模式）Node.js 18+ 和 Maven 3.8+

### 一键启动（生产模式）

```bash
# 克隆项目
git clone <repository-url>
cd One-range

# 启动所有服务
docker-compose up -d

# 查看启动状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

**访问地址**：http://localhost:3000
**默认账号**：admin / admin

### 停止服务

```bash
# 停止服务
docker-compose down

# 停止并删除数据卷（清空数据库）
docker-compose down -v
```

---

## 💻 开发指南

### 开发模式启动

开发模式下，前端使用 Vite 开发服务器（支持热重载），后端和数据库使用 Docker。

#### 1. 启动后端和数据库

```bash
# 启动后端 + 数据库（不启动前端容器）
docker-compose up -d backend mysql

# 查看后端日志
docker-compose logs -f backend
```

#### 2. 启动前端开发服务器

```bash 
# 进入前端目录
cd frontend

# 安装依赖（首次运行）
npm install --legacy-peer-deps

# 启动开发服务器（支持热重载）
npm run dev
```

**前端访问地址**：http://localhost:3000（Vite 开发服务器）
**后端 API 地址**：http://localhost:8000

#### 3. 前端热重载说明

前端使用 Vite 开发服务器，修改代码后会**自动热重载**：

- ✅ **自动生效**：修改 `.vue` 文件、`.js` 文件、`.css` 文件
- ✅ **保持状态**：Vue 组件支持 HMR（Hot Module Replacement）
- ⚠️ **需要刷新**：修改 `vite.config.js`、`router/index.js`、`main.js`

**常见问题**：
- 如果热重载失败，按 `Ctrl+C` 停止，然后重新运行 `npm run dev`
- 如果端口被占用，修改 `vite.config.js` 中的 `server.port`

#### 4. 后端热重载说明

后端使用 Spring Boot DevTools 支持热重载（需要重新构建镜像）：

**方式一：使用 Docker 重启（推荐）**

```bash
# 修改 Java 代码后，重新构建并重启后端
docker-compose up -d --build backend

# 查看重启日志
docker-compose logs -f backend
```

**方式二：本地运行后端（完全热重载）**

```bash
# 停止 Docker 中的后端
docker-compose stop backend

# 本地运行后端（支持自动重启）
./mvnw spring-boot:run

# 或使用 IDE（IDEA/Eclipse）运行 Application.java
```

本地运行后端时，修改 Java 代码后：
- ✅ **自动重启**：使用 Spring Boot DevTools
- ⚠️ **需要重新编译**：修改后需要触发编译

#### 5. 数据库修改

如果修改了数据库结构（`one_range.sql`）：

```bash
# 停止并删除数据库容器和数据卷
docker-compose down -v

# 重新启动（会重新初始化数据库）
docker-compose up -d mysql backend
```

### 前端开发常用命令

```bash
cd frontend

# 安装依赖
npm install --legacy-peer-deps

# 启动开发服务器（热重载）
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 代码格式化
npm run format

# 代码检查
npm run lint
```

### 后端开发常用命令

```bash
# 使用 Maven Wrapper（推荐）
./mvnw clean package              # 构建项目（Linux/Mac）
mvnw.cmd clean package            # 构建项目（Windows）
./mvnw spring-boot:run            # 运行后端服务

# 或使用 Maven
mvn clean package
mvn spring-boot:run

# 跳过测试构建
./mvnw clean package -DskipTests
```


## 📁 项目架构

### 目录结构

```
One-range/
├── frontend/                      # 前端项目（Vue 3）
│   ├── src/
│   │   ├── assets/               # 静态资源
│   │   ├── components/           # 公共组件
│   │   │   └── VulnTemplate.vue  # 漏洞页面模板组件
│   │   ├── router/               # 路由配置
│   │   │   └── index.js          # 路由定义（25个漏洞路由）
│   │   ├── utils/                # 工具函数
│   │   │   └── axios.js          # Axios 配置（Session 支持）
│   │   ├── views/                # 页面组件
│   │   │   ├── Layout.vue        # 主布局（侧边栏+内容区）
│   │   │   ├── Login.vue         # 登录页面
│   │   │   ├── Register.vue      # 注册页面
│   │   │   ├── Progress.vue      # 学习进度页面
│   │   │   ├── UserManagement.vue # 用户管理页面
│   │   │   └── vulns/            # 25个漏洞演示组件
│   │   │       ├── XSSReflect.vue
│   │   │       ├── SQLIntBased.vue
│   │   │       ├── RCERuntime.vue
│   │   │       └── ...
│   │   ├── App.vue               # 根组件
│   │   └── main.js               # 应用入口
│   ├── package.json              # 前端依赖
│   ├── vite.config.js            # Vite 配置（代理、端口）
│   └── nginx.conf                # Nginx 配置（生产环境）
│
├── src/main/                      # 后端项目（Spring Boot）
│   ├── java/com/one/range/
│   │   ├── controller/           # 控制器层
│   │   │   ├── AdminController.java          # 用户认证、注册
│   │   │   ├── LearningProgressController.java # 学习进度管理
│   │   │   ├── basevul/          # 基础漏洞（19个）
│   │   │   │   ├── XSSVul.java
│   │   │   │   ├── CORSVul.java
│   │   │   │   ├── file/         # 文件操作漏洞
│   │   │   │   ├── rce/          # 命令执行漏洞
│   │   │   │   ├── sqli/         # SQL注入漏洞
│   │   │   │   └── xxe/          # XXE漏洞
│   │   │   ├── componentsvul/    # 组件漏洞（6个）
│   │   │   │   ├── FastJson.java
│   │   │   │   ├── Jackson.java
│   │   │   │   └── ...
│   │   │   └── deserialize/      # 反序列化漏洞（1个）
│   │   ├── service/              # 服务层
│   │   │   ├── AdminService.java
│   │   │   ├── LearningProgressService.java
│   │   │   └── impl/             # 服务实现
│   │   ├── dao/                  # 数据访问层
│   │   │   ├── AdminDao.java
│   │   │   └── LearningProgressDao.java
│   │   ├── model/                # 实体类
│   │   │   ├── Admin.java
│   │   │   └── LearningProgress.java
│   │   ├── config/               # 配置类
│   │   │   └── CorsConfig.java   # CORS 跨域配置
│   │   └── filter/               # 过滤器
│   │       └── MvcConfig.java    # MVC 配置
│   └── resources/
│       ├── mapper/               # MyBatis XML 映射
│       │   ├── AdminDaoServiceImpl.xml
│       │   └── LearningProgressDaoServiceImpl.xml
│       └── application.yml       # Spring Boot 配置
│
├── one_range.sql                 # 数据库初始化脚本
├── docker-compose.yml            # Docker Compose 编排文件
├── Dockerfile                    # 多阶段构建文件
├── pom.xml                       # Maven 依赖配置
├── README.md                     # 项目说明（本文件）
└── .mvn/                         # Maven Wrapper

```

### 后端架构（三层架构）

```
Controller（控制器层）
  ├── 接收 HTTP 请求
  ├── 参数验证
  └── 返回 JSON 响应
    ↓
Service（服务层）
  ├── 业务逻辑处理
  ├── 事务管理
  └── 数据转换
    ↓
DAO（数据访问层）
  ├── 数据库操作
  └── MyBatis 映射
    ↓
Database（MySQL）
```

### 前端架构（组件化）

```
App.vue（根组件）
  ↓
Router（路由）
  ├── /login          → Login.vue
  ├── /register       → Register.vue
  └── /               → Layout.vue（主布局）
      ├── /home       → Home.vue
      ├── /progress   → Progress.vue
      └── /vulns/*    → 25个漏洞组件
          └── VulnTemplate.vue（通用模板）
```

---

## ⚙️ 配置文件修改指南

当你删除或添加功能模块时，需要同步修改相关配置文件。以下是快速定位指南：

### 删除后端功能时需要修改的文件

| 文件 | 修改内容 | 说明 |
|------|---------|------|
| **pom.xml** | 删除相关依赖 | 如删除Groovy RCE，需删除`groovy-all`依赖；删除XStream，需删除`xstream`依赖 |
| **application.yml** | 删除相关配置 | 如删除某些组件的特定配置项 |
| **README.md** | 更新漏洞列表 | 更新"漏洞演示模块"章节的数量和列表 |


**示例：删除Groovy RCE功能**
```xml
<!-- pom.xml - 删除以下依赖 -->
<dependency>
    <groupId>org.codehaus.groovy</groupId>
    <artifactId>groovy-all</artifactId>
    <version>2.5.6</version>
</dependency>
```

### 删除前端功能时需要修改的文件

| 文件 | 修改内容 | 说明 |
|------|---------|------|
| **frontend/src/router/index.js** | 删除路由配置 | 删除对应的路由定义（path、name、component） |
| **frontend/src/views/Layout.vue** | 删除菜单项 | 删除侧边栏导航菜单中的`<a-menu-item>`或`<a-sub-menu>` |
| **frontend/src/views/Home.vue** | 更新统计数据 | 更新`vulnStats`数组中的漏洞数量统计 |
| **README.md** | 更新漏洞列表 | 更新"漏洞演示模块"章节 |

**示例：删除MyBatis注入功能**
```javascript
// frontend/src/router/index.js - 删除以下路由
{
  path: '/sqli/mybatis_orderby',
  name: 'MyBatisOrderBy',
  component: () => import('@/views/vulns/MyBatisOrderBy.vue')
}
```

```vue
<!-- frontend/src/views/Layout.vue - 删除以下菜单项 -->
<a-menu-item key="/sqli/mybatis_orderby">MyBatis OrderBy注入</a-menu-item>
```

### 添加新功能时需要修改的文件

#### 添加后端功能

1. **创建Controller** - `src/main/java/com/one/range/controller/`
2. **添加依赖（如需要）** - `pom.xml`
3. **更新文档** - `README.md`

#### 添加前端功能

1. **创建Vue组件** - `frontend/src/views/vulns/`
2. **添加路由** - `frontend/src/router/index.js`
3. **添加菜单项** - `frontend/src/views/Layout.vue`
4. **更新统计** - `frontend/src/views/Home.vue`（vulnStats数组）
5. **更新文档** - `README.md`、`CLAUDE.md`

### 配置文件检查清单

删除功能后，按以下顺序检查：

**后端检查清单：**
- [ ] 删除Controller文件
- [ ] 删除Service/DAO/Model（如有）
- [ ] 清理pom.xml中的专用依赖
- [ ] 更新README.md
**前端检查清单：**
- [ ] 删除Vue组件文件
- [ ] 删除router/index.js中的路由
- [ ] 删除Layout.vue中的菜单项
- [ ] 更新Home.vue中的统计数据
- [ ] 更新README.md

---

## 🎯 功能模块

### 用户系统
- ✅ 用户注册（用户名、密码、验证码）
- ✅ 用户登录（Session 认证）
- ✅ 用户管理（管理员功能）
- ✅ 权限控制（管理员/普通用户）

### 学习进度管理
- ✅ 自动记录完成的漏洞模块
- ✅ 进度统计（已完成数、总数、完成率）
- ✅ 进度可视化（进度条、图表）
- ✅ 学习记录列表（完成时间）

### 漏洞演示模块（25个）

#### 基础漏洞（19个）

**XSS 跨站脚本（3个）**
- XSS 反射型
- XSS 存储型
- XSS DOM 型

**SQL 注入（4个）**
- SQL 注入 - 整型注入
- SQL 注入 - 报错注入
- SQL 注入 - 时间盲注
- SQL 注入 - 布尔盲注

**命令执行 RCE（2个）**
- RCE - Runtime.exec()
- RCE - ProcessBuilder

**文件操作（2个）**
- 文件上传漏洞
- 文件下载漏洞

**XXE 外部实体注入（3个）**
- XXE - XMLReader
- XXE - SAXReader
- XXE - SAXBuilder

**其他漏洞（5个）**
- SSRF 服务端请求伪造
- XFF 伪造
- CORS 跨域资源共享
- URL 重定向
- 未授权访问

#### 组件漏洞（5个）
- Fastjson 反序列化（1.2.24）
- Jackson 反序列化（2.11.0）
- Shiro 反序列化（1.2.4）
- Log4j RCE（2.8.2）
- Actuator 信息泄露

#### 反序列化漏洞（1个）
- ReadObject 反序列化

---

## 📝 常用命令

### Docker Compose 命令

```bash
# 启动所有服务
docker-compose up -d

# 启动指定服务
docker-compose up -d backend mysql

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f              # 所有服务
docker-compose logs -f backend      # 仅后端
docker-compose logs -f frontend     # 仅前端
docker-compose logs -f mysql        # 仅数据库

# 重启服务
docker-compose restart              # 重启所有
docker-compose restart backend      # 重启后端

# 停止服务
docker-compose stop                 # 停止所有
docker-compose stop backend         # 停止后端

# 停止并删除容器
docker-compose down

# 停止并删除容器和数据卷（清空数据库）
docker-compose down -v

# 重新构建镜像
docker-compose build                # 构建所有
docker-compose build backend        # 仅构建后端
docker-compose build frontend       # 仅构建前端

# 重新构建并启动
docker-compose up -d --build
```

### 数据库操作

```bash
# 进入 MySQL 容器
docker-compose exec mysql bash

# 连接 MySQL
docker-compose exec mysql mysql -uroot -p123456 one_range

# 导出数据库
docker-compose exec mysql mysqldump -uroot -p123456 one_range > backup.sql

# 导入数据库
docker-compose exec -T mysql mysql -uroot -p123456 one_range < backup.sql

# 查看数据库日志
docker-compose logs -f mysql
```

### 后端操作

```bash
# 查看后端日志
docker-compose logs -f backend

# 进入后端容器
docker-compose exec backend bash

# 重启后端（修改代码后）
docker-compose restart backend

# 重新构建后端镜像
docker-compose build backend
docker-compose up -d backend
```

---

## 🔧 故障排查

### 常见问题

#### 1. 前端无法访问后端 API

**症状**：前端页面加载正常，但功能无法使用，控制台报 404 或 CORS 错误

**排查步骤**：

```bash
# 1. 检查后端是否启动
docker-compose ps

# 2. 查看后端日志
docker-compose logs backend

# 3. 测试后端 API
curl http://localhost:8000/admin/login

# 4. 检查 Vite 代理配置
cat frontend/vite.config.js
```

**解决方案**：
- 确保后端容器正在运行：`docker-compose up -d backend`
- 检查 `vite.config.js` 中的代理配置是否正确
- 确保 `axios.defaults.withCredentials = true` 已配置

#### 2. 数据库连接失败

**症状**：后端日志显示 `Connection refused` 或 `Unknown database`

**排查步骤**：

```bash
# 1. 检查 MySQL 是否启动
docker-compose ps mysql

# 2. 查看 MySQL 日志
docker-compose logs mysql

# 3. 测试数据库连接
docker-compose exec mysql mysql -uroot -p123456 -e "SHOW DATABASES;"

# 4. 检查数据库配置
cat src/main/resources/application.yml
```

**解决方案**：
- 重启 MySQL：`docker-compose restart mysql`
- 重新初始化数据库：`docker-compose down -v && docker-compose up -d`
- 检查 `application.yml` 中的数据库连接配置

#### 3. 端口被占用

**症状**：启动失败，提示 `port is already allocated`

**排查步骤**：

```bash
# Windows
netstat -ano | findstr :3000
netstat -ano | findstr :8000
netstat -ano | findstr :3307

# Linux/Mac
lsof -i :3000
lsof -i :8000
lsof -i :3307
```

**解决方案**：
- 停止占用端口的进程
- 或修改 `docker-compose.yml` 中的端口映射

#### 4. Maven 构建失败

**症状**：Docker 构建后端镜像时，Maven 下载依赖失败

**排查步骤**：

```bash
# 查看构建日志
docker-compose build backend

# 本地测试 Maven 构建
./mvnw clean package -DskipTests
```

**解决方案**：
- 使用 Maven 镜像源（已在 `pom.xml` 配置清华源）
- 清理 Maven 缓存：`./mvnw clean`
- 检查网络连接

#### 5. 前端依赖安装失败

**症状**：`npm install` 报错或卡住

**排查步骤**：

```bash
cd frontend

# 查看 npm 配置
npm config list

# 清理缓存
npm cache clean --force

# 删除 node_modules 和 package-lock.json
rm -rf node_modules package-lock.json
```

**解决方案**：
- 使用淘宝镜像：`npm config set registry https://registry.npmmirror.com`
- 使用 `--legacy-peer-deps` 参数：`npm install --legacy-peer-deps`
- 升级 Node.js 版本到 18+

#### 6. 学习进度功能失败

**症状**：点击"标记完成"按钮无反应，或进度页面显示"加载失败"

**排查步骤**：

```bash
# 1. 检查后端日志
docker-compose logs backend | grep progress

# 2. 检查数据库表是否存在
docker-compose exec mysql mysql -uroot -p123456 one_range -e "SHOW TABLES LIKE 'learning_progress';"

# 3. 测试 API
curl -X POST http://localhost:8000/api/progress/complete \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "vulnModule=xss_reflect" \
  --cookie "JSESSIONID=your-session-id"
```

**解决方案**：
- 确保 `axios.defaults.withCredentials = true` 已配置
- 确保 Vite 代理配置中没有 `rewrite` 规则去掉 `/api` 前缀
- 重新初始化数据库：`docker-compose down -v && docker-compose up -d`

#### 7. Docker 构建缓存问题

**症状**：修改代码后重新构建，但使用的还是旧代码

**解决方案**：

```bash
# 清理 Docker 缓存并重新构建
docker-compose build --no-cache backend
docker-compose up -d backend

# 或清理所有未使用的 Docker 资源
docker system prune -a
```

### 日志查看技巧

```bash
# 实时查看所有日志
docker-compose logs -f

# 查看最近 100 行日志
docker-compose logs --tail=100

# 查看特定服务的日志
docker-compose logs -f backend

# 查看带时间戳的日志
docker-compose logs -f -t

# 过滤日志内容
docker-compose logs backend | grep ERROR
```

---

## 🌐 端口配置

### 默认端口

| 服务 | 开发模式 | 生产模式（Docker） | 说明 |
|------|---------|-------------------|------|
| 前端 | 3000 (Vite) | 3000 (Nginx) | Vue 前端页面 |
| 后端 | 8000 | 8000 | Spring Boot API |
| MySQL | 3306/3307 | 3307→3306 | 数据库服务 |

### 修改端口

**修改前端端口**：

编辑 `frontend/vite.config.js`：
```javascript
server: {
  port: 5173,  // 修改为你想要的端口
  // ...
}
```

编辑 `docker-compose.yml`：
```yaml
frontend:
  ports:
    - "5173:80"  # 修改左侧端口
```

**修改后端端口**：

编辑 `src/main/resources/application.yml`：
```yaml
server:
  port: 9000  # 修改为你想要的端口
```

编辑 `docker-compose.yml`：
```yaml
backend:
  ports:
    - "9000:9000"  # 修改端口映射
```

**修改数据库端口**：

编辑 `docker-compose.yml`：
```yaml
mysql:
  ports:
    - "3308:3306"  # 修改左侧端口
```

---

## 📚 文档

- **[one_range.sql](one_range.sql)** - 数据库初始化脚本
- **[docker-compose.yml](docker-compose.yml)** - Docker Compose 配置
- **[Dockerfile](Dockerfile)** - 多阶段构建配置

---

## ⚠️ 免责声明

**重要提醒**：

1. 本项目**仅供安全学习和研究使用**，请勿用于非法用途
2. 本项目**故意包含已知漏洞**，禁止在生产环境部署
3. 使用本项目进行安全测试时，请确保获得授权
4. 项目维护者不对使用本项目造成的任何后果负责

**故意使用的漏洞版本**（请勿升级）：
- Fastjson 1.2.24 - RCE 漏洞
- Jackson 2.11.0 - 反序列化漏洞
- Shiro 1.2.4 - 反序列化漏洞
- Log4j 2.8.2 - JNDI 注入漏洞
- Spring Boot 2.4.1 - 包含多个组件漏洞

---



## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

<div align="center">

**One 安全靶场 v2.1.0**

现代化的安全学习平台

Made with ❤️ for Security Learning

</div>
