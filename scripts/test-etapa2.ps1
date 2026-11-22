# Uso: powershell -ExecutionPolicy Bypass -File scripts\test-etapa2.ps1
$Base = if ($env:BASE_URL) { $env:BASE_URL } else { "http://localhost:8080" }
$Email = "teste$([DateTimeOffset]::UtcNow.ToUnixTimeSeconds())@drywall.com"
$script:Failed = $false

function Call($Method, $Path, $Body = $null, $Token = $null) {
    $params = @{ Uri = "$Base$Path"; Method = $Method; UseBasicParsing = $true; ContentType = "application/json" }
    if ($Body) { $params.Body = $Body }
    if ($Token) { $params.Headers = @{ Authorization = "Bearer $Token" } }
    try {
        $r = Invoke-WebRequest @params
        return @{ Status = [int]$r.StatusCode; Body = $r.Content }
    } catch {
        $resp = $_.Exception.Response
        $status = if ($resp) { [int]$resp.StatusCode } else { 0 }
        return @{ Status = $status; Body = "" }
    }
}

function Check($Name, $Expected, $Actual) {
    if ($Expected -eq $Actual) { Write-Host "OK   $Name ($Actual)" }
    else { Write-Host "FAIL $Name (esperado $Expected, obtido $Actual)"; $script:Failed = $true }
}

Check "health publico" 200 (Call GET "/health").Status

$body = @{ name="Joao Silva"; companyName="JS Drywall"; email=$Email; phone="21999990000"; password="Senha1234"; confirmPassword="Senha1234" } | ConvertTo-Json
Check "cadastro" 201 (Call POST "/auth/register" $body).Status
Check "cadastro duplicado" 409 (Call POST "/auth/register" $body).Status

$weak = @{ name="Ana"; email="ana$Email"; phone="21999990000"; password="abc"; confirmPassword="abc" } | ConvertTo-Json
Check "senha fraca" 400 (Call POST "/auth/register" $weak).Status

$mismatch = @{ name="Ana"; email="b$Email"; phone="21999990000"; password="Senha1234"; confirmPassword="Senha9999" } | ConvertTo-Json
Check "senhas diferentes" 400 (Call POST "/auth/register" $mismatch).Status

$bad = @{ email=$Email; password="Errada123" } | ConvertTo-Json
Check "login senha errada" 401 (Call POST "/auth/login" $bad).Status
$unknown = @{ email="nao@existe.com"; password="Senha1234" } | ConvertTo-Json
Check "login email inexistente" 401 (Call POST "/auth/login" $unknown).Status

$ok = @{ email=$Email; password="Senha1234" } | ConvertTo-Json
$login = Call POST "/auth/login" $ok
Check "login ok" 200 $login.Status
$token = ($login.Body | ConvertFrom-Json).accessToken
if (-not $token) { Write-Host "FAIL token nao encontrado"; $script:Failed = $true }

Check "/users/me sem token" 401 (Call GET "/users/me").Status
Check "/users/me token invalido" 401 (Call GET "/users/me" $null "${token}x").Status
Check "/users/me com token" 200 (Call GET "/users/me" $null $token).Status

if ($script:Failed) { Write-Host "HA FALHAS"; exit 1 } else { Write-Host "TUDO OK" }
