Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

# ── File picker ─────────────────────────────────────────────────────────────
$dialog = New-Object System.Windows.Forms.OpenFileDialog
$dialog.Title            = "Select App Icon PNG"
$dialog.Filter           = "Image Files (*.png;*.jpg;*.jpeg)|*.png;*.jpg;*.jpeg|All Files (*.*)|*.*"
$dialog.InitialDirectory = [Environment]::GetFolderPath("Desktop")

if ($dialog.ShowDialog() -ne [System.Windows.Forms.DialogResult]::OK) {
    Write-Host "Cancelled." -ForegroundColor Yellow
    exit 0
}

$srcPath = $dialog.FileName
Write-Host ""
Write-Host "Source: $srcPath" -ForegroundColor Cyan

# ── Density PNGs ─────────────────────────────────────────────────────────────
$resDir   = Join-Path $PSScriptRoot "app\src\main\res"
$densities = [ordered]@{
    "mipmap-mdpi"    = 48
    "mipmap-hdpi"    = 72
    "mipmap-xhdpi"   = 96
    "mipmap-xxhdpi"  = 144
    "mipmap-xxxhdpi" = 192
}

foreach ($entry in $densities.GetEnumerator()) {
    $dir = Join-Path $resDir $entry.Key
    $sz  = $entry.Value
    if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }

    $src = [System.Drawing.Image]::FromFile($srcPath)
    $bmp = New-Object System.Drawing.Bitmap($sz, $sz)
    $g   = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode  = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode      = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode    = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $g.DrawImage($src, 0, 0, $sz, $sz)
    $g.Dispose()

    $bmp.Save((Join-Path $dir "ic_launcher.png"),       [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save((Join-Path $dir "ic_launcher_round.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose(); $src.Dispose()
    Write-Host "  [OK] $($entry.Key)/ic_launcher.png  ($sz x $sz)" -ForegroundColor Green
}

# ── Adaptive icon foreground (432x432, content in centre 288px safe zone) ────
$drawableDir    = Join-Path $resDir "drawable"
$placeholderXml = Join-Path $drawableDir "ic_launcher_foreground.xml"
if (Test-Path $placeholderXml) { Remove-Item $placeholderXml -Force }

$foreSize = 432
$src  = [System.Drawing.Image]::FromFile($srcPath)
$fgBmp = New-Object System.Drawing.Bitmap($foreSize, $foreSize)
$fgG   = [System.Drawing.Graphics]::FromImage($fgBmp)
$fgG.InterpolationMode  = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$fgG.SmoothingMode      = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
$fgG.PixelOffsetMode    = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
$fgG.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
$offset = [int](($foreSize - 288) / 2)
$fgG.DrawImage($src, $offset, $offset, 288, 288)
$fgG.Dispose()
$fgBmp.Save((Join-Path $drawableDir "ic_launcher_foreground.png"), [System.Drawing.Imaging.ImageFormat]::Png)
$fgBmp.Dispose(); $src.Dispose()
Write-Host "  [OK] drawable/ic_launcher_foreground.png" -ForegroundColor Green

# ── Play Store 512x512 ────────────────────────────────────────────────────────
$src  = [System.Drawing.Image]::FromFile($srcPath)
$ps   = New-Object System.Drawing.Bitmap(512, 512)
$pg   = [System.Drawing.Graphics]::FromImage($ps)
$pg.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$pg.DrawImage($src, 0, 0, 512, 512)
$ps.Save((Join-Path $PSScriptRoot "play_store_icon.png"), [System.Drawing.Imaging.ImageFormat]::Png)
$pg.Dispose(); $ps.Dispose(); $src.Dispose()
Write-Host "  [OK] play_store_icon.png (512x512)" -ForegroundColor Green

Write-Host ""
Write-Host "Done! Now run:  build_native_apk.bat" -ForegroundColor Cyan
Write-Host ""
