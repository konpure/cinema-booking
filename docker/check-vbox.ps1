# Check if Windows hypervisor blocks VirtualBox
Write-Host "=== VirtualBox Compatibility Check ===" -ForegroundColor Cyan

Write-Host "`n[1] Hyper-V / hypervisor (systeminfo):"
systeminfo | Select-String -Pattern "Hyper-V"

Write-Host "`n[2] hypervisorlaunchtype (needs Admin CMD: bcdedit | findstr hypervisor):"
Write-Host "    Expected after fix: hypervisorlaunchtype    Off"
Write-Host "    Run in Admin CMD:   bcdedit /set hypervisorlaunchtype off  then REBOOT"

Write-Host "`n[3] Memory Integrity (Core Isolation):"
try {
    $v = (Get-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\DeviceGuard\Scenarios\HypervisorEnforcedCodeIntegrity" -Name Enabled -ErrorAction Stop).Enabled
    if ($v -eq 0) { Write-Host "    [OK] Memory Integrity is OFF" -ForegroundColor Green }
    else { Write-Host "    [FAIL] Memory Integrity is ON - turn off in Windows Security" -ForegroundColor Red }
} catch {
    Write-Host "    (registry key not found - usually OK)"
}

Write-Host "`n[4] VirtualBox VM 'default':"
$vbox = "C:\Program Files\Oracle\VirtualBox\VBoxManage.exe"
if (Test-Path $vbox) {
    & $vbox list vms 2>&1
    $env:VBOX_USE_DISABLE_HARDENING = "1"
    Write-Host "    Trying to start VM ..."
    & $vbox startvm default --type headless 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "    [OK] VM started! Run setup-docker-toolbox.ps1 again." -ForegroundColor Green
    } else {
        Write-Host "    [FAIL] VM still cannot start (Error -104 = hypervisor conflict)" -ForegroundColor Red
    }
} else {
    Write-Host "    VirtualBox not found"
}

Write-Host "`nIf VM still fails after hypervisorlaunchtype Off + reboot:"
Write-Host "  Use local mode: docs/本地启动指南-无Docker.md" -ForegroundColor Yellow
