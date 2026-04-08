param([string]$SourceImage = "icon_source.png")

$ErrorActionPreference = "Stop"

if (!(Test-Path $SourceImage)) {
    Write-Host ""
    Write-Host "ERROR: Source image not found at '$SourceImage'" -ForegroundColor Red
    Write-Host ""
    Write-Host "USAGE:" -ForegroundColor Yellow
    Write-Host "  1. Save the app icon PNG to this folder as 'icon_source.png'"
    Write-Host "  2. Run:  powershell -ExecutionPolicy Bypass -File generate_icons.ps1"
    Write-Host "     Or:   .\generate_icons.ps1 -SourceImage C:\path\to\your\icon.png"
    Write-Host ""
    exit 1
}

Add-Type -AssemblyName System.Drawing

$resDir = "app\src\main\res"

$densities = [ordered]@{
    "mipmap-mdpi"    = 48
    "mipmap-hdpi"    = 72
    "mipmap-xhdpi"   = 96
    "mipmap-xxhdpi"  = 144
    "mipmap-xxxhdpi" = 192
}

$srcPath = (Resolve-Path $SourceImage).Path
Write-Host ""
Write-Host "Generating Android launcher icons from: $srcPath" -ForegroundColor Cyan
Write-Host ""

foreach ($entry in $densities.GetEnumerator()) {
    $dir  = Join-Path $resDir $entry.Key
    $size = $entry.Value

    if (!(Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir | Out-Null
    }

    $src = [System.Drawing.Image]::FromFile($srcPath)
    $bmp = New-Object System.Drawing.Bitmap($size, $size)
    $g   = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode  = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode      = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode    = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $g.DrawImage($src, 0, 0, $size, $size)

    $outFile     = Join-Path $dir "ic_launcher.png"
    $outFileRound = Join-Path $dir "ic_launcher_round.png"
    $bmp.Save($outFile,      [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save($outFileRound, [System.Drawing.Imaging.ImageFormat]::Png)

    $g.Dispose(); $bmp.Dispose(); $src.Dispose()
    Write-Host "  [OK] $($entry.Key)/ic_launcher.png  ($size x $size)" -ForegroundColor Green
}

# Adaptive icon foreground PNG → drawable/ic_launcher_foreground.png
# (replaces the placeholder XML so there is no resource name conflict)
$drawableDir = Join-Path $resDir "drawable"
if (!(Test-Path $drawableDir)) { New-Item -ItemType Directory -Path $drawableDir | Out-Null }

$placeholderXml = Join-Path $drawableDir "ic_launcher_foreground.xml"
if (Test-Path $placeholderXml) { Remove-Item $placeholderXml -Force }

# 432 x 432 = 108dp @ xxxhdpi; content centred in the 72dp safe zone (288px)
$foreSize = 432
$src      = [System.Drawing.Image]::FromFile($srcPath)
$fgBmp    = New-Object System.Drawing.Bitmap($foreSize, $foreSize)
$fgG      = [System.Drawing.Graphics]::FromImage($fgBmp)
$fgG.InterpolationMode  = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$fgG.SmoothingMode      = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
$fgG.PixelOffsetMode    = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
$fgG.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
$offset = [int](($foreSize - 288) / 2)
$fgG.DrawImage($src, $offset, $offset, 288, 288)
$fgG.Dispose()
$fgBmp.Save((Join-Path $drawableDir "ic_launcher_foreground.png"), [System.Drawing.Imaging.ImageFormat]::Png)
$fgBmp.Dispose(); $src.Dispose()
Write-Host "  [OK] drawable/ic_launcher_foreground.png (432 x 432)" -ForegroundColor Green

# Play Store 512x512
$src = [System.Drawing.Image]::FromFile($srcPath)
$ps = New-Object System.Drawing.Bitmap(512, 512)
$pg = [System.Drawing.Graphics]::FromImage($ps)
$pg.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$pg.DrawImage($src, 0, 0, 512, 512)
$ps.Save("play_store_icon.png", [System.Drawing.Imaging.ImageFormat]::Png)
$pg.Dispose(); $ps.Dispose(); $src.Dispose()
Write-Host "  [OK] play_store_icon.png  (512 x 512)" -ForegroundColor Green

Write-Host ""
Write-Host "Icon generation complete! Run build_native_apk.bat to rebuild." -ForegroundColor Cyan
Write-Host ""
