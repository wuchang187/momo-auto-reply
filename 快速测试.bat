@echo off
chcp 65001 >nul
title 快速真机测试 - 陌陌AI自动回复

echo ================================
echo 陌陌AI自动回复 - 快速真机测试
echo ================================
echo.

echo 📱 真机测试方案选择：
echo.
echo [1] 使用GitHub Actions在线构建（推荐）
echo [2] 使用在线APK构建网站  
echo [3] 创建手动测试版本
echo [4] 检查现有测试文件
echo.

set /p choice=请选择方案 (1-4): 

if "%choice%"=="1" goto github_actions
if "%choice%"=="2" goto online_build
if "%choice%"=="3" goto manual_build
if "%choice%"=="4" goto check_files

:github_actions
echo.
echo 🚀 GitHub Actions在线构建方案
echo.
echo 步骤：
echo 1. 在GitHub创建新仓库"momo-auto-reply"
echo 2. 上传所有项目文件
echo 3. 推送代码后，GitHub会自动构建APK
echo 4. 在Actions页面下载app-debug.apk
echo 5. 将APK传输到手机安装
echo.
echo 需要上传的文件：
echo ✅ app\ (完整Android项目)
echo ✅ gradle\ (Gradle配置)
echo ✅ build.gradle (构建配置)
echo ✅ settings.gradle (项目设置)
echo ✅ .github\workflows\build.yml (已创建)
echo.
echo 🌐 在线构建网站备选：
echo - https://www.apkonline.net/
echo - https://buildapk.online/
echo - http://appinventor.mit.edu/
echo.
pause
exit /b 0

:online_build
echo.
echo 🌐 在线APK构建网站
echo.
echo 推荐网站：
echo 1. ApkOnline: https://www.apkonline.net/
echo 2. Build APK Online: https://buildapk.online/
echo 3. MIT App Inventor: http://appinventor.mit.edu/
echo.
echo 使用步骤：
echo 1. 压缩整个项目文件夹为ZIP
echo 2. 上传到构建网站
echo 3. 选择Android版本和配置
echo 4. 等待构建完成
echo 5. 下载生成的APK文件
echo.
echo ⚠️ 注意：免费版本可能有功能限制
echo.
pause
exit /b 0

:manual_build
echo.
echo 🔧 手动创建测试版本
echo.
echo 由于网络问题，我们创建简化版本：
echo 1. 基础AndroidManifest.xml
echo 2. 简化的MainActivity
echo 3. 基本的无障碍服务配置
echo.
echo 这个版本将提供：
echo ✅ 应用启动界面
echo ✅ 基础权限管理
echo ✅ 无障碍服务开启
echo ✅ 简单的消息处理框架
echo.
set /p create=是否创建手动测试版本? (y/n): 
if /i "%create%"=="y" goto create_manual
goto end

:create_manual
echo 创建手动测试版本...
echo 这将创建基础的APK结构供测试使用
echo 详细说明请查看：快速真机测试方案.md
pause
goto end

:check_files
echo.
echo 📁 检查项目文件
echo.
echo 项目结构：
dir /s /b *.java *.xml *.gradle 2>nul | findstr "Momo\|Main\|build" 
echo.
echo 主要文件：
if exist "app\src\main\AndroidManifest.xml" echo ✅ AndroidManifest.xml
if exist "app\src\main\java\com\momoautoreply\MainActivity.java" echo ✅ MainActivity.java  
if exist "app\src\main\java\com\momoautoreply\MomoAccessibilityService.java" echo ✅ MomoAccessibilityService.java
if exist "app\build.gradle" echo ✅ app\build.gradle
if exist "build.gradle" echo ✅ build.gradle
echo.
echo 下一步建议：
echo 1. 确保所有文件完整
echo 2. 选择合适的构建方案
echo 3. 按照对应指南操作
echo.
pause
goto end

:end
echo.
echo ================================
echo 测试完成！请选择最适合的方案进行真机测试。
echo ================================
pause