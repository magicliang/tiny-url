#!/bin/bash

# 短链接系统启动脚本
echo "=== 短链接系统启动脚本 ==="

# 设置环境变量
export DB_HOST=11.142.154.110
export DB_PORT=3306
export DB_NAME=6n9pyl60
export DB_USERNAME=with_racdjgzrtmhgtadh
export DB_PASSWORD="K#2C@Y3pTOpOJu"

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请安装Java 8或更高版本"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven环境，请安装Maven"
    exit 1
fi

echo "正在编译项目..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "编译失败，请检查代码"
    exit 1
fi

echo "正在运行测试..."
mvn test -q

if [ $? -ne 0 ]; then
    echo "测试失败，请检查代码"
    exit 1
fi

echo "正在启动应用..."
mvn spring-boot:run

echo "应用已启动，访问地址: http://localhost:8000"