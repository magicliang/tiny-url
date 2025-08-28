#!/bin/bash

# Docker运行脚本
echo "=== 短链接系统 Docker 启动脚本 ==="

# 设置环境变量
export DB_HOST=11.142.154.110
export DB_PORT=3306
export DB_NAME=6n9pyl60
export DB_USERNAME=with_racdjgzrtmhgtadh
export DB_PASSWORD="K#2C@Y3pTOpOJu"

# 检查Docker环境
if ! command -v docker &> /dev/null; then
    echo "错误: 未找到Docker环境，请安装Docker"
    exit 1
fi

echo "正在构建Docker镜像..."
docker build -t url-shortener:latest .

if [ $? -ne 0 ]; then
    echo "Docker镜像构建失败"
    exit 1
fi

echo "正在停止已存在的容器..."
docker stop url-shortener-container 2>/dev/null || true
docker rm url-shortener-container 2>/dev/null || true

echo "正在启动Docker容器..."
docker run -d \
  --name url-shortener-container \
  -p 8000:8000 \
  -e DB_HOST=$DB_HOST \
  -e DB_PORT=$DB_PORT \
  -e DB_NAME=$DB_NAME \
  -e DB_USERNAME=$DB_USERNAME \
  -e DB_PASSWORD="$DB_PASSWORD" \
  url-shortener:latest

if [ $? -eq 0 ]; then
    echo "应用已启动，访问地址: http://localhost:8000"
    echo "查看日志: docker logs -f url-shortener-container"
else
    echo "容器启动失败"
    exit 1
fi