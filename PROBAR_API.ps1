# Ejecutar despues de INICIAR.cmd desde PowerShell: .\PROBAR_API.ps1
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
Write-Host '1. Catalogo publico: GET /api/productos'
$catalogo = @(Invoke-RestMethod -Uri 'http://localhost:8080/api/productos')
Write-Host "   Productos recibidos: $($catalogo.Count)"
$catalogo | Select-Object id, nombre, precio, stock | Format-Table
if ($catalogo.Count -gt 0) {
    $detalle = Invoke-RestMethod -Uri "http://localhost:8080/api/productos/$($catalogo[0].id)"
    Write-Host "2. Detalle publico: $($detalle.nombre)"
}
Write-Host '3. Login JWT de demostracion'
$valores = @{}
Get-Content '.env' | ForEach-Object {
    if ($_ -match '^([A-Z0-9_]+)=(.*)$') { $valores[$matches[1]] = $matches[2] }
}
if ($valores['APP_DEMO_ENABLED'] -ne 'true') {
    Write-Host '   Modo demo desactivado: prueba un usuario registrado.'
    exit 0
}
$datos = @{correo=$valores['APP_ADMIN_EMAIL']; password=$valores['APP_ADMIN_PASSWORD']} | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body $datos
if (-not $login.token) { throw 'No se obtuvo un token JWT' }
Write-Host "   JWT recibido; rol: $($login.rol)"
$pagos = @(Invoke-RestMethod -Uri 'http://localhost:8080/api/metodos-pago' -Headers @{Authorization="Bearer $($login.token)"})
Write-Host "4. Endpoint protegido OK. Metodos de pago: $($pagos.Count)"
Write-Host 'Pruebas HTTP completadas.' -ForegroundColor Green
