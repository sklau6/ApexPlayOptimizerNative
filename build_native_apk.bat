@echo off
title Apex Play Optimizer - Native Android Build
echo =====================================================
echo   APEX PLAY OPTIMIZER - NATIVE ANDROID BUILD
echo   Kotlin + Jetpack Compose
echo =====================================================
echo.

cd /d C:\Users\Tlau\src\ApexPlayOptimizerNative

set "JAVA_HOME=C:\Program Files\Java\jdk-18.0.1.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "GRADLE_EXE=C:\Users\Tlau\.gradle\wrapper\dists\gradle-8.10.2-all\7iv73wktx1xtkvlq19urqw1wm\gradle-8.10.2\bin\gradle.bat"

echo [1/2] Verifying local.properties SDK path...
if exist local.properties (
    echo   [OK] local.properties exists.
) else (
    echo   Writing local.properties...
    echo sdk.dir=C:/Users/Tlau/AppData/Local/Android/Sdk> local.properties
    echo   [OK] Created.
)
echo.

echo [2/2] Building Debug APK (first build may take 10-20 min to download dependencies)...
echo       Kotlin 1.9.25 + Compose BOM 2024.05.00
echo.
call "%GRADLE_EXE%" assembleDebug
if %ERRORLEVEL% neq 0 (
    echo.
    echo =====================================================
    echo   BUILD FAILED. Review errors above.
    echo =====================================================
    pause
    exit /b 1
)

echo.
echo =====================================================
echo   BUILD SUCCESS!
echo   APK: app\build\outputs\apk\debug\app-debug.apk
echo =====================================================
echo.
echo To install on connected device:
echo   adb install app\build\outputs\apk\debug\app-debug.apk
echo.
pause
