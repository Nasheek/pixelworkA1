# Pixelworks - inicio seguro y reparacion de credenciales de PostgreSQL.
# Compatible con Windows PowerShell 5.1 y PowerShell 7.
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

function Fail([string]$message) {
    Write-Host "ERROR: $message" -ForegroundColor Red
    exit 1
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Fail 'Docker no esta instalado o no esta en PATH. Abre Docker Desktop.'
}
& docker compose version
if ($LASTEXITCODE -ne 0) { Fail 'Docker Compose no esta disponible.' }

function New-Secret([int]$length) {
    $bytes = New-Object byte[] $length
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    return -join ($bytes | ForEach-Object { $_.ToString('x2') })
}

# No se sobreescribe .env si ya existe: conserva las claves de una instalacion anterior.
if (-not (Test-Path -LiteralPath '.env')) {
    $passwordBD = New-Secret 16
    $jwt = New-Secret 32
    $passwordAdmin = New-Secret 12
    $lineas = @(
        'DB_NAME=pixelworks_db',
        'DB_USER=pixelworks',
        "DB_PASSWORD=$passwordBD",
        'DB_EXTERNAL_PORT=5433',
        'SERVER_PORT=8080',
        'API_BIND_HOST=127.0.0.1',
        "JWT_SECRET=$jwt",
        'APP_DEMO_ENABLED=true',
        'APP_ADMIN_EMAIL=admin@pixelworks.local',
        "APP_ADMIN_PASSWORD=$passwordAdmin"
    )
    $utf8SinBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllLines((Join-Path $PSScriptRoot '.env'), $lineas, $utf8SinBom)
    Write-Host 'Se creo .env con contrasenas aleatorias y privadas.' -ForegroundColor Green
} else {
    Write-Host 'Se conserva el .env existente.'
}

# Lee solo los valores necesarios del .env local; no imprime contrasenas.
$valores = @{}
Get-Content -LiteralPath '.env' | ForEach-Object {
    if ($_ -match '^\s*([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
        $valores[$matches[1]] = $matches[2].Trim()
    }
}
$dbUser = [string]$valores['DB_USER']
$dbName = [string]$valores['DB_NAME']
$dbPassword = [string]$valores['DB_PASSWORD']
$apiPort = [string]$valores['SERVER_PORT']
if ([string]::IsNullOrWhiteSpace($dbUser)) { $dbUser = 'pixelworks' }
if ([string]::IsNullOrWhiteSpace($dbName)) { $dbName = 'pixelworks_db' }
if ([string]::IsNullOrWhiteSpace($apiPort)) { $apiPort = '8080' }
if (-not ($dbUser -cmatch '^[A-Za-z_][A-Za-z0-9_]*$')) { Fail 'DB_USER tiene un formato no admitido en .env.' }
if (-not ($dbName -cmatch '^[A-Za-z_][A-Za-z0-9_]*$')) { Fail 'DB_NAME tiene un formato no admitido en .env.' }
if ([string]::IsNullOrWhiteSpace($dbPassword) -or $dbPassword.StartsWith('CAMBIAR_')) {
    Fail 'DB_PASSWORD no esta configurada en .env. Usa INICIAR.cmd en una carpeta nueva o configura la clave.'
}
if (-not ($apiPort -match '^[0-9]{2,5}$')) { Fail 'SERVER_PORT invalido en .env.' }

Write-Host ''
Write-Host '1/4 - Validando configuracion Docker Compose...'
& docker compose config --quiet
if ($LASTEXITCODE -ne 0) { Fail 'Revisa el archivo .env y docker-compose.yml.' }

Write-Host '2/4 - Iniciando PostgreSQL (sin eliminar datos)...'
& docker compose up -d db
if ($LASTEXITCODE -ne 0) { Fail 'No se pudo iniciar PostgreSQL; revisa Docker Desktop.' }

# Espera a que PostgreSQL acepte conexiones, incluso con un volumen ya inicializado.
$disponible = $false
for ($i = 0; $i -lt 40; $i++) {
    & docker compose exec -T db pg_isready -U $dbUser -d $dbName 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { $disponible = $true; break }
    Start-Sleep -Seconds 2
}
if (-not $disponible) {
    & docker compose logs --tail 30 db
    Fail 'PostgreSQL no quedo disponible.'
}

