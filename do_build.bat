@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-18.0.1.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "GRADLE=C:\Users\Tlau\.gradle\wrapper\dists\gradle-8.10.2-all\7iv73wktx1xtkvlq19urqw1wm\gradle-8.10.2\bin\gradle.bat"
call "%GRADLE%" assembleDebug --no-daemon > build_log.txt 2>&1
echo EXIT:%ERRORLEVEL% >> build_log.txt
