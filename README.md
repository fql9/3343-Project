# CS3343 Group 11 - Second-Hand Trading Platform

A JavaFX-based second-hand goods trading platform system that supports user registration, item posting, favorites, messaging, and more.

## Project Overview

This project is a complete second-hand trading platform application that provides the following core features:
- User registration and login (supports buyer and admin roles)
- Item browsing and searching
- Item posting and management
- Favorites functionality
- Internal messaging system
- User management (admin feature)

## Technology Stack

- **Programming Language**: Java 21
- **UI Framework**: JavaFX 17.0.10
- **Database**: SQLite 3.46.0.0
- **Build Tool**: Gradle 9.2.1
- **Logging Framework**: SLF4J 2.0.16

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

## Requirements

- **JDK**: Java 21 or higher
- **Gradle**: 6.4 or higher (Gradle Wrapper recommended)
- **Operating System**: Windows / macOS / Linux

## Installation and Running

### 1. Clone the Repository

```bash
git clone https://github.com/fql9/3343-Project.git
cd 3343-Project
```

### 2. Build the Project

Build the project using Gradle Wrapper:

```bash
# Windows
.\gradlew.bat build

# macOS/Linux
./gradlew build
```

### 3. Run the Application

```bash
# Windows
.\gradlew.bat run

# macOS/Linux
./gradlew run
```

Alternatively, run `MainApp.java` directly in your IDE (IntelliJ IDEA or VS Code).

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

⚠️ **Important**: Running `.\gradlew.bat clean build` will NOT delete your database or demo data. The `clean` command only removes build artifacts (compiled classes). Your `secondhand.db` database file remains intact.

To populate the database with comprehensive sample data, use the Gradle task:

```bash
# Windows
.\gradlew.bat initDemoData

# macOS/Linux
./gradlew initDemoData
```

This will create:
- **15 users**: 1 admin (admin), 7 sellers (alice, charlie, evan, grace, iris, kevin, mike), 7 buyers (bob, diana, frank, henry, julia, laura, nathan)
- **20 items**: Electronics, Gaming, Furniture, Books, and Sports categories
- **18 messages**: Multiple conversation threads between users
- **6 orders**: Orders with various statuses and delivery addresses
- **30 favorites**: Distributed across all buyers
- All accounts use password: **`password123`**

**If you want to reset the database** (delete all data and start fresh):
```bash
# Windows
Remove-Item secondhand.db -Force
.\gradlew.bat initDemoData

# macOS/Linux
rm secondhand.db
./gradlew initDemoData
```

**Image Storage**: 
- Images are stored in the `item_images/` folder
- All users can see the images as they reference the same local folder
- The database stores the image file paths (not the actual image data)
- Sample images are already provided in the `item_images/` folder

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
- Location: `test/unit/`
- Services tested with mocked dependencies:
  - `ItemServiceTest` (17 test cases)
  - `FavoriteServiceTest` (11 test cases)
  - `ValidationUtilsTest`, `PasswordUtilsTest`

#### Integration Tests (with real SQLite database)
- Location: `test/integration/`
- Components tested:
  - **DAO Layer**: `UserDaoImplTest`, `ItemDaoImplTest`, `MessageDaoImplTest`, `FavoriteDaoImplTest`
  - **Service Layer**: `UserServiceTest`, `MessageServiceTest`, `OrderServiceTest`, `ReviewServiceTest`, `NotificationServiceTest`
  - **Config**: `DatabaseConfigTest`
  - **Utilities**: `ExportUsersTest`

### Test Coverage Report (JaCoCo)

**Overall Coverage:**
- **Line Coverage**: 87% (3,378 of 3,877 instructions)
- **Branch Coverage**: 71% (205 of 287 branches)
- **Method Coverage**: 100% (all 267 methods)
- **Class Coverage**: 100% (all 26 classes)

**By Package:**
| Package | Line Coverage | Branch Coverage | Methods |
|---------|---------------|-----------------|---------|
| model | 99% | n/a | 110/110 |
| util | 92% | 100% | 16/16 |
| service | 89% | 80% | 79/79 |
| dao.impl | 83% | 55% | 56/56 |
| config | 77% | 75% | 6/6 |

**Total Test Cases**: 231 (all passing ✅)

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

### Test Coverage Roadmap

**Current Status**: 231 test cases, 87% code coverage

**Planned Improvements**:
1. **Service Unit Tests** (Planned): Add unit tests for MessageService, OrderService, ReviewService, NotificationService with mocked dependencies (~40-50 cases)
2. **DAO Integration Tests** (Planned): Add tests for ReviewDaoImpl, NotificationDaoImpl, OrderDaoImpl (~15-20 cases)
3. **Controller Integration Tests** (Planned): Add integration tests for critical Controllers with mocked Services (~30-50 cases)
   - LoginController, BoardController, CheckoutController, OrderHistoryController, etc.

**Target Coverage**: 95%+

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

## Troubleshooting

### 1. Gradle Download Failure
If you encounter Gradle download issues:
- Manually download Gradle and configure `GRADLE_HOME`
- Use locally installed Gradle
- Disable Gradle Wrapper in VS Code settings: `java.import.gradle.wrapper.enabled = false`

### 2. JavaFX Runtime Error
Ensure Java 21 is properly installed and JavaFX modules are configured.

### 3. Missing Database File
After deleting the `secondhand.db` file, restart the program to automatically create a new database.

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
