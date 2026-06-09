# 本地启动指南（不用 Docker）

> 适用于 Docker Toolbox / VirtualBox / Hyper-V 无法正常工作的情况。  
> **架构技术照样可以演示**：Spring Security、Redis、RabbitMQ、Vue3、ECharts、JUnit 等均可本地运行。  
> Docker + Nginx 负载均衡可在报告中结合 `docker/` 目录下的配置文件讲解。

---

## 一、需要安装的软件

| 软件 | 用途 | 安装方式（PowerShell 管理员） |
|------|------|-------------------------------|
| MySQL 8 | 业务数据库 | `winget install Oracle.MySQL` 或官网安装 |
| Redis | 座位锁 | `winget install Redis.Redis` 或 [Memurai](https://www.memurai.com/) |
| RabbitMQ | 异步订单 | 先装 Erlang，再装 [RabbitMQ Windows](https://www.rabbitmq.com/install-windows.html) |
| JDK 17 | 后端 | 你已有 OpenJDK 17 |
| Node.js | 前端 | 你已有 v22 |

安装后确认服务已启动：
- MySQL 端口 **3306**
- Redis 端口 **6379**
- RabbitMQ 端口 **5672**（管理台 15672）

---

## 二、初始化数据库

1. 登录 MySQL，创建库和用户：

```sql
CREATE DATABASE cinema DEFAULT CHARACTER SET utf8mb4;
CREATE USER 'cinema'@'localhost' IDENTIFIED BY 'cinema123';
GRANT ALL ON cinema.* TO 'cinema'@'localhost';
FLUSH PRIVILEGES;
```

2. 执行建表脚本：

用 MySQL 客户端或命令行导入项目中的：

```
cinema-booking/docker/init.sql
```

（只需建表部分，演示数据由后端 `DataInitializer` 自动插入。）

---

## 三、启动后端（IntelliJ IDEA 推荐）

1. 用 IDEA 打开 `cinema-booking/backend` 目录（Maven 项目）
2. 等待依赖下载完成
3. 运行 `com.cinema.CinemaApplication`
4. 看到 `Started CinemaApplication` 即成功
5. 访问 http://localhost:8080/api/health 应返回 JSON

**命令行启动（需本机有 Maven 或在 IDEA 里 Build 后）：**

```powershell
cd "C:\Users\24281\IdeaProjects\software architecture\cinema-booking\backend"
# IDEA 内置 Maven 或安装 Maven 后：
mvn spring-boot:run
```

默认连接：
- MySQL：`localhost:3306` 用户 `cinema` 密码 `cinema123`
- Redis：`localhost:6379`
- RabbitMQ：`localhost:5672` guest/guest

---

## 四、启动前端

新开一个 PowerShell：

```powershell
cd "C:\Users\24281\IdeaProjects\software architecture\cinema-booking\frontend"
npm install
npm run dev
```

浏览器打开：**http://localhost:5173**

`vite.config.js` 已配置代理，API 会转发到 `localhost:8080`。

---

## 五、演示账号

| 账号 | 密码 | 角色 |
|------|------|------|
| demo | demo123 | 普通用户 |
| admin | admin123 | 管理员 |

---

## 六、答辩时如何讲 Docker / Nginx

本地跑通核心流程后，答辩可以这样组织：

| 技术 | 本地演示 | 报告/配置讲解 |
|------|---------|--------------|
| Spring Security | 登录/权限 | 源码 |
| Redis | 选座占座 | `RedisSeatService.java` |
| RabbitMQ | 下单后看管理台 15672 | `OrderConsumer.java` |
| Nginx + 负载均衡 | 本地单实例 | 展示 `docker/nginx/nginx.conf` + 架构图 |
| Docker Compose | 本地不用跑 | 展示 `docker/docker-compose.yml` + 说明容器编排 |

---

## 七、常见问题

### 后端启动报 Redis 连接失败
确认 Redis 服务已启动：`redis-cli ping` 应返回 `PONG`

### 后端启动报 RabbitMQ 连接失败
确认 RabbitMQ 服务已启动，浏览器打开 http://localhost:15672

### 后端启动报 MySQL 连接失败
检查库名 `cinema`、用户 `cinema`、密码 `cinema123` 是否已创建

### 前端能开但接口 404
确认后端已在 8080 端口运行，且前端用的是 `npm run dev`（不是直接打开 html）

---

## 八、一键检查脚本

```powershell
cd "C:\Users\24281\IdeaProjects\software architecture\cinema-booking\docker"
.\check-local.ps1
```

会检测 MySQL / Redis / RabbitMQ 端口是否可用。
