# 陌陌自动回复

一个基于Android无障碍服务的陌陌消息自动回复应用，支持多用户对话管理和AI回复生成。

## 功能特点

1. **无需OCR，直接读取消息**：利用Android无障碍服务直接读取聊天消息，速度快，准确率高
2. **支持多用户对话**：可以同时处理多个用户的聊天消息，每个用户有独立的对话历史
3. **AI回复生成**：支持多种AI模型，包括OpenAI、百度文心一言、讯飞星火等
4. **对话历史管理**：自动保存对话历史，支持角色设定和上下文理解
5. **智能回复**：根据对话上下文和角色设定生成自然流畅的回复
6. **自动发送回复**：无需手动操作，自动发送生成的回复
7. **服务状态监控**：实时监控无障碍服务状态，引导用户开启服务
8. **设备启动自动检查**：设备重启后自动检查服务状态，提醒用户开启服务

## 技术栈

- **开发语言**：Java
- **开发框架**：Android SDK
- **核心技术**：Android无障碍服务（AccessibilityService）
- **数据存储**：SQLite数据库
- **AI模型**：支持多种AI模型API
- **并发处理**：Java线程池

## 项目结构

```
├── app/src/main/
│   ├── java/com/momoautoreply/
│   │   ├── MomoAccessibilityService.java    # 无障碍服务类
│   │   ├── ConversationManager.java         # 对话管理类
│   │   ├── AIReplyGenerator.java            # AI回复生成器
│   │   ├── MainActivity.java                # 主界面
│   │   └── BootCompleteReceiver.java        # 启动完成广播接收器
│   ├── res/
│   │   ├── drawable/                        # 图片资源
│   │   ├── layout/                          # 布局文件
│   │   │   └── activity_main.xml             # 主界面布局
│   │   ├── values/                           # 资源文件
│   │   │   ├── strings.xml                   # 字符串资源
│   │   │   ├── colors.xml                   # 颜色资源
│   │   │   └── styles.xml                    # 样式资源
│   │   └── xml/                              # XML配置文件
│   │       └── accessibility_service_config.xml  # 无障碍服务配置
│   └── AndroidManifest.xml                   # 应用配置
└── README.md                                 # 项目说明
```

## 安装方法

### 方法一：直接安装APK

1. 下载最新的APK文件
2. 在Android设备上允许安装未知来源应用
3. 点击APK文件进行安装

### 方法二：使用Android Studio编译

1. 克隆或下载项目源码
2. 使用Android Studio打开项目
3. 连接Android设备或启动模拟器
4. 点击Run按钮编译并安装应用

## 使用步骤

1. **安装应用**：按照上述安装方法安装应用
2. **开启无障碍服务**：
   - 打开应用，点击"开启无障碍服务"按钮
   - 在无障碍服务列表中找到"陌陌自动回复"并开启
   - 返回到应用，确认服务已开启
3. **配置AI模型**：
   - 目前默认使用OpenAI模型，需要在`AIReplyGenerator.java`中修改API密钥
   - 支持切换到其他AI模型，如百度文心一言、讯飞星火等
4. **打开陌陌聊天**：
   - 打开陌陌应用
   - 进入聊天界面
   - 应用会自动读取消息并生成回复

## 配置说明

### AI模型配置

在`AIReplyGenerator.java`中修改`ModelConfig`类的配置：

```java
// OpenAI配置
String openaiApiKey = "your_openai_api_key"; // 替换为实际的API密钥
String openaiModel = "gpt-3.5-turbo";
String openaiApiUrl = "https://api.openai.com/v1/chat/completions";

// 百度文心一言配置
String baiduApiKey = "your_baidu_api_key"; // 替换为实际的API密钥
String baiduSecretKey = "your_baidu_secret_key"; // 替换为实际的密钥
String baiduApiUrl = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions";
```

### 角色设定配置

在`ConversationManager.java`中修改默认角色设定：

```java
// 默认角色设定
private static final String DEFAULT_ROLE_SETTING = "你是一个友好、亲切、自然的聊天助手，擅长和人轻松愉快地交流。请使用简洁明了的语言回复，避免过于复杂的表达。";
```

