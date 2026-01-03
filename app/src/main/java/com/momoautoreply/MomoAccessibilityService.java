package com.momoautoreply;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 陌陌消息自动回复无障碍服务
 * 功能：
 * 1. 监听陌陌聊天界面变化
 * 2. 读取聊天消息内容
 * 3. 识别消息发送者
 * 4. 生成AI回复
 * 5. 自动发送回复
 * 6. 管理多用户对话历史
 */
public class MomoAccessibilityService extends AccessibilityService {
    
    private static final String TAG = "MomoAccessibilityService";
    private static final String MOMO_PACKAGE_NAME = "com.immomo.momo";
    
    // 陌陌聊天相关控件ID（需根据实际版本调整）
    private static final String MOMO_MESSAGE_TEXT_ID = "com.immomo.momo:id/message_text";
    private static final String MOMO_CHAT_TITLE_ID = "com.immomo.momo:id/chat_title";
    private static final String MOMO_INPUT_ID = "com.immomo.momo:id/chat_input";
    private static final String MOMO_SEND_BUTTON_ID = "com.immomo.momo:id/send_button";
    private static final String MOMO_USER_AVATAR_ID = "com.immomo.momo:id/user_avatar";
    
    // 对话管理器，用于管理多个用户的对话历史
    private ConversationManager conversationManager;
    // AI回复生成器
    private AIReplyGenerator aiReplyGenerator;
    // 线程池，用于异步处理消息和生成回复
    private ExecutorService executorService;
    // 当前处理的消息缓存，避免重复处理
    private Map<String, String> lastMessageCache;
    
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "陌陌无障碍服务已连接");
        
        // 初始化组件
        conversationManager = new ConversationManager(this);
        aiReplyGenerator = new AIReplyGenerator(this);
        executorService = Executors.newFixedThreadPool(5);
        lastMessageCache = new HashMap<>();
        
        // 配置服务信息
        configureServiceInfo();
    }
    
    /**
     * 配置无障碍服务信息
     */
    private void configureServiceInfo() {
        AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
        // 监听所有类型的事件
        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        // 所有类型的反馈
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        // 默认标志
        serviceInfo.flags = AccessibilityServiceInfo.FLAG_DEFAULT |
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        // 只监听陌陌应用
        serviceInfo.packageNames = new String[]{MOMO_PACKAGE_NAME};
        // 事件通知超时时间
        serviceInfo.notificationTimeout = 100;
        // 可以检索窗口内容
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
        // 释放资源
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
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
        if (currentUser == null) {
            Log.e(TAG, "无法获取当前聊天用户");
            return;
        }
        Log.d(TAG, "当前聊天用户: " + currentUser);
        
        // 2. 获取聊天消息列表
        List<AccessibilityNodeInfo> messageNodes = rootNode.findAccessibilityNodeInfosByViewId(MOMO_MESSAGE_TEXT_ID);
        if (messageNodes == null || messageNodes.isEmpty()) {
            Log.d(TAG, "未找到消息控件");
            return;
        }
        
        // 3. 处理最新的消息
        AccessibilityNodeInfo latestMessageNode = messageNodes.get(messageNodes.size() - 1);
        if (latestMessageNode != null && latestMessageNode.getText() != null) {
            String latestMessage = latestMessageNode.getText().toString().trim();
            
            // 避免重复处理相同消息
            if (isMessageProcessed(currentUser, latestMessage)) {
                Log.d(TAG, "消息已处理，跳过: " + latestMessage);
                return;
            }
            
            // 4. 识别消息发送者
            String sender = getMessageSender(latestMessageNode);
            
            // 5. 只处理对方发送的消息
            if (!sender.equals("self")) {
                Log.d(TAG, "收到消息 - 用户: " + currentUser + ", 发送者: " + sender + ", 内容: " + latestMessage);
                
                // 6. 异步处理消息，生成并发送回复
                processMessageAsync(currentUser, latestMessage);
                
                // 7. 更新消息缓存
                updateMessageCache(currentUser, latestMessage);
            }
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
        // 方法1：通过聊天标题栏获取
        List<AccessibilityNodeInfo> titleNodes = rootNode.findAccessibilityNodeInfosByViewId(MOMO_CHAT_TITLE_ID);
        if (titleNodes != null && !titleNodes.isEmpty()) {
            AccessibilityNodeInfo titleNode = titleNodes.get(0);
            if (titleNode != null && titleNode.getText() != null) {
                return titleNode.getText().toString().trim();
            }
        }
        
        // 方法2：通过聊天界面其他特征获取
        // 这里可以添加备用方法，提高兼容性
        
        return null;
    }
    
    /**
     * 获取消息发送者
     * @return "self" 表示自己发送的消息，"other" 表示对方发送的消息
     */
    private String getMessageSender(AccessibilityNodeInfo messageNode) {
        // 方法1：通过消息节点的父控件特征判断
        AccessibilityNodeInfo parent = messageNode.getParent();
        if (parent != null) {
            // 获取父控件的资源ID或其他特征
            String parentResourceName = parent.getViewIdResourceName();
            
            // 通常，自己发送的消息和对方发送的消息会有不同的父控件ID
            // 这里需要根据实际情况调整判断逻辑
            if (parentResourceName != null) {
                if (parentResourceName.contains("send") || parentResourceName.contains("right")) {
                    return "self";
                } else if (parentResourceName.contains("receive") || parentResourceName.contains("left")) {
                    return "other";
                }
            }
            
            // 方法2：通过布局方向判断
            // 自己发送的消息通常靠右，对方发送的消息通常靠左
            // 可以通过获取父控件的boundsInParent或boundsInScreen来判断
        }
        
        // 默认返回other
        return "other";
    }
    
    /**
     * 检查消息是否已处理
     */
    private boolean isMessageProcessed(String user, String message) {
        String cachedMessage = lastMessageCache.get(user);
        return cachedMessage != null && cachedMessage.equals(message);
    }
    
    /**
     * 更新消息缓存
     */
    private void updateMessageCache(String user, String message) {
        lastMessageCache.put(user, message);
    }
    
    /**
     * 异步处理消息，生成并发送回复
     */
    private void processMessageAsync(String user, String message) {
        executorService.execute(() -> {
            try {
                // 1. 添加消息到对话历史
                conversationManager.addMessage(user, message, false);
                
                // 2. 获取对话历史
                List<ConversationManager.Message> history = conversationManager.getConversationHistory(user);
                
                // 3. 生成AI回复
                String reply = aiReplyGenerator.generateReply(user, history);
                if (reply == null || reply.isEmpty()) {
                    Log.e(TAG, "AI生成回复失败");
                    return;
                }
                
                // 4. 发送回复
                sendReply(reply);
                
                // 5. 将回复添加到对话历史
                conversationManager.addMessage(user, reply, true);
                
            } catch (Exception e) {
                Log.e(TAG, "处理消息异常: " + e.getMessage(), e);
            }
        });
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
        if (inputNodes == null || inputNodes.isEmpty()) {
            Log.e(TAG, "未找到输入框");
            return;
        }
        
        AccessibilityNodeInfo inputNode = inputNodes.get(0);
        
        // 2. 清除输入框现有内容（可选）
        inputNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
        
        // 3. 输入回复内容
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, replyContent);
        boolean textSet = inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        
        if (!textSet) {
            Log.e(TAG, "设置回复内容失败");
            return;
        }
        
        // 4. 查找发送按钮
        List<AccessibilityNodeInfo> sendNodes = rootNode.findAccessibilityNodeInfosByViewId(MOMO_SEND_BUTTON_ID);
        if (sendNodes == null || sendNodes.isEmpty()) {
            Log.e(TAG, "未找到发送按钮");
            return;
        }
        
        // 5. 点击发送按钮
        AccessibilityNodeInfo sendNode = sendNodes.get(0);
        boolean sent = sendNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        
        if (sent) {
            Log.d(TAG, "回复发送成功: " + replyContent);
        } else {
            Log.e(TAG, "回复发送失败");
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
                
                // 可以在这里实现通知点击和自动回复功能
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
            case AccessibilityEvent.TYPE_VIEW_SCROLLED: return "视图滚动";
            case AccessibilityEvent.TYPE_VIEW_FOCUSED: return "视图获取焦点";
            default: return "未知事件 (" + eventType + ")";
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "陌陌无障碍服务已销毁");
        
        // 释放资源
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        if (conversationManager != null) {
            conversationManager.close();
        }
    }
}
