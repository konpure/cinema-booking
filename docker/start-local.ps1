# Cinema Booking - Local startup helper (no Docker)
Write-Host "=== Cinema Local Startup ===" -ForegroundColor Cyan

function Test-Port($name, $port) {
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $tcp.Connect("127.0.0.1", $port)
        $tcp.Close()
        Write-Host "[OK]   $name  port $port" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "[FAIL] $name  port $port" -ForegroundColor Red
        return $false
    }
}

$mysql  = Test-Port "MySQL"    3306
$redis  = Test-Port "Redis"    6379
$rabbit = Test-Port "RabbitMQ" 5672

Write-Host ""
if (-not ($mysql -and $redis -and $rabbit)) {
    Write-Host "Missing services. Install Redis + RabbitMQ first:" -ForegroundColor Yellow
    Write-Host "  Redis:    https://github.com/tporadowski/redis/releases (Redis-x64-*.msi)"
    Write-Host "  RabbitMQ: https://www.rabbitmq.com/install-windows.html (need Erlang first)"
    Write-Host ""
    Write-Host "After install, start them in services.msc, then run this script again."
    exit 1
}

Write-Host "All services ready!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "  1. MySQL: create DB (once) - run docs/init-local-db.sql in MySQL"
Write-Host "  2. Backend: open backend/ in IDEA -> Run CinemaApplication"
Write-Host "  3. Frontend: new terminal:"
Write-Host '     cd "..\frontend"'
Write-Host "     npm run dev"
Write-Host ""
Write-Host "  Site: http://localhost:5173"
Write-Host "  Login: demo / demo123   admin / admin123"
