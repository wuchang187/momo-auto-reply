# Android Studio 编译指南

## 项目状态检查

您的陌陌AI自动回复项目已经配置完整，所有必要的文件都已创建：

✅ **已完成的配置文件**：
- `build.gradle` (项目级别)
- `settings.gradle` 
- `gradle.properties`
- `app/build.gradle` (应用级别)
- `AndroidManifest.xml`

✅ **已完成的源代码**：
- MainActivity.java (主界面)
- MomoAccessibilityService.java (无障碍服务)
- ConversationManager.java (对话管理)
- AIReplyGenerator.java (AI回复生成)
- BootCompleteReceiver.java (开机自启动)

✅ **已完成的资源文件**：
- 布局文件、字符串资源、颜色样式等

## 在Android Studio中编译步骤

### 步骤1：打开Android Studio
1. 启动Android Studio
2. 选择 "Open an Existing Project"
3. 浏览到 `h:\AI临时\momo_auto_reply` 文件夹
4. 选择该文件夹并点击 "OK"

### 步骤2：项目同步
1. Android Studio会自动检测Gradle配置
2. 如果出现同步提示，点击 "Sync Now"
3. 等待Gradle下载依赖并同步完成

### 步骤3：配置SDK（如果需要）
1. 打开 File → Project Structure (Ctrl+Alt+Shift+S)
2. 在 "Project" 选项卡中确认：
   - Project SDK: Android API 34
   - Project language level: 8
3. 在 "Modules" → "app" 选项卡中确认：
   - Compile Sdk Version: 34
   - Build Tools Version: 最新版本

### 步骤4：编译项目
1. 选择菜单 Build → Make Project (Ctrl+F9)
2. 或者使用 Build → Build Bundle(s) / APK(s) → Build APK(s)

### 步骤5：运行应用
1. **连接Android设备**：
   - 开启USB调试
   - 在设备上授权调试
   - 或使用Android模拟器

2. **安装和运行**：
   - 点击运行按钮 (绿色三角形)
   - 选择目标设备
   - 应用会自动安装和启动

## 预期结果

成功编译后，您将获得：
- APK文件：`app/build/outputs/apk/debug/app-debug.apk`
- 应用安装包，可以在Android设备上安装和使用

## 权限配置

应用需要以下权限（在AndroidManifest.xml中已配置）：
- `android.permission.INTERNET` (网络访问)
- `android.permission.BIND_ACCESSIBILITY_SERVICE` (无障碍服务)

## 后续步骤

1. **安装应用**：在Android设备上安装编译生成的APK
2. **开启无障碍服务**：
   - 进入设置 → 辅助功能 → 陌陌AI自动回复
   - 开启无障碍服务
3. **配置AI API**：根据使用指南配置AI接口密钥
4. **测试功能**：在陌陌中测试自动回复功能

## 常见问题解决

### 问题1：Gradle同步失败
**解决方案**：
- 检查网络连接
- 使用国内镜像源
- 重新导入项目

### 问题2：SDK版本不匹配
**解决方案**：
- 安装或更新Android SDK 34
- 在SDK Manager中下载必要的组件

### 问题3：依赖下载失败
**解决方案**：
- 配置代理或使用国内镜像
- 手动下载依赖到本地

### 问题4：编译错误
**解决方案**：
- 检查Java代码语法
- 确认所有资源文件存在
- 验证AndroidManifest.xml配置

## 技术规格

- **最低Android版本**：API 26 (Android 8.0)
- **目标Android版本**：API 34 (Android 14)
- **Java版本**：8
- **支持架构**：arm64-v8a, armeabi-v7a

## 联系支持

如果在编译过程中遇到问题，请提供：
1. 具体的错误信息
2. Android Studio版本
3. 操作系统版本
4. 设备型号和Android版本

---

**注意**：此应用为学习研究目的开发，请遵守相关应用的使用条款和法律法规。