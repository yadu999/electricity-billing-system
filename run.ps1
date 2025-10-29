param(
    [Parameter(Mandatory=$false)] [string]$DbUrl,
    [Parameter(Mandatory=$false)] [string]$DbUser = "root",
    [Parameter(Mandatory=$false)] [string]$DbPass = ""
)

Set-Location -Path (Split-Path -Path $MyInvocation.MyCommand.Definition -Parent)

if (-not $DbUrl) {
    Write-Host "No -DbUrl provided. You can pass the database URL as a parameter or environment variable. Example:" -ForegroundColor Yellow
    Write-Host "> .\run.ps1 -DbUrl 'jdbc:mysql://localhost:3306/ebilling?serverTimezone=UTC' -DbUser 'ebilluser' -DbPass 's3cret'" -ForegroundColor Cyan
    Exit 1
}

# Ensure lib contains at least one JAR (the MySQL Connector/J)
$jar = Get-ChildItem -Path .\lib -Filter '*.jar' -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $jar) {
    Write-Host "No JDBC driver found in ./lib. Please download the MySQL Connector/J jar and place it in the 'lib' folder." -ForegroundColor Red
    Write-Host "Download from: https://dev.mysql.com/downloads/connector/j/" -ForegroundColor Cyan
    Exit 2
}

$driverPattern = "lib/*"

# Build classpath for Windows (semicolon separator)
$cp = "out;$driverPattern"

$javaArgs = "-cp `"$cp`" -Ddb.url=`"$DbUrl`" -Ddb.user=`"$DbUser`" -Ddb.pass=`"$DbPass`" com.ebilling.app.Main"

Write-Host "Starting application with:" -ForegroundColor Green
Write-Host "java $javaArgs" -ForegroundColor Gray

# Run java with arguments
& java -cp "$cp" -Ddb.url="$DbUrl" -Ddb.user="$DbUser" -Ddb.pass="$DbPass" com.ebilling.app.Main
