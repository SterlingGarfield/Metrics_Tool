param(
    [string]$JarPath = '.\metrics-backend\target\metrics-backend-0.0.1-SNAPSHOT.jar'
)

$resolvedJar = Resolve-Path $JarPath
$process = Start-Process java -ArgumentList '-jar', $resolvedJar -PassThru
Start-Sleep -Seconds 8

try {
    $health = Invoke-RestMethod -Uri 'http://localhost:8080/api/metrics/health'
    if ($health.status -ne 'UP') {
        throw 'Health endpoint did not report UP'
    }
    Write-Host 'Smoke test passed.'
} finally {
    Stop-Process -Id $process.Id -Force
}
