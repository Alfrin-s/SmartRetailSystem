@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ===============================================================
echo     Smart Retail Inventory ^& Billing System - Build Script
echo ===============================================================

rem Detect javac
set JAVAC_CMD=javac
where javac >nul 2>nul
if %errorlevel% neq 0 (
    if exist "C:\Users\ALFRIN\.jdks\jdk-17.0.20.1+1\bin\javac.exe" (
        set JAVAC_CMD="C:\Users\ALFRIN\.jdks\jdk-17.0.20.1+1\bin\javac.exe"
    ) else (
        echo [ERROR] javac could not be found. Please ensure JDK 17+ is installed.
        exit /b 1
    )
)

if not exist "%~dp0bin" mkdir "%~dp0bin"
if not exist "%~dp0receipts" mkdir "%~dp0receipts"

set CP="%~dp0lib\*"

echo Compiling Java source files...
%JAVAC_CMD% -encoding UTF-8 -d "%~dp0bin" -cp %CP% src\model\*.java src\util\*.java src\dao\*.java src\controller\*.java src\view\*.java src\test\*.java src\Main.java

if %errorlevel% equ 0 (
    echo [SUCCESS] Compilation completed successfully!
    echo Output directory: bin\
) else (
    echo [ERROR] Compilation failed.
    exit /b %errorlevel%
)
