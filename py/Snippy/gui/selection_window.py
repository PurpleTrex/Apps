"""
Selection Window for capturing screen areas
"""

from PyQt5.QtWidgets import QWidget, QApplication, QRubberBand, QDesktopWidget
from PyQt5.QtCore import Qt, QRect, QPoint, pyqtSignal, QTimer
from PyQt5.QtGui import QPainter, QColor, QPen, QPixmap, QCursor, QPainterPath, QPolygon, QRegion, QBrush, QFont
import mss
from PIL import Image
import io

class SelectionWindow(QWidget):
    capture_completed = pyqtSignal(object)  # For screenshots
    capture_cancelled = pyqtSignal()
    area_selected = pyqtSignal(QRect)  # For recording area selection
    
    def __init__(self, mode="rectangular", for_recording=False):
        super().__init__()
        self.mode = mode  # rectangular, window, freeform
        self.for_recording = for_recording  # True if selecting area for recording
        self.setWindowFlags(Qt.FramelessWindowHint | Qt.WindowStaysOnTopHint | Qt.Tool)
        self.setAttribute(Qt.WA_TranslucentBackground)
        
        # Get all screens geometry
        desktop = QDesktopWidget()
        self.full_rect = QRect()
        for i in range(desktop.screenCount()):
            self.full_rect = self.full_rect.united(desktop.screenGeometry(i))
        
        self.setGeometry(self.full_rect)
        
        # Only capture screenshot for screenshot mode, not for recording
        if not self.for_recording:
            self.screenshot = self.capture_screen()
        else:
            self.screenshot = None
        
        # Selection tracking
        self.origin = QPoint()
        self.end_point = QPoint()
        self.rubber_band = QRubberBand(QRubberBand.Rectangle, self)
        self.is_selecting = False
        
        # Free-form mode
        self.freeform_points = []
        
        # Set crosshair cursor
        self.setCursor(Qt.CrossCursor)
        
    def capture_screen(self):
        """Capture the entire screen"""
        try:
            with mss.mss() as sct:
                # Capture all monitors as one
                monitor = sct.monitors[0]  # Monitor 0 is all monitors combined
                screenshot = sct.grab(monitor)
                
                # Convert to PIL Image
                img = Image.frombytes('RGB', screenshot.size, screenshot.rgb)
                
                # Convert to QPixmap
                img_bytes = io.BytesIO()
                img.save(img_bytes, format='PNG')
                pixmap = QPixmap()
                pixmap.loadFromData(img_bytes.getvalue())
                
                return pixmap
        except Exception as e:
            print(f"Error capturing screen: {e}")
            # Return empty pixmap
            return QPixmap(self.full_rect.size())
            
    def paintEvent(self, event):
        """Paint the overlay and selection"""
        painter = QPainter(self)
        painter.setRenderHint(QPainter.Antialiasing)
        
        if self.for_recording:
            # For recording mode: lighter overlay, no frozen screenshot
            # Very light transparent overlay so you can see the live screen
            overlay = QColor(0, 0, 0, 60)
            painter.fillRect(self.rect(), overlay)
        else:
            # For screenshot mode: show frozen screenshot
            if self.screenshot:
                painter.drawPixmap(self.full_rect.topLeft(), self.screenshot)
            
            # Semi-transparent overlay
            overlay = QColor(0, 0, 0, 100)
            painter.fillRect(self.rect(), overlay)
        
        if self.is_selecting or (self.origin != QPoint() and self.end_point != QPoint()):
            if self.mode == "rectangular" or self.mode == "window":
                # Draw selection rectangle
                selection_rect = QRect(self.origin, self.end_point).normalized()
                
                if not self.for_recording and self.screenshot:
                    # For screenshot mode: clear overlay to show frozen image
                    painter.setCompositionMode(QPainter.CompositionMode_SourceOver)
                    painter.setClipRect(selection_rect)
                    painter.fillRect(selection_rect, Qt.transparent)
                    painter.drawPixmap(self.full_rect.topLeft(), self.screenshot)
                    painter.setClipping(False)
                else:
                    # For recording mode: just make it more transparent
                    painter.setCompositionMode(QPainter.CompositionMode_SourceOver)
                    painter.fillRect(selection_rect, QColor(255, 255, 255, 20))
                
                # Draw selection border (brighter for recording mode)
                border_color = QColor(0, 255, 0) if self.for_recording else QColor(0, 120, 215)
                pen = QPen(border_color, 3, Qt.SolidLine)
                painter.setPen(pen)
                painter.drawRect(selection_rect)
                
                # Draw corner handles
                handle_size = 8
                painter.setBrush(QBrush(border_color))
                corners = [
                    selection_rect.topLeft(),
                    selection_rect.topRight(),
                    selection_rect.bottomLeft(),
                    selection_rect.bottomRight()
                ]
                for corner in corners:
                    painter.drawEllipse(corner, handle_size, handle_size)
                
                # Draw dimensions
                width = selection_rect.width()
                height = selection_rect.height()
                dim_text = f"{width} × {height}"
                
                # Draw dimension background
                font = QFont()
                font.setPointSize(11)
                font.setBold(True)
                painter.setFont(font)
                metrics = painter.fontMetrics()
                text_rect = metrics.boundingRect(dim_text)
                
                # Position above selection if possible, otherwise below
                if selection_rect.top() > 30:
                    text_rect.moveCenter(QPoint(selection_rect.center().x(), selection_rect.top() - 15))
                else:
                    text_rect.moveCenter(QPoint(selection_rect.center().x(), selection_rect.bottom() + 15))
                
                text_rect.adjust(-8, -4, 8, 4)
                
                painter.setPen(Qt.NoPen)
                bg_color = QColor(0, 200, 0, 220) if self.for_recording else QColor(0, 120, 215, 220)
                painter.setBrush(QBrush(bg_color))
                painter.drawRoundedRect(text_rect, 4, 4)
                
                painter.setPen(Qt.white)
                painter.drawText(text_rect, Qt.AlignCenter, dim_text)
                
            elif self.mode == "freeform" and len(self.freeform_points) > 1:
                # Draw free-form selection
                path = QPainterPath()
                path.moveTo(self.freeform_points[0])
                for point in self.freeform_points[1:]:
                    path.lineTo(point)
                
                # Draw the path
                pen = QPen(QColor(0, 120, 215), 3, Qt.SolidLine)
                painter.setPen(pen)
                painter.drawPath(path)
        
        # Draw instructions
        if not self.is_selecting and self.origin == QPoint():
            painter.setPen(Qt.white)
            font = QFont()
            font.setPointSize(14)
            font.setBold(True)
            painter.setFont(font)
            
            if self.for_recording:
                instruction = "🎥 Select area to record (ESC to cancel)"
            else:
                instruction = "📸 Click and drag to capture area (ESC to cancel)"
            
            text_rect = painter.fontMetrics().boundingRect(instruction)
            text_rect.moveCenter(self.rect().center())
            
            # Draw background
            bg_rect = text_rect.adjusted(-20, -10, 20, 10)
            painter.setPen(Qt.NoPen)
            bg_color = QColor(0, 150, 0, 200) if self.for_recording else QColor(0, 0, 0, 200)
            painter.setBrush(QBrush(bg_color))
            painter.drawRoundedRect(bg_rect, 5, 5)
            
            # Draw text
            painter.setPen(Qt.white)
            painter.drawText(text_rect, Qt.AlignCenter, instruction)
                
    def mousePressEvent(self, event):
        """Handle mouse press"""
        if event.button() == Qt.LeftButton:
            self.is_selecting = True
            self.origin = event.pos()
            self.end_point = event.pos()
            
            if self.mode == "freeform":
                self.freeform_points = [event.pos()]
                
            self.update()
            
    def mouseMoveEvent(self, event):
        """Handle mouse move"""
        if self.is_selecting:
            self.end_point = event.pos()
            
            if self.mode == "freeform":
                self.freeform_points.append(event.pos())
                
            self.update()
            
    def mouseReleaseEvent(self, event):
        """Handle mouse release"""
        if event.button() == Qt.LeftButton and self.is_selecting:
            self.is_selecting = False
            self.end_point = event.pos()
            
            # Check if this is for recording or screenshot
            if self.for_recording:
                # Emit the selected area for recording
                selection_rect = QRect(self.origin, self.end_point).normalized()
                if selection_rect.width() >= 10 and selection_rect.height() >= 10:
                    self.area_selected.emit(selection_rect)
                    self.close()
                else:
                    self.capture_cancelled.emit()
                    self.close()
            else:
                # Capture the selected area for screenshot
                self.capture_selection()
            
    def keyPressEvent(self, event):
        """Handle key press"""
        if event.key() == Qt.Key_Escape:
            self.capture_cancelled.emit()
            self.close()
            
    def capture_selection(self):
        """Capture the selected area"""
        try:
            if self.mode == "rectangular" or self.mode == "window":
                selection_rect = QRect(self.origin, self.end_point).normalized()
                
                if selection_rect.width() < 5 or selection_rect.height() < 5:
                    self.capture_cancelled.emit()
                    self.close()
                    return
                
                # Capture the actual screen area (not from frozen screenshot)
                # This ensures we get the real content at the moment of capture
                with mss.mss() as sct:
                    # Define the monitor region to capture
                    monitor = {
                        'top': selection_rect.y(),
                        'left': selection_rect.x(),
                        'width': selection_rect.width(),
                        'height': selection_rect.height()
                    }
                    
                    # Capture the area
                    screenshot = sct.grab(monitor)
                    
                    # Convert to PIL Image
                    pil_image = Image.frombytes('RGB', screenshot.size, screenshot.rgb)
                    
                self.capture_completed.emit(pil_image)
                
            elif self.mode == "freeform":
                if len(self.freeform_points) < 3:
                    self.capture_cancelled.emit()
                    self.close()
                    return
                    
                # Get bounding rectangle
                polygon = QPolygon(self.freeform_points)
                bounding_rect = polygon.boundingRect()
                
                # Capture the bounding area from actual screen
                with mss.mss() as sct:
                    monitor = {
                        'top': bounding_rect.y(),
                        'left': bounding_rect.x(),
                        'width': bounding_rect.width(),
                        'height': bounding_rect.height()
                    }
                    
                    screenshot = sct.grab(monitor)
                    img = Image.frombytes('RGB', screenshot.size, screenshot.rgb)
                    
                    # Convert to QPixmap for masking
                    img_bytes = io.BytesIO()
                    img.save(img_bytes, format='PNG')
                    pixmap = QPixmap()
                    pixmap.loadFromData(img_bytes.getvalue())
                
                # Create a mask
                mask = QPixmap(bounding_rect.size())
                mask.fill(Qt.transparent)
                
                mask_painter = QPainter(mask)
                mask_painter.setRenderHint(QPainter.Antialiasing)
                
                # Translate points to mask coordinates
                translated_points = [QPoint(p.x() - bounding_rect.x(), p.y() - bounding_rect.y()) 
                                   for p in self.freeform_points]
                mask_polygon = QPolygon(translated_points)
                
                mask_painter.setBrush(Qt.white)
                mask_painter.setPen(Qt.NoPen)
                mask_painter.drawPolygon(mask_polygon)
                mask_painter.end()
                
                # Apply mask
                result = QPixmap(pixmap.size())
                result.fill(Qt.transparent)
                
                result_painter = QPainter(result)
                result_painter.setRenderHint(QPainter.Antialiasing)
                result_painter.setClipRegion(QRegion(mask_polygon))
                result_painter.drawPixmap(0, 0, pixmap)
                result_painter.end()
                
                # Convert to PIL Image
                img_bytes = io.BytesIO()
                result.save(img_bytes, 'PNG')
                img_bytes.seek(0)
                pil_image = Image.open(img_bytes)
                
                self.capture_completed.emit(pil_image)
                
        except Exception as e:
            print(f"Error capturing selection: {e}")
            import traceback
            traceback.print_exc()
            self.capture_cancelled.emit()
            
        self.close()
