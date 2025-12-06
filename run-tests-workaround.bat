@echo off
REM 临时测试脚本 - 绕过文件锁定问题
REM 将此文件保存为 run-tests-workaround.bat

echo ================================================
echo   Modified Top-Down 测试运行脚本
echo   临时绕过OneDrive文件锁定问题
echo ================================================
echo.

REM 停止所有Gradle守护进程
echo [1/4] 停止Gradle守护进程...
call gradlew.bat --stop
timeout /t 3 /nobreak >nul

REM 尝试重命名build目录而不是删除
echo [2/4] 重命名旧的build目录...
if exist build (
    ren build build.old.%RANDOM%
)
timeout /t 2 /nobreak >nul

REM 运行测试（使用--no-build-cache避免使用缓存）
echo [3/4] 运行测试...
echo.
call gradlew.bat test --no-build-cache

REM 检查结果
echo.
echo [4/4] 测试完成！
echo ================================================
if %ERRORLEVEL% EQU 0 (
    echo   ✓ BUILD SUCCESSFUL
    echo   请查看测试报告: build\reports\tests\test\index.html
) else (
    echo   ✗ BUILD FAILED
    echo   请查看上面的错误信息
)
echo ================================================
echo.

REM 清理旧的build目录（异步，不影响当前操作）
echo 提示: 可以手动删除 build.old.* 目录以释放空间
echo.

pause

