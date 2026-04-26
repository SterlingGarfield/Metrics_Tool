param(
    [switch]$SkipFrontendBuild,
    [switch]$SkipBackendBuild,
    [switch]$SkipJreStage,
    [switch]$SkipOcrStage,
    [string]$JreSourceDir = $env:METRICS_JAVA_HOME
)

$ErrorActionPreference = 'Stop'

$root = Resolve-Path "$PSScriptRoot\..\.."
$desktop = Join-Path $root 'metrics-desktop'
$frontend = Join-Path $root 'metrics-frontend'
$backend = Join-Path $root 'metrics-backend'
$stagedRoot = Join-Path $desktop 'staged'
$payloadRoot = Join-Path $stagedRoot 'payload'
$frontendOut = Join-Path $payloadRoot 'frontend'
$backendOut = Join-Path $payloadRoot 'backend'
$jreOut = Join-Path $payloadRoot 'jre'
$ocrRuntimeOut = Join-Path $payloadRoot 'ocr-runtime'
$ocrModelsOut = Join-Path $payloadRoot 'ocr-models'
$backendJar = Join-Path $backend 'target\metrics-backend-0.0.1-SNAPSHOT.jar'
$backendTargetStatic = Join-Path $backend 'target\classes\static'
$ocrRuntimeSourceDir = Join-Path $root '.ocr311'
$ocrModelSourceDir = Join-Path $root 'models\paddleocr'

if ([string]::IsNullOrWhiteSpace($JreSourceDir)) {
    $JreSourceDir = $env:JAVA_HOME
}

function Invoke-ExternalCommand {
    param(
        [Parameter(Mandatory = $true)]
        [string]$FilePath,
        [string[]]$Arguments = @()
    )

    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed with exit code ${LASTEXITCODE}: $FilePath $($Arguments -join ' ')"
    }
}

if (Test-Path -LiteralPath $frontendOut) {
    Remove-Item -LiteralPath $frontendOut -Recurse -Force
}

if (Test-Path -LiteralPath $backendOut) {
    Remove-Item -LiteralPath $backendOut -Recurse -Force
}

if (Test-Path -LiteralPath $jreOut) {
    Remove-Item -LiteralPath $jreOut -Recurse -Force
}

if (Test-Path -LiteralPath $ocrRuntimeOut) {
    Remove-Item -LiteralPath $ocrRuntimeOut -Recurse -Force
}

if (Test-Path -LiteralPath $ocrModelsOut) {
    Remove-Item -LiteralPath $ocrModelsOut -Recurse -Force
}

New-Item -ItemType Directory -Path $frontendOut -Force | Out-Null
New-Item -ItemType Directory -Path $backendOut -Force | Out-Null
New-Item -ItemType Directory -Path $jreOut -Force | Out-Null
New-Item -ItemType Directory -Path $ocrRuntimeOut -Force | Out-Null
New-Item -ItemType Directory -Path $ocrModelsOut -Force | Out-Null

if (-not $SkipFrontendBuild) {
    Push-Location $frontend
    Invoke-ExternalCommand -FilePath 'npm' -Arguments @('run', 'build')
    Pop-Location
}

if (-not $SkipBackendBuild) {
    if (Test-Path -LiteralPath $backendTargetStatic) {
        Remove-Item -LiteralPath $backendTargetStatic -Recurse -Force
    }

    Push-Location $backend
    Invoke-ExternalCommand -FilePath 'mvn' -Arguments @('package')
    Pop-Location
}

if (-not (Test-Path -LiteralPath (Join-Path $frontend 'dist\index.html'))) {
    throw 'Frontend dist output is missing after staging.'
}

if (-not (Test-Path -LiteralPath $backendJar)) {
    throw 'Backend jar output is missing after staging.'
}

if (-not $SkipJreStage) {
    if ([string]::IsNullOrWhiteSpace($JreSourceDir) -or -not (Test-Path -LiteralPath (Join-Path $JreSourceDir 'bin\java.exe'))) {
        throw 'JRE source directory is missing. Set METRICS_JAVA_HOME or JAVA_HOME before staging desktop runtime.'
    }
}

if (-not $SkipOcrStage) {
    if (-not (Test-Path -LiteralPath (Join-Path $ocrRuntimeSourceDir 'python.exe'))) {
        throw 'OCR runtime directory is missing. Ensure .ocr311\python.exe exists or rerun with -SkipOcrStage.'
    }

    if (-not (Test-Path -LiteralPath (Join-Path $ocrModelSourceDir 'whl'))) {
        throw 'OCR model directory is missing. Ensure models\paddleocr is populated or rerun with -SkipOcrStage.'
    }
}

Copy-Item -Path (Join-Path $frontend 'dist\*') -Destination $frontendOut -Recurse -Force
Copy-Item -Path $backendJar -Destination (Join-Path $backendOut 'metrics-backend-0.0.1-SNAPSHOT.jar') -Force
if (-not $SkipJreStage) {
    Copy-Item -Path (Join-Path $JreSourceDir '*') -Destination $jreOut -Recurse -Force
}
if (-not $SkipOcrStage) {
    Copy-Item -Path (Join-Path $ocrRuntimeSourceDir '*') -Destination $ocrRuntimeOut -Recurse -Force
    Copy-Item -Path (Join-Path $ocrModelSourceDir '*') -Destination $ocrModelsOut -Recurse -Force
}

$manifest = @{
    generatedAt = (Get-Date).ToString('o')
    frontendEntry = 'index.html'
    backendJar = 'metrics-backend-0.0.1-SNAPSHOT.jar'
    javaBin = 'bin/java.exe'
    ocrPython = 'python.exe'
    ocrModelHome = '.'
} | ConvertTo-Json -Depth 4

Set-Content -LiteralPath (Join-Path $stagedRoot 'runtime-manifest.json') -Value $manifest -Encoding UTF8
Write-Host "Staged desktop runtime at $stagedRoot"
