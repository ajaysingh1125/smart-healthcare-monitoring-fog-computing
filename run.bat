@echo off
setlocal
cd /d "%~dp0"

set "JAR_PATH=target\smart-healthcare-fog-0.0.1-SNAPSHOT.jar"

if not exist "%JAR_PATH%" (
    echo [INFO] Building package...
    call "C:\Users\ChangeMe\apache-maven-3.9.15\bin\mvn.cmd" clean package -DskipTests
)

echo [INFO] Starting Smart Healthcare Fog Computing Node...
echo [INFO] Access Health Check: http://localhost:8080/api/health
java -jar "%JAR_PATH%"
pause
