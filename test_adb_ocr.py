import subprocess
import os
import time
from PIL import Image
import pytesseract

# ADB命令封装
class ADBController:
    def __init__(self, adb_path="C:\adb\platform-tools\adb"):
        self.adb_path = adb_path
    
    def screenshot(self, output_path="./screenshot.png"):
        """截图到指定路径"""
        subprocess.run([self.adb_path, "shell", "screencap", "/sdcard/screenshot.png"])
        subprocess.run([self.adb_path, "pull", "/sdcard/screenshot.png", output_path])
        return output_path
    
    def tap(self, x, y):
        """点击指定坐标"""
        subprocess.run([self.adb_path, "shell", "input", "tap", str(x), str(y)])
    
    def input_text(self, text):
        """输入文本"""
        subprocess.run([self.adb_path, "shell", "input", "text", text])
    
    def swipe(self, x1, y1, x2, y2, duration=1000):
        """滑动操作"""
        subprocess.run([self.adb_path, "shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration)])
    
    def get_current_package(self):
        """获取当前运行的包名"""
        result = subprocess.run([self.adb_path, "shell", "dumpsys", "window", "|", "grep", "mCurrentFocus"], 
                              capture_output=True, text=True)
        return result.stdout.strip()

# OCR识别封装
class OCREngine:
    def __init__(self, tesseract_path="C:\Program Files\Tesseract-OCR\tesseract.exe"):
        pytesseract.pytesseract.tesseract_cmd = tesseract_path
    
    def recognize_text(self, image_path, lang='chi_sim'):
        """识别图片中的文本"""
        img = Image.open(image_path)
        text = pytesseract.image_to_string(img, lang=lang)
        return text
    
    def recognize_region(self, image_path, region, lang='chi_sim'):
        """识别图片指定区域的文本"""
        img = Image.open(image_path)
        # region格式: (x1, y1, x2, y2)
        cropped_img = img.crop(region)
        text = pytesseract.image_to_string(cropped_img, lang=lang)
        return text

# 主测试函数
if __name__ == "__main__":
    # 初始化ADB控制器和OCR引擎
    adb = ADBController()
    ocr = OCREngine()
    
    # 创建输出目录
    output_dir = "./output"
    os.makedirs(output_dir, exist_ok=True)
    
    print("=== 陌陌消息读取测试 ===")
    
    # 1. 获取当前运行的包名
    current_package = adb.get_current_package()
    print(f"当前运行的包名: {current_package}")
    
    # 2. 截图当前界面
    screenshot_path = adb.screenshot(os.path.join(output_dir, "current_screen.png"))
    print(f"截图已保存到: {screenshot_path}")
    
    # 3. 识别整个屏幕的文本（测试）
    full_text = ocr.recognize_text(screenshot_path)
    print("\n=== 屏幕文本识别结果 ===")
    print(full_text)
    
    # 4. 提示用户在陌陌聊天界面测试
    print("\n=== 测试说明 ===")
    print("1. 请将手机切换到陌陌聊天界面")
    print("2. 然后按回车键继续测试")
    input("按回车键继续...")
    
    # 5. 再次截图并识别聊天内容
    screenshot_path = adb.screenshot(os.path.join(output_dir, "momo_chat.png"))
    print(f"\n陌陌聊天界面截图已保存到: {screenshot_path}")
    
    # 6. 假设聊天区域坐标（需要根据实际界面调整）
    # 注意：这里的坐标是示例，需要根据实际手机屏幕分辨率调整
    chat_region = (100, 200, 1000, 1800)  # (x1, y1, x2, y2)
    chat_text = ocr.recognize_region(screenshot_path, chat_region)
    
    print("\n=== 陌陌聊天内容识别结果 ===")
    print(chat_text)
    
    # 7. 解析聊天内容（简单示例）
    print("\n=== 聊天内容解析 ===")
    lines = chat_text.strip().split('\n')
    for line in lines:
        if line.strip():
            print(f"- {line.strip()}")
    
    print("\n=== 测试完成 ===")
