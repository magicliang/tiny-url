#!/bin/bash

# 短链接系统初始化设置脚本
echo "=== 短链接系统初始化设置 ==="

# 给所有脚本文件添加执行权限
echo "正在设置脚本执行权限..."

chmod +x run.sh
chmod +x docker-run.sh
chmod +x demo.sh
chmod +x start.sh
chmod +x scripts/deploy.sh
chmod +x scripts/test.sh

echo "✅ 脚本权限设置完成"

# 检查Java环境
echo ""
echo "检查运行环境..."

if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo "✅ Java版本: $JAVA_VERSION"
else
    echo "❌ 未找到Java环境，请安装Java 8或更高版本"
fi

if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version 2>&1 | head -n 1 | cut -d' ' -f3)
    echo "✅ Maven版本: $MVN_VERSION"
else
    echo "❌ 未找到Maven环境，请安装Maven"
fi

if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version | cut -d' ' -f3 | cut -d',' -f1)
    echo "✅ Docker版本: $DOCKER_VERSION"
else
    echo "⚠️  未找到Docker环境（可选）"
fi

if command -v kubectl &> /dev/null; then
    KUBECTL_VERSION=$(kubectl version --client --short 2>/dev/null | cut -d' ' -f3)
    echo "✅ Kubectl版本: $KUBECTL_VERSION"
else
    echo "⚠️  未找到Kubectl环境（可选）"
fi

echo ""
echo "=== 初始化完成 ==="
echo ""
echo "🚀 快速启动命令："
echo "   ./run.sh              # 本地运行"
echo "   ./docker-run.sh       # Docker运行"
echo "   ./demo.sh             # 功能演示"
echo ""
echo "📚 文档说明："
echo "   cat USAGE.md          # 使用指南"
echo "   cat STATUS.md         # 项目状态"
echo "   cat README.md         # 项目说明"
echo ""
echo "🧪 测试命令："
echo "   ./scripts/test.sh     # 运行测试"
echo "   mvn test              # Maven测试"