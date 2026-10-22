@echo off
setlocal
cd /d "%~dp0"

rem Detect java
set JAVA_CMD=java
if exist "C:\Users\ALFRIN\.jdks\jdk-17.0.20.1+1\bin\java.exe" (
    set JAVA_CMD="C:\Users\ALFRIN\.jdks\jdk-17.0.20.1+1\bin\java.exe"
)

set CP="%~dp0bin;%~dp0lib\*"

echo Running automated tests...
%JAVA_CMD% -cp %CP% test.SystemVerificationTest
