# EventHub with MySQL storage

This project keeps the existing Java Swing interface and OOP model classes. MySQL stores users, events, and bookings. Event ownership is stored on every event and booking, and organizer screens filter by the logged-in organizer's email.

## Windows setup

1. Install **JDK 8 or later**, **Apache Maven**, and **MySQL Server 8.0 or later**. Start the MySQL service.
2. In MySQL Workbench or the MySQL command line, create the project database:

   ```sql
   CREATE DATABASE eventhub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. Set the connection environment variables in PowerShell. Replace the example value with the password you chose for your own MySQL account; do not put the password in a source file:

   ```powershell
   $env:EVENTHUB_DB_USER = "root"
   $env:EVENTHUB_DB_PASSWORD = "your-local-mysql-password"
   ```

   The default connection is `localhost:3306/eventhub`. To use another server or port, set `EVENTHUB_DB_URL`, for example:

   ```powershell
   $env:EVENTHUB_DB_URL = "jdbc:mysql://localhost:3307/eventhub?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
   ```

4. Open the `EventHub-SQLite-Backend` folder in VS Code (or open a terminal in that folder) and run:

   ```powershell
   mvn compile exec:java
   ```

   Maven fetches MySQL Connector/J from Maven Central. EventHub creates its tables on first launch and inserts the included sample accounts only when the database is empty: organizer `organizer@eventhub.com` and attendee `aman@gmail.com`, both with sample password `1234`.

## Database and organizer ownership

- `EventHubDatabase.java` creates and accesses the MySQL tables through JDBC. The connection user, password, and optional URL come from `EVENTHUB_DB_USER`, `EVENTHUB_DB_PASSWORD`, and `EVENTHUB_DB_URL`; Java system properties `eventhub.db.user`, `eventhub.db.password`, and `eventhub.db.url` can also be used.
- `EventHub.java` loads the collections at startup and saves after registration, booking, event changes, and check-in. If MySQL cannot be reached, the application shows a database error and stops instead of opening an unsaved demo session.
- Event primary keys include both event name and organizer email, so separate organizers can use the same event name. Bookings retain the organizer email that owns the event. Organizer dashboards, booking lists, attendees, statistics, and check-in enforce that owner filter. Attendee booking history is filtered by the attendee's email.
- This is a database connection shared by app instances and the current save method writes the in-memory collections as one transaction. Avoid running multiple app instances that modify records at the same time; a scoped update strategy would be needed for concurrent multi-user editing.

## Existing SQLite data

The ZIP did not include an SQLite database file, so this package does not migrate existing SQLite records. The first MySQL run creates a fresh database and sample data. Export/import existing records separately if you need to retain them, preserving each event and booking's `organizer_email` value.

The sample passwords are for demonstration only. This project retains the original plain-text password model and should use throwaway credentials.
