$ErrorActionPreference = 'Stop'
$root = Resolve-Path "$PSScriptRoot\.."
$frontend = Join-Path $root 'metrics-frontend'
$backend = Join-Path $root 'metrics-backend'
$staticDir = Join-Path $backend 'src\main\resources\static'

if (Test-Path -LiteralPath $staticDir) {
    Remove-Item -LiteralPath $staticDir -Recurse -Force
}
New-Item -ItemType Directory -Path $staticDir -Force | Out-Null

Push-Location $frontend
npm run build
Pop-Location

Copy-Item -Path (Join-Path $frontend 'dist\*') -Destination $staticDir -Recurse -Force

Push-Location $backend
mvn clean package
Pop-Location

Write-Host 'Demo package created at metrics-backend\target\metrics-backend-0.0.1-SNAPSHOT.jar'
