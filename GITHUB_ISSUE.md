# SQLite Database Locking Issue During Demo Data Initialization

## Issue Description

When running the `initDemoData` Gradle task to populate the database with demo data, the application encountered persistent `SQLITE_BUSY` errors, causing all data insertion operations to fail.

## Error Message

```
org.sqlite.SQLiteException: [SQLITE_BUSY] The database file is locked (database is locked)
    at org.sqlite.core.DB.newSQLException(DB.java:1179)
    at org.sqlite.core.DB.execute(DB.java:985)
    at dao.impl.UserDaoImpl.save(UserDaoImpl.java:83)
    at service.UserService.register(UserService.java:70)
```

## Symptoms

- Running `.\gradlew.bat initDemoData` completed with "SUCCESS" message but database remained empty
- All user registration attempts failed with database lock errors
- Issue occurred even with retry mechanisms (3 retries with 100ms delays)
- Increasing delays between operations (50ms → 300ms) did not resolve the issue

## Root Cause

SQLite's default configuration uses DEFERRED transactions and has limited concurrency support. When multiple database connections are created in rapid succession (as happens during bulk data initialization), SQLite's locking mechanism causes write operations to fail with `SQLITE_BUSY` errors.

The issue was exacerbated by:
1. Creating new connections for each DAO operation
2. No busy timeout configured (default is 0ms - fail immediately)
3. Using default rollback journal mode instead of WAL mode

## Solution

Modified `DatabaseConfig.getConnection()` to enable SQLite's Write-Ahead Logging (WAL) mode and configure a busy timeout:

### Before
```java
public static Connection getConnection() {
    if (DB_URL == null || !DB_URL.startsWith("jdbc:sqlite:")) {
        throw new RuntimeException("Invalid database URL: " + DB_URL);
    }
    try {
        return DriverManager.getConnection(DB_URL);
    } catch (Exception e) {
        throw new RuntimeException("Unable to connect database.", e);
    }
}
```

### After
```java
public static Connection getConnection() {
    if (DB_URL == null || !DB_URL.startsWith("jdbc:sqlite:")) {
        throw new RuntimeException("Invalid database URL: " + DB_URL);
    }
    try {
        // Configure SQLite for better concurrency
        String url = DB_URL + "?journal_mode=WAL&busy_timeout=10000";
        return DriverManager.getConnection(url);
    } catch (Exception e) {
        throw new RuntimeException("Unable to connect database.", e);
    }
}
```

### Configuration Changes

- **`journal_mode=WAL`**: Enables Write-Ahead Logging mode, which allows concurrent reads and writes
- **`busy_timeout=10000`**: Sets a 10-second timeout for lock acquisition; SQLite will automatically retry instead of failing immediately

## Results

After applying the fix:
- ✅ Successfully initialized 15 users (1 admin + 7 sellers + 7 buyers)
- ✅ Successfully created 20 items across multiple categories
- ✅ Successfully created 6 messages, 4 orders, and 15 favorites
- ✅ Total of 60 records inserted without errors
- ✅ Build completed in ~5 seconds (down from hanging/failing)

## Additional Improvements

Also increased retry logic in `DemoDataInitializer`:
- Retry attempts: 3 → 10
- Retry delay: 100ms → 200ms
- Delays between user registrations: 50ms → 300ms

## Testing

To verify the fix:
```bash
# Remove existing database
Remove-Item secondhand.db, secondhand.db-shm, secondhand.db-wal -ErrorAction SilentlyContinue

# Run initialization
.\gradlew.bat initDemoData

# Verify data was created (should show 60 total records)
# Use SQLite browser or run query to check user count
```

## Files Modified

1. `src/config/DatabaseConfig.java` - Added WAL mode and busy timeout to connection URL
2. `src/config/DemoDataInitializer.java` - Increased retry attempts and delays
3. `src/InitializeDemoData.java` - Added 200ms delay between structure init and data init

## References

- [SQLite Write-Ahead Logging](https://www.sqlite.org/wal.html)
- [SQLite Busy Timeout](https://www.sqlite.org/c3ref/busy_timeout.html)
- [JDBC SQLite Driver Configuration](https://github.com/xerial/sqlite-jdbc#usage)

## Environment

- **Java**: 21
- **SQLite JDBC Driver**: 3.46.0.0
- **Gradle**: 9.2.1
- **OS**: Windows

## Labels

`bug`, `database`, `sqlite`, `concurrency`, `fixed`
