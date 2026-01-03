@echo off
chcp 65001 >nul
title 陌陌AI自动回复 - Android项目编译工具

echo ================================
echo 陌陌AI自动回复 - Android项目编译工具
echo ================================
echo.

:: 检查Java环境
echo [1/5] 检查Java环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 未检测到Java环境
    echo 请先安装JDK 8或更高版本
    echo 下载地址：https://adoptium.net/
    echo.
    pause
    exit /b 1
) else (
    echo ✅ Java环境正常
)

:: 检查Android SDK
echo [2/5] 检查Android SDK...
if not defined ANDROID_HOME (
    echo ❌ 未设置ANDROID_HOME环境变量
    echo 请先安装Android SDK并设置环境变量
    echo.
    echo 推荐方案：
    echo 1. 下载Android Studio（包含完整SDK）
    echo 2. 下载Command Line Tools并安装SDK
    echo.
    echo 设置方法：
    echo ANDROID_HOME=C:\Users\%USERNAME%\AppData\Local\Android\Sdk
    echo.
    pause
    exit /b 1
) else (
    echo ✅ Android SDK环境正常：%ANDROID_HOME%
)

:: 检查Gradle
echo [3/5] 检查Gradle...
gradle -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 未检测到Gradle
    echo 正在尝试使用项目的Gradle Wrapper...
    if exist gradlew.bat (
        echo ✅ 找到Gradle Wrapper
    ) else (
        echo ❌ 未找到Gradle Wrapper
        echo 请安装Gradle或下载项目完整版本
        echo 下载地址：https://gradle.org/releases/
        pause
        exit /b 1
    )
) else (
    echo ✅ Gradle环境正常
)

:: 切换到项目目录
echo [4/5] 准备编译...
cd /d "%~dp0"

:: 检查项目结构
if not exist "app\build.gradle" (
    echo ❌ 项目结构不完整
    echo 未找到app/build.gradle文件
    pause
    exit /b 1
)

echo ✅ 项目结构正常
echo.

:: 开始编译
echo [5/5] 开始编译项目...
echo 正在编译Debug版本...
echo.

if exist gradlew.bat (
    gradlew.bat assembleDebug
) else (
    gradle assembleDebug
)

:: 检查编译结果
if errorlevel 1 (
    echo.
    echo ❌ 编译失败！
    echo 请检查上述错误信息
    echo.
    echo 常见解决方案：
    echo 1. 检查网络连接（需要下载依赖）
    echo 2. 更新Android SDK
    echo 3. 检查项目配置文件
    echo.
) else (
    echo.
    echo ✅ 编译成功！
    echo APK文件位置：app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo 接下来可以：
    echo 1. 将APK安装到Android设备
    echo 2. 在设备上开启无障碍服务
    echo 3. 配置AI API密钥
    echo.
)

echo 按任意键退出...
pause >nul