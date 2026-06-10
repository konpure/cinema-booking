# 星影 Cinema — 影院订票系统（小组作业包）

软件架构设计实践课程大作业项目。

> **组员请先读根目录 `【请先读我】组员入门.md`，再按 `本地运行指南-组员版.md` 启动。**

## 架构技术（已实现）

| 技术 | 用途 |
|------|------|
| Spring Security | JWT 认证授权 |
| Redis | 分布式座位锁 |
| RabbitMQ | 购票成功后异步通知 |
| Nginx | 反向代理 + 双实例负载均衡 |
| Docker Compose | 单机容器化部署 |
| Vue 3 | 前端 SPA |
| ECharts | 管理端票房可视化 |
| SLF4J/Logback | 统一日志 |
| JUnit | 单元测试 |

完整对照见：**`课程架构技术-用到与未用到.md`**（或 `docs/` 下同文件）

## 业务功能

- **5 家影城**（北京 3 家 + 上海 + 广州），顶部切换
- **10 部热映影片**，每家影城多场次排片
- 在线选座购票（Redis 分布式锁）
- 卖品加购（爆米花、可乐、套餐）
- 订单查询、管理后台票房统计

## 文档索引

| 文档 | 说明 |
|------|------|
| **`【请先读我】组员入门.md`** | **解压后第一个看** |
| **`本地运行指南-组员版.md`** | 本地 / Docker 启动步骤 |
| **`课程架构技术-用到与未用到.md`** | 课程 PPT 技术清单对照 |
| `docs/小组协作与项目状态.md` | 分工、答辩、协作 |
| `docs/架构技术-文件对照表.md` | 源码位置速查 |
| `docs/技术总结报告.md` | 报告草稿 |
| `docs/Docker-Toolbox-部署指南.md` | Docker 完整栈部署 |

## 两种启动方式

### 方式 A：本地模式（无 Docker）

```powershell
cd docker
.\start-rabbitmq.ps1

cd ..\backend
copy src\main\resources\application-local.yml.example src\main\resources\application-local.yml
# 编辑 application-local.yml 填入 MySQL 密码
.\run-backend.ps1

cd ..\frontend
npm install
npm run dev
```

访问 **http://localhost:5173** · 账号 demo/demo123

### 方式 B：Docker Desktop（推荐有 Docker 的同学）

```powershell
cd docker
docker compose up -d --build
```

访问 **http://localhost** · 账号 demo/demo123

## 演示账号

| 账号 | 密码 | 角色 |
|------|------|------|
| demo | demo123 | 普通用户 |
| admin | admin123 | 管理员 |

## 项目结构

```
本文件夹/
├── 【请先读我】组员入门.md
├── 本地运行指南-组员版.md
├── backend/          Spring Boot 后端
├── frontend/         Vue 3 前端
├── docker/           Docker Compose + RabbitMQ 脚本
└── docs/             报告、分工、架构对照
```
