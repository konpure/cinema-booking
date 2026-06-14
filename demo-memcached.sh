#!/bin/bash
# ====================================
# Memcached vs Redis 对比演示脚本
# 板块6 — 前端技术 答辩展示
# ====================================

echo ""
echo "==============================================="
echo "  Memcached vs Redis 对比演示"
echo "==============================================="
echo ""

# ===== 1. 确认服务状态 =====
echo "--- 1. Memcached 服务状态 ---"
systemctl status memcached 2>/dev/null | head -3 || echo "not running"
echo ""

# ===== 2. Memcached 基本操作 =====
echo "--- 2. Memcached 基本操作 ---"
echo ""

echo "[SET] 写入缓存 (仅 K-V)"
printf "set greeting 0 60 20\r\nHello from Memcached\r\n" | nc -q 1 localhost 11211
echo ""

echo "[GET] 读取缓存"
printf "get greeting\r\n" | nc -q 1 localhost 11211
echo ""

echo "[DELETE] 删除缓存"
printf "delete greeting\r\n" | nc -q 1 localhost 11211
echo ""

echo "[注意] Memcached 只支持字符串，没有 Hash/List/Set 类型"
echo ""

# ===== 3. 对比表格 =====
echo "--- 3. Redis vs Memcached 对比 ---"
echo ""
echo "  维度              |  Redis (项目中用)     |  Memcached"
echo "-------------------|---------------------|---------------------"
echo "  数据结构          |  String/Hash/List   |  仅 Key-Value"
echo "                    |  Set/ZSet(5种)      |"
echo "  持久化            |  RDB/AOF ✅          |  不支持 ❌"
echo "  分布式锁          |  SETNX 原生 ✅       |  需额外实现"
echo "  原子操作(INCR)    |  原生支持            |  不支持"
echo "  多线程            |  6.0+ 支持           |  天生多线程 ✅"
echo "  内存分配          |  灵活配置            |  Slab Allocation"
echo ""

# ===== 4. 选型分析 =====
echo "--- 4. 本项目选型分析 ---"
echo ""
echo "  项目中 Redis 负责:"
echo "   - 座位锁定 (SETNX 分布式锁)"
echo "   - 卖品购物车 (Hash 存储)"
echo "   - 订单号生成 (INCR 原子递增)"
echo "   - 数据持久化 (防重启丢失)"
echo ""
echo "  Memcached 适合场景:"
echo "   - 影片列表缓存 (简单 K-V, 读多写少)"
echo "   - 影城信息缓存 (数据几乎不变)"
echo "   - 卖品列表缓存 (纯只读)"
echo ""
echo "  结论: 项目中 Memcached 可作为 Redis 的补充缓存层,"
echo "        各司其职, 非替代关系。"
echo "==============================================="