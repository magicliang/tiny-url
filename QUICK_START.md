# 🚀 URL Shortener 快速启动指南

## 📋 前置要求

- Java 8+
- Maven 3.6+
- Docker (可选)
- Kubernetes (可选)

## 🏃‍♂️ 快速启动

### 方式一：直接运行 (推荐)

```bash
# 1. 克隆项目
git clone <repository-url>
cd url-shortener

# 2. 给脚本添加执行权限
chmod +x start.sh
chmod +x scripts/*.sh

# 3. 启动应用
./start.sh
```

应用将在 `http://localhost:8000` 启动

### 方式二：使用 Maven

```bash
# 1. 编译项目
mvn clean compile

# 2. 运行测试
mvn test

# 3. 启动应用
mvn spring-boot:run
```

### 方式三：使用 Docker

```bash
# 1. 构建并运行
docker-compose up -d

# 2. 查看日志
docker-compose logs -f

# 3. 停止服务
docker-compose down
```

### 方式四：使用 Kubernetes

```bash
# 1. 部署到 K8s
./scripts/deploy.sh deploy

# 2. 查看状态
./scripts/deploy.sh status

# 3. 端口转发 (如果没有 Ingress)
kubectl port-forward service/url-shortener-service 8000:80 -n url-shortener
```

## 🌐 访问应用

- **Web界面**: http://localhost:8000
- **API文档**: http://localhost:8000/api/v1/health
- **健康检查**: http://localhost:8000/api/v1/health

## 🧪 快速测试

### 1. 创建短链接

```bash
curl -X POST http://localhost:8000/api/v1/shorten \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://www.example.com",
    "customAlias": "test"
  }'
```

### 2. 访问短链接

```bash
curl -L http://localhost:8000/api/v1/test
```

### 3. 查看统计

```bash
curl http://localhost:8000/api/v1/stats/test
```

## 📊 管理功能

### 查看所有链接
```bash
curl http://localhost:8000/api/v1/admin/urls
```

### 系统统计
```bash
curl http://localhost:8000/api/v1/admin/stats
```

### 清理过期链接
```bash
curl -X POST http://localhost:8000/api/v1/admin/cleanup
```

## 🔧 配置说明

### 环境变量
```bash
export DB_HOST=11.142.154.110
export DB_PORT=3306
export DB_NAME=6n9pyl60
export DB_USERNAME=with_racdjgzrtmhgtadh
export DB_PASSWORD=K#2C@Y3pTOpOJu
```

### 应用配置
主要配置文件：`src/main/resources/application.yml`

## 🐛 故障排除

### 1. 端口被占用
```bash
# 查看端口占用
lsof -i :8000

# 杀死进程
kill -9 <PID>
```

### 2. 数据库连接失败
- 检查数据库配置
- 确认网络连通性
- 验证用户名密码

### 3. 内存不足
```bash
# 增加 JVM 内存
export JAVA_OPTS="-Xmx1g -Xms512m"
```

## 📝 常用命令

```bash
# 使用 Makefile (如果支持)
make help          # 查看所有命令
make dev           # 开发模式
make test          # 运行测试
make build         # 构建应用
make docker-build  # 构建 Docker 镜像
make k8s-deploy    # 部署到 K8s

# 使用脚本
./scripts/test.sh all      # 运行所有测试
./scripts/deploy.sh deploy # 部署到 K8s
```

## 🎯 下一步

1. 浏览 Web 界面创建短链接
2. 查看 [README.md](README.md) 了解详细功能
3. 查看 [SYSTEM_DESIGN.md](SYSTEM_DESIGN.md) 了解系统设计
4. 运行测试套件验证功能
5. 部署到生产环境

## 💡 提示

- 首次启动会自动构建项目，需要一些时间
- 数据库已预配置，无需额外设置
- Web 界面提供了完整的功能演示
- 所有 API 都有完整的错误处理

## 🆘 获取帮助

如果遇到问题：

1. 查看应用日志：`tail -f logs/url-shortener.log`
2. 检查健康状态：`curl http://localhost:8000/api/v1/health`
3. 查看系统资源：`make ps`
4. 重启应用：`make kill && make run`

---

**🎉 恭喜！您的 URL Shortener 服务已经启动成功！**