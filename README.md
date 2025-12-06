# One 安全靶场

专业的Web安全漏洞实验平台 - 基于Vue 3 + Ant Design Vue + Spring Boot

> 🚀 **一键启动**：`docker-compose up -d`  
> 🌐 **访问地址**：http://localhost:3000  
> 👤 **默认账号**：admin / admin

## 技术栈

**前端**：Vue 3 + Ant Design Vue + Vite  
**后端**：Spring Boot + MyBatis + MySQL

## 快速开始

```bash
# 方式一：一键启动（推荐）
./start.sh    # Linux/Mac
start.bat     # Windows

# 方式二：直接使用Docker Compose
docker-compose up -d

# 访问应用
http://localhost:3000
账号：admin / admin
```

## 开发模式

```bash
# 1. 启动后端（数据库+API）
docker-compose up -d backend mysql

# 2. 启动前端开发服务器
cd frontend
npm install
npm run dev

# 3. 访问
http://localhost:3000
```

## 项目结构

```
One安全靶场/
├── frontend/           # Vue 3前端（40个漏洞组件）
├── src/main/
│   ├── java/com/one/range/    # Spring Boot后端
│   └── resources/             # 配置文件
├── one_range.sql              # 数据库初始化
├── docker-compose.yml         # Docker编排
└── Dockerfile                 # 统一镜像构建
```

## 功能模块

### 基础漏洞（28个）
XSS、SQL注入、RCE、文件操作、XXE、SSTI、SSRF、SpEL等

### 组件漏洞（7个）
Fastjson、Jackson、Shiro、Log4j、XStream、Actuator等

### 反序列化（4个）
ReadObject、RMI、XMLDecoder、SnakeYaml

**共40个漏洞演示模块**

## 常用命令

```bash
# 启动
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止
docker-compose down

# 重启
docker-compose restart

# 完全清理
docker-compose down -v
```

## 端口说明

- **3000** - Vue前端
- **8000** - Spring Boot后端API
- **3307** - MySQL数据库

## 文档

- [快速开始](QUICKSTART.md) - 详细的快速开始指南
- [部署文档](DEPLOY.md) - 完整的部署说明

## 免责声明

本项目仅供安全学习和研究使用，请勿用于非法用途。

---

**One 安全靶场 v2.0.0** - 现代化的安全学习平台
