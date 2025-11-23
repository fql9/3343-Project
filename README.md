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

- **Programming Language**: Java 25
- **UI Framework**: JavaFX 23.0.1
- **Database**: SQLite 3.46.0.0
- **Build Tool**: Gradle
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
│   │   └── UserManagementController.java # User management controller
│   ├── dao/                         # Data access layer
│   │   └── impl/                    # DAO implementations
│   ├── model/                       # Data models
│   │   ├── User.java
│   │   ├── Item.java
│   │   ├── Message.java
│   │   └── Favorite.java
│   ├── service/                     # Business logic layer
│   │   ├── UserService.java
│   │   ├── ItemService.java
│   │   ├── MessageService.java
│   │   └── FavoriteService.java
│   └── util/                        # Utility classes
│       ├── DialogUtils.java         # Dialog utilities
│       ├── PasswordUtils.java       # Password encryption utilities
│       └── ValidationUtils.java     # Validation utilities
├── build.gradle                     # Gradle build configuration
├── settings.gradle                  # Gradle settings
└── README.md                        # Project documentation
```

## Requirements

- **JDK**: Java 25 or higher
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

The system initializes with the following test accounts:

| Username | Role  | Email                 | Description  |
|----------|-------|-----------------------|--------------|
| admin    | ADMIN | admin@example.com     | Admin account|
| alice    | BUYER | alice@example.com     | Regular buyer|
| bob      | BUYER | bob@example.com       | Regular buyer|
| charlie  | BUYER | charlie@example.com   | Regular buyer|
| david    | BUYER | david@example.com     | Regular buyer|

**Note**: Default passwords for normal users are typically `password` and are `admin123` for administrators. Please change default passwords in production environments.

## Main Features

### 1. User Features
- User registration and login
- Password encryption (SHA-256)
- User profile management
- Role-based access control (Buyer/Admin)

### 2. Item Features
- Post items (title, description, price, contact info)
- Browse and search items
- View item details
- Manage my items (edit/delete)

### 3. Favorites Features
- Add/remove items to/from favorites
- View favorites list

### 4. Messaging Features
- Contact sellers via messages
- View inbox
- Reply to messages

### 5. Admin Features
- User management (view/enable/disable users)
- Item moderation

## Database

The project uses SQLite database with the file `secondhand.db`, containing the following tables:

- `users` - User table
- `items` - Item table
- `favorites` - Favorites table
- `messages` - Messages table

The database is automatically created on first run.

## Development Tools

### View User Information
Run the following utility class to view all users and their information:

```bash
# Run in IDE
src/util/ShowUserPasswords.java
```

### Export User Data
```bash
# Run the corresponding task or utility class in IDE
```

## Troubleshooting

### 1. Gradle Download Failure
If you encounter Gradle download issues:
- Manually download Gradle and configure `GRADLE_HOME`
- Use locally installed Gradle
- Disable Gradle Wrapper in VS Code settings: `java.import.gradle.wrapper.enabled = false`

### 2. JavaFX Runtime Error
Ensure Java 25 is properly installed and JavaFX modules are configured.

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

**Note**: This project is a course assignment and should not be used for commercial purposes.
