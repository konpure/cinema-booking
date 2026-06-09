# Start RabbitMQ locally (keep the CMD window open)
# Run: .\start-rabbitmq.ps1

$erlHome = "C:\Program Files\Erlang OTP"
$sbin = "C:\Program Files\RabbitMQ Server\rabbitmq_server-4.3.1\sbin"

Write-Host "=== RabbitMQ Local Start ===" -ForegroundColor Cyan

if (Test-Path "$erlHome\bin\erl.exe") {
    $ver = & "$erlHome\bin\erl.exe" -noshell -eval "erlang:display(erlang:system_info(otp_release)), halt()." 2>&1
    Write-Host "Erlang OTP release: $ver"
    if ($ver -match "29") {
        Write-Host "[FAIL] Erlang 29 not supported. Install Erlang 27.x" -ForegroundColor Red
        exit 1
    }
}

function Test-Port5672 {
    try {
        $t = New-Object Net.Sockets.TcpClient
        $t.Connect("127.0.0.1", 5672)
        $t.Close()
        return $true
    } catch { return $false }
}

if (Test-Port5672) {
    Write-Host "[OK] RabbitMQ already running on port 5672" -ForegroundColor Green
    Write-Host "Management UI: http://localhost:15672 (guest/guest)"
    Write-Host "Opening management UI in browser ..."
    Start-Process "http://localhost:15672"
    exit 0
}

Write-Host "Starting RabbitMQ in new CMD window (do not close it) ..."
$cmdScript = "set `"ERLANG_HOME=$erlHome`" && cd /d `"$sbin`" && rabbitmq-server.bat"
Start-Process cmd.exe -ArgumentList "/k", $cmdScript

Write-Host "Waiting for port 5672 (up to 120s, first start may be slow) ..."
for ($i = 1; $i -le 60; $i++) {
    Start-Sleep 2
    if (Test-Port5672) {
        Write-Host "[OK] RabbitMQ started! port 5672" -ForegroundColor Green
        Write-Host "Management UI: http://localhost:15672 (guest/guest)"
        Write-Host "Keep the RabbitMQ CMD window open while using the project."
        Write-Host "Opening management UI in browser ..."
        Start-Process "http://localhost:15672"
        exit 0
    }
    if ($i % 10 -eq 0) { Write-Host "  still waiting... ${i}x2s" }
}

Write-Host "[WARN] Script timed out, but RabbitMQ may still be starting." -ForegroundColor Yellow
Write-Host "Check the CMD window. If you see 'completed with 0 plugins', run:"
Write-Host "  .\start-local.ps1"
