# Install Redis + RabbitMQ for local cinema-booking (no Docker)
# Run as Administrator PowerShell

Write-Host "=== Install Local Dependencies ===" -ForegroundColor Cyan
Write-Host "MySQL should already be running on port 3306." -ForegroundColor Green
Write-Host ""

Write-Host "[1/2] Installing Redis ..."
winget install --id Redis.Redis -e --accept-package-agreements --accept-source-agreements

Write-Host ""
Write-Host "[2/2] Installing Erlang + RabbitMQ ..."
winget install --id Erlang.ErlangOTP -e --accept-package-agreements --accept-source-agreements
winget install --id RabbitMQ.RabbitMQ -e --accept-package-agreements --accept-source-agreements

Write-Host ""
Write-Host "After install:" -ForegroundColor Yellow
Write-Host "  1. Restart PowerShell"
Write-Host "  2. Start Redis service (Services.msc -> Redis -> Start)"
Write-Host "  3. Start RabbitMQ service (Services.msc -> RabbitMQ -> Start)"
Write-Host "  4. Run: .\check-local.ps1"
Write-Host "  5. Init DB: see docs/本地启动指南-无Docker.md"
Write-Host "  6. IDEA run CinemaApplication, then npm run dev in frontend"
