@echo off
REM Backend Startup Script for Cetaring Application
REM This script starts the Spring Boot backend on port 8080

setlocal enabledelayedexpansion

echo.
echo ==========================================
echo Cetaring Backend Startup Script
echo ==========================================
echo.

cd /d "%~dp0"

REM Check if Java home if not already set
if not defined JAVA_HOME (
    for /f "tokens=*" %%A in ('where java') do (
        set JAVA_HOME=%%A\..\..
        goto :java_found
    )
    echo ERROR: Java not found in PATH
    exit /b 1
)

:java_found
echo Using Java from: %JAVA_HOME%
echo.

REM Check if JAR exists
if not exist target\cetaring-backend-1.0.0.jar (
    echo ERROR: JAR file not found at target\cetaring-backend-1.0.0.jar
    echo Building project first...
    call mvn clean package -DskipTests
    if errorlevel 1 (
        echo ERROR: Maven build failed
        exit /b 1
    )
)

echo Starting Cetaring Backend...
echo Port: 8080
echo Database: localhost:3306/caterhub
echo.

REM Start the application with environment variables
java -jar target\cetaring-backend-1.0.0.jar ^
    --server.port=8080 ^
    --spring.datasource.url=jdbc:mysql://localhost:3306/caterhub?createDatabaseIfNotExist=true^&useSSL=false^&allowPublicKeyRetrieval=true^&serverTimezone=Asia/Kolkata ^
    --spring.datasource.username=root ^
    --spring.datasource.****** ^
    --spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver ^
    --spring.flyway.enabled=true ^
    --logging.level.root=INFO ^
    --logging.level.com.daily.cetaring=DEBUG

pause
