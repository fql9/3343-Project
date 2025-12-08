# SecondHandTrading - Quick Start Guide

## Installation

1. **Download** `SecondHandTrading-1.5.4.exe` from the [Releases](https://github.com/fql9/3343-Project/releases) page
2. **Run** the installer and follow the wizard
3. **Launch** from Start Menu or Desktop shortcut

> No Java installation required - JDK 21 + JavaFX is bundled!

---

## Demo Accounts

All accounts use password: **`password123`**

| Username | Role   | Description |
|----------|--------|-------------|
| admin    | ADMIN  | Full system access |
| alice    | SELLER | Has sold items |
| bob      | SELLER | Active seller |
| charlie  | BUYER  | Regular buyer |

---

## Features

- **Browse & Search** - Find items by keyword, category, or price range
- **Buy & Sell** - Post items, manage listings, complete transactions
- **Messaging** - Contact sellers directly
- **Favorites** - Save items for later
- **Order Tracking** - View purchase and sales history
- **Reviews** - Rate transactions after completion

---

## Data Location

- **Database**: `C:\Users\<username>\.secondhand-trading\secondhand.db`
- **Images**: Stored in app directory

---

## Troubleshooting

### App won't start?
Run from command line to see error messages:
```cmd
cd "C:\Program Files\SecondHandTrading"
SecondHandTrading.exe
```

### Reset all data?
Delete the database file:
```cmd
del "%USERPROFILE%\.secondhand-trading\secondhand.db"
```
Then restart the app - demo data will be recreated.

### Images not showing?
Ensure `item_images` folder exists in the app directory.

---

## Support

- **Repository**: https://github.com/fql9/3343-Project
- **Issues**: https://github.com/fql9/3343-Project/issues

---

**Version**: 1.5.4  
**Project**: CS3343 Group 11 - Second-Hand Trading Platform

