package com.momoautoreply;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 设备启动完成广播接收器
 * 功能：
 * 1. 监听设备启动完成事件
 * 2. 自动检查无障碍服务状态
 * 3. 可以在必要时重启服务或应用
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootCompleteReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        Log.d(TAG, "收到广播: " + action);
        
        // 处理设备启动完成事件
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
            "android.intent.action.QUICKBOOT_POWERON".equals(action) || 
            "com.htc.intent.action.QUICKBOOT_POWERON".equals(action)) {
            
            handleBootCompleted(context);
        }
    }
    
    /**
     * 处理设备启动完成事件
     * @param context 上下文
     */
    private void handleBootCompleted(Context context) {
        Log.d(TAG, "设备启动完成，开始处理...");
        
        // 在这里可以实现以下功能：
        // 1. 检查无障碍服务是否已开启
        // 2. 如果服务未开启，可以发送通知提醒用户
        // 3. 可以启动应用的主Activity，引导用户开启服务
        // 4. 可以做一些初始化工作，如清理旧数据、更新配置等
        
        // 示例：检查无障碍服务状态
        boolean isServiceEnabled = isAccessibilityServiceEnabled(context);
        Log.d(TAG, "无障碍服务状态: " + (isServiceEnabled ? "已开启" : "已关闭"));
        
        if (!isServiceEnabled) {
            // 服务未开启，可以发送通知提醒用户
            sendNotification(context);
        }
        
        // 示例：清理过期的对话历史
        ConversationManager conversationManager = new ConversationManager(context);
        // 清理超过7天未活跃的对话（7天 = 7 * 24 * 60 * 60 * 1000毫秒）
        conversationManager.cleanupInactiveConversations(7 * 24 * 60 * 60 * 1000);
        conversationManager.close();
        
        Log.d(TAG, "设备启动完成处理完毕");
    }
    
    /**
     * 检查无障碍服务是否已开启
     * @param context 上下文
     * @return 是否开启
     */
    private boolean isAccessibilityServiceEnabled(Context context) {
        String serviceName = context.getPackageName() + "/" + MomoAccessibilityService.class.getName();
        String enabledServices = android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        
        return enabledServices != null && enabledServices.contains(serviceName);
    }
    
    /**
     * 发送通知提醒用户开启无障碍服务
     * @param context 上下文
     */
    private void sendNotification(Context context) {
        Log.d(TAG, "发送通知提醒用户开启无障碍服务");
        
        // 这里可以实现发送通知的功能，引导用户开启无障碍服务
        // 示例代码：
        /*
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // 创建通知渠道（Android O及以上）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "momo_auto_reply_channel",
                    "陌陌自动回复",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }
        
        // 创建通知内容
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        Notification notification = new NotificationCompat.Builder(context, "momo_auto_reply_channel")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("陌陌自动回复")
                .setContentText("您的无障碍服务已关闭，请及时开启以保证正常使用。")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        
        // 发送通知
        notificationManager.notify(1, notification);
        */
    }
}
