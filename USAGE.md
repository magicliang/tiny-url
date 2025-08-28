# 短链接系统使用指南

## 系统概述

这是一个基于Java Spring Boot开发的短链接系统，具有以下特性：

- **高性能**: 使用缓存机制，支持高并发访问
- **可扩展**: 支持K8s部署，具备自动扩缩容能力
- **完整测试**: 包含单元测试和集成测试
- **现代化UI**: 提供美观的Web界面
- **统计分析**: 支持点击统计和数据分析

## 快速启动

### 方式一：使用启动脚本（推荐）

```bash
# 给脚本执行权限
chmod +x run.sh

# 启动应用
./run.sh
```

### 方式二：使用Docker

```bash
# 给脚本执行权限
chmod +x docker-run.sh

# 启动Docker容器
./docker-run.sh
```

### 方式三：手动启动

```bash
# 设置环境变量
export DB_HOST=11.142.154.110
export DB_PORT=3306
export DB_NAME=6n9pyl60
export DB_USERNAME=with_racdjgzrtmhgtadh
export DB_PASSWORD="K#2C@Y3pTOpOJu"

# 编译并运行
mvn clean compile
mvn spring-boot:run
```

## 访问应用

启动成功后，访问以下地址：

- **Web界面**: http://localhost:8000
- **API文档**: http://localhost:8000/swagger-ui.html
- **健康检查**: http://localhost:8000/actuator/health

## 主要功能

### 1. 创建短链接

**Web界面操作**:
1. 在首页输入长URL
2. 点击"生成短链接"按钮
3. 复制生成的短链接

**API调用**:
```bash
curl -X POST http://localhost:8000/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://www.example.com"}'
```

### 2. 访问短链接

直接在浏览器中访问生成的短链接，系统会自动重定向到原始URL。

### 3. 查看统计信息

**Web界面**: 在首页点击"查看统计"按钮

**API调用**:
```bash
curl http://localhost:8000/api/stats/{shortCode}
```

### 4. 批量操作

**批量创建短链接**:
```bash
curl -X POST http://localhost:8000/api/batch/shorten \
  -H "Content-Type: application/json" \
  -d '{"urls": ["https://www.example1.com", "https://www.example2.com"]}'
```

## K8s部署

### 部署到K8s集群

```bash
# 使用部署脚本
chmod +x scripts/deploy.sh
./scripts/deploy.sh

# 或手动部署
kubectl apply -f k8s/
```

### 查看部署状态

```bash
# 查看Pod状态
kubectl get pods -n url-shortener

# 查看服务状态
kubectl get svc -n url-shortener

# 查看日志
kubectl logs -f deployment/url-shortener -n url-shortener
```

## 测试

### 运行所有测试

```bash
# 使用测试脚本
chmod +x scripts/test.sh
./scripts/test.sh

# 或手动运行
mvn test
```

### 单独运行测试类型

```bash
# 单元测试
mvn test -Dtest="*Test"

# 集成测试
mvn test -Dtest="*IntegrationTest"
```

## 性能测试

系统支持高并发访问，可以使用以下工具进行性能测试：

```bash
# 使用Apache Bench测试
ab -n 1000 -c 10 http://localhost:8000/

# 使用wrk测试
wrk -t12 -c400 -d30s http://localhost:8000/
```

## 监控和日志

### 应用监控

- **Actuator端点**: http://localhost:8000/actuator
- **健康检查**: http://localhost:8000/actuator/health
- **指标信息**: http://localhost:8000/actuator/metrics

### 日志查看

```bash
# Docker容器日志
docker logs -f url-shortener-container

# K8s Pod日志
kubectl logs -f deployment/url-shortener -n url-shortener
```

## 配置说明

### 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| DB_HOST | 数据库主机 | 11.142.154.110 |
| DB_PORT | 数据库端口 | 3306 |
| DB_NAME | 数据库名称 | 6n9pyl60 |
| DB_USERNAME | 数据库用户名 | with_racdjgzrtmhgtadh |
| DB_PASSWORD | 数据库密码 | K#2C@Y3pTOpOJu |

### 应用配置

主要配置文件位于 `src/main/resources/application.yml`，包含：

- 数据库连接配置
- 缓存配置
- JPA配置
- 服务器配置

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查数据库服务是否正常
   - 验证连接信息是否正确
   - 确认网络连通性

2. **端口占用**
   - 检查8000端口是否被占用
   - 修改配置文件中的端口设置

3. **内存不足**
   - 调整JVM参数
   - 增加系统内存

### 日志级别调整

在 `application.yml` 中调整日志级别：

```yaml
logging:
  level:
    com.shorturl: DEBUG
    org.springframework: INFO
```

## 系统架构

详细的系统设计文档请参考：
- [系统设计文档](SYSTEM_DESIGN.md)
- [项目总结](PROJECT_SUMMARY.md)
- [快速启动指南](QUICK_START.md)

## 技术栈

- **后端**: Java 8, Spring Boot 2.7, Spring Data JPA
- **数据库**: MySQL 8.0
- **缓存**: Caffeine
- **容器化**: Docker
- **编排**: Kubernetes
- **测试**: JUnit 5, Mockito, TestContainers
- **前端**: HTML5, CSS3, JavaScript ES6+

## 联系支持

如有问题或建议，请通过以下方式联系：

- 查看文档: [README.md](README.md)
- 系统设计: [SYSTEM_DESIGN.md](SYSTEM_DESIGN.md)
- 快速启动: [QUICK_START.md](QUICK_START.md)