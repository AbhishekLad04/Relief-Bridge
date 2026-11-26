$bodyAdmin = '{"email":"admin@reliefbridge.com","password":"Admin@12345678"}'
$resAdmin = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method Post -ContentType "application/json" -Body $bodyAdmin
Write-Output "ADMIN LOGIN OK: $($resAdmin.email) - Role: $($resAdmin.role) - Token length: $($resAdmin.token.Length)"

$bodyNgo = '{"email":"ngo.test@example.com","password":"Admin@12345678"}'
$resNgo = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method Post -ContentType "application/json" -Body $bodyNgo
Write-Output "NGO LOGIN OK: $($resNgo.email) - Role: $($resNgo.role) - Token length: $($resNgo.token.Length)"

$randomEmail = "testuser" + (Get-Random -Minimum 1000 -Maximum 9999) + "@reliefbridge.org"
$bodyReg = "{`"name`":`"Test Volunteer`",`"email`":`"$randomEmail`",`"password`":`"Admin@12345678`",`"role`":`"CITIZEN`",`"isDonor`":true,`"isVolunteer`":true,`"skills`":`"First Aid, Rescue`"}"
$resReg = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" -Method Post -ContentType "application/json" -Body $bodyReg
Write-Output "REGISTRATION OK: $($resReg.email) - Role: $($resReg.role) - Approved: $($resReg.approved)"
