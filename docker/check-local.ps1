# Check local services for cinema-booking (no Docker)
Write-Host "=== Cinema Local Service Check ===" -ForegroundColor Cyan

function Test-Port($name, $host, $port) {
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $tcp.Connect($host, $port)
        $tcp.Close()
        Write-Host "[OK]   $name  ${host}:${port}" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "[FAIL] $name  ${host}:${port}  - not reachable" -ForegroundColor Red
        return $false
    }
}

$mysql  = Test-Port "MySQL"    "127.0.0.1" 3306
$redis  = Test-Port "Redis"    "127.0.0.1" 6379
$rabbit = Test-Port "RabbitMQ" "127.0.0.1" 5672

Write-Host ""
if ($mysql -and $redis -and $rabbit) {
    Write-Host "All services OK. You can start backend + frontend." -ForegroundColor Green
    Write-Host ""
    Write-Host "Backend:  Run CinemaApplication in IDEA, or: mvn spring-boot:run"
    Write-Host "Frontend: cd ..\frontend && npm run dev"
    Write-Host "Site:     http://localhost:5173"
}
else {
    Write-Host "Some services missing. See docs/本地启动指南-无Docker.md" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "--- Docker status (optional) ---"
if (Get-Command docker-machine -ErrorAction SilentlyContinue) {
    docker-machine ls
}
else {
    Write-Host "docker-machine not found"
}

if ($env:DOCKER_HOST) {
    Write-Host "DOCKER_HOST=$env:DOCKER_HOST"
}
else {
    Write-Host "DOCKER_HOST not set (normal for local mode)"
    Write-Host "If docker info shows dockerDesktopLinuxEngine error, ignore it - use local mode."
}
