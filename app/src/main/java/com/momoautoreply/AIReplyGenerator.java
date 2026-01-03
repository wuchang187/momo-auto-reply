package com.momoautoreply;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI回复生成器
 * 功能：
 * 1. 生成AI回复
 * 2. 支持多种AI模型
 * 3. 支持角色设定
 * 4. 支持对话历史上下文
 * 5. 异步生成回复
 */
public class AIReplyGenerator {
    
    private static final String TAG = "AIReplyGenerator";
    
    // AI模型类型
    public enum AIModel {
        OPENAI,      // OpenAI GPT系列
        BAIDU_WENXIN, // 百度文心一言
        XUNFEI,       // 讯飞星火
        ZHIPU,        // 智谱AI
        LOCAL         // 本地模型
    }
    
    // 当前使用的AI模型
    private AIModel currentModel = AIModel.OPENAI;
    
    // AI模型配置
    private ModelConfig modelConfig;
    
    // 上下文
    private Context context;
    
    // 线程池，用于异步生成回复
    private ExecutorService executorService;
    
    // 对话管理器，用于获取角色设定
    private ConversationManager conversationManager;
    
    /**
     * 模型配置类
     */
    private static class ModelConfig {
        // OpenAI配置
        String openaiApiKey = "your_openai_api_key"; // 替换为实际的API密钥
        String openaiModel = "gpt-3.5-turbo";
        String openaiApiUrl = "https://api.openai.com/v1/chat/completions";
        
        // 百度文心一言配置
        String baiduApiKey = "your_baidu_api_key"; // 替换为实际的API密钥
        String baiduSecretKey = "your_baidu_secret_key"; // 替换为实际的密钥
        String baiduApiUrl = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions";
        
        // 其他模型配置...
    }
    
    /**
     * 构造函数
     */
    public AIReplyGenerator(Context context) {
        this.context = context;
        this.modelConfig = new ModelConfig();
        this.executorService = Executors.newSingleThreadExecutor();
        this.conversationManager = new ConversationManager(context);
    }
    
    /**
     * 生成AI回复
     * @param userName 用户名
     * @param history 对话历史
     * @return AI回复内容
     */
    public String generateReply(String userName, List<ConversationManager.Message> history) {
        try {
            // 1. 获取角色设定
            String roleSetting = conversationManager.getRoleSetting(userName);
            
            // 2. 构建对话上下文
            String prompt = buildPrompt(roleSetting, history);
            
            // 3. 根据当前模型生成回复
            String reply;
            switch (currentModel) {
                case OPENAI:
                    reply = generateOpenAIReply(prompt);
                    break;
                case BAIDU_WENXIN:
                    reply = generateBaiduWenxinReply(prompt);
                    break;
                case XUNFEI:
                    reply = generateXunfeiReply(prompt);
                    break;
                case ZHIPU:
                    reply = generateZhipuReply(prompt);
                    break;
                case LOCAL:
                    reply = generateLocalReply(prompt);
                    break;
                default:
                    reply = generateDefaultReply();
                    break;
            }
            
            Log.d(TAG, "AI回复生成成功 - 用户: " + userName + ", 回复: " + reply);
            return reply;
            
        } catch (Exception e) {
            Log.e(TAG, "生成AI回复异常: " + e.getMessage(), e);
            return generateDefaultReply();
        }
    }
    
    /**
     * 构建对话提示
     * @param roleSetting 角色设定
     * @param history 对话历史
     * @return 构建好的提示
     */
    private String buildPrompt(String roleSetting, List<ConversationManager.Message> history) {
        StringBuilder prompt = new StringBuilder();
        
        // 添加角色设定
        prompt.append(roleSetting).append("\n\n");
        
        // 添加对话历史
        for (ConversationManager.Message message : history) {
            String role = message.sender.equals("self") ? "助手" : "用户";
            prompt.append(role).append(": ").append(message.content).append("\n");
        }
        
        // 添加当前请求
        prompt.append("助手: ");
        
        return prompt.toString();
    }
    
