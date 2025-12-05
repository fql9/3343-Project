# GitHub Issues - Resolved Problems

## Issue #1: ListView IndexOutOfBoundsException when clicking message items

**Labels:** `bug`, `ui`, `javafx`, `fixed`

**Priority:** High

### Description

When clicking on message items in the MessageController's ListView, the application crashes with an IndexOutOfBoundsException error.

### Error Message

```
Exception in thread "JavaFX Application Thread" java.lang.IndexOutOfBoundsException: [ fromIndex: 0, toIndex: 1, size: 0 ]
    at javafx.controls@23.0.1/com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList.subList
    at javafx.scene.control.MultipleSelectionModelBase.clearAndSelect
```

### Steps to Reproduce

1. Launch the application
2. Navigate to Messages view
3. Click on any message item in the list
4. Exception is thrown

### Root Cause

The ListView selection model was trying to select items while the list was being cleared or modified. This caused a race condition where JavaFX attempted to access indices that no longer existed.

### Solution

Modified `MessageController.java`:
- Added `clearSelection()` before clearing list items
- Added validation in selection listener to ensure item exists before processing
- Prevented automatic list refresh that could interfere with user clicks

**Modified Files:**
- `src/controller/MessageController.java` - Line 104-108
- `src/controller/UserManagementController.java` - Line 161

### Testing

- ✅ Click message items - no crash
- ✅ Refresh message list - no crash
- ✅ Switch between conversations - works smoothly

---

## Issue #2: SQLite Database Locked During Demo Data Initialization

**Labels:** `bug`, `database`, `sqlite`, `concurrency`, `fixed`

**Priority:** Critical

### Description

When running `.\gradlew.bat initDemoData` to populate the database with demo data, all operations failed with `SQLITE_BUSY` errors. The task showed "SUCCESS" but the database remained empty.

### Error Message

```
org.sqlite.SQLiteException: [SQLITE_BUSY] The database file is locked (database is locked)
    at org.sqlite.core.DB.execute(DB.java:985)
    at dao.impl.UserDaoImpl.save(UserDaoImpl.java:83)
    at service.UserService.register(UserService.java:70)
```

### Steps to Reproduce

1. Run `.\gradlew.bat initDemoData`
2. Task completes with "BUILD SUCCESSFUL"
3. Check database - 0 records inserted
4. All user registrations failed silently

### Root Cause

SQLite's default configuration uses DEFERRED transactions with no busy timeout. Multiple rapid database connections caused file locking conflicts:
- Default busy timeout = 0ms (fail immediately)
- Rollback journal mode has poor concurrency
- Each DAO operation creates a new connection

### Solution

**Modified `DatabaseConfig.java`:**

```java
// Before:
return DriverManager.getConnection(DB_URL);

// After:
String url = DB_URL + "?journal_mode=WAL&busy_timeout=10000";
return DriverManager.getConnection(url);
```

**Configuration changes:**
- `journal_mode=WAL`: Write-Ahead Logging mode for concurrent reads/writes
- `busy_timeout=10000`: 10-second timeout for lock acquisition with automatic retries

**Additional improvements in `DemoDataInitializer.java`:**
- Retry attempts: 3 → 10
- Retry delay: 100ms → 200ms
- Delays between operations: 50ms → 300ms

### Results

- ✅ Successfully initialized 15 users
- ✅ Successfully created 20 items
- ✅ Successfully created 6 messages, 4 orders, 15 favorites
- ✅ Total 60 records inserted without errors
- ✅ Build time: ~5 seconds (previously hanging/failing)

### Testing

```bash
Remove-Item secondhand.db -ErrorAction SilentlyContinue
.\gradlew.bat initDemoData
# Verify: 60 total records
```

