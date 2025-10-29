Setting up MySQL for the Electricity Billing System

This project now requires a MySQL database. Follow these steps to install MySQL, create the database, add the JDBC driver, and run the Swing app.

1) Install MySQL Server (Windows)
   - Download MySQL Installer from https://dev.mysql.com/downloads/installer/
   - Run the installer and install MySQL Server (choose a root password you remember).
   - Optionally install MySQL Workbench to run SQL scripts.

2) Create the database and user
   - Open a terminal (PowerShell) and run the following (replace root password prompts as needed):

     # Using the mysql client (run from command prompt after adding mysql to PATH)
     mysql -u root -p
     -- then inside mysql client:
     SOURCE C:/path/to/Electricity\ Billing\ System/create_mysql_db.sql;

   - Or open the supplied `create_mysql_db.sql` in MySQL Workbench and run it.

3) Download MySQL Connector/J (JDBC driver)
   - Download the Connector/J JAR from: https://dev.mysql.com/downloads/connector/j/
   - Place the downloaded jar file into the project's `lib/` directory (create it if missing). Example target:
     D:\Electricity Billing System\lib\mysql-connector-java-8.0.xx.jar

4) Run the application (PowerShell)
   - Example (adjust paths and password):

     cd 'D:\Electricity Billing System'
     $driver = 'lib\mysql-connector-java-8.0.xx.jar'
     .\run.ps1 -DbUrl 'jdbc:mysql://localhost:3306/ebilling?serverTimezone=UTC' -DbUser 'ebilluser' -DbPass 's3cret'

   - Or use the direct java command (if you prefer):

     java -cp "out;lib/*" -Ddb.url='jdbc:mysql://localhost:3306/ebilling?serverTimezone=UTC' -Ddb.user='ebilluser' -Ddb.pass='s3cret' com.ebilling.app.Main

5) Troubleshooting
   - If the app fails with a DB connection error, ensure MySQL server is running and the credentials are correct.
   - If you get classpath / driver not found errors, confirm the connector jar is in `lib/` and named correctly.

If you want, I can:
- Attempt to run the `create_mysql_db.sql` for you here if you install MySQL or provide credentials.
- Add a GUI page to manage DB connection settings inside the app.
- Convert the project to Maven/Gradle and add the Connector/J dependency so it is managed automatically.
