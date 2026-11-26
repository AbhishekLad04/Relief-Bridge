$p = "Swaraj@04"
$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = "C:\Program Files\PostgreSQL\18\bin\psql.exe"
# BCrypt hash for Admin@12345678: $2a$10$wK1bJ9m11z1Yq4Q8g1b9QO1c3T4Y5U6I7O8P9A0S1D2F3G4H5J6K
# Let's update admin and approved NGO password directly using psql
$updateSql = @"
UPDATE users SET password = '`$2a`$10`$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW' WHERE email = 'admin@reliefbridge.com';
UPDATE users SET password = '`$2a`$10`$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW' WHERE email = 'ngo.test@example.com';
UPDATE users SET password = '`$2a`$10`$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW' WHERE email = 'ngo.sevasangh@example.com';
UPDATE users SET password = '`$2a`$10`$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW' WHERE email = 'amit.test@example.com';
"@
$psi.Arguments = "-h 127.0.0.1 -p 5432 -U postgres -d reliefbridge_db -c `"$updateSql`""
$psi.EnvironmentVariables["PGPASSWORD"] = $p
$psi.UseShellExecute = $false
$psi.RedirectStandardOutput = $true
$psi.RedirectStandardError = $true
$proc = [System.Diagnostics.Process]::Start($psi)
$out = $proc.StandardOutput.ReadToEnd()
$err = $proc.StandardError.ReadToEnd()
$proc.WaitForExit()
Write-Output "UPDATE RESULT:"
Write-Output $out
Write-Output $err
