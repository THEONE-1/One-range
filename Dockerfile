# ==========================================
# One安全靶场
# 支持构建后端和前端镜像
# ==========================================

# 构建参数：指定构建目标（backend或frontend）
ARG BUILD_TARGET=backend

# ==========================================
# 后端构建阶段
# ==========================================
FROM docker.1ms.run/library/maven:3.8-openjdk-8-slim AS backend-builder

WORKDIR /app

# 先复制依赖配置文件和 Maven wrapper
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn ./.mvn

# 下载依赖（利用 Docker 层缓存，只有 pom.xml 变化时才重新下载）
RUN mvn dependency:go-offline -B || true

# 再复制源代码
COPY src ./src

# 编译打包（跳过测试编译和执行，加快构建速度）
RUN mvn clean package -Dmaven.test.skip=true

# ==========================================
# 前端构建阶段
# ==========================================
FROM docker.1ms.run/library/node:18-alpine AS frontend-builder

WORKDIR /app

# 配置 npm 使用淘宝镜像源加速
RUN npm config set registry https://registry.npmmirror.com

# 先复制依赖文件
COPY frontend/package.json frontend/package-lock.json* ./

# 安装依赖
RUN npm install --legacy-peer-deps

# 复制源代码和配置文件
COPY frontend/ ./

# 构建前端项目
RUN npm run build

# ==========================================
# 后端运行镜像
# ==========================================
FROM docker.1ms.run/adoptopenjdk/openjdk8 AS backend

WORKDIR /app
COPY --from=backend-builder /app/target/*.jar app.jar

EXPOSE 8000

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# ==========================================
# 前端运行镜像（Nginx）
# ==========================================
FROM docker.1ms.run/library/nginx:alpine AS frontend

COPY --from=frontend-builder /app/dist /usr/share/nginx/html
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
