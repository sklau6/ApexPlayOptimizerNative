@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-18.0.1.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "GRADLE=C:\Users\Tlau\.gradle\wrapper\dists\gradle-8.10.2-all\7iv73wktx1xtkvlq19urqw1wm\gradle-8.10.2\bin\gradle.bat"
cd /d C:\Users\Tlau\src\ApexPlayOptimizerNative
call "%GRADLE%" assembleDebug --no-daemon
exit /b %ERRORLEVEL%
