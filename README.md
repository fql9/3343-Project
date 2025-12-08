# CS3343 Group 11 - Second-Hand Trading Platform

A JavaFX + SQLite based second-hand trading platform that supports user registration, item posting, favorites, messaging, orders, reviews and admin management.

---

## Quick Start

### Option 1: Windows Installer (.exe) — No Java required (Recommended)

1. Go to the project [**Releases**](https://github.com/fql9/3343-Project/releases) page and download the latest installer:  
   `SecondHandTrading-1.5.3.exe` (~57 MB)
2. Double-click the installer and follow the wizard to complete installation.
3. Launch **SecondHandTrading** from the **Start Menu** or the **Desktop shortcut**.

> The installer bundles **JDK 21 + JavaFX 23**, so the target machine does **not** need Java pre-installed.

---

### Option 2: Windows Portable Version (no installation, USB friendly)

1. Download or build the portable package locally (see **Building Executable Packages** → `jpackagePortable`).
2. After extraction, the folder structure looks like:

   ```
   SecondHandTrading/
   ├── SecondHandTrading.exe   # Main executable (double-click to run)
   ├── runtime/                # Bundled JDK 21 + JavaFX
   └── item_images/            # Product images (sample + user uploads)
   ```

3. Double-click `SecondHandTrading.exe` to run the app.

> At runtime the database is created automatically under  
> `C:\Users\<username>\.secondhand-trading\secondhand.db`, so you **do not** need to ship a `.db` file with the app.

---

### Option 3: Run from Source (requires local JDK 21+)

```bash
# Clone the repository
git clone https://github.com/fql9/3343-Project.git
cd 3343-Project

# Windows
.\gradlew.bat run

# macOS / Linux
./gradlew run
```

### Demo Accounts

All accounts use password: **`password123`**

| Username | Role   | Description |
|----------|--------|-------------|
| admin    | ADMIN  | Full system access |
| alice    | SELLER | Has sold items |
| bob      | SELLER | Active seller |
| charlie  | BUYER  | Regular buyer |

---

## 📋 Project Overview

This project is a complete second-hand trading platform application that provides the following core features:
- User registration and login (supports buyer and admin roles)
- Item browsing and searching
- Item posting and management
- Favorites functionality
- Internal messaging system
- User management (admin feature)

## 💻 System Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| Java      | JDK 21  | JDK 21+     |
| RAM       | 512 MB  | 1 GB+       |
| Disk Space| 100 MB  | 200 MB      |
| OS        | Windows 10 / macOS 10.14 / Linux | Latest version |

## 🔧 Technology Stack

| Component | Version | Description |
|-----------|---------|-------------|
| Java      | 21      | Programming language |
| JavaFX    | 23.0.1  | UI framework |
| SQLite    | 3.46.0.0| Embedded database |
| Gradle    | 9.2.1   | Build tool |
| SLF4J     | 2.0.16  | Logging framework |
| JUnit 5   | 5.10.2  | Testing framework |
| Mockito   | 5.14.2  | Mocking framework |

## Project Structure

```
3343-Project/
├── src/
│   ├── MainApp.java                 # Application entry point
│   ├── config/
│   │   └── DatabaseConfig.java      # Database configuration
│   ├── controller/                  # Controller layer
│   │   ├── LoginController.java     # Login controller
│   │   ├── MainController.java      # Main interface controller
│   │   ├── BoardController.java     # Item list controller
│   │   ├── ItemDetailController.java # Item detail controller
│   │   ├── MyItemsController.java   # My items controller
│   │   ├── FavoritesController.java # Favorites controller
│   │   ├── MessageController.java   # Message controller
│   │   ├── OrderHistoryController.java # Buyer order history
│   │   ├── SalesHistoryController.java # Seller sales history
│   │   ├── NotificationController.java # Notification center
│   │   ├── UserProfileController.java # User profile center
│   │   └── UserManagementController.java # User management controller
│   ├── dao/                         # Data access layer
│   │   └── impl/                    # DAO implementations
│   ├── model/                       # Data models
│   │   ├── User.java
│   │   ├── Item.java
│   │   ├── Message.java
│   │   ├── Favorite.java
│   │   ├── Order.java
│   │   ├── Review.java
│   │   └── Notification.java
│   ├── service/                     # Business logic layer
│   │   ├── UserService.java
│   │   ├── ItemService.java
│   │   ├── MessageService.java
│   │   ├── FavoriteService.java
│   │   ├── OrderService.java
│   │   ├── ReviewService.java
│   │   └── NotificationService.java
│   └── util/                        # Utility classes
│       ├── DialogUtils.java         # Dialog utilities
│       ├── PasswordUtils.java       # Password encryption utilities
│       └── ValidationUtils.java     # Validation utilities
├── build.gradle                     # Gradle build configuration
├── settings.gradle                  # Gradle settings
└── README.md                        # Project documentation
```

## 🛠️ Environment Setup (for developers)

If you only use the installer or portable version, you can skip this section.  
This is only required when developing from source or building packages yourself.

### Step 1: Install Java 21+

Choose one of the following options:

#### Windows
1. Download [Adoptium Temurin JDK 21](https://adoptium.net/temurin/releases/?version=21&os=windows)
2. Run the installer (.msi file)
3. **Important**: Check "Set JAVA_HOME variable" during installation
4. Verify installation:
   ```powershell
   java -version
   # Should show: openjdk version "21.x.x"
   ```

#### macOS
```bash
# Using Homebrew
brew install openjdk@21

# Add to PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Verify
java -version
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-21-jdk

# Verify
java -version
```

> JavaFX 23.0.1 is managed via Gradle dependencies and jpackage tasks. No separate installation is needed.

---

## 📦 Installation & Running (from source)

### Run directly with Gradle

```bash
# Windows
.\gradlew.bat run

# macOS / Linux
./gradlew run
```

### Build FAT JAR (for development / debugging)

```bash
# Windows
.\gradlew.bat fatJar

# macOS / Linux
./gradlew fatJar

# Run
java -jar build/libs/3343-Project-1.0-SNAPSHOT-all.jar
```

> Note: The Gradle Wrapper is included, so you **do not** need a local Gradle installation.

---

## 📤 Building Executable Packages (Windows)

This project uses **jpackage** to generate a Windows installer and portable build, both bundling **Liberica JDK 21 Full + JavaFX**:

- All commands are run from the project root: `cd 3343-Project`
- On first run, Gradle will automatically download Liberica JDK Full (~200 MB), so internet access is required

### 1. Build Windows Installer (.exe)

```bash
.\gradlew.bat jpackageWindows
```

Output:

- Installer: `build/installer/SecondHandTrading-1.5.3.exe`
- Behavior: installs to `C:\Program Files\SecondHandTrading` and creates Start Menu / Desktop shortcuts

### 2. Build Windows Portable Version (app-image, no installation)

```bash
.\gradlew.bat jpackagePortable
```

Output directory:

```
build/jpackage/app-image/SecondHandTrading/
├── SecondHandTrading.exe   # Main executable
├── runtime/                # Bundled runtime (Liberica JDK 21 + JavaFX)
└── item_images/            # Product images
```

Zip the whole `SecondHandTrading` directory and share it with users.  
After extraction they can simply double-click `SecondHandTrading.exe` to run.

### 3. Skip tests when building (faster)

```bash
.\gradlew.bat fatJar -x test
```

> For release builds, it is recommended to run the full test suite first: `.\gradlew.bat test`.

---

## 📁 Distribution Summary

Recommended distribution options:

- **Windows Installer**: `build/installer/SecondHandTrading-1.5.3.exe`
  - Best for end users; one-click installation with automatic shortcuts
- **Windows Portable Version**: `build/jpackage/app-image/SecondHandTrading/`
  - Best for USB / “green software” style; unzip and run `SecondHandTrading.exe` directly

## Default User Accounts

The system includes pre-initialized demo accounts with sample data. All accounts use the password: **`password123`**

### User Accounts

| Username | Password    | Role   | Email                | Status & Description                                    |
|----------|-------------|--------|----------------------|---------------------------------------------------------|
| admin    | password123 | ADMIN  | admin@example.com    | Administrator - Full system access                      |
| alice    | password123 | SELLER | alice@example.com    | Active seller - Sold 2 items (MacBook, iPhone), earned $13,598 |
| bob      | password123 | SELLER | bob@example.com      | Active seller - Sold 1 item (Sony headphones), earned $1,499, has 1 item for sale (Dell monitor) |
| charlie  | password123 | BUYER  | charlie@example.com  | Active buyer - Purchased 1 item (iPhone) from Alice    |
| diana    | password123 | SELLER | diana@example.com    | Seller/Buyer - Purchased 1 item (Sony headphones)      |
| evan     | password123 | BUYER  | evan@example.com     | Seller - Has 1 item for sale (IKEA desk)               |
| frank    | password123 | SELLER | frank@example.com    | Active seller - Has 2 items for sale (Nikon camera, ThinkPad) |
| grace    | password123 | BUYER  | grace@example.com    | Active buyer - Has 1 order in progress (Nintendo Switch) |
| henry    | password123 | SELLER | henry@example.com    | Active seller - Sold 1 item (Nintendo Switch), earned $1,899 |
| iris     | password123 | BUYER  | iris@example.com     | Active buyer - Has favorites and product inquiries      |

### Sample Data Overview

The database includes comprehensive demo data to showcase all platform features:

**📦 Items (20 total)**
- Categories: Electronics, Gaming, Furniture, Books, Sports
- Various prices ranging from $50 to $8,999
- All items include matching product images

**💬 Messages (18 conversations)**
- Multiple conversation threads between buyers and sellers
- Inquiries about products, price negotiations, and delivery arrangements

**🛒 Orders (6 transactions)**
- Various order statuses: completed, shipped, paid
- Orders include complete shipping addresses in the US

**❤️ Favorites (30 items)**
- Distributed across all buyer accounts
- Multiple favorites per user for testing

### Initializing Demo Data

- On first launch, if the database is empty, the app **automatically initializes demo data**, including:
  - 15 users (1 admin, multiple SELLER / BUYER accounts)
  - 20 items
  - 18 messages
  - 6 orders
  - 30 favorites
- All accounts use **`password123`** by default.

Default database path:

- Windows: `C:\Users\<username>\.secondhand-trading\secondhand.db`
- macOS / Linux: `~/.secondhand-trading/secondhand.db`

#### Manually re-initialize demo data (for development / debugging)

```bash
# Windows
.\gradlew.bat initDemoData

# macOS / Linux
./gradlew initDemoData
```

To completely reset the database (delete all data and recreate demo data):

```bash
# Windows
Remove-Item "$env:USERPROFILE\.secondhand-trading\secondhand.db" -Force
.\gradlew.bat initDemoData

# macOS / Linux
rm ~/.secondhand-trading/secondhand.db
./gradlew initDemoData
```

**Image Storage**:
- Sample and uploaded images are stored under the `item_images/` directory
- The database stores **image paths only**, not binary image data
- For the portable build, `item_images/` is packaged together with the app

**Note**: All accounts use `password123` for easy testing. Change passwords in production environments.

## Main Features

### 1. User Features
- User registration and login
- Password encryption (SHA-256)
- **User Profile Center**:
  - Avatar upload and display
  - Bio and personal information editing
  - Statistics (Total Sales, Total Purchases, Join Date)
- Role-based access control (Buyer/Admin)

### 2. Item Features
- Post items (title, description, price, contact info)
- **Image Upload**: Upload and display item images
  - Supported formats: JPG, PNG, GIF, BMP
  - Images stored in local `item_images/` folder
  - All users can view uploaded images
- **Advanced Search**: Filter by keyword, price range, category, and sort by price/date
- View item details with seller rating and item images
- Manage my items (edit/delete)

### 3. Transaction & Order System
- **Order Lifecycle**: Purchase -> Paid -> Shipped -> Received -> Completed
- **Order History**: View past purchases and sales
- **Review System**: Rate and review sellers after transaction completion

### 4. Favorites & Notifications
- Add/remove items to/from favorites
- **Real-time Notifications**: Get alerts for order status changes (Shipped, Received)
- Notification Center to view all alerts

### 5. Messaging Features
- Contact sellers via messages
- View inbox
- Reply to messages

### 6. Admin Features
- User management (view/enable/disable users)
- Item moderation

## Testing

### Test Structure

The project follows a mixed testing approach combining unit tests and integration tests:

#### Unit Tests (with Mockito)
- Location: `src/test/java/unit/`
- Services tested with mocked dependencies:
  - `ItemServiceTest` (17 test cases)
  - `FavoriteServiceTest` (11 test cases)
  - `ImageUtilsTest` (15 test cases for image processing)
  - `ValidationUtilsTest`, `PasswordUtilsTest`

#### Integration Tests (with real SQLite database)
- Location: `src/test/java/integration/`
- Components tested:
  - **DAO Layer**: `UserDaoImplTest`, `ItemDaoImplTest` (11 search tests), `MessageDaoImplTest`, `FavoriteDaoImplTest`, `OrderDaoImplTest` (10 test cases)
  - **Service Layer**: `UserServiceTest`, `MessageServiceTest`, `OrderServiceTest`, `ReviewServiceTest` (comprehensive review system tests), `NotificationServiceTest`
  - **Config**: `DatabaseConfigTest`, `DemoDataInitializerTest`
  - **Utilities**: `ExportUsersTest`

### Test Coverage Report (JaCoCo)

**Overall Coverage:**
- **Instruction Coverage**: 92% (4,929 of 5,318 instructions)
- **Branch Coverage**: 80% (296 of 367 branches)
- **Method Coverage**: 95% (272 of 286 methods)
- **Class Coverage**: 100% (all 28 classes)
- **Line Coverage**: 91% (1,104 of 1,215 lines)

**By Package:**
| Package | Instruction Coverage | Branch Coverage | Methods |
|---------|---------------------|-----------------|---------|
| model | 99% | n/a | 110/110 |
| config | 94% | 75% | 18/18 |
| dao.impl | 92% | 76% | 56/56 |
| service | 90% | 80% | 79/79 |
| util | 88% | 96% | 23/23 |

**Total Test Cases**: 271 (all passing ✅)

### Running Tests

```bash
# Run all tests
.\gradlew test

# Run only unit tests
.\gradlew unitTest

# Run only integration tests
.\gradlew integrationTest

# Generate coverage report
.\gradlew jacocoTestReport

# View coverage report
# Open: build/jacocoHtml/index.html in browser
```

### Test Coverage Achievements

**Current Status**: 271 test cases, 92% instruction coverage, 80% branch coverage ✅

**Recent Improvements** (Test branch → Master):
1.  **Enhanced DAO Tests**: Added comprehensive tests for `ItemDaoImpl.searchItems()` (11 cases) and `OrderDaoImpl` (10 cases)
2.  **Service Integration Tests**: Comprehensive testing for `ReviewService`, `OrderService`, `NotificationService`
3.  **Utility Tests**: Added `ImageUtilsTest` with 15 test cases for image processing
4.  **Configuration Tests**: `DemoDataInitializerTest` for data initialization validation
5.  **Database Safety**: Fixed critical database isolation bug preventing production data loss

**Next Steps** (Optional enhancements):
- Controller Integration Tests: Add tests for UI Controllers with mocked Services (~30-50 cases)
- Edge Case Coverage: Improve branch coverage in complex error handling scenarios

**Target**: Maintain 90%+ coverage with continued refactoring

## Database

The project uses SQLite database with the file `secondhand.db`, containing the following tables:

- `users` - User table (updated with avatar, bio, stats)
- `items` - Item table
- `favorites` - Favorites table
- `messages` - Messages table
- `orders` - Order transaction records
- `reviews` - User ratings and reviews
- `notifications` - System notifications

The database is automatically created on first run. Schema migrations are handled automatically.

## Development Tools

### View User Information
Run the following utility class to view all users and their information:

```bash
# View all users with password hashes and roles
java -cp "build/classes/java/main;lib/*" util.QueryUsers

# Or run directly in IDE: src/util/QueryUsers.java
```

### Export User Data
Export all user information to a text file:

```bash
# Export users to users_export.txt
java -cp "build/classes/java/main;lib/*" util.ExportUsers

# Or run directly in IDE: src/util/ExportUsers.java
```

## ❓ Troubleshooting

### 1. "java is not recognized" or "java: command not found"

**Problem**: Java is not installed or not in system PATH.

**Solution**:
```bash
# Check if Java is installed
java -version

# If not found, install Java 21:
# Windows: Download from https://adoptium.net/temurin/releases/?version=21
# macOS: brew install openjdk@21
# Linux: sudo apt install openjdk-21-jdk
```

### 2. "JavaFX runtime components are missing"

**Problem**: Running with wrong Java version or missing JavaFX.

**Solution**: Use the portable package with the Launcher:
```bash
# Download the portable package and run
cd SecondHandTrading
java -cp ".;3343-Project.jar" Launcher
```

Or use `Start.bat` which handles this automatically.

**Note**: You may see a warning `Unsupported JavaFX configuration: classes were loaded from 'unnamed module'`. This is expected and does not affect functionality.

### 3. Gradle Build Failure - "Unable to delete directory"

**Problem**: Build directory is locked by another process (IDE, OneDrive sync, etc.)

**Solution**:
```bash
# Windows PowerShell
.\gradlew.bat --stop
Remove-Item -Recurse -Force .\build
.\gradlew.bat fatJar

# macOS/Linux
./gradlew --stop
rm -rf build
./gradlew fatJar
```

### 4. Gradle Download Failure

**Problem**: Network issues downloading Gradle wrapper.

**Solution**:
- Check your internet connection
- Try using a VPN
- Or use locally installed Gradle:
  ```bash
  gradle fatJar
  ```

### 5. Images Not Displaying

**Problem**: The `item_images/` folder is missing or not in the correct location.

**Solution**: Ensure `item_images/` folder is in the same directory as the JAR file:
```
your-folder/
├── 3343-Project-1.0-SNAPSHOT-all.jar
├── secondhand.db
└── item_images/
    ├── bike.jpg
    ├── laptop.jpg
    └── ...
```

### 6. Database Connection Error

**Problem**: Database file is corrupted or locked.

**Solution**:
```bash
# Delete the database and reinitialize
# Windows
Remove-Item secondhand.db -Force
.\gradlew.bat initDemoData

# macOS/Linux
rm secondhand.db
./gradlew initDemoData
```

### 7. Chinese Characters Display as Garbled Text

**Problem**: System encoding mismatch.

**Solution**: Run with UTF-8 encoding:
```bash
java -Dfile.encoding=UTF-8 -jar build/libs/3343-Project-1.0-SNAPSHOT-all.jar
```

## Contributing

Issues and Pull Requests are welcome!

1. Fork the project
2. Create a new branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Create a Pull Request

## License

This project is for educational purposes only.

## Contact

- **Project**: CS3343 Group 11
- **Repository**: https://github.com/fql9/3343-Project

---

**Note**: This project is a course assignment of CS3343 and should not be used for commercial purposes.
