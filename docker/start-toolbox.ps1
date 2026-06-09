# Cinema Booking - Docker Toolbox startup (PowerShell)
# Prerequisite: run setup-docker-toolbox.ps1 first (once after reboot)

$ToolboxPath = "D:\Docker Toolbox"
if (Test-Path $ToolboxPath) { $env:PATH = "$ToolboxPath;$env:PATH" }

Write-Host "=== Cinema Booking - Docker Toolbox ===" -ForegroundColor Cyan

if (-not (Get-Command docker-machine -ErrorAction SilentlyContinue)) {
    Write-Host "ERROR: docker-machine not found. Install Docker Toolbox first." -ForegroundColor Red
    exit 1
}

# Remove Docker Desktop env vars that conflict with Toolbox
Remove-Item Env:DOCKER_HOST -ErrorAction SilentlyContinue
Remove-Item Env:DOCKER_TLS_VERIFY -ErrorAction SilentlyContinue
Remove-Item Env:DOCKER_CERT_PATH -ErrorAction SilentlyContinue
Remove-Item Env:DOCKER_MACHINE_NAME -ErrorAction SilentlyContinue

# Check if default VM exists
$machines = docker-machine ls --format "{{.Name}}" 2>$null
if ($machines -notcontains "default") {
    Write-Host "No 'default' VM found. Creating one (first time, ~5 min) ..." -ForegroundColor Yellow
    Write-Host "Requires VirtualBox. Do not close this window." -ForegroundColor Yellow
    docker-machine create --driver virtualbox default
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Failed to create VM. Check VirtualBox is installed." -ForegroundColor Red
        Write-Host "Manual: docker-machine create --driver virtualbox default" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "VM 'default' created." -ForegroundColor Green
}

Write-Host "Starting docker-machine default ..."
docker-machine start default 2>&1 | Write-Host

Write-Host "Setting Docker environment (PowerShell) ..."
docker-machine env default --shell powershell | Invoke-Expression

$ip = (docker-machine ip default).Trim()
if ([string]::IsNullOrWhiteSpace($ip)) {
    Write-Host "ERROR: Cannot get VM IP. Run: docker-machine ls" -ForegroundColor Red
    exit 1
}
Write-Host "VM IP: $ip" -ForegroundColor Green

Write-Host "Checking Docker connection ..."
docker info 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Cannot connect to Docker VM." -ForegroundColor Red
    Write-Host "Try: docker-machine restart default" -ForegroundColor Yellow
    exit 1
}

Set-Location $PSScriptRoot
Write-Host "Working dir: $(Get-Location)"
Write-Host "Building and starting containers (first run may take 10+ minutes) ..." -ForegroundColor Yellow

if (Get-Command docker-compose -ErrorAction SilentlyContinue) {
    docker-compose up -d --build
}
elseif (Get-Command docker -ErrorAction SilentlyContinue) {
    docker compose up -d --build
}
else {
    Write-Host "ERROR: docker-compose or docker not found." -ForegroundColor Red
    exit 1
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: docker-compose failed. See messages above." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host " Done!"
Write-Host " Site:     http://$ip"
Write-Host " RabbitMQ: http://${ip}:15672  (guest/guest)"
Write-Host " Health:   http://${ip}/api/health"
Write-Host " Login:    demo/demo123  admin/admin123"
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Use http://$ip  -  Do NOT use localhost" -ForegroundColor Yellow
