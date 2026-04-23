@echo off
title Apex Play Optimizer - Debug APK
cd /d C:\Users\Tlau\src\ApexPlayOptimizerNative
set "JAVA_HOME=C:\Program Files\Java\jdk-18.0.1.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "GRADLE_EXE=C:\Users\Tlau\.gradle\wrapper\dists\gradle-8.10.2-all\7iv73wktx1xtkvlq19urqw1wm\gradle-8.10.2\bin\gradle.bat"

echo [APK] Building debug APK for testing...
echo.
call "%GRADLE_EXE%" assembleDebug
if %ERRORLEVEL% neq 0 (
    echo.
    echo BUILD FAILED.
    pause
    exit /b 1
)
echo.
echo BUILD SUCCESS!
echo APK: app\build\outputs\apk\debug\app-debug.apk
echo.
pause
