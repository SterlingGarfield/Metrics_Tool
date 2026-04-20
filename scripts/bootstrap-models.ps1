param(
    [switch]$SkipTests,
    [switch]$RecreateVenv
)

$ErrorActionPreference = 'Stop'

function Invoke-Step {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name,
        [Parameter(Mandatory = $true)]
        [scriptblock]$Action
    )

    Write-Host "==> $Name"
    & $Action
    if ($LASTEXITCODE -ne 0) {
        throw "$Name failed with exit code $LASTEXITCODE"
    }
}

function Resolve-PythonExecutable {
    param([Parameter(Mandatory = $true)][string]$VirtualEnvDir)

    $candidates = @(
        (Join-Path $VirtualEnvDir 'Scripts\python.exe'),
        (Join-Path $VirtualEnvDir 'python.exe')
    )
    foreach ($candidate in $candidates) {
        if (Test-Path -LiteralPath $candidate) {
            # Keep the alias/junction path as-is to avoid falling back to long physical paths on Windows.
            return $candidate
        }
    }
    return $null
}

function New-Python311Venv {
    param([Parameter(Mandatory = $true)][string]$VirtualEnvDir)

    $pyLauncher = Get-Command py -ErrorAction SilentlyContinue
    if ($null -ne $pyLauncher) {
        & $pyLauncher.Source -3.11 -m venv $VirtualEnvDir
        return
    }

    foreach ($candidate in @('python', 'python3')) {
        $command = Get-Command $candidate -ErrorAction SilentlyContinue
        if ($null -eq $command) {
            continue
        }

        $version = & $command.Source -c "import sys; print(f'{sys.version_info[0]}.{sys.version_info[1]}')"
        if ($LASTEXITCODE -ne 0) {
            continue
        }
        if ($version.Trim() -ne '3.11') {
            continue
        }

        & $command.Source -m venv $VirtualEnvDir
        return
    }

    throw "Python 3.11 was not found. Install Python 3.11 (or py launcher) and rerun bootstrap."
}

function Read-PinnedPackage {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Lines,
        [Parameter(Mandatory = $true)]
        [string]$PackageName
    )

    $line = $Lines | Where-Object { $_ -match "^\s*$PackageName==([^;\s]+)" } | Select-Object -First 1
    if (-not $line) {
        throw "Unable to find pinned version for $PackageName in requirements"
    }
    $version = [regex]::Match($line, "^\s*$PackageName==([^;\s]+)").Groups[1].Value
    return "$PackageName==$version"
}

function New-ShortWorkRoot {
    param(
        [Parameter(Mandatory = $true)][string]$TargetRoot,
        [string]$AliasPrefix = 'm1'
    )

    $aliasRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("$AliasPrefix-$PID")
    if (Test-Path -LiteralPath $aliasRoot) {
        Remove-Item -LiteralPath $aliasRoot -Recurse -Force
    }

    try {
        New-Item -ItemType Junction -Path $aliasRoot -Target $TargetRoot | Out-Null
        return $aliasRoot
    }
    catch {
        return $TargetRoot
    }
}

