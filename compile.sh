#!/bin/bash

# 优先使用 Gradle
if [ -f "gradlew" ]; then
    chmod +x gradlew
    ./gradlew run
    exit $?
elif command -v gradle &> /dev/null; then
    gradle run
    exit $?
fi

# 手动编译方式
echo "使用手动编译..."

mkdir -p lib bin

# 下载 JAR 文件
download_jar() {
    [ -f "$2" ] && return 0
    echo "下载: $(basename $2)"
    curl -L -o "$2" "$1" 2>/dev/null || return 1
}

# 检测平台
OS=$(uname -s)
ARCH=$(uname -m)
[ "$OS" = "Darwin" ] && PLATFORM=$([ "$ARCH" = "arm64" ] && echo "mac-aarch64" || echo "mac-x64")
[ "$OS" = "Linux" ] && PLATFORM=$([ "$ARCH" = "aarch64" ] && echo "linux-aarch64" || echo "linux-x64")
[ -z "$PLATFORM" ] && PLATFORM="win-x64"

VERSION="25.0.1"
BASE="https://repo1.maven.org/maven2"

# 下载依赖
download_jar "$BASE/org/openjfx/javafx-base/$VERSION/javafx-base-$VERSION-$PLATFORM.jar" "lib/javafx-base.jar" || exit 1
download_jar "$BASE/org/openjfx/javafx-controls/$VERSION/javafx-controls-$VERSION-$PLATFORM.jar" "lib/javafx-controls.jar" || exit 1
download_jar "$BASE/org/openjfx/javafx-fxml/$VERSION/javafx-fxml-$VERSION-$PLATFORM.jar" "lib/javafx-fxml.jar" || exit 1
download_jar "$BASE/org/openjfx/javafx-graphics/$VERSION/javafx-graphics-$VERSION-$PLATFORM.jar" "lib/javafx-graphics.jar" || exit 1
download_jar "$BASE/org/xerial/sqlite-jdbc/3.46.0.0/sqlite-jdbc-3.46.0.0.jar" "lib/sqlite-jdbc.jar" || exit 1
download_jar "$BASE/org/slf4j/slf4j-api/2.0.16/slf4j-api-2.0.16.jar" "lib/slf4j-api.jar" || exit 1
download_jar "$BASE/org/slf4j/slf4j-simple/2.0.16/slf4j-simple-2.0.16.jar" "lib/slf4j-simple.jar" || exit 1

# 编译
echo "编译中..."
find src -name "*.java" > /tmp/sources.txt
javac --module-path lib --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics \
      -cp "lib/sqlite-jdbc.jar:lib/slf4j-api.jar:lib/slf4j-simple.jar:src" \
      -d bin @/tmp/sources.txt || exit 1
rm -f /tmp/sources.txt

# 运行
echo "运行中..."
java --module-path lib --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics \
     -cp "lib/sqlite-jdbc.jar:lib/slf4j-api.jar:lib/slf4j-simple.jar:bin" MainApp
