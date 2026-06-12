# 验证 RabbitMQ 与 ZooKeeper 已集成

> 本文仅说明如何验证两项技术已在项目中生效，不涉及 Docker。

---

## 前置条件

启动前确认以下服务在运行：

```bash
ss -tlnp | grep -E '5672|2181|8080'
```

| 端口 | 服务 |
|------|------|
| 5672 | RabbitMQ |
| 2181 | ZooKeeper |
| 8080 | 后端 Spring Boot |

若后端未启动：

```bash
cd backend
java -jar target/cinema-booking-1.0.0.jar
```

---

## 一、验证 ZooKeeper

### 1. 健康检查（含 ZK 注册状态）

```bash
curl http://127.0.0.1:8080/api/health
```

**通过标准：** 返回 JSON 中 `data.zookeeper.connected` 为 `true`，且存在 `registeredPath`（例如 `/cinema/instances/cinema-booking`）。

### 2. 查询已注册实例

```bash
curl http://127.0.0.1:8080/api/zookeeper/instances
```

**通过标准：** 返回 JSON 中 `data.instances` 为非空数组，包含 `instanceId`、`host`、`port` 等字段。

### 3. 后端启动日志（可选）

```bash
grep "Registered backend instance in ZooKeeper" /tmp/cinema-backend.log
```

**通过标准：** 出现类似：

```text
Registered backend instance in ZooKeeper: /cinema/instances/cinema-booking
```

---

## 二、验证 RabbitMQ

### 1. 确认 RabbitMQ 进程在监听

```bash
ss -tlnp | grep 5672
```

**通过标准：** 5672 端口处于 `LISTEN` 状态。

### 2. 后端启动时已连接 RabbitMQ（可选）

查看后端日志，应出现：

```text
Created new connection: rabbitConnectionFactory... amqp://guest@127.0.0.1:5672/
```

### 3. 购票触发消息生产与消费（核心验证）

在系统中完成一次购票（登录 `demo/demo123` → 选座 → 支付），或在命令行执行：

```bash
TOKEN=$(curl -s -X POST http://127.0.0.1:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo","password":"demo123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])")

SID=161   # 换成实际存在的场次 ID

LOCK=$(curl -s -X POST http://127.0.0.1:8080/api/bookings/lock \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d "{\"screeningId\":$SID,\"seats\":[{\"row\":1,\"col\":2}]}")
LT=$(echo "$LOCK" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['lockToken'])")

curl -s -X POST http://127.0.0.1:8080/api/bookings/submit \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d "{\"screeningId\":$SID,\"seats\":[{\"row\":1,\"col\":2}],\"lockToken\":\"$LT\",\"snacks\":[]}"
```

购票成功后，检查后端日志：

```bash
grep -E 'Order notification sent|Async order notification processed' /tmp/cinema-backend.log | tail -5
```

**通过标准：** 同一订单号出现两行日志：

```text
Order notification sent to RabbitMQ: C17xxxxxxxx-x
Async order notification processed: orderNo=C17xxxxxxxx-x user=... screening=...
```

第一行表示 `BookingService` 已发消息，第二行表示 `OrderConsumer` 已消费，说明 RabbitMQ 全链路正常。

---

## 快速核对清单

| 项目 | 命令 / 操作 | 预期 |
|------|------------|------|
| ZooKeeper 连接 | `curl .../api/health` | `zookeeper.connected: true` |
| ZooKeeper 注册 | `curl .../api/zookeeper/instances` | `instances` 非空 |
| RabbitMQ 监听 | `ss -tlnp \| grep 5672` | 端口在监听 |
| RabbitMQ 消息 | 购票后查日志 | 生产 + 消费各一行 |

以上四项均满足，即表示 RabbitMQ 与 ZooKeeper 已成功集成。
