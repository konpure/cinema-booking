# Docker Toolbox - Complete setup from scratch (PowerShell Admin recommended)
# Usage:
#   cd "C:\Users\24281\IdeaProjects\software architecture\cinema-booking\docker"
#   .\setup-docker-toolbox.ps1

$ErrorActionPreference = "Stop"
$ToolboxPath = "D:\Docker Toolbox"

Write-Host "=== Docker Toolbox Setup ===" -ForegroundColor Cyan

# 1. Add Toolbox to PATH for this session
if (Test-Path $ToolboxPath) {
    $env:PATH = "$ToolboxPath;$env:PATH"
    Write-Host "[OK] Toolbox: $ToolboxPath" -ForegroundColor Green
} else {
    Write-Host "[FAIL] Docker Toolbox not found at $ToolboxPath" -ForegroundColor Red
    Write-Host "Edit this script if your install path is different." -ForegroundColor Yellow
    exit 1
}

# 2. Clear old Docker Desktop env
Remove-Item Env:DOCKER_HOST -ErrorAction SilentlyContinue
Remove-Item Env:DOCKER_TLS_VERIFY -ErrorAction SilentlyContinue
Remove-Item Env:DOCKER_CERT_PATH -ErrorAction SilentlyContinue
Remove-Item Env:DOCKER_MACHINE_NAME -ErrorAction SilentlyContinue

# VirtualBox hardening workaround (fixes Error -104 on some Win10/11)
$env:VBOX_USE_DISABLE_HARDENING = "1"
Write-Host "[INFO] VBOX_USE_DISABLE_HARDENING=1" -ForegroundColor Yellow

Write-Host "docker:         $(docker --version)"
Write-Host "docker-machine: $(docker-machine --version)"
Write-Host "docker-compose: $(docker-compose --version)"

# 3. Check boot2docker ISO
$iso = "$env:USERPROFILE\.docker\machine\cache\boot2docker.iso"
if (-not (Test-Path $iso)) {
    Write-Host "[FAIL] Missing $iso" -ForegroundColor Red
    Write-Host "Download boot2docker.iso v19.03.12 and save as boot2docker.iso (no space!)" -ForegroundColor Yellow
    exit 1
}
Write-Host "[OK] boot2docker.iso ($((Get-Item $iso).Length) bytes)" -ForegroundColor Green

# 4. Create VM if not exists
$machines = @(docker-machine ls --format "{{.Name}}" 2>$null)
if ($machines -notcontains "default") {
    Write-Host ""
    Write-Host "Creating VM 'default' with VirtualBox (first time ~3 min) ..." -ForegroundColor Yellow
    Write-Host "If this fails, see docs/Docker-Toolbox setup guide (VirtualBox exit code 1 section)" -ForegroundColor Yellow
    Write-Host ""

    # Clean broken partial VM if any (ignore if not exists)
    $prevEAP = $ErrorActionPreference
    $ErrorActionPreference = "SilentlyContinue"
    docker-machine rm -f default 2>$null | Out-Null
    $ErrorActionPreference = $prevEAP

    docker-machine create --driver virtualbox default
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "[FAIL] VM creation failed." -ForegroundColor Red
        Write-Host "Most common fix: disable Hyper-V and reboot, then run this script again." -ForegroundColor Yellow
        exit 1
    }
} else {
    Write-Host "[OK] VM 'default' already exists" -ForegroundColor Green
}

# 5. Start VM
Write-Host "Starting VM ..."
docker-machine start default 2>&1 | Write-Host

# 6. Configure PowerShell env (CRITICAL: --shell powershell)
Write-Host "Setting Docker environment ..."
docker-machine env default --shell powershell | Invoke-Expression

$ip = (docker-machine ip default).Trim()
Write-Host "VM IP: $ip" -ForegroundColor Green
Write-Host "DOCKER_HOST: $env:DOCKER_HOST" -ForegroundColor Green

# 7. Verify connection
Write-Host "Testing docker connection ..."
docker info 2>&1 | Select-Object -First 8
if ($LASTEXITCODE -ne 0) {
    Write-Host "[FAIL] docker info failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host " Docker Toolbox is ready!"
Write-Host " VM IP: $ip"
Write-Host ""
Write-Host " Next: run .\start-toolbox.ps1 to start cinema project"
Write-Host " Or manually:"
Write-Host "   docker-compose up -d --build"
Write-Host " Site: http://$ip"
Write-Host "==========================================" -ForegroundColor Green
