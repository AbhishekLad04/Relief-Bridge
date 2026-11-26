$p = "Swaraj@04"
$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = "C:\Program Files\PostgreSQL\18\bin\psql.exe"
$psi.Arguments = "-h 127.0.0.1 -p 5432 -U postgres -d reliefbridge_db -c ""SELECT id, email, role, approved FROM users;"""
$psi.EnvironmentVariables["PGPASSWORD"] = $p
$psi.UseShellExecute = $false
$psi.RedirectStandardOutput = $true
$psi.RedirectStandardError = $true
$proc = [System.Diagnostics.Process]::Start($psi)
$out = $proc.StandardOutput.ReadToEnd()
$err = $proc.StandardError.ReadToEnd()
$proc.WaitForExit()
Write-Output "RESULT:"
Write-Output $out
Write-Output $err
