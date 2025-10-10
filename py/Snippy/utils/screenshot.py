"""
Screenshot Capture Utilities
"""

import mss
from PIL import Image
import io
from PyQt5.QtGui import QPixmap

class ScreenshotCapture:
    """Handle screenshot capture operations"""
    
    def __init__(self):
        self.sct = None
        
    def capture_full_screen(self):
        """Capture the entire screen"""
        with mss.mss() as sct:
            # Capture primary monitor
            monitor = sct.monitors[1]
            screenshot = sct.grab(monitor)
            
            # Convert to PIL Image
            img = Image.frombytes('RGB', screenshot.size, screenshot.rgb)
            return img
            
    def capture_area(self, x, y, width, height):
        """Capture a specific area of the screen"""
        with mss.mss() as sct:
            monitor = {
                'top': y,
                'left': x,
                'width': width,
                'height': height
            }
            screenshot = sct.grab(monitor)
            
            # Convert to PIL Image
            img = Image.frombytes('RGB', screenshot.size, screenshot.rgb)
            return img
            
    def capture_monitor(self, monitor_index=1):
        """Capture a specific monitor"""
        with mss.mss() as sct:
            if monitor_index < len(sct.monitors):
                monitor = sct.monitors[monitor_index]
                screenshot = sct.grab(monitor)
                
                # Convert to PIL Image
                img = Image.frombytes('RGB', screenshot.size, screenshot.rgb)
                return img
            return None