### 陌陌控件ID配置

在`MomoAccessibilityService.java`中修改陌陌聊天相关的控件ID：

```java
// 陌陌聊天相关控件ID（需根据实际版本调整）
private static final String MOMO_MESSAGE_TEXT_ID = "com.immomo.momo:id/message_text";
private static final String MOMO_CHAT_TITLE_ID = "com.immomo.momo:id/chat_title";
private static final String MOMO_INPUT_ID = "com.immomo.momo:id/chat_input";
private static final String MOMO_SEND_BUTTON_ID = "com.immomo.momo:id/send_button";
private static final String MOMO_USER_AVATAR_ID = "com.immomo.momo:id/user_avatar";
```

## 注意事项

1. **无障碍服务权限**：
   - 应用需要无障碍服务权限才能正常工作
   - 首次使用需要手动开启无障碍服务
   - 设备重启后可能需要重新开启服务

2. **陌陌版本兼容性**：
   - 不同版本的陌陌可能使用不同的控件ID
   - 需要根据实际使用的陌陌版本调整控件ID
   - 建议使用最新版本的陌陌

3. **AI模型API密钥**：
   - 需要自行申请AI模型的API密钥
   - 不同AI模型的API调用方式可能不同，需要根据实际情况调整代码

4. **对话历史存储**：
   - 对话历史存储在SQLite数据库中
   - 默认清理超过7天未活跃的对话历史
   - 可以在`BootCompleteReceiver.java`中调整清理时间

5. **性能和电量**：
   - 无障碍服务会持续监听设备事件，可能会增加电量消耗
   - 建议在需要时开启服务，不需要时关闭服务

## 常见问题

### Q：应用无法读取消息怎么办？
A：请检查以下几点：
1. 确认无障碍服务已开启
2. 确认陌陌版本与应用兼容
3. 确认控件ID配置正确
4. 尝试重启应用和陌陌

### Q：AI回复生成失败怎么办？
A：请检查以下几点：
1. 确认AI模型API密钥配置正确
2. 确认网络连接正常
3. 尝试切换到其他AI模型
4. 检查日志信息，查看具体错误原因

### Q：应用被陌陌检测怎么办？
A：为了避免被检测，建议：
1. 不要过度使用自动回复功能
2. 适当调整回复间隔，模拟真实人类聊天
3. 不要在短时间内发送大量回复
4. 定期更新应用，适应陌陌的反自动化机制

## 开发说明

### 开发环境

- **Android Studio**：Arctic Fox 或更高版本
- **Android SDK**：API 26 或更高版本
- **Java Development Kit**：JDK 8 或更高版本

### 构建命令

```bash
# 编译项目
./gradlew build

# 安装Debug版本
./gradlew installDebug

# 生成Release版本
./gradlew assembleRelease
```

## 贡献指南

欢迎提交Issue和Pull Request，共同改进应用功能。

### 提交Pull Request前请确保：
1. 代码编译通过，没有语法错误
2. 功能正常，没有崩溃或异常
3. 代码风格统一，符合Java代码规范
4. 提交的代码有明确的功能说明

## 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 免责声明

1. 本应用仅供学习和研究使用，请勿用于商业用途
2. 使用本应用可能违反陌陌的用户协议，使用前请仔细阅读陌陌的相关规定
3. 作者不对使用本应用造成的任何后果负责
4. 请合理使用本应用，避免对他人造成骚扰

## 更新日志

### 版本 1.0.0
- 首次发布
- 实现了无障碍服务监听陌陌消息
- 支持多用户对话管理
- 集成了AI回复生成功能
- 实现了自动发送回复
- 支持服务状态监控和引导
- 实现了设备启动自动检查

## 联系方式

如有问题或建议，欢迎通过以下方式联系：

- GitHub Issues：https://github.com/yourusername/momo-auto-reply/issues
- 电子邮件：your.email@example.com

---

**使用本应用即表示您同意上述条款和免责声明**
