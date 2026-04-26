$ErrorActionPreference = 'Stop'

$root = Resolve-Path "$PSScriptRoot\.."
$frontend = Join-Path $root 'metrics-frontend'
$desktop = Join-Path $root 'metrics-desktop'
$backend = Join-Path $root 'metrics-backend'
$backendJar = Join-Path $backend 'target\metrics-backend-0.0.1-SNAPSHOT.jar'
$backendTargetStatic = Join-Path $backend 'target\classes\static'

function Start-DesktopTerminal {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Command
    )

    Start-Process powershell -ArgumentList '-NoExit', '-ExecutionPolicy', 'Bypass', '-Command', $Command
}

function Test-PathHasFilesNewerThan {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [Parameter(Mandatory = $true)]
        [datetime]$ReferenceTimeUtc
    )

    if (-not (Test-Path -LiteralPath $Path)) {
        return $false
    }

    return [bool](Get-ChildItem -LiteralPath $Path -Recurse -File |
        Where-Object { $_.LastWriteTimeUtc -gt $ReferenceTimeUtc } |
        Select-Object -First 1)
}

function Test-BackendNeedsRebuild {
    if (-not (Test-Path -LiteralPath $backendJar)) {
        return $true
    }

    if (Test-Path -LiteralPath $backendTargetStatic) {
        return $true
    }

    $jarWriteTimeUtc = (Get-Item -LiteralPath $backendJar).LastWriteTimeUtc
    if ((Get-Item -LiteralPath (Join-Path $backend 'pom.xml')).LastWriteTimeUtc -gt $jarWriteTimeUtc) {
        return $true
    }

    return (Test-PathHasFilesNewerThan -Path (Join-Path $backend 'src\main') -ReferenceTimeUtc $jarWriteTimeUtc) -or
        (Test-PathHasFilesNewerThan -Path (Join-Path $backend 'src\test') -ReferenceTimeUtc $jarWriteTimeUtc)
}

if (Test-BackendNeedsRebuild) {
    if (Test-Path -LiteralPath $backendTargetStatic) {
        Write-Host 'Detected stale backend static assets in target\classes\static. Rebuilding analyzer jar...'
        Remove-Item -LiteralPath $backendTargetStatic -Recurse -Force
    } elseif (-not (Test-Path -LiteralPath $backendJar)) {
        Write-Host 'Backend analyzer jar not found. Building metrics-backend package first...'
    } else {
        Write-Host 'Detected backend source changes newer than analyzer jar. Rebuilding metrics-backend package first...'
    }

    Push-Location $backend
    try {
        mvn clean package
        if ($LASTEXITCODE -ne 0) {
            throw "Maven package failed with exit code $LASTEXITCODE"
        }
    }
    finally {
        Pop-Location
    }
}

Start-DesktopTerminal -Command "Set-Location '$frontend'; npm run dev"
Start-Sleep -Seconds 2
Start-DesktopTerminal -Command "`$env:METRICS_DESKTOP_DEV_SERVER_URL='http://localhost:5173'; Set-Location '$desktop'; npm run dev"

Write-Host 'Renderer dev server: http://localhost:5173'
Write-Host 'Desktop shell: metrics-desktop (Electron)'
Write-Host "Analyzer jar: $backendJar"
