@echo off
chcp 65001 >nul
title GitHub Actions自动上传脚本

echo ================================
echo GitHub Actions自动上传脚本
echo ================================
echo.

echo 📋 检查项目文件...
echo.

:: 检查必要文件
echo [1/6] 检查项目结构...
if not exist "app\build.gradle" (
    echo ❌ 缺少 app\build.gradle
    echo 请确保在正确的项目目录中运行
    pause
    exit /b 1
)

if not exist ".github\workflows\build.yml" (
    echo ❌ 缺少 .github\workflows\build.yml
    echo GitHub Actions配置文件不存在
    pause
    exit /b 1
)

echo ✅ 项目结构完整

:: 检查Git
echo [2/6] 检查Git环境...
git --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 未检测到Git
    echo 请安装Git：https://git-scm.com/
    pause
    exit /b 1
) else (
    echo ✅ Git环境正常
)

:: 获取GitHub用户名
echo [3/6] 配置GitHub信息...
echo.
echo 请输入您的GitHub信息：
set /p github_user=GitHub用户名: 
if "%github_user%"=="" (
    echo ❌ 用户名不能为空
    pause
    exit /b 1
)

:: 初始化Git仓库
echo [4/6] 初始化Git仓库...
if not exist ".git" (
    git init
    echo ✅ Git仓库初始化完成
) else (
    echo ✅ Git仓库已存在
)

:: 配置Git用户信息
git config user.name "%github_user%"
git config user.email "%github_user%@users.noreply.github.com"

:: 添加文件
echo [5/6] 添加项目文件...
git add .
if errorlevel 1 (
    echo ❌ 添加文件失败
    pause
    exit /b 1
)

:: 提交文件
echo [6/6] 提交更改...
git commit -m "陌陌AI自动回复系统 - 完整项目代码"

:: 检查远程仓库
git remote get-url origin >nul 2>&1
if errorlevel 1 (
    echo.
    echo 🔗 添加远程仓库...
    git remote add origin https://github.com/wuchang187/momo-auto-reply.git
    echo ✅ 远程仓库添加完成
) else (
    echo ✅ 远程仓库已存在
)

:: 推送到GitHub
echo.
echo 🚀 推送到GitHub...
git branch -M main
git push -u origin main

if errorlevel 1 (
    echo.
    echo ❌ 推送失败，可能原因：
    echo 1. 仓库不存在，请先在GitHub创建仓库
    echo 2. 网络连接问题
    echo 3. 认证失败
    echo.
    echo 手动创建仓库步骤：
    echo 1. 访问 https://github.com/new
    echo 2. 仓库名：momo-auto-reply
    echo 3. 设为Public
    echo 4. 不要勾选"Add a README file"
    echo 5. 点击"Create repository"
    echo 6. 然后重新运行此脚本
    echo.
    pause
    exit /b 1
) else (
    echo.
    echo ✅ 推送成功！
    echo.
    echo 🎉 项目已成功推送到GitHub！
    echo.
    echo 📋 下一步操作：
    echo 1. 访问 https://github.com/wuchang187/momo-auto-reply
    echo 2. 点击"Actions"标签页
    echo 3. 等待构建完成（通常3-5分钟）
    echo 4. 下载生成的app-debug.apk
    echo 5. 安装到手机进行测试
    echo.
    echo 📱 构建状态监控：
    echo https://github.com/wuchang187/momo-auto-reply/actions
    echo.
    echo 🔗 直接下载APK（构建完成后）：
    echo https://github.com/wuchang187/momo-auto-reply/actions/runs
)

echo.
pause