# Docker Toolbox 部署指南

> 适用于 **Docker Toolbox**（VirtualBox + docker-machine），**不是** Docker Desktop。

---

## 1. 前置条件

1. 已安装 **Docker Toolbox** 和 **VirtualBox**
2. **首次使用**需创建 default 虚拟机（只需一次，约 5 分钟）：

```powershell
docker-machine create --driver virtualbox default
```

若提示 `default does not exist`，说明还没执行过上面这条命令。  
`start-toolbox.ps1` 脚本也会自动尝试创建。

3. 确认虚拟机状态：

```powershell
docker-machine ls
# STATUS 应为 Running；若是 Stopped：
docker-machine start default
```

4. 查看虚拟机 IP（后面访问网站要用）：

```powershell
docker-machine ip default
# 通常是 192.168.99.100
```

---

## 2. 进入项目目录

### 在 PowerShell 中（推荐 Windows 用户）

```powershell
cd "C:\Users\24281\IdeaProjects\software architecture\cinema-booking\docker"
.\start-toolbox.ps1
```

> PowerShell **不要**用 `/c/Users/...` 这种路径，那是 Git Bash 写法。  
> 如果提示「无法加载，因为在此系统上禁止运行脚本」，先执行：
> ```powershell
> Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
> ```

### 在 Docker Quickstart Terminal 中（Git Bash）

```bash
cd "/c/Users/24281/IdeaProjects/software architecture/cinema-booking/docker"
bash start-toolbox.sh
```

---

## 3. 启动全部服务

### 方式 A：一键脚本

**PowerShell：**
```powershell
cd "C:\Users\24281\IdeaProjects\software architecture\cinema-booking\docker"
.\start-toolbox.ps1
```

**Docker Quickstart Terminal：**
```bash
bash start-toolbox.sh
```

### 方式 B：手动命令

```bash
# Toolbox 通常用 docker-compose（带连字符），不是 docker compose
docker-compose up -d --build
```

首次构建需下载镜像并编译后端，约 5–15 分钟。

---

## 4. 访问地址

**不要用 localhost**，请用虚拟机 IP：

```bash
IP=$(docker-machine ip default)
echo "网站:      http://$IP"
echo "RabbitMQ:  http://$IP:15672  (guest/guest)"
echo "健康检查:  http://$IP/api/health"
```

示例（IP 为 192.168.99.100 时）：

| 服务 | 地址 |
|------|------|
| 网站首页 | http://192.168.99.100 |
| RabbitMQ 管理台 | http://192.168.99.100:15672 |
| 负载均衡验证 | http://192.168.99.100/api/health |

| 账号 | 密码 | 角色 |
|------|------|------|
| demo | demo123 | 普通用户 |
| admin | admin123 | 管理员 |

---

## 5. 常用运维命令

```bash
# 查看容器状态
docker-compose ps

# 查看日志
docker-compose logs -f nginx
docker-compose logs -f backend-1

# 停止
docker-compose down

# 停止并清除数据卷（重置数据库）
docker-compose down -v
```

---

## 6. Toolbox 常见问题

### Q1：`failed to connect to docker API at npipe://./pipe/dockerDesktopLinuxEngine`

说明 PowerShell 仍在连接 **Docker Desktop**，而不是 Toolbox 虚拟机。

**解决步骤：**

1. 关闭 Docker Desktop（若已安装）
2. 在新 PowerShell 中执行：

```powershell
docker-machine start default
docker-machine env default --shell powershell | Invoke-Expression
docker info
```

3. 确认 `docker info` 不再报错，且 `DOCKER_HOST` 类似 `tcp://192.168.99.100:2376`：

```powershell
echo $env:DOCKER_HOST
```

4. 再运行 `.\start-toolbox.ps1`

> 关键：PowerShell 必须用 `--shell powershell`，不能用默认的 bash 格式。

### Q2：`Cannot connect to the Docker daemon`

```bash
docker-machine start default
eval $(docker-machine env default)
```

在 Docker Quickstart Terminal 里，`eval` 通常已自动执行；在普通 PowerShell 中需手动运行上面两行。

### Q3：PowerShell 里要用 docker-compose

```powershell
# 先配置环境变量（每次新开 PowerShell 需执行）
docker-machine env default --shell powershell | Invoke-Expression

cd "C:\Users\24281\IdeaProjects\software architecture\cinema-booking\docker"
docker-compose up -d --build

# 查看 IP
docker-machine ip default
```

### Q4：构建失败 / 内存不足

VirtualBox 虚拟机默认内存较小，可在 VirtualBox 管理器中将 `default` 虚拟机内存调到 **4096 MB** 以上，然后：

```bash
docker-machine stop default
docker-machine start default
```

### Q5：端口无法访问

- 确认 VirtualBox 网络适配器正常
- Windows 防火墙允许 VirtualBox 网络
- 使用 `docker-machine ip default` 的 IP，不要用 `127.0.0.1`

### Q6：`docker compose` 命令不存在

Toolbox 使用旧版 CLI，请改用：

```bash
docker-compose up -d --build   # 有连字符
```

---

## 7. 与 Docker Desktop 的区别

| 项目 | Docker Desktop | Docker Toolbox |
|------|---------------|----------------|
| 虚拟机 | WSL2/Hyper-V | VirtualBox |
| 访问地址 | http://localhost | http://192.168.99.100（或 `docker-machine ip`） |
| 启动命令 | `docker compose up` | `docker-compose up` |
| 终端 | 普通 PowerShell 即可 | 建议 Docker Quickstart Terminal |
