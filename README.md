# Second-hand Trading Platform

CS3343 Group 11 Project

## 项目简介

这是一个基于 JavaFX 的二手交易平台，支持用户注册、商品发布、交易管理、消息系统等功能。

## 环境要求

- JDK 25 或更高版本
- Gradle（可选，用于构建项目）

## 快速开始

### 方法 1：使用 Gradle（推荐）

1. 克隆项目到本地
2. 在 IDE 中打开项目（IntelliJ IDEA / VS Code / Cursor）
3. 等待 Gradle 自动同步依赖（JavaFX 和 SQLite JDBC 会自动下载）
4. 运行 `MainApp` 类

### 方法 2：使用编译脚本

1. 确保已安装 JDK 25
2. 运行编译脚本：
   ```bash
   ./compile.sh
   ```
   脚本会自动下载所需的依赖（SQLite JDBC 和 SLF4J）

## 项目结构

```
src/
├── config/          # 数据库配置
├── controller/      # 控制器层（UI）
├── dao/            # 数据访问层接口
│   └── impl/       # 数据访问层实现
├── model/          # 数据模型
├── service/        # 业务逻辑层
└── util/           # 工具类
```

## 主要功能

- 用户注册和登录
- 商品发布、搜索、浏览
- 交易管理（协商、支付、发货、收货）
- 收藏功能
- 消息系统
- 用户管理（管理员）

## 数据库

项目使用 SQLite 数据库，数据库文件 `secondhand.db` 会在首次运行时自动创建。

## 依赖说明

所有依赖都通过 Maven 自动下载，无需手动配置：
- JavaFX 25.0.1
- SQLite JDBC 3.46.0.0
- SLF4J 2.0.16

## 注意事项

- 首次运行前，IDE 会自动从 Maven 下载 JavaFX 依赖
- 如果使用编译脚本，依赖会自动下载到 `lib/` 目录
- 数据库文件 `secondhand.db` 不应提交到 Git
