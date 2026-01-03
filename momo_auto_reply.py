#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
陌陌AI自动回复系统 - Python版本
功能：模拟Android无障碍服务，读取消息并生成AI回复

使用方法：
1. 直接运行：python momo_auto_reply.py
2. 配置API密钥在代码中
3. 运行后等待消息输入并生成回复
"""

import json
import time
import threading
import random
import re
from datetime import datetime
from typing import Dict, List, Optional
import requests

class ConversationManager:
    """对话管理器 - 管理多个用户的对话历史"""
    
    def __init__(self):
        self.conversations: Dict[str, List[Dict]] = {}
        
    def add_message(self, user_id: str, user_name: str, content: str, is_ai: bool = False) -> bool:
        """添加消息到对话历史"""
        try:
            if user_id not in self.conversations:
                self.conversations[user_id] = []
            
            message = {
                "timestamp": datetime.now().isoformat(),
                "user_name": user_name,
                "content": content,
                "is_ai": is_ai
            }
            
            self.conversations[user_id].append(message)
            
            # 保持对话历史不超过50条
            if len(self.conversations[user_id]) > 50:
                self.conversations[user_id] = self.conversations[user_id][-50:]
            
            return True
            
        except Exception as e:
            print(f"添加消息失败: {e}")
            return False
    
    def get_conversation_history(self, user_id: str) -> List[Dict]:
        """获取用户对话历史"""
        return self.conversations.get(user_id, [])
    
    def clear_conversation(self, user_id: str) -> bool:
        """清空用户对话历史"""
        try:
            if user_id in self.conversations:
                self.conversations[user_id] = []
            return True
        except:
            return False

class AIReplyGenerator:
    """AI回复生成器"""
    
    def __init__(self):
        # 配置您的AI API密钥
        self.openai_api_key = ""  # 在这里填入您的OpenAI API密钥
        self.baidu_api_key = ""   # 在这里填入您的百度文心API密钥
        self.ai_model = "openai"  # 可选: openai, baidu, local
        
        # 角色设定
        self.character_setting = {
            "personality": "友善、幽默、聪明",
            "style": "自然对话风格",
            "response_length": "适中回复",
            "language": "中文"
        }
    
    def generate_reply(self, message: str, user_name: str, conversation_history: List[Dict]) -> str:
        """生成AI回复"""
        try:
            if self.ai_model == "openai":
                return self._generate_openai_reply(message, user_name, conversation_history)
            elif self.ai_model == "baidu":
                return self._generate_baidu_reply(message, user_name, conversation_history)
            else:
                return self._generate_local_reply(message, user_name, conversation_history)
                
        except Exception as e:
            print(f"生成回复失败: {e}")
            return self._generate_fallback_reply(message, user_name)
    
    def _generate_openai_reply(self, message: str, user_name: str, history: List[Dict]) -> str:
        """使用OpenAI生成回复"""
        if not self.openai_api_key:
            return self._generate_local_reply(message, user_name, history)
        
        try:
            # 构建对话上下文
            messages = [
                {"role": "system", "content": f"你是一个{self.character_setting['personality']}的AI助手，{self.character_setting['style']}。请用{self.character_setting['language']}回复，回复长度{self.character_setting['response_length']}。"}
            ]
            
            # 添加历史对话
            for msg in history[-10:]:  # 只取最近10条对话
                role = "assistant" if msg["is_ai"] else "user"
                messages.append({"role": role, "content": msg["content"]})
            
            # 添加当前消息
            messages.append({"role": "user", "content": f"{user_name}说: {message}"})
            
            response = requests.post(
                "https://api.openai.com/v1/chat/completions",
                headers={
                    "Authorization": f"Bearer {self.openai_api_key}",
                    "Content-Type": "application/json"
                },
                json={
                    "model": "gpt-3.5-turbo",
                    "messages": messages,
                    "max_tokens": 500,
                    "temperature": 0.8
                },
                timeout=30
            )
            
            if response.status_code == 200:
                result = response.json()
                return result["choices"][0]["message"]["content"].strip()
            else:
                print(f"OpenAI API错误: {response.status_code}")
                return self._generate_local_reply(message, user_name, history)
                
        except Exception as e:
            print(f"OpenAI回复生成失败: {e}")
            return self._generate_local_reply(message, user_name, history)
    
    def _generate_baidu_reply(self, message: str, user_name: str, history: List[Dict]) -> str:
        """使用百度文心生成回复"""
        if not self.baidu_api_key:
            return self._generate_local_reply(message, user_name, history)
        
        try:
            # 百度文心API调用逻辑
            # 这里需要根据实际的百度文心API文档实现
            return self._generate_local_reply(message, user_name, history)
        except:
            return self._generate_local_reply(message, user_name, history)
    
    def _generate_local_reply(self, message: str, user_name: str, history: List[Dict]) -> str:
        """本地生成回复（无需API）"""
        message_lower = message.lower()
        
        # 问候语回复
        greetings = ["你好", "嗨", "hello", "hi", "早上好", "晚上好"]
        if any(greeting in message_lower for greeting in greetings):
            responses = [
                f"你好 {user_name}！很高兴见到你！",
                f"嗨 {user_name}，今天过得怎么样？",
                f"你好啊！有什么想聊的吗？",
                f"hi {user_name}！😊",
                f"早上好 {user_name}！希望你今天开心！"
            ]
            return random.choice(responses)
        
        # 问题回复
        if "?" in message or "？" in message or "怎么" in message or "如何" in message:
            responses = [
                f"这是个很好的问题，{user_name}！让我想想...",
                f"关于这个问题，我觉得可以从几个角度考虑，{user_name}。",
                f"这确实需要仔细思考呢，{user_name}。",
                f"我觉得这取决于具体情况，{user_name}。",
                f"这个问题很有趣，让我想想最佳答案！"
            ]
            return random.choice(responses)
        
        # 情感回复
        emotions = ["开心", "高兴", "快乐", "难过", "生气", "郁闷"]
        if any(emotion in message_lower for emotion in emotions):
            responses = [
                f"我理解你的感受，{user_name}。希望你能一直保持好心情！",
                f"每个人都会有这样的时候，{user_name}，重要的是保持积极的心态！",
                f"相信明天会更好，{user_name}！💪",
                f"无论遇到什么困难，都要相信自己的力量，{user_name}！",
                f"你的感受我很理解，{user_name}，愿你每天都开心！"
            ]
            return random.choice(responses)
        
        # 默认回复
        default_responses = [
            f"嗯嗯，{user_name}，这个话题很有意思！",
            f"我明白了，{user_name}，能再详细说说吗？",
            f"原来如此，{user_name}！这确实值得思考。",
            f"听起来不错，{user_name}！😊",
            f"我赞同你的想法，{user_name}！",
            f"这确实是个不错的观点，{user_name}。",
            f"有趣的分享，{user_name}！继续聊聊吧～",
            f"好的好的，{user_name}！我很感兴趣呢！",
            f"原来是这样，{user_name}！学到了新知识！",
            f"这想法很棒，{user_name}！👏"
        ]
        return random.choice(default_responses)
    
    def _generate_fallback_reply(self, message: str, user_name: str) -> str:
        """备用回复"""
        fallbacks = [
            f"收到，{user_name}！",
            f"好的，{user_name}！",
            f"明白，{user_name}！",
            f"OK，{user_name}！",
            f"好的好的，{user_name}！"
        ]
        return random.choice(fallbacks)

class MomoAutoReply:
    """陌陌自动回复主类"""
    
    def __init__(self):
        self.conversation_manager = ConversationManager()
        self.ai_reply_generator = AIReplyGenerator()
        self.is_running = False
        self.auto_reply_enabled = True
        
    def start(self):
        """启动自动回复服务"""
        print("🚀 陌陌AI自动回复系统启动中...")
        print("=" * 50)
        
        self.is_running = True
        
        # 显示配置信息
        self._show_config()
        
        # 显示功能说明
        self._show_usage()
        
        # 启动消息监听（模拟）
        self._start_message_listener()
    
    def stop(self):
        """停止自动回复服务"""
        self.is_running = False
        print("🛑 陌陌AI自动回复系统已停止")
    
    def _show_config(self):
        """显示当前配置"""
        print("📋 当前配置:")
        print(f"   AI模型: {self.ai_reply_generator.ai_model}")
        print(f"   自动回复: {'✅ 开启' if self.auto_reply_enabled else '❌ 关闭'}")
        print(f"   角色设定: {self.ai_reply_generator.character_setting}")
        print()
    
    def _show_usage(self):
        """显示使用说明"""
        print("📱 使用说明:")
        print("1. 输入 'help' 查看帮助")
        print("2. 输入 'quit' 或 'exit' 退出程序")
        print("3. 输入 'status' 查看状态")
        print("4. 输入 'config' 查看配置")
        print("5. 输入 'clear' 清空当前对话")
        print("6. 直接输入消息内容模拟收到消息")
        print("7. 输入 'user:消息' 模拟特定用户发送消息")
        print("=" * 50)
        print()
    
    def _start_message_listener(self):
        """启动消息监听器（命令行版本）"""
        print("🎯 开始监听消息...")
        print("💡 提示: 输入消息内容即可模拟收到聊天消息")
        print("-" * 50)
        
        # 模拟系统消息
        self._simulate_system_messages()
        
        try:
            while self.is_running:
                try:
                    # 获取用户输入
                    user_input = input("\n👤 您: ").strip()
                    
                    if not user_input:
                        continue
                    
                    # 处理命令
                    if self._handle_command(user_input):
                        continue
                    
                    # 处理消息
                    self._handle_message(user_input)
                    
                except KeyboardInterrupt:
                    print("\n🛑 接收到停止信号...")
                    self.stop()
                    break
                except EOFError:
                    break
                    
        except Exception as e:
            print(f"❌ 监听器异常: {e}")
        finally:
            print("👋 程序已退出")
    
    def _simulate_system_messages(self):
        """模拟系统消息（演示用）"""
        def send_demo_message():
            demo_messages = [
                ("demo_user", "你好！"),
                ("demo_user", "今天天气怎么样？"),
                ("friend", "在干嘛呢？"),
                ("colleague", "明天有空吗？"),
            ]
            
            for user_id, message in demo_messages:
                if self.is_running:
                    time.sleep(2)
                    print(f"\n📱 模拟消息: {user_id} -> {message}")
                    self._handle_message(message, user_id)
        
        # 启动演示消息线程
        demo_thread = threading.Thread(target=send_demo_message, daemon=True)
        demo_thread.start()
    
    def _handle_command(self, command: str) -> bool:
        """处理命令"""
        command_lower = command.lower().strip()
        
        if command_lower in ['quit', 'exit', '退出']:
            self.stop()
            return True
            
        elif command_lower == 'help':
            print("\n📚 帮助信息:")
            print("- help: 显示此帮助信息")
            print("- quit/exit: 退出程序")
            print("- status: 查看运行状态")
            print("- config: 查看当前配置")
            print("- clear: 清空当前对话")
            print("- 直接输入: 模拟收到消息")
            print("- user:消息: 模拟指定用户发送消息")
            return True
            
        elif command_lower == 'status':
            print(f"\n📊 运行状态:")
            print(f"- 运行状态: {'🟢 运行中' if self.is_running else '🔴 已停止'}")
            print(f"- 自动回复: {'✅ 开启' if self.auto_reply_enabled else '❌ 关闭'}")
            print(f"- 活跃用户数: {len(self.conversation_manager.conversations)}")
            return True
            
        elif command_lower == 'config':
            self._show_config()
            return True
            
        elif command_lower == 'clear':
            print("\n🗑️ 清空所有对话历史")
            self.conversation_manager.conversations.clear()
            print("✅ 对话历史已清空")
            return True
            
        return False
    
    def _handle_message(self, message: str, user_id: str = "unknown_user"):
        """处理收到的消息"""
        if not self.auto_reply_enabled:
            return
        
        # 生成用户名（如果没有指定）
        user_name = user_id if user_id != "unknown_user" else f"用户_{random.randint(1000, 9999)}"
        
        print(f"\n📨 收到消息:")
        print(f"   用户: {user_name}")
        print(f"   内容: {message}")
        print(f"   时间: {datetime.now().strftime('%H:%M:%S')}")
        
        # 添加用户消息到对话历史
        self.conversation_manager.add_message(user_id, user_name, message, False)
        
        # 获取对话历史
        conversation_history = self.conversation_manager.get_conversation_history(user_id)
        
        # 生成AI回复
        print("🤖 AI正在思考...")
        reply = self.ai_reply_generator.generate_reply(message, user_name, conversation_history)
        
        # 模拟回复延迟
        time.sleep(random.uniform(0.5, 2.0))
        
        print(f"\n🤖 AI回复:")
        print(f"   {reply}")
        print(f"   时间: {datetime.now().strftime('%H:%M:%S')}")
        
        # 添加AI回复到对话历史
        self.conversation_manager.add_message(user_id, user_name, reply, True)

def main():
    """主函数"""
    print("🎉 欢迎使用陌陌AI自动回复系统！")
    print("📱 这是一个命令行版本，用于演示和测试功能")
    print()
    
    try:
        # 创建并启动自动回复系统
        auto_reply = MomoAutoReply()
        auto_reply.start()
        
    except Exception as e:
        print(f"❌ 程序启动失败: {e}")
    finally:
        print("👋 再见！")

if __name__ == "__main__":
    main()