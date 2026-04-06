$env:JAVA_HOME = 'C:\Program Files\Java\jdk-18.0.1.1'
$env:PATH = $env:JAVA_HOME + '\bin;' + $env:PATH
$gradle = 'C:\Users\Tlau\.gradle\wrapper\dists\gradle-8.10.2-all\7iv73wktx1xtkvlq19urqw1wm\gradle-8.10.2\bin\gradle.bat'
$logFile = 'C:\Users\Tlau\src\ApexPlayOptimizerNative\build_result.txt'
Set-Location 'C:\Users\Tlau\src\ApexPlayOptimizerNative'
$output = & $gradle assembleDebug --no-daemon 2>&1
$output | Out-File -FilePath $logFile -Encoding UTF8
"EXITCODE:$LASTEXITCODE" | Add-Content $logFile
