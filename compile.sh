#!/bin/bash

# JavaFX SDK 路径
JAVAFX_LIB="/Users/liyuan/Downloads/javafx-sdk-25.0.1/lib"

# 查找 SQLite JDBC 驱动（Gradle 缓存中）
SQLITE_JAR=$(find ~/.gradle/caches/modules-2/files-2.1/org.xerial/sqlite-jdbc -name "*.jar" 2>/dev/null | head -1)

# 如果找不到，尝试下载
if [ -z "$SQLITE_JAR" ]; then
    echo "正在下载 SQLite JDBC 驱动..."
    mkdir -p lib
    curl -L -o lib/sqlite-jdbc.jar "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.0.0/sqlite-jdbc-3.46.0.0.jar" 2>/dev/null
    if [ -f "lib/sqlite-jdbc.jar" ]; then
        SQLITE_JAR="lib/sqlite-jdbc.jar"
        echo "SQLite JDBC 驱动下载成功"
    else
        echo "警告: 无法下载 SQLite JDBC 驱动，请手动下载"
        echo "下载地址: https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.0.0/sqlite-jdbc-3.46.0.0.jar"
        echo "保存到: lib/sqlite-jdbc.jar"
        exit 1
    fi
fi

echo "使用 SQLite JDBC: $SQLITE_JAR"

# SLF4J 日志库路径
SLF4J_API="lib/slf4j-api.jar"
SLF4J_SIMPLE="lib/slf4j-simple.jar"

# 检查 SLF4J 是否存在
if [ ! -f "$SLF4J_API" ] || [ ! -f "$SLF4J_SIMPLE" ]; then
    echo "警告: SLF4J 日志库未找到，正在下载..."
    mkdir -p lib
    curl -L -o "$SLF4J_API" "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16.jar" 2>/dev/null
    curl -L -o "$SLF4J_SIMPLE" "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.16/slf4j-simple-2.0.16.jar" 2>/dev/null
fi

# 编译所有 Java 文件，包含 JavaFX、SQLite 和 SLF4J 类路径
javac -cp "$JAVAFX_LIB/*:$SQLITE_JAR:$SLF4J_API:$SLF4J_SIMPLE:src" -d bin src/**/*.java

if [ $? -eq 0 ]; then
    echo "编译成功！"
    # 运行程序，包含所有必需的依赖
    java --module-path "$JAVAFX_LIB" --add-modules javafx.controls,javafx.fxml -cp "$JAVAFX_LIB/*:$SQLITE_JAR:$SLF4J_API:$SLF4J_SIMPLE:bin" MainApp
else
    echo "编译失败！"
    exit 1
fi

