# One 安全靶场 - 快速开始

## 🚀 一键启动

```bash
# Windows用户
start.bat

# Linux/Mac用户
./start.sh

# 或直接使用Docker Compose
docker-compose up -d
```

等待30秒后访问：http://localhost:3000

**默认账号**：admin / admin

---

## 📋 完整步骤

### 1. 启动服务

```bash
docker-compose up -d
```

这将启动：
- MySQL数据库（3306端口）
- Spring Boot后端（8000端口）
- Vue前端（3000端口）

### 2. 检查状态

```bash
# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 3. 访问应用

```
地址：http://localhost:3000
账号：admin
密码：admin
```

---

## 🎯 第一次使用

### 登录

1. 打开浏览器访问 http://localhost:3000
2. 输入账号：admin
3. 输入密码：admin
4. 输入验证码
5. 点击"登录"

### 测试XSS

1. 登录后，点击左侧菜单：**基础漏洞** → **XSS** → **反射型XSS**
2. 在测试表单输入：`<script>alert('XSS')</script>`
3. 点击"执行测试"
4. 观察结果

### 测试SQL注入

1. 点击：**基础漏洞** → **SQL注入** → **整型注入**
2. 输入：`1 or 1=1`
3. 点击"查询数据"
4. 查看返回的数据

### 测试RCE

1. 点击：**基础漏洞** → **远程代码执行** → **Runtime**
2. Windows输入：`whoami`，Linux输入：`id`
3. 点击"执行命令"
4. 查看命令执行结果

---

## 🔧 常用操作

### 查看日志

```bash
# 所有服务
docker-compose logs -f

# 只看后端
docker-compose logs -f backend

# 只看前端
docker-compose logs -f frontend
```

### 停止服务

```bash
# 停止
docker-compose down

# 停止并删除数据
docker-compose down -v
```

### 重启服务

```bash
# 重启所有
docker-compose restart

# 重启单个服务
docker-compose restart backend
```

---

## 💻 开发模式

如果需要修改代码：

```bash
# 1. 只启动数据库和后端
docker-compose up -d mysql backend

# 2. 启动前端开发服务器（支持热重载）
cd frontend
npm install
npm run dev

# 3. 访问
http://localhost:3000
```

前端代码修改后会自动刷新！

---

## 📖 功能导航

### 基础漏洞
- **XSS**：反射型、存储型
- **SQL注入**：整型、报错、盲注、MyBatis系列
- **RCE**：5种不同执行方式
- **文件**：上传、下载
- **其他**：XXE、SSTI、SSRF、SpEL等

### 组件漏洞
- Fastjson、Jackson、Shiro
- Log4j、XStream
- Spring Boot Actuator

### 反序列化
- ReadObject、RMI
- XMLDecoder、SnakeYaml

---

## ⚠️ 注意事项

1. **首次启动**：需要下载Docker镜像和npm依赖，可能需要5-10分钟
2. **端口占用**：确保3000、8000、3306端口未被占用
3. **网络连接**：首次运行需要网络连接
4. **资源需求**：至少2GB可用内存

---

## 🐛 故障排除

### 端口被占用

```bash
# Windows查看端口
netstat -ano | findstr "3000"

# Linux/Mac查看端口
lsof -i :3000
```

### Docker启动失败

```bash
# 检查Docker状态
docker ps

# 查看Docker日志
docker-compose logs
```

### 前端无法访问

1. 检查后端是否启动：http://localhost:8000
2. 查看浏览器控制台错误
3. 检查Docker网络：`docker network ls`

---

## 🎉 开始学习

服务启动成功后，访问 http://localhost:3000

开始您的安全学习之旅！🚀

**One 安全靶场** - 让安全学习更简单
