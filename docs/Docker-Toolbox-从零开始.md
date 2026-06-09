# Docker Toolbox 从零开始（已卸载 Docker Desktop）

> 你的 Docker Toolbox 安装在：`D:\Docker Toolbox`  
> boot2docker.iso 已就绪。按下面顺序操作即可。

---

## 第 0 步：确认 Desktop 已卸载

打开 PowerShell 执行：

```powershell
docker --version
```

应显示类似 `Docker version 19.03.1`（来自 Toolbox），**不应**再报 `dockerDesktopLinuxEngine`。

如果 `docker` 命令不存在，把 Toolbox 加入 PATH：

```powershell
$env:PATH = "D:\Docker Toolbox;$env:PATH"
```

---

## 第 1 步：处理 Hyper-V 冲突（重要）

你的系统 **Hyper-V 已开启**，会和 VirtualBox 冲突，导致虚拟机 `exit code 1` 启动失败。

**Docker Toolbox 必须用 VirtualBox，所以需要关闭 Hyper-V：**

1. **Win + R** → 输入 `optionalfeatures` → 回车
2. **取消勾选**：
   - Hyper-V
   - 虚拟机平台
   - Windows 虚拟机监控程序平台
3. **重启电脑**（必须重启才生效）

> 如果暂时不能重启，可先跳到文末「本地启动方案」完成答辩。

---

## 第 2 步：一键初始化 Docker（管理员 PowerShell）

重启后，**以管理员身份**打开 PowerShell：

```powershell
cd "C:\Users\24281\IdeaProjects\software architecture\cinema-booking\docker"
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
.\setup-docker-toolbox.ps1
```

脚本会自动：
1. 配置 Toolbox 路径
2. 清除 Desktop 残留环境变量
3. 创建 `default` 虚拟机（首次约 3 分钟）
4. 启动并配置 `docker-machine env`
5. 验证 `docker info`

成功后会显示 VM IP，通常是 **192.168.99.100**。

---

## 第 3 步：启动影院项目

**同一个 PowerShell 窗口**（环境变量已配置）中：

```powershell
.\start-toolbox.ps1
```

或手动：

```powershell
docker-compose up -d --build
```

首次构建约 10–20 分钟（需拉取 MySQL、Redis 等镜像，**建议开加速器**）。

---

## 第 4 步：访问网站

```powershell
docker-machine ip default
```

浏览器打开：**http://192.168.99.100**（以实际 IP 为准）

| 服务 | 地址 |
|------|------|
| 网站 | http://192.168.99.100 |
| RabbitMQ | http://192.168.99.100:15672 |
| 健康检查 | http://192.168.99.100/api/health |

账号：`demo/demo123`，管理员：`admin/admin123`

---

## 每次开机后怎么用 Docker

**每次重启电脑后**，Docker 虚拟机不会自动连上，需要重新配置：

```powershell
$env:PATH = "D:\Docker Toolbox;$env:PATH"
docker-machine start default
docker-machine env default --shell powershell | Invoke-Expression
cd "C:\Users\24281\IdeaProjects\software architecture\cinema-booking\docker"
docker-compose up -d
```

然后访问 `http://192.168.99.100`

---

## 常用命令速查

```powershell
# 查看虚拟机状态
docker-machine ls

# 查看运行中的容器
docker ps

# 查看日志
docker-compose logs -f

# 停止项目
docker-compose down

# 停止虚拟机
docker-machine stop default
```

---

## 常见错误

### `dockerDesktopLinuxEngine` 找不到
Desktop 没卸干净，或 PATH 里还有 Desktop 的 docker。确认：

```powershell
(Get-Command docker).Source
# 应显示 D:\Docker Toolbox\docker.exe
```

### `default does not exist`
先运行 `.\setup-docker-toolbox.ps1`

### VirtualBox `exit code 1` / Error -104

**A. 彻底关闭 Hyper-V：** `optionalfeatures` 取消相关项 + 管理员 CMD 执行 `bcdedit /set hypervisorlaunchtype off` + **重启**

**B. 关闭内存完整性：** Windows 安全中心 → 设备安全性 → 核心隔离 → 关闭「内存完整性」→ 重启

**C. 禁用 VBox Hardening 后重试：**
```powershell
$env:VBOX_USE_DISABLE_HARDENING = "1"
docker-machine rm -f default
.\setup-docker-toolbox.ps1
```

**D. 仍失败 → 用 `docs/本地启动指南-无Docker.md`**

### `docker info` 卡住
没执行 `docker-machine env default --shell powershell | Invoke-Expression`

### 拉镜像很慢
开加速器后重新 `docker-compose up -d --build`

---

## 本地启动方案（Docker 仍搞不定时）

见 **`docs/本地启动指南-无Docker.md`**
