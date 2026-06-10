# Run backend without IDEA (Maven Wrapper)
# Usage:
#   cd cinema-booking/backend
#   .\run-backend.ps1
# If MySQL root has password:
#   $env:DB_PASS="your_password"; .\run-backend.ps1

$backend = Split-Path -Parent $MyInvocation.MyCommand.Path
$jar = Join-Path $backend ".mvn\wrapper\maven-wrapper.jar"
$localConfig = Join-Path $backend "src\main\resources\application-local.yml"

Write-Host "=== Cinema Backend ===" -ForegroundColor Cyan
Write-Host "MySQL user: $(if ($env:DB_USER) { $env:DB_USER } else { 'root' })"
if (Test-Path $localConfig) {
    $env:SPRING_PROFILES_ACTIVE = "local"
    Write-Host "Using profile: local (application-local.yml)"
} elseif (-not $env:DB_PASS) {
    Write-Host "Tip: set password with `$env:DB_PASS='your_password' or create application-local.yml"
}
Write-Host "Tables will auto-create on first start (no MySQL GUI needed)."
Write-Host ""

java -classpath $jar "-Dmaven.multiModuleProjectDirectory=$backend" org.apache.maven.wrapper.MavenWrapperMain spring-boot:run -B