Write-Host '3/4 - Detectando el usuario real del volumen PostgreSQL (sin borrar datos)...'
function Test-LocalRole([string]$usuario) {
    # Ejecutar psql DIRECTAMENTE: no usar "sh -c" ni argumentos posicionales
    # ($0/$1); esa combinacion fallaba en Windows PowerShell 5.1 y terminaba
    # intentando iniciar sesion como el usuario "_".
    # El fallo de un rol inexistente ES normal al explorar una BD previa.
    # Evita que PowerShell 5.1 lo convierta en NativeCommandError terminante.
    $ErrorActionPreference = 'Continue'
    $respuesta = & docker compose exec -T db psql -X -qAt -v ON_ERROR_STOP=1 -U $usuario -d postgres -c 'SELECT 1' 2>$null
    $codigo = $LASTEXITCODE
    return ($codigo -eq 0 -and ($respuesta -join '').Trim() -eq '1')
}
if (-not (Test-LocalRole $dbUser)) {
    # En la API original el usuario predeterminado era 'postgres'; en la
    # revision anterior se cambio a 'pixelworks'. El volumen no cambia de rol
    # al reemplazar docker-compose.yml, asi que detectamos la version antigua.
    if ($dbUser -ne 'postgres' -and (Test-LocalRole 'postgres')) {
        Write-Host 'Detectada una base anterior cuyo usuario es postgres.' -ForegroundColor Yellow
        Write-Host 'Actualizando DB_USER en .env para reutilizar todas las tablas y registros.'
        $rutaEnv = Join-Path $PSScriptRoot '.env'
        $lineasEnv = [System.IO.File]::ReadAllLines($rutaEnv)
        $encontroUser = $false
        for ($indice = 0; $indice -lt $lineasEnv.Length; $indice++) {
            if ($lineasEnv[$indice] -match '^\s*DB_USER=') {
                $lineasEnv[$indice] = 'DB_USER=postgres'
                $encontroUser = $true
            }
        }
        if (-not $encontroUser) { $lineasEnv += 'DB_USER=postgres' }
        [System.IO.File]::WriteAllLines($rutaEnv, [string[]]$lineasEnv, (New-Object System.Text.UTF8Encoding($false)))
        $dbUser = 'postgres'
        & docker compose up -d db
        if ($LASTEXITCODE -ne 0) { Fail 'No se pudo adaptar el servicio db al usuario postgres.' }
        # La adaptacion de DB_USER recrea el contenedor, no el volumen.
        # Esperar a que PostgreSQL termine de iniciar antes de ALTER ROLE.
        $disponible = $false
        for ($i = 0; $i -lt 40; $i++) {
            & docker compose exec -T db pg_isready -U $dbUser -d $dbName 2>$null | Out-Null
            if ($LASTEXITCODE -eq 0) { $disponible = $true; break }
            Start-Sleep -Seconds 2
        }
        if (-not $disponible) { Fail 'PostgreSQL no respondio despues de adaptar el usuario.' }
    } else {
        Fail 'No se pudo acceder por el socket local con el usuario actual ni con postgres. Los datos no se modificaron.'
    }
}

Write-Host 'Sincronizando la clave de PostgreSQL con .env...'
# POSTGRES_PASSWORD solo se usa al crear un volumen nuevo: en un volumen antiguo NO
# cambia la clave. Por eso actualizamos el rol por el socket LOCAL de PostgreSQL.
# La clave viaja por stdin; no aparece en argumentos, consola ni logs de Docker.
# La imagen oficial postgres:16 usa autenticacion local por socket para este rol.
$usuarioSql = '"' + ($dbUser -replace '"', '""') + '"'
$claveSql = "'" + ($dbPassword -replace "'", "''") + "'"
$sql = "ALTER ROLE $usuarioSql WITH PASSWORD $claveSql;"
$sql | & docker compose exec -T db psql -X -q -v ON_ERROR_STOP=1 -U $dbUser -d postgres
if ($LASTEXITCODE -ne 0) {
    Fail 'No se pudo ajustar la clave con la conexion local. NO se borraron datos. Comprueba el usuario y el volumen de PostgreSQL.'
}

# Verificar TCP con el usuario y la contraseña EXACTOS de .env.
# No usamos sh -c ni parametros posicionales del shell de Linux.
# La clave se entrega solo al proceso temporal de psql en el contenedor,
# y nunca se imprime en la pantalla ni se escribe en un archivo nuevo.
$ErrorActionPreference = 'Continue'
$resultadoBD = & docker compose exec -T -e "PGPASSWORD=$dbPassword" db psql -X -qAt -v ON_ERROR_STOP=1 -h 127.0.0.1 -U $dbUser -d $dbName -c 'SELECT 1' 2>$null
$codigoBD = $LASTEXITCODE
$ErrorActionPreference = 'Stop'
if ($codigoBD -ne 0 -or ($resultadoBD -join '').Trim() -ne '1') {
    Fail 'Las credenciales de .env aun no permiten conectarse a PostgreSQL por TCP. Los datos se conservaron.'
}
Write-Host 'Clave de PostgreSQL comprobada por TCP correctamente.' -ForegroundColor Green

Write-Host '4/4 - Construyendo e iniciando Pixelworks API...'
& docker compose up -d --build api
if ($LASTEXITCODE -ne 0) { Fail 'Fallo la compilacion o el arranque de la API; consulta los logs.' }

$uri = "http://127.0.0.1:$apiPort/api/productos"
$apiLista = $false
for ($i = 0; $i -lt 40; $i++) {
    try {
        $respuesta = Invoke-WebRequest -Uri $uri -UseBasicParsing -TimeoutSec 3
        if ($respuesta.StatusCode -eq 200) { $apiLista = $true; break }
    } catch { }
    Start-Sleep -Seconds 2
}
if (-not $apiLista) {
    Write-Host 'La API todavia no responde. Ultimos registros:' -ForegroundColor Yellow
    & docker compose logs --tail 75 api
    Fail 'El servicio no respondio correctamente en /api/productos.'
}

Write-Host ''
Write-Host 'Pixelworks API y PostgreSQL funcionando.' -ForegroundColor Green
& docker compose ps
Write-Host "Catalogo: $uri"
Write-Host 'Ejecuta PROBAR_API.ps1 para revisar el login JWT.'
Write-Host 'Las credenciales privadas estan en .env (no compartas ese archivo).'
exit 0
