Add-Type -AssemblyName System.Drawing

$resDir = "app\src\main\res"
$sizes  = @{ "mipmap-mdpi"=48; "mipmap-hdpi"=72; "mipmap-xhdpi"=96; "mipmap-xxhdpi"=144; "mipmap-xxxhdpi"=192 }

foreach ($entry in $sizes.GetEnumerator()) {
    $dir = Join-Path $resDir $entry.Key
    $sz  = $entry.Value

    if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }

    $bmp = New-Object System.Drawing.Bitmap($sz, $sz)
    $g   = [System.Drawing.Graphics]::FromImage($bmp)
    $g.Clear([System.Drawing.Color]::FromArgb(255, 13, 27, 42))
    $g.Dispose()

    $bmp.Save((Join-Path $dir "ic_launcher.png"),       [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save((Join-Path $dir "ic_launcher_round.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()

    Write-Host "Created $($entry.Key)/ic_launcher.png ($sz x $sz)"
}

# ic_launcher_foreground placeholder for adaptive icon
$fgSz  = 432
$fgDir = Join-Path $resDir "mipmap-xxxhdpi"
$fgBmp = New-Object System.Drawing.Bitmap($fgSz, $fgSz)
$fgG   = [System.Drawing.Graphics]::FromImage($fgBmp)
$fgG.Clear([System.Drawing.Color]::Transparent)
$fgG.Dispose()
$fgBmp.Save((Join-Path $fgDir "ic_launcher_foreground.png"), [System.Drawing.Imaging.ImageFormat]::Png)
$fgBmp.Dispose()
Write-Host "Created mipmap-xxxhdpi/ic_launcher_foreground.png"

# mipmap-anydpi-v26 adaptive icon XMLs
$anydpiDir = Join-Path $resDir "mipmap-anydpi-v26"
if (!(Test-Path $anydpiDir)) { New-Item -ItemType Directory -Path $anydpiDir | Out-Null }

$xml = '<?xml version="1.0" encoding="utf-8"?><adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android"><background android:drawable="@color/ic_launcher_background"/><foreground android:drawable="@mipmap/ic_launcher_foreground"/></adaptive-icon>'
$xml | Out-File -FilePath (Join-Path $anydpiDir "ic_launcher.xml")       -Encoding utf8
$xml | Out-File -FilePath (Join-Path $anydpiDir "ic_launcher_round.xml") -Encoding utf8
Write-Host "Created mipmap-anydpi-v26 XMLs"

Write-Host ""
Write-Host "Placeholder icons ready. Run generate_icons.ps1 with your source image to apply the real icon."
