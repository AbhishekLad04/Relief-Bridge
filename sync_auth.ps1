$bodyLogin = '{"email":"testuser6539@reliefbridge.org","password":"Admin@12345678"}'
$resLogin = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method Post -ContentType "application/json" -Body $bodyLogin
Write-Output "LOGIN VERIFIED: $($resLogin.email) Token: $($resLogin.token.Substring(0,25))..."

# Let's copy this exact password hash from testuser6539 to admin and NGO in PostgreSQL
$p = "Swaraj@04"
$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = "C:\Program Files\PostgreSQL\18\bin\psql.exe"
$copySql = "UPDATE users SET password = (SELECT password FROM users WHERE email='testuser6539@reliefbridge.org') WHERE email IN ('admin@reliefbridge.com', 'ngo.test@example.com', 'amit.test@example.com');"
$psi.Arguments = "-h 127.0.0.1 -p 5432 -U postgres -d reliefbridge_db -c `"$copySql`""
$psi.EnvironmentVariables["PGPASSWORD"] = $p
$psi.UseShellExecute = $false
$psi.RedirectStandardOutput = $true
$psi.RedirectStandardError = $true
$proc = [System.Diagnostics.Process]::Start($psi)
$out = $proc.StandardOutput.ReadToEnd()
$proc.WaitForExit()
Write-Output "SYNCED HASH: $out"

# Test Admin & NGO Login now:
$resAdmin = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"admin@reliefbridge.com","password":"Admin@12345678"}'
Write-Output "ADMIN SUCCESS: $($resAdmin.email) | Role: $($resAdmin.role)"

$resNgo = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"ngo.test@example.com","password":"Admin@12345678"}'
Write-Output "NGO SUCCESS: $($resNgo.email) | Role: $($resNgo.role)"
