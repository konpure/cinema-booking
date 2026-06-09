#!/bin/bash
set -e

echo "=== Cinema Booking - Docker Toolbox 启动 ==="

if command -v docker-machine >/dev/null 2>&1; then
  docker-machine start default 2>/dev/null || true
  eval "$(docker-machine env default)"
  IP=$(docker-machine ip default)
else
  IP="localhost"
  echo "未检测到 docker-machine，按 localhost 处理"
fi

cd "$(dirname "$0")"

echo "当前目录: $(pwd)"
echo "正在构建并启动容器（首次较慢）..."

docker-compose up -d --build

echo ""
echo "=========================================="
echo " 启动完成！"
echo " 网站:     http://${IP}"
echo " RabbitMQ: http://${IP}:15672  (guest/guest)"
echo " 健康检查: http://${IP}/api/health"
echo " 账号 demo/demo123  admin/admin123"
echo "=========================================="