    /**
     * 生成OpenAI回复
     * @param prompt 提示
     * @return 回复内容
     * @throws IOException IO异常
     * @throws JSONException JSON异常
     */
    private String generateOpenAIReply(String prompt) throws IOException, JSONException {
        URL url = new URL(modelConfig.openaiApiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // 设置请求头
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + modelConfig.openaiApiKey);
        connection.setDoOutput(true);
        
        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", modelConfig.openaiModel);
        
        JSONArray messages = new JSONArray();
        
        // 添加系统角色
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", prompt.substring(0, prompt.indexOf("\n\n")));
        messages.put(systemMessage);
        
        // 添加对话历史
        String[] lines = prompt.substring(prompt.indexOf("\n\n") + 2).split("\n");
        for (String line : lines) {
            if (line.contains(": ")) {
                String[] parts = line.split(": ", 2);
                if (parts.length == 2) {
                    String role = parts[0].equals("用户") ? "user" : "assistant";
                    String content = parts[1];
                    
                    JSONObject message = new JSONObject();
                    message.put("role", role);
                    message.put("content", content);
                    messages.put(message);
                }
            }
        }
        
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 100);
        requestBody.put("temperature", 0.7);
        
        // 发送请求
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
        // 读取响应
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        
        // 解析响应
        JSONObject responseJson = new JSONObject(response.toString());
        JSONArray choices = responseJson.getJSONArray("choices");
        if (choices.length() > 0) {
            JSONObject choice = choices.getJSONObject(0);
            JSONObject message = choice.getJSONObject("message");
            return message.getString("content").trim();
        }
        
        return generateDefaultReply();
    }
    
    /**
     * 生成百度文心一言回复
     * @param prompt 提示
     * @return 回复内容
     */
    private String generateBaiduWenxinReply(String prompt) {
        // 实现百度文心一言API调用
        // 这里是示例实现，需要替换为实际API调用
        return "百度文心一言：" + prompt.substring(prompt.lastIndexOf("用户: ") + 4);
    }
    
    /**
     * 生成讯飞星火回复
     * @param prompt 提示
     * @return 回复内容
     */
    private String generateXunfeiReply(String prompt) {
        // 实现讯飞星火API调用
        // 这里是示例实现，需要替换为实际API调用
        return "讯飞星火：" + prompt.substring(prompt.lastIndexOf("用户: ") + 4);
    }
    
    /**
     * 生成智谱AI回复
     * @param prompt 提示
     * @return 回复内容
     */
    private String generateZhipuReply(String prompt) {
        // 实现智谱AI API调用
        // 这里是示例实现，需要替换为实际API调用
        return "智谱AI：" + prompt.substring(prompt.lastIndexOf("用户: ") + 4);
    }
    
    /**
     * 生成本地模型回复
     * @param prompt 提示
     * @return 回复内容
     */
    private String generateLocalReply(String prompt) {
        // 实现本地模型调用
        // 这里是示例实现，需要替换为实际本地模型调用
        return "本地模型：" + prompt.substring(prompt.lastIndexOf("用户: ") + 4);
    }
    
    /**
     * 生成默认回复
     * @return 默认回复
     */
    private String generateDefaultReply() {
        // 默认回复列表，随机选择一个
        String[] defaultReplies = {
            "你好呀，我现在有点忙，稍后再和你聊哦~",
            "哈哈，这个话题很有趣呢！",
            "我不太明白你的意思，可以再说一遍吗？",
            "听起来不错！",
            "谢谢你的分享！",
            "我觉得你说得很有道理。",
            "今天天气真不错呢！",
            "你最近在忙什么呢？",
            "很高兴和你聊天！",
            "期待和你的下次交流！"
        };
        
        int randomIndex = (int) (Math.random() * defaultReplies.length);
        return defaultReplies[randomIndex];
    }
    
    /**
     * 设置当前使用的AI模型
     * @param model AI模型类型
     */
    public void setAIModel(AIModel model) {
        this.currentModel = model;
        Log.d(TAG, "AI模型已切换为: " + model.name());
    }
    
    /**
     * 获取当前使用的AI模型
     * @return 当前AI模型类型
     */
    public AIModel getCurrentModel() {
        return currentModel;
    }
    
    /**
     * 设置模型配置
     * @param config 模型配置
     */
    public void setModelConfig(ModelConfig config) {
        this.modelConfig = config;
    }
    
    /**
     * 获取模型配置
     * @return 模型配置
     */
    public ModelConfig getModelConfig() {
        return modelConfig;
    }
    
    /**
     * 关闭资源
     */
    public void close() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