function Resolve-CABundlePath {
    if ($env:PIP_CERT -and (Test-Path -LiteralPath $env:PIP_CERT)) {
        return $env:PIP_CERT
    }

    foreach ($candidate in @('python', 'python3')) {
        $command = Get-Command $candidate -ErrorAction SilentlyContinue
        if ($null -eq $command) {
            continue
        }

        $bundle = & $command.Source -c "import certifi; print(certifi.where())" 2>$null
        if ($LASTEXITCODE -eq 0 -and $bundle -and (Test-Path -LiteralPath $bundle.Trim())) {
            return $bundle.Trim()
        }
    }

    return $null
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$physicalServiceDir = Join-Path $repoRoot 'diagram-recognition-service'
$serviceDir = New-ShortWorkRoot -TargetRoot $physicalServiceDir -AliasPrefix 'svc'
$requirementsPath = Join-Path $serviceDir 'requirements.txt'
$venvDir = Join-Path $serviceDir '.venv311'
$modelsDir = Join-Path $repoRoot 'models'

if (-not (Test-Path -LiteralPath $requirementsPath)) {
    throw "Missing requirements file at $requirementsPath"
}

if ($RecreateVenv -and (Test-Path -LiteralPath $venvDir)) {
    Remove-Item -LiteralPath $venvDir -Recurse -Force
}

$pythonExe = Resolve-PythonExecutable -VirtualEnvDir $venvDir
if ($null -eq $pythonExe) {
    Invoke-Step -Name "Create Python 3.11 venv" -Action {
        New-Python311Venv -VirtualEnvDir $venvDir
    }
}

$pythonExe = Resolve-PythonExecutable -VirtualEnvDir $venvDir
if ($null -eq $pythonExe) {
    throw "Expected venv interpreter under $venvDir (python.exe or Scripts\python.exe)"
}

$previousModelCache = $env:METRICS_TOOL_MODEL_CACHE
$previousPipCert = $env:PIP_CERT
$previousRequestsCaBundle = $env:REQUESTS_CA_BUNDLE
$previousSslCertFile = $env:SSL_CERT_FILE
$env:METRICS_TOOL_MODEL_CACHE = $modelsDir

$caBundle = Resolve-CABundlePath
if ($null -ne $caBundle) {
    $env:PIP_CERT = $caBundle
    $env:REQUESTS_CA_BUNDLE = $caBundle
    $env:SSL_CERT_FILE = $caBundle
}

New-Item -ItemType Directory -Path $modelsDir -Force | Out-Null

$originalLocation = Get-Location
$summary = [ordered]@{
    venv = 'pending'
    requirements = 'pending'
    packageRepair = 'pending'
    dependencyCheck = 'pending'
    warmModels = 'pending'
    tests = if ($SkipTests) { 'skipped' } else { 'pending' }
}

try {
    Set-Location $serviceDir

    Invoke-Step -Name "Bootstrap pip from stdlib" -Action {
        & $pythonExe -m ensurepip --upgrade
    }

    Invoke-Step -Name "Upgrade pip tooling" -Action {
        & $pythonExe -m pip install --upgrade pip setuptools wheel
    }

    $summary.venv = 'ok'

    Invoke-Step -Name "Install pinned requirements" -Action {
        & $pythonExe -m pip install --no-cache-dir -r $requirementsPath
    }
    $summary.requirements = 'ok'

    $requirementsLines = Get-Content -LiteralPath $requirementsPath
    $paddle = Read-PinnedPackage -Lines $requirementsLines -PackageName 'paddlepaddle'
    $paddleocr = Read-PinnedPackage -Lines $requirementsLines -PackageName 'paddleocr'
    $protobuf = Read-PinnedPackage -Lines $requirementsLines -PackageName 'protobuf'

    Invoke-Step -Name "Repair pinned runtime packages" -Action {
        & $pythonExe -m pip uninstall -y paddlepaddle paddleocr protobuf
        & $pythonExe -m pip install --no-cache-dir $protobuf $paddle $paddleocr
    }
    $summary.packageRepair = 'ok'

    Invoke-Step -Name "Validate dependency graph" -Action {
        & $pythonExe -m pip check
    }
    $summary.dependencyCheck = 'ok'

    Invoke-Step -Name "Warm models" -Action {
        & $pythonExe -m app.tools.warm_models
    }
    $summary.warmModels = 'ok'

    if (-not $SkipTests) {
        Invoke-Step -Name "Run runtime environment tests" -Action {
            & $pythonExe -m pytest tests/test_runtime_environment.py -q
        }
        $summary.tests = 'ok'
    }

    Write-Host "Bootstrap completed: venv=$($summary.venv); requirements=$($summary.requirements); repair=$($summary.packageRepair); deps=$($summary.dependencyCheck); warm_models=$($summary.warmModels); tests=$($summary.tests)"
}
catch {
    Write-Error $_
    Write-Host "Bootstrap failed: venv=$($summary.venv); requirements=$($summary.requirements); repair=$($summary.packageRepair); deps=$($summary.dependencyCheck); warm_models=$($summary.warmModels); tests=$($summary.tests)"
    Write-Host "Hint: if you still hit package/path issues, rerun with -RecreateVenv."
    exit 1
}
finally {
    Set-Location $originalLocation
    if ($serviceDir -ne $physicalServiceDir -and (Test-Path -LiteralPath $serviceDir)) {
        Remove-Item -LiteralPath $serviceDir -Recurse -Force
    }
    if ($null -eq $previousModelCache) {
        Remove-Item Env:METRICS_TOOL_MODEL_CACHE -ErrorAction SilentlyContinue
    } else {
        $env:METRICS_TOOL_MODEL_CACHE = $previousModelCache
    }

    if ($null -eq $previousPipCert) {
        Remove-Item Env:PIP_CERT -ErrorAction SilentlyContinue
    } else {
        $env:PIP_CERT = $previousPipCert
    }

    if ($null -eq $previousRequestsCaBundle) {
        Remove-Item Env:REQUESTS_CA_BUNDLE -ErrorAction SilentlyContinue
    } else {
        $env:REQUESTS_CA_BUNDLE = $previousRequestsCaBundle
    }

    if ($null -eq $previousSslCertFile) {
        Remove-Item Env:SSL_CERT_FILE -ErrorAction SilentlyContinue
    } else {
        $env:SSL_CERT_FILE = $previousSslCertFile
    }
}
