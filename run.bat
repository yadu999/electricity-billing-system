@echo off
REM run.bat - start the Electricity Billing System with MySQL
REM Usage: run.bat <jdbc-url> <db-user> <db-pass>

if "%1"=="" (
  echo Usage: run.bat "jdbc:mysql://host:3306/ebilling?serverTimezone=UTC" dbuser dbpass
  exit /b 1
)

set DB_URL=%~1
set DB_USER=%~2
set DB_PASS=%~3

echo Starting app with DB URL: %DB_URL%
java -cp "out;lib/*" -Ddb.url="%DB_URL%" -Ddb.user="%DB_USER%" -Ddb.pass="%DB_PASS%" com.ebilling.app.Main
pause
