# PowerShell Run Script for Smart Healthcare Fog Node
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

$jarPath = Join-Path $scriptDir "target\smart-healthcare-fog-0.0.1-SNAPSHOT.jar"

if (-Not (Test-Path $jarPath)) {
    Write-Host "[INFO] Building package..." -ForegroundColor Cyan
    & "C:\Users\ChangeMe\apache-maven-3.9.15\bin\mvn.cmd" clean package -DskipTests
}

Write-Host "[INFO] Starting Smart Healthcare Fog Computing Node..." -ForegroundColor Green
Write-Host "[INFO] Access Health Check: http://localhost:8080/api/health" -ForegroundColor Yellow
java -jar $jarPath
