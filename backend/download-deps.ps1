# Download Maven dependencies for IDEA (no Maven install needed)
$backend = Split-Path -Parent $MyInvocation.MyCommand.Path
$jar = Join-Path $backend ".mvn\wrapper\maven-wrapper.jar"
Write-Host "Downloading Maven dependencies ..." -ForegroundColor Cyan
java -classpath $jar "-Dmaven.multiModuleProjectDirectory=$backend" org.apache.maven.wrapper.MavenWrapperMain dependency:resolve -B
if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Dependencies downloaded. Now reload Maven in IDEA." -ForegroundColor Green
} else {
    Write-Host "[FAIL] See errors above" -ForegroundColor Red
}
