$ErrorActionPreference = 'Stop'

$root = Resolve-Path "$PSScriptRoot\..\.."
$desktop = Join-Path $root 'metrics-desktop'
$sourcePath = Join-Path $root 'metrics-frontend\src\assets\branding\logo-desktop.png'
$outputPath = Join-Path $desktop 'installer\metrics-tool.ico'

Add-Type -AssemblyName System.Drawing
Add-Type @"
using System;
using System.Runtime.InteropServices;

public static class NativeMethods
{
    [DllImport("user32.dll", CharSet = CharSet.Auto)]
    public static extern bool DestroyIcon(IntPtr handle);
}
"@

if (-not (Test-Path -LiteralPath $sourcePath)) {
    throw "Could not find the branding logo source at $sourcePath"
}

$image = [System.Drawing.Image]::FromFile($sourcePath)
try {
    $size = 256
    $bitmap = [System.Drawing.Bitmap]::new($size, $size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    try {
        $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
        try {
            $graphics.Clear([System.Drawing.Color]::Transparent)
            $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
            $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
            $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
            $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
            $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
            $graphics.DrawImage($image, 0, 0, $size, $size)
        }
        finally {
            $graphics.Dispose()
        }

        $iconHandle = $bitmap.GetHicon()
        try {
            $icon = [System.Drawing.Icon]::FromHandle($iconHandle)
            try {
                New-Item -ItemType Directory -Path (Split-Path -Parent $outputPath) -Force | Out-Null
                $stream = [System.IO.File]::Create($outputPath)
                try {
                    $icon.Save($stream)
                }
                finally {
                    $stream.Dispose()
                }
            }
            finally {
                $icon.Dispose()
            }
        }
        finally {
            [NativeMethods]::DestroyIcon($iconHandle) | Out-Null
        }
    }
    finally {
        $bitmap.Dispose()
    }
}
finally {
    $image.Dispose()
}

Write-Host "Generated installer icon at $outputPath from $sourcePath"
