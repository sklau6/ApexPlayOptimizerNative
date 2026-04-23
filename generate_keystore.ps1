# Finds the JDK keytool bundled with Android Studio, generates a release keystore,
# and writes keystore.properties — run once before building a signed release AAB.

$keystoreDir  = "C:\Users\Tlau\keystores"
$keystorePath = "$keystoreDir\apexplayoptimizer.jks"
$propsFile    = "$PSScriptRoot\keystore.properties"
$alias        = "apexplay"

# --- Locate keytool.exe ---
$candidates = @(
    "$env:JAVA_HOME\bin\keytool.exe",
    "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe",
    "C:\Program Files\Android\Android Studio\jre\bin\keytool.exe",
    "C:\Program Files\Java\jdk-17\bin\keytool.exe",
    "C:\Program Files\Java\jdk-21\bin\keytool.exe",
    "C:\Program Files\Eclipse Adoptium\jdk-17*\bin\keytool.exe",
    "C:\Program Files\Microsoft\jdk-17*\bin\keytool.exe"
)

$keytool = $null
foreach ($c in $candidates) {
    $resolved = Resolve-Path $c -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty Path
    if ($resolved -and (Test-Path $resolved)) { $keytool = $resolved; break }
}

# fallback: search Android Studio folder
if (-not $keytool) {
    $found = Get-ChildItem "C:\Program Files\Android" -Recurse -Filter keytool.exe -ErrorAction SilentlyContinue |
             Select-Object -First 1
    if ($found) { $keytool = $found.FullName }
}

if (-not $keytool) {
    Write-Error "Could not find keytool.exe. Install Android Studio or set JAVA_HOME."
    exit 1
}
Write-Host "Using keytool: $keytool" -ForegroundColor Cyan

# --- Prompt for passwords ---
$storePwd = Read-Host "Enter keystore password (min 6 chars)" -AsSecureString
$storePwd2 = Read-Host "Confirm keystore password" -AsSecureString
$keyPwd   = Read-Host "Enter key password (min 6 chars)"  -AsSecureString

$plain1 = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
              [Runtime.InteropServices.Marshal]::SecureStringToBSTR($storePwd))
$plain2 = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
              [Runtime.InteropServices.Marshal]::SecureStringToBSTR($storePwd2))
$plainKey = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
              [Runtime.InteropServices.Marshal]::SecureStringToBSTR($keyPwd))

if ($plain1 -ne $plain2) { Write-Error "Passwords do not match."; exit 1 }
if ($plain1.Length -lt 6) { Write-Error "Password must be at least 6 characters."; exit 1 }

# --- Generate keystore (skip if already exists) ---
New-Item -ItemType Directory -Force -Path $keystoreDir | Out-Null

if (Test-Path $keystorePath) {
    Write-Host "Keystore already exists at $keystorePath - skipping keytool." -ForegroundColor Yellow
} else {
    & $keytool -genkey -v `
        -keystore $keystorePath `
        -alias $alias `
        -keyalg RSA -keysize 2048 -validity 10000 `
        -dname "CN=ApexPlay,O=ApexPlay,C=US" `
        -storepass $plain1 `
        -keypass $plainKey

    if ($LASTEXITCODE -ne 0) { Write-Error "keytool failed."; exit 1 }
}

# --- Write keystore.properties without BOM (Java Properties.load requires no BOM) ---
$content = "storeFile=$($keystorePath.Replace('\','/'))`r`nstorePassword=$plain1`r`nkeyAlias=$alias`r`nkeyPassword=$plainKey`r`n"
[System.IO.File]::WriteAllText($propsFile, $content, [System.Text.Encoding]::ASCII)

Write-Host ""
Write-Host "Done! keystore.properties written." -ForegroundColor Green
Write-Host "Now run: run_build_aab.bat" -ForegroundColor Yellow
