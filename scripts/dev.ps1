$root = Resolve-Path "$PSScriptRoot\.."
Start-Process powershell -ArgumentList '-NoExit', '-ExecutionPolicy', 'Bypass', '-Command', "Set-Location '$root\metrics-backend'; mvn spring-boot:run"
Start-Process powershell -ArgumentList '-NoExit', '-ExecutionPolicy', 'Bypass', '-Command', "Set-Location '$root\metrics-frontend'; npm run dev"
Write-Host 'Backend: http://localhost:8080'
Write-Host 'Frontend: http://localhost:5173'
