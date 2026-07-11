# ============================================
# 阶段1：构建 Java 后端（Spring Boot）
# ============================================
FROM maven:3.9-eclipse-temurin-21 AS java-builder

WORKDIR /build

# 复制全部源码（dockerignore 排除了 target/ 和 node_modules/）
COPY . .

# 构建后端 JAR 及其所有依赖
# --mount=type=cache 缓存 Maven 本地仓库，加速重复构建
# 如果项目中有 .mvn/settings.xml（如阿里云镜像配置）则自动使用，否则用 Maven 默认源
RUN --mount=type=cache,id=maven,target=/root/.m2/repository \
    if [ -f /build/.mvn/settings.xml ]; then \
      MAVEN_SETTINGS="-s /build/.mvn/settings.xml"; \
    fi; \
    mvn clean package -Dspring-javaformat.skip=true -DskipTests \
    $MAVEN_SETTINGS \
    -pl phoenix-admin/phoenix-admin-manager -am

# ============================================
# 阶段2：构建前端（Vue + Vite）
# ============================================
FROM node:22-slim AS node-builder

ENV NODE_OPTIONS=--max-old-space-size=8192
ENV CI=true

RUN npm i -g corepack && corepack enable

WORKDIR /build

# 只复制前端 monorepo
COPY web-frontend/ .

# 安装依赖并构建 admin-ui
RUN pnpm config set store-dir /pnpm/store
RUN --mount=type=cache,id=pnpm,target=/pnpm/store \
    pnpm install --frozen-lockfile
RUN --mount=type=cache,id=pnpm,target=/pnpm/store \
    pnpm build:ele

# ============================================
# 阶段3：生产运行镜像
# ============================================
FROM eclipse-temurin:21-jre-alpine AS production

LABEL maintainer="军哥 463805737@qq.com"

# 安装 nginx 和 python3（PythonExecuteNode 需要）
RUN apk add --no-cache nginx python3 py3-pip

WORKDIR /app

# 复制后端 JAR
COPY --from=java-builder /build/phoenix-admin/phoenix-admin-manager/target/phoenix-admin.jar /app/phoenix-admin.jar

# 复制前端静态文件
COPY --from=node-builder /build/apps/admin-ui/dist /usr/share/nginx/html

# 复制 nginx 配置和启动脚本
COPY docker/nginx.conf /etc/nginx/nginx.conf
COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# 创建日志目录
RUN mkdir -p /app/logs

EXPOSE 80

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

ENTRYPOINT ["/entrypoint.sh"]