**References:**
- [SQLite Write-Ahead Logging](https://www.sqlite.org/wal.html)
- [SQLite Busy Timeout](https://www.sqlite.org/c3ref/busy_timeout.html)

---

## Issue #3: Message List Infinite Refresh Loop

**Labels:** `bug`, `ui`, `performance`, `fixed`

**Priority:** Medium

### Description

When clicking on a message item in the message list, the UI continuously flickers and refreshes, creating an infinite loop that makes the interface unusable.

### Symptoms

- Message list flickers rapidly when clicked
- High CPU usage
- UI becomes unresponsive
- Cannot view conversation properly

### Root Cause

Circular dependency in message list refresh logic:
1. User clicks message item → triggers `showConversation()`
2. `showConversation()` marks messages as read → calls `Platform.runLater(this::loadMessageList)`
3. `loadMessageList()` restores previous selection → triggers selection change event
4. Selection change → triggers `showConversation()` again
5. **Infinite loop**

### Solution

**Removed automatic restore logic:**
- Eliminated `previousSelection` tracking
- Removed automatic re-selection after list refresh
- Kept `clearSelection()` to prevent IndexOutOfBoundsException
- Message content still refreshes on send via `handleSendMessage()`

**Modified Files:**
- `src/controller/MessageController.java`
  - Line 102-106: Simplified `loadMessageList()`
  - Line 207: Removed auto-refresh in `showConversation()`

### Testing

- ✅ Click message items - no flickering
- ✅ View conversations - stable UI
- ✅ Send messages - conversation updates correctly
- ✅ No infinite loops

---

## Issue #4: Missing Product Images in Demo Data

**Labels:** `enhancement`, `ui`, `demo-data`, `fixed`

**Priority:** Low

### Description

12 out of 20 demo items had no images (`image_url` was NULL), resulting in missing or placeholder images in the UI.

### Affected Products

| ID | Product | Category | Status |
|----|---------|----------|--------|
| 7 | iPad Pro 11-inch | Electronics | ❌ No image |
| 8 | Samsung Galaxy Tab | Electronics | ❌ No image |
| 9 | Mechanical Keyboard | Electronics | ❌ No image |
| 10 | Wireless Mouse | Electronics | ❌ No image |
| 12 | PlayStation 5 | Gaming | ❌ No image |
| 13 | Xbox Series X | Gaming | ❌ No image |
| 15 | Bookshelf | Furniture | ❌ No image |
| 16 | Office Chair | Furniture | ❌ No image |
| 17 | Programming Books | Books | ❌ No image |
| 18 | Design Books | Books | ❌ No image |
| 19 | Road Bike | Sports | ❌ No image |
| 20 | Gym Equipment | Sports | ❌ No image |

### Solution

**Phase 1: Initial mapping (temporary)**
- Reused existing images for similar items
- All items had images but some mismatched

**Phase 2: Download real images**
Downloaded 10 new high-quality images from Unsplash:
- `ipad.jpg` - iPad Pro
- `tablet.jpg` - Samsung Galaxy Tab
- `keyboard.jpg` - Mechanical Keyboard
- `mouse.jpg` - Wireless Mouse
- `ps5.jpg` - PlayStation 5
- `xbox.jpg` - Xbox Series X
- `bookshelf.jpg` - Bookshelf
- `chair.jpg` - Office Chair
- `programming.jpg` - Programming Books
- `design.jpg` - Design Books
- `bike.jpg` - Road Bike
- `gym.jpg` - Gym Equipment

### Final Results

**Total images: 22 unique product images**

All 20 items now have matching, professional images:

📱 **Electronics (10 items - 10 unique images)**
- Each electronic item has a specific matching image

🎮 **Gaming (3 items - 3 unique images)**
- Nintendo Switch, PS5, Xbox each have dedicated images

🪑 **Furniture (3 items - 3 unique images)**
- Desk, Bookshelf, Chair each have dedicated images

📚 **Books (2 items - 2 unique images)**
- Programming and Design books have separate images

🚴 **Sports (2 items - 2 unique images)**
- Bike and Gym equipment have dedicated images

### Image Source

All images sourced from [Unsplash](https://unsplash.com/) - high-quality, license-free photography.

### Testing

- ✅ All items display images correctly
- ✅ Images match product descriptions
- ✅ No broken image links
- ✅ Images load quickly

---

## Issue #5: Unread Message Badge Not Clearing on Click

**Labels:** `bug`, `ui`, `messaging`, `fixed`

**Priority:** Medium

### Description

When clicking on a conversation with unread messages (red badge with count), the messages were marked as read in the database but the red badge remained visible in the UI until manual refresh.

### Expected Behavior

- Click conversation with red badge
- Badge should disappear immediately
- Main unread count should update

### Actual Behavior

- Click conversation
- Messages marked as read in database
- Red badge still visible
- Main unread count not updated
- Requires manual refresh to see changes

### Root Cause

The `showConversation()` method called `markConversationAsRead()` but did not update the UI. The message list displayed stale data until a full refresh occurred.

### Solution

Modified `MessageController.showConversation()`:

```java
// Mark messages as read and refresh the list to remove red badge
messageService.markConversationAsRead(currentUserId, otherUserId);

// Refresh message list in background to update unread count
javafx.application.Platform.runLater(() -> {
    // Find the item in the list for this user
    for (int i = 0; i < messageListView.getItems().size(); i++) {
        MessageItem item = messageListView.getItems().get(i);
        if (item.userId.equals(otherUserId) && item.unreadCount > 0) {
            // Update the unread count to 0
            item.unreadCount = 0;
            // Force the cell to refresh
            messageListView.getItems().set(i, item);
            break;
        }
    }
    
    // Update main view unread count
    if (mainController != null) {
        mainController.updateUnreadCount();
    }
});
```

### Features

- ✅ Database updated immediately
- ✅ UI updates in background (no blocking)
- ✅ Red badge disappears on click
- ✅ Main unread count updates
- ✅ No list flickering or performance issues

### Testing

1. Send message from user A to user B
2. Login as user B
3. See red badge on user A's conversation
4. Click on conversation
5. **Result:** Red badge disappears immediately

---

## Summary

| Issue | Type | Priority | Status | Files Changed |
|-------|------|----------|--------|---------------|
| #1 ListView IndexOutOfBoundsException | Bug | High | ✅ Fixed | MessageController.java, UserManagementController.java |
| #2 SQLite Database Locked | Bug | Critical | ✅ Fixed | DatabaseConfig.java, DemoDataInitializer.java |
| #3 Message List Infinite Loop | Bug | Medium | ✅ Fixed | MessageController.java |
| #4 Missing Product Images | Enhancement | Low | ✅ Fixed | Database + 12 new image files |
| #5 Unread Badge Not Clearing | Bug | Medium | ✅ Fixed | MessageController.java |

**Total issues resolved:** 5  
**Code files modified:** 4  
**New assets added:** 12 image files  
**Database records:** 60 demo records

