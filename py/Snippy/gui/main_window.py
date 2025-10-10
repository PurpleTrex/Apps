"""
Main Window for Snippy Application
"""

from PyQt5.QtWidgets import (QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, 
                             QPushButton, QLabel, QComboBox, QSpinBox, QFileDialog,
                             QSystemTrayIcon, QMenu, QAction, QMessageBox, QApplication, QSizePolicy, QLineEdit)
from PyQt5.QtCore import Qt, QTimer, QSize
from PyQt5.QtGui import QIcon, QPixmap, QPalette, QColor, QFont, QPainter, QBrush, QPen, QImage, QTransform
from gui.selection_window import SelectionWindow
from gui.screen_recorder import ScreenRecorder
from utils.screenshot import ScreenshotCapture
import os
import sys
from datetime import datetime

def resource_path(relative_path):
    """Get absolute path to resource, works for dev and for PyInstaller"""
    try:
        # PyInstaller creates a temp folder and stores path in _MEIPASS
        base_path = sys._MEIPASS
    except Exception:
        base_path = os.path.abspath(".")
    return os.path.join(base_path, relative_path)

class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Snippy - Modern Snipping Tool")
        self.setGeometry(100, 100, 580, 780)
        self.setMinimumSize(550, 750)
        self.setMaximumSize(700, 900)
        
        # Set window icon
        icon_path = resource_path("icon.ico")
        if os.path.exists(icon_path):
            self.setWindowIcon(QIcon(icon_path))
        
        # Modern Dark/Neutral Black Theme
        self.setStyleSheet("""
            QMainWindow {
                background: #1C1C1C;
                color: #FFFFFF;
            }
            
            QLabel {
                color: #FFFFFF;
                font-family: 'Segoe UI', 'Segoe UI Variable Display', system-ui, sans-serif;
                font-size: 14px;
                font-weight: 400;
            }
            
            QPushButton {
                background: #0078D4;
                color: #FFFFFF;
                border: 1px solid #0078D4;
                padding: 10px 20px;
                font-family: 'Segoe UI', 'Segoe UI Variable Display', system-ui, sans-serif;
                font-size: 14px;
                font-weight: 600;
                border-radius: 6px;
                text-align: left;
                min-height: 36px;
                icon-size: 24px;
            }
            QPushButton:hover {
                background: #106EBE;
                border: 1px solid #106EBE;
            }
            QPushButton:pressed {
                background: #005A9E;
                border: 1px solid #005A9E;
            }
            QPushButton:focus {
                border: 2px solid #60CDFF;
                outline: none;
            }
            
            QComboBox {
                background: #2D2D2D;
                color: #FFFFFF;
                border: 1px solid #404040;
                padding: 10px 14px;
                padding-right: 36px;
                border-radius: 6px;
                font-family: 'Segoe UI', 'Segoe UI Variable Display', system-ui, sans-serif;
                font-size: 14px;
                font-weight: 400;
                min-height: 36px;
            }
            QComboBox:hover {
                border: 1px solid #606060;
                background: #343434;
            }
            QComboBox:focus {
                border: 2px solid #60CDFF;
                outline: none;
                background: #2D2D2D;
            }
            QComboBox::drop-down {
                subcontrol-origin: padding;
                subcontrol-position: top right;
                width: 36px;
                border-left: none;
                border-top-right-radius: 6px;
                border-bottom-right-radius: 6px;
                background: transparent;
            }
            QComboBox::down-arrow {
                width: 12px;
                height: 12px;
                image: none;
                border-left: 5px solid transparent;
                border-right: 5px solid transparent;
                border-top: 5px solid #FFFFFF;
            }
            QComboBox QAbstractItemView {
                background: #2D2D2D;
                color: #FFFFFF;
                selection-background-color: #404040;
                selection-color: #FFFFFF;
                border: 1px solid #404040;
                border-radius: 6px;
                outline: none;
                padding: 6px;
            }
            QComboBox QAbstractItemView::item {
                padding: 10px 14px;
                border-radius: 4px;
                margin: 2px;
            }
            QComboBox QAbstractItemView::item:hover {
                background: #404040;
            }
            QComboBox QAbstractItemView::item:selected {
                background: #0078D4;
                color: #FFFFFF;
            }
            
            QLineEdit {
                background: #2D2D2D;
                color: #FFFFFF;
                border: 1px solid #404040;
                padding: 10px 14px;
                border-radius: 6px;
                font-family: 'Segoe UI', 'Segoe UI Variable Display', system-ui, sans-serif;
                font-size: 14px;
                font-weight: 400;
                min-height: 36px;
            }
            QLineEdit:hover {
                border: 1px solid #606060;
                background: #343434;
            }
            QLineEdit:focus {
                border: 2px solid #60CDFF;
                outline: none;
                background: #2D2D2D;
            }
            
            QSpinBox {
                background: #2D2D2D;
                color: #FFFFFF;
                border: 1px solid #404040;
                padding: 10px 14px;
                border-radius: 6px;
                font-family: 'Segoe UI', 'Segoe UI Variable Display', system-ui, sans-serif;
                font-size: 14px;
                font-weight: 400;
                min-height: 36px;
            }
            QSpinBox:hover {
                border: 1px solid #606060;
                background: #343434;
            }
            QSpinBox:focus {
                border: 2px solid #60CDFF;
                outline: none;
                background: #2D2D2D;
            }
            QSpinBox::up-button, QSpinBox::down-button {
                background: #404040;
                border-left: 1px solid #505050;
                width: 32px;
                subcontrol-origin: border;
            }
            QSpinBox::up-button {
                subcontrol-position: top right;
                border-top-right-radius: 5px;
                border-bottom: 1px solid #303030;
            }
            QSpinBox::down-button {
                subcontrol-position: bottom right;
                border-bottom-right-radius: 5px;
            }
            QSpinBox::up-button:hover {
                background: #505050;
            }
            QSpinBox::down-button:hover {
                background: #505050;
            }
            QSpinBox::up-button:pressed {
                background: #0078D4;
            }
            QSpinBox::down-button:pressed {
                background: #0078D4;
            }
        """)
        
        self.screenshot_capture = ScreenshotCapture()
        self.screen_recorder = None
        self.delay_timer = QTimer()
        self.delay_timer.setSingleShot(True)
        self.delay_timer.timeout.connect(self.execute_capture)
        
        # Default save directory (set before setup_ui)
        self.save_directory = os.path.join(os.path.expanduser("~"), "Desktop", "Snippy")
        os.makedirs(self.save_directory, exist_ok=True)
        
        self.setup_ui()
        self.setup_tray_icon()
        
    def load_icon(self, icon_name, size=24):
        """Load icon from file with specified size and add light background"""
        from PyQt5.QtGui import QPainter, QBrush, QPen, QImage
        
        icon_path = resource_path(f"{icon_name}.png")
        if os.path.exists(icon_path):
            # Load original icon
            original_pixmap = QPixmap(icon_path)
            if not original_pixmap.isNull():
                # Scale the icon
                scaled_pixmap = original_pixmap.scaled(size, size, Qt.KeepAspectRatio, Qt.SmoothTransformation)
                
                # Convert to image and invert colors to make dark icons light
                image = scaled_pixmap.toImage()
                image.invertPixels(QImage.InvertRgb)
                inverted_pixmap = QPixmap.fromImage(image)
                
                # Create a new pixmap with extra space for background
                final_size = size + 8
                final_pixmap = QPixmap(final_size, final_size)
                final_pixmap.fill(Qt.transparent)
                
                # Draw light circular background
                painter = QPainter(final_pixmap)
                painter.setRenderHint(QPainter.Antialiasing)
                
                # Draw circle background
                painter.setPen(Qt.NoPen)
                painter.setBrush(QBrush(QColor("#505050")))  # Light gray background
                painter.drawEllipse(0, 0, final_size, final_size)
                
                # Draw the inverted icon on top
                x_offset = (final_size - size) // 2
                y_offset = (final_size - size) // 2
                painter.drawPixmap(x_offset, y_offset, inverted_pixmap)
                painter.end()
                
                return QIcon(final_pixmap)
        return QIcon()  # Return empty icon if file not found
        
    def setup_ui(self):
        """Set up the user interface"""
        central_widget = QWidget()
        self.setCentralWidget(central_widget)
        
        main_layout = QVBoxLayout(central_widget)
        main_layout.setSpacing(20)
        main_layout.setContentsMargins(28, 28, 28, 28)
        
        # Header Section - Dark Theme
        header_widget = QWidget()
        header_widget.setStyleSheet("""
            QWidget {
                background: #2D2D2D;
                border: 1px solid #404040;
                border-radius: 10px;
            }
        """)
        header_layout = QVBoxLayout(header_widget)
        header_layout.setContentsMargins(28, 24, 28, 24)
        header_layout.setSpacing(10)
        
        # Title
        title_label = QLabel("Snippy")
        title_font = QFont('Segoe UI', 32, QFont.Weight.DemiBold)
        title_label.setFont(title_font)
        title_label.setAlignment(Qt.AlignCenter)
        title_label.setStyleSheet("color: #FFFFFF; background: transparent;")
        header_layout.addWidget(title_label)
        
        # Subtitle
        subtitle_label = QLabel("Modern Screenshot & Recording Tool")
        subtitle_font = QFont('Segoe UI', 15, QFont.Weight.Normal)
        subtitle_label.setFont(subtitle_font)
        subtitle_label.setAlignment(Qt.AlignCenter)
        subtitle_label.setStyleSheet("color: #C7C7C7; background: transparent;")
        header_layout.addWidget(subtitle_label)
        
        main_layout.addWidget(header_widget)
        
        # Settings Card
        settings_card = QWidget()
        settings_card.setStyleSheet("""
            QWidget {
                background: #2D2D2D;
                border: 1px solid #404040;
                border-radius: 10px;
            }
        """)
        settings_layout = QVBoxLayout(settings_card)
        settings_layout.setContentsMargins(28, 24, 28, 24)
        settings_layout.setSpacing(20)
        
        # Section Label with icon
        settings_layout_header = QHBoxLayout()
        settings_icon = QLabel()
        settings_icon.setPixmap(self.load_icon("settings", 20).pixmap(20, 20))
        settings_title = QLabel("Capture Settings")
        settings_title_font = QFont('Segoe UI', 18, QFont.Weight.DemiBold)
        settings_title.setFont(settings_title_font)
        settings_title.setStyleSheet("color: #FFFFFF; background: transparent; margin-left: 8px;")
        settings_layout_header.addWidget(settings_icon)
        settings_layout_header.addWidget(settings_title)
        settings_layout_header.addStretch()
        settings_layout.addLayout(settings_layout_header)
        
        # Delay Setting
        delay_layout = QVBoxLayout()
        delay_layout.setSpacing(10)
        delay_label = QLabel("Capture Delay (seconds before capture)")
        delay_label_font = QFont('Segoe UI', 15, QFont.Weight.DemiBold)
        delay_label.setFont(delay_label_font)
        delay_label.setStyleSheet("background: transparent; color: #FFFFFF;")
        
        # Regular input box for delay
        self.delay_input = QLineEdit()
        self.delay_input.setPlaceholderText("Type delay amount in seconds (0-10)")
        self.delay_input.setText("0")
        self.delay_input.setMinimumHeight(45)
        self.delay_input.setMaximumHeight(45)
        self.delay_input.setSizePolicy(QSizePolicy.Expanding, QSizePolicy.Fixed)
        self.delay_input.setToolTip("Set delay before capture starts (0-10 seconds)")
        
        delay_layout.addWidget(delay_label)
        delay_layout.addWidget(self.delay_input)
        settings_layout.addLayout(delay_layout)
        
        main_layout.addWidget(settings_card)
        
        # Actions Card
        actions_card = QWidget()
        actions_card.setStyleSheet("""
            QWidget {
                background: #2D2D2D;
                border: 1px solid #404040;
                border-radius: 10px;
            }
        """)
        actions_layout = QVBoxLayout(actions_card)
        actions_layout.setContentsMargins(28, 24, 28, 24)
        actions_layout.setSpacing(18)
        
        # Section Label with icon
        actions_layout_header = QHBoxLayout()
        actions_icon = QLabel()
        actions_icon.setPixmap(self.load_icon("action", 20).pixmap(20, 20))
        actions_title = QLabel("Actions")
        actions_title_font = QFont('Segoe UI', 18, QFont.Weight.DemiBold)
        actions_title.setFont(actions_title_font)
        actions_title.setStyleSheet("color: #FFFFFF; background: transparent; margin-left: 8px;")
        actions_layout_header.addWidget(actions_icon)
        actions_layout_header.addWidget(actions_title)
        actions_layout_header.addStretch()
        actions_layout.addLayout(actions_layout_header)
        
        # Capture Buttons
        button_layout = QVBoxLayout()
        button_layout.setSpacing(14)
        
        self.new_capture_btn = QPushButton("New Capture")
        self.new_capture_btn.setIcon(self.load_icon("capture", 24))
        self.new_capture_btn.setMinimumHeight(60)
        self.new_capture_btn.setMaximumHeight(60)
        self.new_capture_btn.setSizePolicy(QSizePolicy.Expanding, QSizePolicy.Fixed)
        self.new_capture_btn.setCursor(Qt.PointingHandCursor)
        self.new_capture_btn.setToolTip("Take a screenshot using your selected capture mode with optional delay")
        self.new_capture_btn.clicked.connect(self.start_new_capture)
        button_layout.addWidget(self.new_capture_btn)
        
        self.record_btn = QPushButton("Screen Record")
        self.record_btn.setIcon(self.load_icon("record", 24))
        self.record_btn.setMinimumHeight(60)
        self.record_btn.setMaximumHeight(60)
        self.record_btn.setSizePolicy(QSizePolicy.Expanding, QSizePolicy.Fixed)
        self.record_btn.setCursor(Qt.PointingHandCursor)
        self.record_btn.setToolTip("Record your screen activity to create a video file")
        self.record_btn.clicked.connect(self.start_screen_record)
        button_layout.addWidget(self.record_btn)
        
        actions_layout.addLayout(button_layout)
        
        # Settings button
        settings_btn_layout = QHBoxLayout()
        self.save_location_btn = QPushButton("Save Location")
        self.save_location_btn.setIcon(self.load_icon("folder", 20))
        self.save_location_btn.setMinimumHeight(50)
        self.save_location_btn.setMaximumHeight(50)
        self.save_location_btn.setSizePolicy(QSizePolicy.Expanding, QSizePolicy.Fixed)
        self.save_location_btn.setCursor(Qt.PointingHandCursor)
        self.save_location_btn.setToolTip("Choose where your screenshots and recordings will be saved")
        self.save_location_btn.setStyleSheet("""
            QPushButton {
                background: #404040;
                color: #FFFFFF;
                border: 1px solid #606060;
            }
            QPushButton:hover {
                background: #505050;
                border: 1px solid #707070;
            }
            QPushButton:pressed {
                background: #303030;
                border: 1px solid #0078D4;
            }
            QPushButton:focus {
                border: 2px solid #60CDFF;
            }
        """)
        self.save_location_btn.clicked.connect(self.change_save_location)
        settings_btn_layout.addWidget(self.save_location_btn)
        actions_layout.addLayout(settings_btn_layout)
        
        main_layout.addWidget(actions_card)
        
        # Info Section
        info_card = QWidget()
        info_card.setStyleSheet("""
            QWidget {
                background: #252525;
                border: 1px solid #383838;
                border-radius: 8px;
            }
        """)
        info_layout = QVBoxLayout(info_card)
        info_layout.setContentsMargins(20, 16, 20, 16)
        
        # Save location display with icon
        save_info_layout = QHBoxLayout()
        save_icon = QLabel()
        save_icon.setPixmap(self.load_icon("save", 16).pixmap(16, 16))
        
        self.save_location_label = QLabel(f"Saves to: {self.save_directory}")
        info_font = QFont('Segoe UI', 13, QFont.Weight.Normal)
        self.save_location_label.setFont(info_font)
        self.save_location_label.setStyleSheet("""
            color: #C7C7C7; 
            background: transparent;
            padding: 6px;
        """)
        self.save_location_label.setWordWrap(True)
        
        save_info_layout.addWidget(save_icon)
        save_info_layout.addWidget(self.save_location_label)
        save_info_layout.addStretch()
        info_layout.addLayout(save_info_layout)
        
        main_layout.addWidget(info_card)
        
        main_layout.addStretch()
            
    def setup_tray_icon(self):
        """Set up system tray icon"""
        self.tray_icon = QSystemTrayIcon(self)
        
        # Use the same icon as the main window
        icon_path = resource_path("icon.ico")
        if os.path.exists(icon_path):
            self.tray_icon.setIcon(QIcon(icon_path))
        else:
            # Fallback to simple colored icon
            pixmap = QPixmap(32, 32)
            pixmap.fill(QColor("#0078d4"))
            self.tray_icon.setIcon(QIcon(pixmap))
        
        # Tray menu
        tray_menu = QMenu()
        
        show_action = QAction(self.load_icon("show", 16), "Show Snippy", self)
        show_action.triggered.connect(self.show)
        tray_menu.addAction(show_action)
        
        capture_action = QAction(self.load_icon("quick_capture", 16), "Quick Capture", self)
        capture_action.triggered.connect(self.start_new_capture)
        tray_menu.addAction(capture_action)
        
        tray_menu.addSeparator()
        
        quit_action = QAction(self.load_icon("exit", 16), "Exit", self)
        quit_action.triggered.connect(self.quit_application)
        tray_menu.addAction(quit_action)
        
        self.tray_icon.setContextMenu(tray_menu)
        self.tray_icon.activated.connect(self.tray_icon_activated)
        self.tray_icon.show()
        
    def tray_icon_activated(self, reason):
        """Handle tray icon activation"""
        if reason == QSystemTrayIcon.DoubleClick:
            self.show()
            
    def start_new_capture(self):
        """Start a new capture with optional delay"""
        try:
            delay = int(self.delay_input.text())
            # Validate range
            if delay < 0:
                delay = 0
            elif delay > 10:
                delay = 10
        except ValueError:
            # If invalid input, use 0
            delay = 0
            self.delay_input.setText("0")
        
        if delay > 0:
            self.hide()
            self.delay_timer.start(delay * 1000)
        else:
            self.execute_capture()
            
    def execute_capture(self):
        """Execute the screen capture"""
        # Always use rectangular selection window
        self.hide()
        QTimer.singleShot(200, self._show_selection_window)
            
    def _show_selection_window(self):
        """Show selection window after delay"""
        self.selection_window = SelectionWindow(mode="rectangular", for_recording=False)
        self.selection_window.capture_completed.connect(self.on_capture_completed)
        self.selection_window.capture_cancelled.connect(self.on_capture_cancelled)
        self.selection_window.showFullScreen()
            
    def capture_full_screen(self):
        """Capture full screen"""
        self.hide()
        QTimer.singleShot(200, self._do_full_screen_capture)
        
    def _do_full_screen_capture(self):
        """Perform full screen capture"""
        screenshot = self.screenshot_capture.capture_full_screen()
        if screenshot:
            self.save_screenshot(screenshot)
        self.show()
        
    def on_capture_completed(self, screenshot):
        """Handle completed capture"""
        if screenshot:
            self.save_screenshot(screenshot)
        self.show()
        
    def on_capture_cancelled(self):
        """Handle cancelled capture"""
        self.show()
        
    def save_screenshot(self, screenshot):
        """Save screenshot to file"""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"Snippy_{timestamp}.png"
        filepath = os.path.join(self.save_directory, filename)
        
        try:
            screenshot.save(filepath)
            self.show_notification("Screenshot Saved", f"Saved to {filename}")
        except Exception as e:
            QMessageBox.warning(self, "Error", f"Failed to save screenshot: {str(e)}")
            
    def start_screen_record(self):
        """Start or stop screen recording"""
        if self.screen_recorder is None or not self.screen_recorder.is_recording:
            # Start area selection for recording
            self.hide()
            QTimer.singleShot(100, self.show_area_selection_for_recording)
        else:
            # Stop recording
            self.screen_recorder.stop_recording()
            
    def show_area_selection_for_recording(self):
        """Show area selection window for recording"""
        from gui.selection_window import SelectionWindow
        self.selection_window = SelectionWindow(mode="rectangular", for_recording=True)
        self.selection_window.area_selected.connect(self.on_recording_area_selected)
        self.selection_window.capture_cancelled.connect(self.on_recording_cancelled)
        self.selection_window.showFullScreen()
        self.selection_window.raise_()
        self.selection_window.activateWindow()
        
    def on_recording_area_selected(self, area):
        """Handle recording area selection"""
        # Start recording with selected area
        if self.screen_recorder is None:
            self.screen_recorder = ScreenRecorder()
            self.screen_recorder.recording_stopped.connect(self.on_recording_stopped)
            
        self.screen_recorder.start_recording(self.save_directory, area=area)
        
        self.record_btn.setText("⏹️ Stop Recording")
        self.record_btn.setStyleSheet("""
            QPushButton {
                background-color: #d41400;
                color: white;
            }
            QPushButton:hover {
                background-color: #e41400;
            }
        """)
        self.show()
        
    def on_recording_cancelled(self):
        """Handle recording cancelled"""
        self.show()
            
    def on_recording_stopped(self, filepath):
        """Handle recording stopped"""
        self.record_btn.setText("🎥 Screen Record")
        self.record_btn.setStyleSheet("""
            QPushButton {
                background-color: #0078d4;
                color: white;
            }
            QPushButton:hover {
                background-color: #1084d8;
            }
        """)
        
        if filepath:
            filename = os.path.basename(filepath)
            self.show_notification("Recording Saved", f"Saved to {filename}")
            
    def change_save_location(self):
        """Change the save directory"""
        directory = QFileDialog.getExistingDirectory(
            self, 
            "Select Save Directory", 
            self.save_directory
        )
        
        if directory:
            self.save_directory = directory
            self.save_location_label.setText(f"Saves to: {self.save_directory}")
            
    def show_notification(self, title, message):
        """Show system tray notification"""
        self.tray_icon.showMessage(
            title,
            message,
            QSystemTrayIcon.Information,
            3000
        )
        
    def quit_application(self):
        """Quit the application"""
        # Clean up tray icon
        if hasattr(self, 'tray_icon'):
            self.tray_icon.hide()
            self.tray_icon.deleteLater()
        
        # Close any open windows
        if hasattr(self, 'selection_window') and self.selection_window:
            self.selection_window.close()
        
        # Stop any recording
        if hasattr(self, 'screen_recorder') and self.screen_recorder:
            if hasattr(self.screen_recorder, 'is_recording') and self.screen_recorder.is_recording:
                self.screen_recorder.stop_recording()
        
        # Quit the application
        QApplication.quit()
        
    def closeEvent(self, event):
        """Handle window close event"""
        event.ignore()
        self.hide()
        self.show_notification("Snippy", "Application minimized to tray")
