package com.momoautoreply;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 对话管理器
 * 功能：
 * 1. 管理多个用户的对话历史
 * 2. 存储消息到SQLite数据库
 * 3. 支持查询和删除对话历史
 * 4. 支持清理旧对话
 * 5. 支持角色设定管理
 */
public class ConversationManager {
    
    private static final String TAG = "ConversationManager";
    
    // 数据库相关常量
    private static final String DB_NAME = "momo_conversations.db";
    private static final int DB_VERSION = 1;
    
    // 对话表常量
    private static final String TABLE_CONVERSATIONS = "conversations";
    private static final String COLUMN_CONV_ID = "id";
    private static final String COLUMN_CONV_USER_NAME = "user_name";
    private static final String COLUMN_CONV_LAST_ACTIVE = "last_active";
    private static final String COLUMN_CONV_ROLE_SETTING = "role_setting";
    
    // 消息表常量
    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_MSG_ID = "id";
    private static final String COLUMN_MSG_CONV_ID = "conversation_id";
    private static final String COLUMN_MSG_SENDER = "sender";
    private static final String COLUMN_MSG_CONTENT = "content";
    private static final String COLUMN_MSG_TIMESTAMP = "timestamp";
    
    // 默认角色设定
    private static final String DEFAULT_ROLE_SETTING = "你是一个友好、亲切、自然的聊天助手，擅长和人轻松愉快地交流。请使用简洁明了的语言回复，避免过于复杂的表达。";
    
    // 数据库助手
    private DatabaseHelper dbHelper;
    // 上下文
    private Context context;
    
    /**
     * 消息数据类
     */
    public static class Message {
        public String id;
        public String sender;
        public String content;
        public long timestamp;
        
