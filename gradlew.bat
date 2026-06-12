@echo off
setlocal
set DIR=%~dp0
powershell -NoProfile -ExecutionPolicy Bypass -File "%DIR%gradle\wrapper\bootstrap-gradle.ps1" -GradleVersion "9.1.0" %*
exit /b %ERRORLEVEL%
