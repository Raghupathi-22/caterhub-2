@echo off
REM Cetaring Backend - Simple Startup Script
REM This script builds and starts the backend application

setlocal enabledelayedexpansion

echo.
echo ========================================
echo  Cetaring Backend - Quick Start
echo ========================================
echo.

cd /d "%~dp0"

REM Check if Python is available (preferred)
where python >nul 2>&1
if %errorlevel% equ 0 (
    echo Using Python startup script...
    python start_backend.py
    exit /b %errorlevel%
)

REM Fall back to PowerShell
echo Using PowerShell startup script...
powershell -NoProfile -ExecutionPolicy Bypass -File startup-backend.ps1 -BuildFirst

pause