        public Message(String id, String sender, String content, long timestamp) {
            this.id = id;
            this.sender = sender;
            this.content = content;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * 对话数据类
     */
    public static class Conversation {
        public String id;
        public String userName;
        public long lastActive;
        public String roleSetting;
        public List<Message> messages;
        
        public Conversation(String id, String userName, long lastActive, String roleSetting) {
            this.id = id;
            this.userName = userName;
            this.lastActive = lastActive;
            this.roleSetting = roleSetting;
            this.messages = new ArrayList<>();
        }
    }
    
    /**
     * 构造函数
     */
    public ConversationManager(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }
    
    /**
     * 添加消息到对话历史
     * @param userName 用户名
     * @param content 消息内容
     * @param isSelf 是否是自己发送的消息
     */
    public synchronized void addMessage(String userName, String content, boolean isSelf) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            
            // 1. 查找或创建对话
            String conversationId = findOrCreateConversation(db, userName);
            if (conversationId == null) {
                Log.e(TAG, "无法创建对话");
                return;
            }
            
            // 2. 插入消息
            ContentValues msgValues = new ContentValues();
            msgValues.put(COLUMN_MSG_ID, UUID.randomUUID().toString());
            msgValues.put(COLUMN_MSG_CONV_ID, conversationId);
            msgValues.put(COLUMN_MSG_SENDER, isSelf ? "self" : "other");
            msgValues.put(COLUMN_MSG_CONTENT, content);
            msgValues.put(COLUMN_MSG_TIMESTAMP, System.currentTimeMillis());
            
            long msgResult = db.insert(TABLE_MESSAGES, null, msgValues);
            if (msgResult == -1) {
                Log.e(TAG, "插入消息失败");
                return;
            }
            
            // 3. 更新对话的最后活跃时间
            ContentValues convValues = new ContentValues();
            convValues.put(COLUMN_CONV_LAST_ACTIVE, System.currentTimeMillis());
            
            String whereClause = COLUMN_CONV_ID + " = ?";
            String[] whereArgs = {conversationId};
            int convResult = db.update(TABLE_CONVERSATIONS, convValues, whereClause, whereArgs);
            if (convResult == 0) {
                Log.e(TAG, "更新对话最后活跃时间失败");
            }
            
            Log.d(TAG, "消息添加成功 - 用户: " + userName + ", 内容: " + content);
            
        } catch (Exception e) {
            Log.e(TAG, "添加消息异常: " + e.getMessage(), e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
    
    /**
     * 获取用户的对话历史
     * @param userName 用户名
     * @return 对话历史消息列表
     */
    public synchronized List<Message> getConversationHistory(String userName) {
        List<Message> messages = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        
        try {
            db = dbHelper.getReadableDatabase();
            
            // 1. 查找对话
            String conversationId = findConversationId(db, userName);
            if (conversationId == null) {
                return messages;
            }
            
            // 2. 查询消息
            String query = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + 
                          COLUMN_MSG_CONV_ID + " = ? ORDER BY " + COLUMN_MSG_TIMESTAMP + " ASC";
            String[] selectionArgs = {conversationId};
            
            cursor = db.rawQuery(query, selectionArgs);
            
            // 3. 处理结果
            while (cursor != null && cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MSG_ID));
                String sender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MSG_SENDER));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MSG_CONTENT));
                long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_MSG_TIMESTAMP));
                
                Message message = new Message(id, sender, content, timestamp);
                messages.add(message);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "获取对话历史异常: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        
        return messages;
    }
    
    /**
     * 获取对话的角色设定
     * @param userName 用户名
     * @return 角色设定
     */
    public synchronized String getRoleSetting(String userName) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        String roleSetting = DEFAULT_ROLE_SETTING;
        
        try {
            db = dbHelper.getReadableDatabase();
            
            // 查询对话的角色设定
            String query = "SELECT " + COLUMN_CONV_ROLE_SETTING + " FROM " + TABLE_CONVERSATIONS + " WHERE " + 
                          COLUMN_CONV_USER_NAME + " = ?";
            String[] selectionArgs = {userName};
            
            cursor = db.rawQuery(query, selectionArgs);
            
            if (cursor != null && cursor.moveToFirst()) {
                roleSetting = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONV_ROLE_SETTING));
            }
            
        } catch (Exception e) {
            Log.e(TAG, "获取角色设定异常: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        
        return roleSetting;
    }
    
    /**
     * 设置对话的角色设定
     * @param userName 用户名
     * @param roleSetting 角色设定
     */
    public synchronized void setRoleSetting(String userName, String roleSetting) {
        SQLiteDatabase db = null;
        
        try {
            db = dbHelper.getWritableDatabase();
            
            // 查找或创建对话
            String conversationId = findOrCreateConversation(db, userName);
            if (conversationId == null) {
                return;
            }
            
            // 更新角色设定
            ContentValues values = new ContentValues();
            values.put(COLUMN_CONV_ROLE_SETTING, roleSetting);
            
            String whereClause = COLUMN_CONV_ID + " = ?";
            String[] whereArgs = {conversationId};
            
            int result = db.update(TABLE_CONVERSATIONS, values, whereClause, whereArgs);
            if (result > 0) {
                Log.d(TAG, "角色设定更新成功 - 用户: " + userName + ", 设定: " + roleSetting);
            } else {
                Log.e(TAG, "角色设定更新失败");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "设置角色设定异常: " + e.getMessage(), e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
    
    /**
     * 删除用户的对话历史
     * @param userName 用户名
     */
    public synchronized void deleteConversation(String userName) {
        SQLiteDatabase db = null;
        
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            
            // 1. 查找对话
            String conversationId = findConversationId(db, userName);
            if (conversationId == null) {
                return;
            }
            
            // 2. 删除消息
            String msgWhereClause = COLUMN_MSG_CONV_ID + " = ?";
            String[] msgWhereArgs = {conversationId};
            int msgResult = db.delete(TABLE_MESSAGES, msgWhereClause, msgWhereArgs);
            
            // 3. 删除对话
            String convWhereClause = COLUMN_CONV_ID + " = ?";
            String[] convWhereArgs = {conversationId};
            int convResult = db.delete(TABLE_CONVERSATIONS, convWhereClause, convWhereArgs);
            
            db.setTransactionSuccessful();
            
            Log.d(TAG, "对话删除成功 - 用户: " + userName + ", 删除消息数: " + msgResult + ", 删除对话数: " + convResult);
            
        } catch (Exception e) {
            Log.e(TAG, "删除对话异常: " + e.getMessage(), e);
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }
    
    /**
     * 清理长时间未活跃的对话
     * @param maxInactiveTime 最大不活跃时间（毫秒）
     */
    public synchronized void cleanupInactiveConversations(long maxInactiveTime) {
        SQLiteDatabase db = null;
        
        try {
            db = dbHelper.getWritableDatabase();
            long currentTime = System.currentTimeMillis();
            long cutoffTime = currentTime - maxInactiveTime;
            
            // 查找不活跃的对话
            String query = "SELECT " + COLUMN_CONV_ID + " FROM " + TABLE_CONVERSATIONS + " WHERE " + 
                          COLUMN_CONV_LAST_ACTIVE + " < ?";
            String[] selectionArgs = {String.valueOf(cutoffTime)};
            
            Cursor cursor = db.rawQuery(query, selectionArgs);
            List<String> inactiveConvIds = new ArrayList<>();
            
            while (cursor != null && cursor.moveToNext()) {
                String convId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONV_ID));
                inactiveConvIds.add(convId);
            }
            
            if (cursor != null) {
                cursor.close();
            }
            
            // 删除不活跃的对话和消息
            for (String convId : inactiveConvIds) {
                db.beginTransaction();
                
                // 删除消息
                String msgWhereClause = COLUMN_MSG_CONV_ID + " = ?";
                String[] msgWhereArgs = {convId};
                db.delete(TABLE_MESSAGES, msgWhereClause, msgWhereArgs);
                
                // 删除对话
                String convWhereClause = COLUMN_CONV_ID + " = ?";
                String[] convWhereArgs = {convId};
                db.delete(TABLE_CONVERSATIONS, convWhereClause, convWhereArgs);
                
                db.setTransactionSuccessful();
                db.endTransaction();
            }
            
            Log.d(TAG, "清理不活跃对话成功 - 清理对话数: " + inactiveConvIds.size());
            
        } catch (Exception e) {
            Log.e(TAG, "清理不活跃对话异常: " + e.getMessage(), e);
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
    
    /**
     * 获取所有对话列表
     * @return 对话列表
     */
    public synchronized List<Conversation> getAllConversations() {
        List<Conversation> conversations = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor convCursor = null;
        
        try {
            db = dbHelper.getReadableDatabase();
            
            // 查询所有对话
            String convQuery = "SELECT * FROM " + TABLE_CONVERSATIONS + " ORDER BY " + 
                              COLUMN_CONV_LAST_ACTIVE + " DESC";
            
            convCursor = db.rawQuery(convQuery, null);
            
            while (convCursor != null && convCursor.moveToNext()) {
                String convId = convCursor.getString(convCursor.getColumnIndexOrThrow(COLUMN_CONV_ID));
                String userName = convCursor.getString(convCursor.getColumnIndexOrThrow(COLUMN_CONV_USER_NAME));
                long lastActive = convCursor.getLong(convCursor.getColumnIndexOrThrow(COLUMN_CONV_LAST_ACTIVE));
                String roleSetting = convCursor.getString(convCursor.getColumnIndexOrThrow(COLUMN_CONV_ROLE_SETTING));
                
                Conversation conversation = new Conversation(convId, userName, lastActive, roleSetting);
                
                // 查询对话的消息
                String msgQuery = "SELECT * FROM " + TABLE_MESSAGES + " WHERE " + 
                                 COLUMN_MSG_CONV_ID + " = ? ORDER BY " + COLUMN_MSG_TIMESTAMP + " ASC";
                String[] msgSelectionArgs = {convId};
                
                Cursor msgCursor = db.rawQuery(msgQuery, msgSelectionArgs);
                
                while (msgCursor != null && msgCursor.moveToNext()) {
                    String msgId = msgCursor.getString(msgCursor.getColumnIndexOrThrow(COLUMN_MSG_ID));
                    String sender = msgCursor.getString(msgCursor.getColumnIndexOrThrow(COLUMN_MSG_SENDER));
                    String content = msgCursor.getString(msgCursor.getColumnIndexOrThrow(COLUMN_MSG_CONTENT));
                    long timestamp = msgCursor.getLong(msgCursor.getColumnIndexOrThrow(COLUMN_MSG_TIMESTAMP));
                    
                    Message message = new Message(msgId, sender, content, timestamp);
                    conversation.messages.add(message);
                }
                
                if (msgCursor != null) {
                    msgCursor.close();
                }
                
                conversations.add(conversation);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "获取所有对话异常: " + e.getMessage(), e);
        } finally {
            if (convCursor != null) {
                convCursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        
        return conversations;
    }
    
    /**
     * 查找或创建对话
     * @param db 数据库
     * @param userName 用户名
     * @return 对话ID
     */
    private String findOrCreateConversation(SQLiteDatabase db, String userName) {
        String conversationId = findConversationId(db, userName);
        
        if (conversationId == null) {
            // 创建对话
            ContentValues values = new ContentValues();
            conversationId = UUID.randomUUID().toString();
            values.put(COLUMN_CONV_ID, conversationId);
            values.put(COLUMN_CONV_USER_NAME, userName);
            values.put(COLUMN_CONV_LAST_ACTIVE, System.currentTimeMillis());
            values.put(COLUMN_CONV_ROLE_SETTING, DEFAULT_ROLE_SETTING);
            
            long result = db.insert(TABLE_CONVERSATIONS, null, values);
            if (result == -1) {
                Log.e(TAG, "创建对话失败");
                return null;
            }
            
            Log.d(TAG, "对话创建成功 - 用户: " + userName + ", 对话ID: " + conversationId);
        }
        
        return conversationId;
    }
    
    /**
     * 查找对话ID
     * @param db 数据库
     * @param userName 用户名
     * @return 对话ID，找不到返回null
     */
    private String findConversationId(SQLiteDatabase db, String userName) {
        String conversationId = null;
        Cursor cursor = null;
        
        try {
            String query = "SELECT " + COLUMN_CONV_ID + " FROM " + TABLE_CONVERSATIONS + " WHERE " + 
                          COLUMN_CONV_USER_NAME + " = ?";
            String[] selectionArgs = {userName};
            
            cursor = db.rawQuery(query, selectionArgs);
            
            if (cursor != null && cursor.moveToFirst()) {
                conversationId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONV_ID));
            }
            
        } catch (Exception e) {
            Log.e(TAG, "查找对话ID异常: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return conversationId;
    }
    
    /**
     * 关闭数据库连接
     */
    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
    
    /**
     * 数据库助手类
     */
    private class DatabaseHelper extends SQLiteOpenHelper {
        
        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
            // 创建对话表
            String createConvTable = "CREATE TABLE " + TABLE_CONVERSATIONS + " (" +
                                    COLUMN_CONV_ID + " TEXT PRIMARY KEY, " +
                                    COLUMN_CONV_USER_NAME + " TEXT UNIQUE NOT NULL, " +
                                    COLUMN_CONV_LAST_ACTIVE + " INTEGER NOT NULL, " +
                                    COLUMN_CONV_ROLE_SETTING + " TEXT NOT NULL" +
                                    ");";
            db.execSQL(createConvTable);
            
            // 创建消息表
            String createMsgTable = "CREATE TABLE " + TABLE_MESSAGES + " (" +
                                   COLUMN_MSG_ID + " TEXT PRIMARY KEY, " +
                                   COLUMN_MSG_CONV_ID + " TEXT NOT NULL, " +
                                   COLUMN_MSG_SENDER + " TEXT NOT NULL, " +
                                   COLUMN_MSG_CONTENT + " TEXT NOT NULL, " +
                                   COLUMN_MSG_TIMESTAMP + " INTEGER NOT NULL, " +
                                   "FOREIGN KEY (" + COLUMN_MSG_CONV_ID + ") REFERENCES " + TABLE_CONVERSATIONS + "(" + COLUMN_CONV_ID + ") ON DELETE CASCADE" +
                                   ");";
            db.execSQL(createMsgTable);
            
            // 创建索引
            db.execSQL("CREATE INDEX idx_conversations_user_name ON " + TABLE_CONVERSATIONS + "(" + COLUMN_CONV_USER_NAME + ");");
            db.execSQL("CREATE INDEX idx_messages_conversation_id ON " + TABLE_MESSAGES + "(" + COLUMN_MSG_CONV_ID + ");");
            db.execSQL("CREATE INDEX idx_messages_timestamp ON " + TABLE_MESSAGES + "(" + COLUMN_MSG_TIMESTAMP + ");");
            
            Log.d(TAG, "数据库创建成功");
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // 数据库升级逻辑
            Log.d(TAG, "数据库升级 - 旧版本: " + oldVersion + ", 新版本: " + newVersion);
            
            // 简单的升级逻辑，实际项目中需要更复杂的处理
            if (oldVersion < 2) {
                // 版本2的升级逻辑
            }
        }
    }
}
