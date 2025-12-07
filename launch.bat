@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   Second-Hand Trading Platform Launcher
echo ========================================
echo.

:: Get script directory
set "SCRIPT_DIR=%~dp0"

:: Check for JAR
if exist "%SCRIPT_DIR%3343-Project.jar" (
    set "JAR_FILE=%SCRIPT_DIR%3343-Project.jar"
) else if exist "%SCRIPT_DIR%3343-Project-1.0-SNAPSHOT-all.jar" (
    set "JAR_FILE=%SCRIPT_DIR%3343-Project-1.0-SNAPSHOT-all.jar"
) else (
    echo [ERROR] Cannot find JAR file!
    pause
    exit /b 1
)

echo [INFO] Found JAR: %JAR_FILE%

:: Try common Java 21+ locations
set "JAVA_EXE="

:: Check Adoptium JDK 23
if exist "C:\Program Files\Eclipse Adoptium\jdk-23.0.2.7-hotspot\bin\java.exe" (
    set "JAVA_EXE=C:\Program Files\Eclipse Adoptium\jdk-23.0.2.7-hotspot\bin\java.exe"
    goto :found_java
)

:: Check Adoptium JDK 25
if exist "C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\java.exe" (
    set "JAVA_EXE=C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin\java.exe"
    goto :found_java
)

:: Check JAVA_HOME
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
        goto :found_java
    )
)

:: Check PATH
where java >nul 2>&1
if %errorlevel%==0 (
    for /f "tokens=*" %%i in ('where java 2^>nul') do (
        set "JAVA_EXE=%%i"
        goto :found_java
    )
)

:: Java not found
echo.
echo [ERROR] Java 21 or higher is required!
echo.
echo Please download and install Java 21 from:
echo   https://adoptium.net/temurin/releases/?version=21
echo.
pause
exit /b 1

:found_java
echo [INFO] Using Java: %JAVA_EXE%
echo.
echo [INFO] Starting application...
echo ========================================
echo.

:: Run with Launcher if available (for JavaFX compatibility)
cd /d "%SCRIPT_DIR%"
if exist "Launcher.class" (
    "%JAVA_EXE%" -Dfile.encoding=UTF-8 -cp ".;3343-Project.jar" Launcher
) else (
    "%JAVA_EXE%" -Dfile.encoding=UTF-8 -jar "%JAR_FILE%"
)

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Application exited with error code %errorlevel%
    pause
)
