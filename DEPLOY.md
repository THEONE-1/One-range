# One 安全靶场 - 部署文档

## 📦 系统架构

```
Vue前端(3000) → Nginx反向代理 → Spring Boot后端(8000) → MySQL(3306)
```

---

## 🚀 生产部署

### Docker部署（推荐）

```bash
# 克隆项目
cd One安全靶场

# 一键启动
docker-compose up -d

# 查看状态
docker-compose ps
```

**服务列表**：
- `mysql` - MySQL 8.0数据库
- `backend` - Spring Boot后端API
- `frontend` - Vue前端（Nginx）

**访问**：http://localhost:3000

---

## 💻 开发部署

### 方式一：后端Docker + 前端开发

```bash
# 1. 启动数据库和后端
docker-compose up -d mysql backend

# 2. 启动前端开发服务器
cd frontend
npm install
npm run dev

# 3. 访问
http://localhost:3000
```

**优点**：前端支持热重载，修改代码自动刷新

### 方式二：完全本地部署

**前置要求**：
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+

**步骤**：

```bash
# 1. 创建数据库
mysql -u root -p
CREATE DATABASE one_range;
exit

# 2. 导入数据
mysql -u root -p one_range < one_range.sql

# 3. 修改配置（如需要）
# 编辑 src/main/resources/application.yml

# 4. 启动后端
./mvnw spring-boot:run

# 5. 启动前端（新终端）
cd frontend
npm install
npm run dev

# 6. 访问
http://localhost:3000
```

---

## ⚙️ 配置说明

### 后端配置

文件：`src/main/resources/application.yml`

```yaml
server:
  port: 8000  # 后端端口

spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/one_range?...
    username: root
    password: 123456  # 修改数据库密码
```

### 前端配置

文件：`frontend/vite.config.js`

```javascript
server: {
  port: 3000,  // 前端端口
  proxy: {
    '/home': {
      target: 'http://localhost:8000'  // 后端地址
    }
  }
}
```

### Docker配置

文件：`docker-compose.yml`

```yaml
services:
  frontend:
    ports:
      - "3000:80"  # 前端端口映射
  
  backend:
    ports:
      - "8000:8000"  # 后端端口映射
    environment:
      SPRING_DATASOURCE_PASSWORD: 123456  # 数据库密码
```

---

## 🔧 高级配置

### 修改端口

**前端端口**：
```yaml
# docker-compose.yml
services:
  frontend:
    ports:
      - "8080:80"  # 改为8080端口
```

**后端端口**：
```yaml
# application.yml
server:
  port: 9000  # 改为9000端口

# docker-compose.yml  
services:
  backend:
    ports:
      - "9000:9000"

# frontend/nginx.conf
proxy_pass http://backend:9000;  # 同步修改
```

### 数据持久化

数据自动保存在Docker数据卷中：

```bash
# 查看数据卷
docker volume ls

# 备份数据
docker exec one-range-mysql mysqldump -uroot -p123456 one_range > backup.sql

# 恢复数据
docker exec -i one-range-mysql mysql -uroot -p123456 one_range < backup.sql
```

### 生产环境优化

**1. 修改数据库密码**

编辑 `docker-compose.yml`：
```yaml
environment:
  MYSQL_ROOT_PASSWORD: your_strong_password
  
environment:
  SPRING_DATASOURCE_PASSWORD: your_strong_password
```

**2. 禁用不必要的端口**

```yaml
services:
  mysql:
    # ports:
    #   - "3306:3306"  # 注释掉，不暴露到宿主机
```

**3. 限制资源使用**

```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
```

---

## 📊 服务健康检查

### 检查后端API

```bash
# 测试后端是否正常
curl http://localhost:8000/

# 应返回JSON：
# {"message":"One Security Range API v2.0.0",...}
```

### 检查前端

```bash
# 访问前端
curl http://localhost:3000

# 应返回HTML页面
```

### 检查数据库

```bash
# 连接数据库
docker exec -it one-range-mysql mysql -uroot -p123456

# 查看数据库
SHOW DATABASES;  # 应该有 one_range

# 查看表
USE one_range;
SHOW TABLES;  # 应该有 users 和 xss
```

---

## 🐛 故障排除

### 1. 容器启动失败

```bash
# 查看详细日志
docker-compose logs

# 查看特定服务
docker-compose logs backend
```

### 2. 前端无法访问后端

**症状**：前端页面显示但API调用失败

**解决**：
```bash
# 1. 检查后端是否运行
docker-compose ps

# 2. 测试后端API
curl http://localhost:8000/

# 3. 检查网络
docker network inspect one-range-network

# 4. 重启服务
docker-compose restart
```

### 3. 数据库连接失败

**症状**：后端日志显示数据库连接错误

**解决**：
```bash
# 1. 等待MySQL完全启动（约30秒）
docker-compose logs mysql

# 2. 检查数据库密码
# 确保docker-compose.yml中的密码一致

# 3. 重启后端
docker-compose restart backend
```

### 4. npm install失败

**解决**：使用国内镜像
```bash
npm config set registry https://registry.npmmirror.com
npm install
```

---

## 📝 备份与恢复

### 备份

```bash
# 备份数据库
docker exec one-range-mysql mysqldump -uroot -p123456 one_range > backup_$(date +%Y%m%d).sql

# 备份数据卷
docker run --rm -v one-range_mysql-data:/data -v $(pwd):/backup alpine tar czf /backup/mysql-data-backup.tar.gz -C /data .
```

### 恢复

```bash
# 恢复数据库
docker exec -i one-range-mysql mysql -uroot -p123456 one_range < backup.sql
```

---

## 🎯 性能优化

### Nginx优化

编辑 `frontend/nginx.conf`：

```nginx
# 启用Gzip压缩
gzip on;
gzip_types text/css application/javascript application/json;
gzip_min_length 1000;

# 静态资源缓存
location ~* \.(js|css|png|jpg|jpeg|gif|svg|ico)$ {
    expires 7d;
    add_header Cache-Control "public, immutable";
}
```

### 后端优化

编辑 `application.yml`：

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

---

## 📌 部署检查清单

部署完成后检查：

- [ ] 前端可以访问（http://localhost:3000）
- [ ] 可以登录（admin/admin）
- [ ] 验证码显示正常
- [ ] 左侧菜单正常显示
- [ ] 选择一个漏洞模块测试
- [ ] API调用正常（查看Network）
- [ ] 后端日志无错误
- [ ] 数据库连接正常

---

**One 安全靶场** - 部署完成，开始学习！
