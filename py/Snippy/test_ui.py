"""
Visual UI Test - Shows the new modern interface
Run this to see the updated GUI design
"""

import sys
from PyQt5.QtWidgets import QApplication, QMessageBox
from gui.main_window import MainWindow

def show_ui_features():
    """Display UI features information"""
    app = QApplication(sys.argv)
    
    # Show features message
    msg = QMessageBox()
    msg.setWindowTitle("Snippy - Modern UI Update")
    msg.setIcon(QMessageBox.Information)
    msg.setText("✨ New Modern Interface Features ✨")
    msg.setInformativeText(
        "🎨 Gradient backgrounds and buttons\n"
        "🎯 Improved dropdown with better visibility\n"
        "📋 Card-based layout design\n"
        "🖱️ Hover effects on all interactive elements\n"
        "🎭 Color-coded sections\n"
        "✨ Smooth rounded corners\n"
        "🔵 Blue accent color throughout\n"
        "📐 Better spacing and padding\n\n"
        "Click OK to launch Snippy!"
    )
    msg.setStandardButtons(QMessageBox.Ok | QMessageBox.Cancel)
    msg.setDefaultButton(QMessageBox.Ok)
    
    result = msg.exec_()
    
    if result == QMessageBox.Ok:
        window = MainWindow()
        window.show()
        sys.exit(app.exec_())
    else:
        sys.exit(0)

if __name__ == '__main__':
    show_ui_features()
