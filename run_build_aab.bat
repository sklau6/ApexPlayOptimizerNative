@echo off

REM powershell -ExecutionPolicy Bypass -File .\generate_keystore.ps1

title Apex Play Optimizer - Release AAB
cd /d C:\Users\Tlau\src\ApexPlayOptimizerNative
set "JAVA_HOME=C:\Program Files\Java\jdk-18.0.1.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "GRADLE_EXE=C:\Users\Tlau\.gradle\wrapper\dists\gradle-8.10.2-all\7iv73wktx1xtkvlq19urqw1wm\gradle-8.10.2\bin\gradle.bat"

if not exist keystore.properties (
    echo ERROR: keystore.properties not found.
    echo Run: powershell -ExecutionPolicy Bypass -File .\generate_keystore.ps1
    pause
    exit /b 1
)

echo [AAB] Building signed release AAB for production...
echo.
call "%GRADLE_EXE%" bundleRelease
if %ERRORLEVEL% neq 0 (
    echo.
    echo BUILD FAILED.
    pause
    exit /b 1
)
echo.
echo BUILD SUCCESS!
echo AAB: app\build\outputs\bundle\release\app-release.aab
echo Upload this file to Google Play Console ^> Internal Testing ^> Create release.
echo.
pause