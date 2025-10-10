"""
Screen Recorder Module
"""

from PyQt5.QtCore import QObject, pyqtSignal, QThread, QTimer, QRect
from PyQt5.QtWidgets import QWidget, QPushButton, QVBoxLayout, QLabel, QApplication
from PyQt5.QtCore import Qt
from PyQt5.QtGui import QFont
import cv2
import numpy as np
import mss
from datetime import datetime
import os

class RecorderThread(QThread):
    """Thread for recording screen"""
    
    def __init__(self, output_path, area=None, fps=30):
        super().__init__()
        self.output_path = output_path
        self.area = area  # QRect object for recording area
        self.fps = fps
        self.is_recording = False
        
    def run(self):
        """Run the recording"""
        self.is_recording = True
        
        with mss.mss() as sct:
            # Set up monitor region
            if self.area:
                # Record specific area
                monitor = {
                    'top': self.area.y(),
                    'left': self.area.x(),
                    'width': self.area.width(),
                    'height': self.area.height()
                }
                width = self.area.width()
                height = self.area.height()
            else:
                # Record full primary monitor
                monitor = sct.monitors[1]
                width = monitor['width']
                height = monitor['height']
            
            # Set up video writer
            fourcc = cv2.VideoWriter_fourcc(*'mp4v')
            out = cv2.VideoWriter(
                self.output_path,
                fourcc,
                self.fps,
                (width, height)
            )
            
            try:
                while self.is_recording:
                    # Capture screen
                    screenshot = sct.grab(monitor)
                    
                    # Convert to numpy array
                    frame = np.array(screenshot)
                    
                    # Convert BGRA to BGR
                    frame = cv2.cvtColor(frame, cv2.COLOR_BGRA2BGR)
                    
                    # Write frame
                    out.write(frame)
                    
                    # Control frame rate
                    self.msleep(int(1000 / self.fps))
                    
            finally:
                out.release()
                
    def stop(self):
        """Stop recording"""
        self.is_recording = False


class RecordingOverlay(QWidget):
    """Overlay window shown during recording"""
    stop_requested = pyqtSignal()
    
    def __init__(self, recording_area=None):
        super().__init__()
        self.recording_area = recording_area
        self.setWindowFlags(Qt.FramelessWindowHint | Qt.WindowStaysOnTopHint | Qt.Tool)
        self.setAttribute(Qt.WA_TranslucentBackground)
        
        # Position at top-right corner
        screen = QApplication.desktop().screenGeometry()
        self.setGeometry(screen.width() - 270, 20, 250, 140)
        
        layout = QVBoxLayout()
        layout.setContentsMargins(0, 0, 0, 0)
        
        # Container widget for rounded corners and gradient
        container = QWidget()
        container.setStyleSheet("""
            QWidget {
                background: qlineargradient(x1:0, y1:0, x2:0, y2:1,
                    stop:0 #2b2b2b, stop:1 #1e1e1e);
                border-radius: 15px;
                border: 2px solid #ff4444;
            }
        """)
        
        container_layout = QVBoxLayout(container)
        container_layout.setContentsMargins(20, 18, 20, 18)
        container_layout.setSpacing(10)
        
        # Recording indicator with pulse effect
        self.status_label = QLabel("🔴 Recording...")
        self.status_label.setAlignment(Qt.AlignCenter)
        font = QFont()
        font.setPointSize(12)
        font.setBold(True)
        self.status_label.setFont(font)
        self.status_label.setStyleSheet("color: #ff4444; background: transparent;")
        container_layout.addWidget(self.status_label)
        
        # Area info (if recording specific area)
        if recording_area:
            area_text = f"📐 {recording_area.width()}×{recording_area.height()}"
            self.area_label = QLabel(area_text)
            self.area_label.setAlignment(Qt.AlignCenter)
            self.area_label.setStyleSheet("font-size: 11px; color: #aaaaaa; background: transparent;")
            container_layout.addWidget(self.area_label)
        
        # Timer label with modern styling
        self.timer_label = QLabel("00:00")
        self.timer_label.setAlignment(Qt.AlignCenter)
        timer_font = QFont()
        timer_font.setPointSize(20)
        timer_font.setBold(True)
        self.timer_label.setFont(timer_font)
        self.timer_label.setStyleSheet("font-size: 22px; color: #ffffff; background: transparent; padding: 5px;")
        container_layout.addWidget(self.timer_label)
        
        # Stop button with modern gradient
        stop_btn = QPushButton("⏹️ Stop Recording")
        stop_btn.setCursor(Qt.PointingHandCursor)
        stop_btn.setStyleSheet("""
            QPushButton {
                background: qlineargradient(x1:0, y1:0, x2:0, y2:1,
                    stop:0 #e84545, stop:1 #d41400);
                color: white;
                border: none;
                padding: 12px;
                font-size: 13px;
                border-radius: 8px;
                font-weight: bold;
            }
            QPushButton:hover {
                background: qlineargradient(x1:0, y1:0, x2:0, y2:1,
                    stop:0 #ff5555, stop:1 #e41400);
            }
            QPushButton:pressed {
                background: qlineargradient(x1:0, y1:0, x2:0, y2:1,
                    stop:0 #c83838, stop:1 #b01000);
            }
        """)
        stop_btn.clicked.connect(self.stop_requested.emit)
        container_layout.addWidget(stop_btn)
        
        layout.addWidget(container)
        self.setLayout(layout)
        
        # Timer
        self.elapsed_seconds = 0
        self.timer = QTimer()
        self.timer.timeout.connect(self.update_timer)
        self.timer.start(1000)
        
    def update_timer(self):
        """Update recording timer"""
        self.elapsed_seconds += 1
        minutes = self.elapsed_seconds // 60
        seconds = self.elapsed_seconds % 60
        self.timer_label.setText(f"{minutes:02d}:{seconds:02d}")
        
    def closeEvent(self, event):
        """Handle close event"""
        self.timer.stop()
        super().closeEvent(event)


class ScreenRecorder(QObject):
    """Screen Recorder Manager"""
    recording_stopped = pyqtSignal(str)
    area_selection_started = pyqtSignal()
    
    def __init__(self):
        super().__init__()
        self.recorder_thread = None
        self.overlay = None
        self.is_recording = False
        self.output_path = None
        self.recording_area = None
        
    def start_recording(self, save_directory, area=None):
        """Start screen recording"""
        if self.is_recording:
            return
            
        # Store the recording area
        self.recording_area = area
            
        # Generate filename
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"Snippy_Recording_{timestamp}.mp4"
        self.output_path = os.path.join(save_directory, filename)
        
        # Create and start recorder thread
        self.recorder_thread = RecorderThread(self.output_path, area=area)
        self.recorder_thread.start()
        
        # Show overlay
        self.overlay = RecordingOverlay(recording_area=area)
        self.overlay.stop_requested.connect(self.stop_recording)
        self.overlay.show()
        
        self.is_recording = True
        
    def stop_recording(self):
        """Stop screen recording"""
        if not self.is_recording:
            return
            
        # Stop recorder thread
        if self.recorder_thread:
            self.recorder_thread.stop()
            self.recorder_thread.wait()
            
        # Close overlay
        if self.overlay:
            self.overlay.close()
            self.overlay = None
            
        self.is_recording = False
        self.recording_area = None
        self.recording_stopped.emit(self.output_path)
