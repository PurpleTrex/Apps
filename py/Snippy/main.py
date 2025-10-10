"""
Snippy - Modern Snipping Tool
A full-featured screenshot and screen recording tool for Windows
"""

import sys
import os
from PyQt5.QtWidgets import QApplication, QMessageBox
from PyQt5.QtGui import QIcon
from PyQt5.QtCore import QSharedMemory
from gui.main_window import MainWindow

def resource_path(relative_path):
    """Get absolute path to resource, works for dev and for PyInstaller"""
    try:
        # PyInstaller creates a temp folder and stores path in _MEIPASS
        base_path = sys._MEIPASS
    except Exception:
        base_path = os.path.abspath(".")
    return os.path.join(base_path, relative_path)

def main():
    app = QApplication(sys.argv)
    app.setStyle('Fusion')
    
    # Single instance check using shared memory
    shared_memory = QSharedMemory("SnippyAppSingleInstance")
    
    if shared_memory.attach():
        # Another instance is already running
        QMessageBox.warning(None, "Snippy Already Running", 
                          "Snippy is already running. Check your system tray.")
        return
    
    if not shared_memory.create(1):
        # Failed to create shared memory
        QMessageBox.warning(None, "Snippy Already Running", 
                          "Snippy is already running. Check your system tray.")
        return
    
    # Set application metadata
    app.setApplicationName("Snippy")
    app.setOrganizationName("Snippy")
    app.setApplicationVersion("1.0.0")
    
    # Set application icon
    icon_path = resource_path("icon.ico")
    if os.path.exists(icon_path):
        app.setWindowIcon(QIcon(icon_path))
    
    window = MainWindow()
    window.show()
    
    exit_code = app.exec_()
    
    # Clean up shared memory on exit
    shared_memory.detach()
    
    sys.exit(exit_code)

if __name__ == '__main__':
    main()
