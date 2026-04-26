$ErrorActionPreference = 'Stop'

$root = Resolve-Path "$PSScriptRoot\..\.."
$desktop = Join-Path $root 'metrics-desktop'
$stageScript = Join-Path $desktop 'scripts\stage-runtime.ps1'
$builderBinary = Join-Path $desktop 'node_modules\.bin\electron-builder.cmd'

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

Push-Location $desktop
try {
    powershell -ExecutionPolicy Bypass -File $stageScript

    if (-not (Test-Path -LiteralPath $builderBinary)) {
        throw 'electron-builder is not installed in metrics-desktop. Run npm install inside metrics-desktop before building the installer.'
    }

    Invoke-ExternalCommand -FilePath 'npm' -Arguments @('run', 'dist')
}
finally {
    Pop-Location
}
