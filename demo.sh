#!/bin/bash

# 短链接系统功能演示脚本
echo "=== 短链接系统功能演示 ==="

BASE_URL="http://localhost:8000"

# 检查服务是否启动
echo "1. 检查服务状态..."
curl -s "$BASE_URL/actuator/health" | grep -q "UP" 
if [ $? -eq 0 ]; then
    echo "✅ 服务正常运行"
else
    echo "❌ 服务未启动，请先运行 ./run.sh 或 ./docker-run.sh"
    exit 1
fi

echo ""
echo "2. 创建短链接..."

# 创建短链接
RESPONSE=$(curl -s -X POST "$BASE_URL/api/shorten" \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://www.baidu.com"}')

echo "响应: $RESPONSE"

# 提取短链接代码
SHORT_CODE=$(echo $RESPONSE | grep -o '"shortCode":"[^"]*"' | cut -d'"' -f4)

if [ -n "$SHORT_CODE" ]; then
    echo "✅ 短链接创建成功: $BASE_URL/$SHORT_CODE"
    
    echo ""
    echo "3. 测试重定向..."
    
    # 测试重定向
    REDIRECT_URL=$(curl -s -I "$BASE_URL/$SHORT_CODE" | grep -i "location:" | cut -d' ' -f2 | tr -d '\r')
    
    if [ -n "$REDIRECT_URL" ]; then
        echo "✅ 重定向测试成功: $REDIRECT_URL"
    else
        echo "❌ 重定向测试失败"
    fi
    
    echo ""
    echo "4. 查看统计信息..."
    
    # 查看统计
    STATS=$(curl -s "$BASE_URL/api/stats/$SHORT_CODE")
    echo "统计信息: $STATS"
    
else
    echo "❌ 短链接创建失败"
fi

echo ""
echo "5. 批量创建短链接..."

# 批量创建
BATCH_RESPONSE=$(curl -s -X POST "$BASE_URL/api/batch/shorten" \
  -H "Content-Type: application/json" \
  -d '{"urls": ["https://www.google.com", "https://www.github.com", "https://www.stackoverflow.com"]}')

echo "批量创建响应: $BATCH_RESPONSE"

echo ""
echo "6. 获取所有短链接..."

# 获取所有短链接
ALL_URLS=$(curl -s "$BASE_URL/api/urls")
echo "所有短链接: $ALL_URLS"

echo ""
echo "=== 演示完成 ==="
echo "🌐 访问Web界面: $BASE_URL"
echo "📚 API文档: $BASE_URL/swagger-ui.html"
echo "💊 健康检查: $BASE_URL/actuator/health"