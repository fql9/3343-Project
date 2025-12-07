@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo   Second-Hand Trading Platform Launcher
echo ========================================
echo.

:: Get script directory
set "SCRIPT_DIR=%~dp0"
set "JAR_FILE=%SCRIPT_DIR%build\libs\3343-Project-1.0-SNAPSHOT-all.jar"

:: Check if JAR exists in build/libs
if not exist "%JAR_FILE%" (
    set "JAR_FILE=%SCRIPT_DIR%3343-Project-1.0-SNAPSHOT-all.jar"
)

:: Check if JAR exists in current directory
if not exist "%JAR_FILE%" (
    echo [ERROR] Cannot find JAR file!
    echo Expected locations:
    echo   - %SCRIPT_DIR%build\libs\3343-Project-1.0-SNAPSHOT-all.jar
    echo   - %SCRIPT_DIR%3343-Project-1.0-SNAPSHOT-all.jar
    echo.
    pause
    exit /b 1
)

echo [INFO] Found JAR: %JAR_FILE%
echo.

:: Initialize JAVA_EXE
set "JAVA_EXE="
set "JAVA_VERSION=0"

:: Check 1: JAVA_HOME environment variable
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        call :check_java_version "%JAVA_HOME%\bin\java.exe"
        if !JAVA_VERSION! GEQ 21 (
            set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
            echo [INFO] Using JAVA_HOME: !JAVA_EXE! ^(Java !JAVA_VERSION!^)
            goto :run_app
        )
    )
)

:: Check 2: java in PATH
where java >nul 2>&1
if %errorlevel%==0 (
    for /f "tokens=*" %%i in ('where java') do (
        call :check_java_version "%%i"
        if !JAVA_VERSION! GEQ 21 (
            set "JAVA_EXE=%%i"
            echo [INFO] Using PATH java: !JAVA_EXE! ^(Java !JAVA_VERSION!^)
            goto :run_app
        )
    )
)

:: Check 3: Common installation paths
echo [INFO] Searching for Java 21+ in common locations...

set "SEARCH_PATHS="
set "SEARCH_PATHS=!SEARCH_PATHS! C:\Program Files\Eclipse Adoptium"
set "SEARCH_PATHS=!SEARCH_PATHS! C:\Program Files\Java"
set "SEARCH_PATHS=!SEARCH_PATHS! C:\Program Files\Microsoft"
set "SEARCH_PATHS=!SEARCH_PATHS! C:\Program Files\Zulu"
set "SEARCH_PATHS=!SEARCH_PATHS! C:\Program Files\BellSoft"
set "SEARCH_PATHS=!SEARCH_PATHS! C:\Program Files\Amazon Corretto"

for %%p in (!SEARCH_PATHS!) do (
    if exist "%%p" (
        for /d %%d in ("%%p\*") do (
            if exist "%%d\bin\java.exe" (
                call :check_java_version "%%d\bin\java.exe"
                if !JAVA_VERSION! GEQ 21 (
                    set "JAVA_EXE=%%d\bin\java.exe"
                    echo [INFO] Found: !JAVA_EXE! ^(Java !JAVA_VERSION!^)
                    goto :run_app
                )
            )
        )
    )
)

:: No suitable Java found
echo.
echo ========================================
echo [ERROR] Java 21 or higher is required!
echo ========================================
echo.
echo Your current Java version is not compatible.
echo Please download and install Java 21 from:
echo.
echo   https://adoptium.net/temurin/releases/?version=21
echo.
echo After installation, run this script again.
echo.
pause
exit /b 1

:run_app
echo.
echo [INFO] Starting application...
echo ========================================
echo.

:: Run the application
"%JAVA_EXE%" -Dfile.encoding=UTF-8 -jar "%JAR_FILE%"

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Application exited with error code %errorlevel%
    pause
)
exit /b 0

:check_java_version
:: Extract Java version from java -version output
set "JAVA_VERSION=0"
for /f "tokens=3" %%v in ('"%~1" -version 2^>^&1 ^| findstr /i "version"') do (
    set "ver=%%v"
    set "ver=!ver:"=!"
    for /f "tokens=1 delims=." %%a in ("!ver!") do (
        set "JAVA_VERSION=%%a"
    )
)
exit /b 0

