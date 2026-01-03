package com.momoautoreply;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * 陌陌消息读取无障碍服务示例
 * 功能：直接读取陌陌聊天消息，无需OCR，速度快
 * 说明：
 * 1. 该服务需要在AndroidManifest.xml中注册
 * 2. 需要在系统设置中开启无障碍服务
 * 3. 支持监听陌陌通知和聊天界面变化
 */
public class MomoAccessibilityService extends AccessibilityService {
    
    private static final String TAG = "MomoAccessibilityService";
    private static final String MOMO_PACKAGE_NAME = "com.immomo.momo";
    
    // 陌陌聊天相关控件ID（需实际验证，可能因版本不同而变化）
    private static final String MOMO_MESSAGE_TEXT_ID = "com.immomo.momo:id/message_text";
    private static final String MOMO_CHAT_TITLE_ID = "com.immomo.momo:id/chat_title";
    private static final String MOMO_INPUT_ID = "com.immomo.momo:id/chat_input";
    private static final String MOMO_SEND_BUTTON_ID = "com.immomo.momo:id/send_button";
    
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "陌陌无障碍服务已连接");
        
        // 配置服务信息
        AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        serviceInfo.flags = AccessibilityServiceInfo.FLAG_DEFAULT;
        serviceInfo.packageNames = new String[]{MOMO_PACKAGE_NAME};
        serviceInfo.notificationTimeout = 100;
        serviceInfo.canRetrieveWindowContent = true;
        
        setServiceInfo(serviceInfo);
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        String packageName = event.getPackageName().toString();
        
        // 只处理陌陌应用的事件
        if (!packageName.equals(MOMO_PACKAGE_NAME)) {
            return;
        }
        
        Log.d(TAG, "收到陌陌事件: " + eventTypeToString(eventType));
        
        // 处理不同类型的事件
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                // 处理通知事件（新消息通知）
                handleNotificationEvent(event);
                break;
                
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                // 处理聊天界面变化事件
                handleChatEvent();
                break;
                
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                // 处理文本变化事件（输入框内容变化）
                handleTextChangedEvent(event);
                break;
                
            default:
                Log.d(TAG, "未处理的事件类型: " + eventType);
                break;
        }
    }
    
    @Override
    public void onInterrupt() {
        Log.d(TAG, "陌陌无障碍服务已中断");
    }
    
    /**
     * 处理通知事件
     */
    private void handleNotificationEvent(AccessibilityEvent event) {
        Log.d(TAG, "处理陌陌通知事件");
        
        // 从通知中获取消息内容
        List<CharSequence> texts = event.getText();
        if (texts != null && !texts.isEmpty()) {
            for (CharSequence text : texts) {
                Log.d(TAG, "通知内容: " + text.toString());
                // 解析通知内容，提取发送者和消息
                parseNotificationContent(text.toString());
            }
        }
    }
    
    /**
     * 处理聊天界面事件
     */
    private void handleChatEvent() {
        Log.d(TAG, "处理陌陌聊天界面事件");
        
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "根节点为空，无法获取聊天内容");
            return;
        }
        
        // 1. 获取当前聊天用户
        String currentUser = getCurrentChatUser(rootNode);
        Log.d(TAG, "当前聊天用户: " + currentUser);
        
        // 2. 获取聊天消息列表
        List<AccessibilityNodeInfo> messageNodes = rootNode.findAccessibilityNodeInfosByViewId(MOMO_MESSAGE_TEXT_ID);
        if (messageNodes != null && !messageNodes.isEmpty()) {
            Log.d(TAG, "找到 " + messageNodes.size() + " 条消息");
            
            // 3. 遍历消息，获取内容
            for (int i = 0; i < messageNodes.size(); i++) {
                AccessibilityNodeInfo node = messageNodes.get(i);
                if (node != null && node.getText() != null) {
                    String message = node.getText().toString();
                    String sender = getMessageSender(node, currentUser);
                    Log.d(TAG, "消息 " + (i + 1) + ": " + sender + ": " + message);
                    
                    // 4. 处理消息（可以发送到AI服务生成回复）
                    handleMessage(currentUser, message);
                }
            }
        } else {
            Log.d(TAG, "未找到消息控件");
        }
    }
    
    /**
     * 处理文本变化事件
     */
    private void handleTextChangedEvent(AccessibilityEvent event) {
        Log.d(TAG, "处理文本变化事件");
        // 可以用于监听输入框内容变化
    }
    
    /**
     * 获取当前聊天用户
     */
    private String getCurrentChatUser(AccessibilityNodeInfo rootNode) {
        List<AccessibilityNodeInfo> titleNodes = rootNode.findAccessibilityNodeInfosByViewId(MOMO_CHAT_TITLE_ID);
        if (titleNodes != null && !titleNodes.isEmpty()) {
            AccessibilityNodeInfo titleNode = titleNodes.get(0);
            if (titleNode != null && titleNode.getText() != null) {
                return titleNode.getText().toString();
            }
        }
        return "未知用户";
    }
    
    /**
     * 获取消息发送者
     */
    private String getMessageSender(AccessibilityNodeInfo messageNode, String currentUser) {
        // 这里需要根据陌陌聊天界面的结构来判断消息发送者
        // 通常可以通过消息气泡的位置、颜色或父控件属性来判断
        // 示例：假设左侧消息是对方发送，右侧是自己发送
        
        // 简单实现：获取消息节点的父控件，判断其布局属性
        AccessibilityNodeInfo parent = messageNode.getParent();
        if (parent != null) {
            // 根据实际情况实现发送者判断逻辑
            // 这里返回示例值
            return "对方";
        }
        return currentUser;
    }
    
    /**
     * 处理收到的消息
     */
    private void handleMessage(String user, String message) {
        Log.d(TAG, "处理消息 - 用户: " + user + ", 内容: " + message);
        
        // 这里可以实现：
        // 1. 将消息发送到AI服务生成回复
        // 2. 调用sendReply()方法发送回复
        // 3. 保存对话历史
        
        // 示例：简单回复
        // sendReply("你好，我是自动回复助手，收到消息: " + message);
    }
    
    /**
     * 发送回复消息
     */
    public void sendReply(String replyContent) {
        Log.d(TAG, "发送回复: " + replyContent);
        
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "根节点为空，无法发送回复");
            return;
        }
        
        // 1. 查找输入框
        List<AccessibilityNodeInfo> inputNodes = rootNode.findAccessibilityNodeInfosByViewId(MOMO_INPUT_ID);
        if (inputNodes != null && !inputNodes.isEmpty()) {
            AccessibilityNodeInfo inputNode = inputNodes.get(0);
            
            // 2. 输入回复内容
            inputNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            // 使用ACTION_SET_TEXT设置文本
            android.os.Bundle arguments = new android.os.Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, replyContent);
            inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
            
            // 3. 查找发送按钮
            List<AccessibilityNodeInfo> sendNodes = rootNode.findAccessibilityNodeInfosByViewId(MOMO_SEND_BUTTON_ID);
            if (sendNodes != null && !sendNodes.isEmpty()) {
                // 4. 点击发送按钮
                sendNodes.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.d(TAG, "回复已发送");
            } else {
                Log.e(TAG, "未找到发送按钮");
            }
        } else {
            Log.e(TAG, "未找到输入框");
        }
    }
    
    /**
     * 解析通知内容
     */
    private void parseNotificationContent(String notification) {
        Log.d(TAG, "解析通知: " + notification);
        // 示例：假设通知格式为 "用户名: 消息内容"
        if (notification.contains(":")) {
            String[] parts = notification.split(":", 2);
            if (parts.length == 2) {
                String user = parts[0].trim();
                String message = parts[1].trim();
                Log.d(TAG, "从通知中解析到 - 用户: " + user + ", 消息: " + message);
            }
        }
    }
    
    /**
     * 将事件类型转换为字符串
     */
    private String eventTypeToString(int eventType) {
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED: return "通知事件";
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: return "窗口内容变化";
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: return "窗口状态变化";
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED: return "文本变化";
            case AccessibilityEvent.TYPE_VIEW_CLICKED: return "视图点击";
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED: return "视图长按";
            default: return "未知事件 (" + eventType + ")";
        }
    }
}


/**
 * AndroidManifest.xml配置示例
 * 需要在AndroidManifest.xml中添加以下配置：
 */
/*
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.momoautoreply">
    
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MomoAutoReply">
        
        <!-- 注册无障碍服务 -->
        <service
            android:name=".MomoAccessibilityService"
            android:enabled="true"
            android:exported="true"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            
            <!-- 无障碍服务配置 -->
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_config" />
        </service>
        
        <!-- 主Activity -->
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
    
</manifest>
*/


/**
 * accessibility_service_config.xml配置示例
 * 需要在res/xml目录下创建该文件
 */
/*
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFeedbackType="feedbackAllMask"
    android:accessibilityFlags="flagDefault"
    android:canRetrieveWindowContent="true"
    android:description="@string/accessibility_service_description"
    android:notificationTimeout="100"
    android:packageNames="com.immomo.momo" />
*/
