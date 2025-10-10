# Snippy - Modern Snipping Tool

A full-featured, modern screenshot and screen recording tool for Windows, inspired by Windows Snipping Tool but better!

## ✨ Key Features

### 📸 Screenshot Capture Modes
- **Rectangular Snip** - Select a rectangular area to capture with live preview
- **Full Screen** - Capture the entire screen instantly  
- **Window Snip** - Capture a specific window area
- **Free-form Snip** - Draw a custom shape to capture

### 🎥 Screen Recording with Area Selection
- **Select area FIRST, then record** - Choose exactly what to record before starting
- **Live screen preview** - See what you're selecting in real-time (no frozen screen!)
- **Real-time recording overlay** with timer and area dimensions
- **One-click stop** recording button
- **High-quality MP4 output** at 30 FPS

### 🎨 Modern Interface
- Clean, dark-themed UI inspired by modern design
- **Live selection overlay** - See exactly what you're capturing
- **Visual feedback** - Green border for recording, blue for screenshots
- Real-time dimension display during selection
- System tray integration for quick access
- Customizable save location
- Delay timer for captures (0-10 seconds)

### 💾 Smart Saving
- Auto-saves to Pictures/Snippy folder
- Customizable save location
- Timestamped filenames (never overwrite!)
- System notifications on save

## 🚀 Quick Start

### Installation

1. **Install Python 3.7+** if you don't have it

2. **Install dependencies:**
```bash
pip install -r requirements.txt
```

3. **Run the application:**
```bash
python main.py
```

Or double-click `run.bat` on Windows!

## 📖 How to Use

### Taking Screenshots

1. **Launch Snippy**
2. **Select capture mode** from dropdown:
   - 📐 Rectangular Snip (recommended)
   - 🖥️ Full Screen
   - 🪟 Window Snip
   - ✏️ Free-form Snip
3. *Optional:* Set delay timer (useful for capturing menus)
4. **Click "📷 New Capture"**
5. **For area modes:**
   - A frozen screenshot appears with dark overlay
   - Click and drag to select the area
   - Release to capture
   - Press ESC to cancel
6. Screenshot is **automatically saved**!

### Recording Screen

1. **Click "🎥 Screen Record"**
2. **Selection overlay appears** (live, not frozen!)
   - You can see your screen in real-time
   - Green border shows recording will use this color
3. **Click and drag to select recording area**
   - Dimensions shown in real-time
   - Position apps/windows exactly as you want
4. **Release to start recording**
   - Recording overlay appears in top-right
   - Timer shows recording duration
   - Area dimensions displayed
5. **Click "⏹️ Stop Recording"** when done
6. Video is **automatically saved as MP4**!

### System Tray

- App minimizes to system tray when closed
- Right-click tray icon for:
  - Show Snippy
  - Quick Capture
  - Exit
- Double-click tray icon to restore window

### Keyboard Shortcuts

- **ESC** - Cancel current capture/selection

## 🎯 Pro Tips

### For Perfect Recordings

1. Click "Screen Record"
2. **Set up your window/content** while selection overlay is visible
3. The screen is **LIVE** - not frozen!
4. **Position everything perfectly**
5. Select the area
6. Recording starts with exactly what you want

### For Better Screenshots

- Use delay timer to capture dropdown menus or tooltips
- Rectangular mode is fastest for most tasks
- Free-form mode is great for irregular shapes
- Full screen captures everything instantly

### Saving Tips

- Default location: `C:\Users\YourName\Pictures\Snippy\`
- Change save location anytime via "📁 Change Save Location"
- Files are timestamped: `Snippy_20250109_143052.png`
- Recordings: `Snippy_Recording_20250109_143052.mp4`

## 🛠️ Technical Details

### File Structure

```
Snippy/
├── main.py                 # Application entry point
├── requirements.txt        # Python dependencies
├── run.bat                 # Windows launcher
├── gui/
│   ├── main_window.py      # Main application window
│   ├── selection_window.py # Live area selection overlay
│   └── screen_recorder.py  # Screen recording engine
├── utils/
│   └── screenshot.py       # Screenshot capture utilities
└── resources/              # Application resources
```

### Technologies Used

- **PyQt5** - Modern, native-looking GUI framework
- **Pillow (PIL)** - Image processing and manipulation
- **mss** - Lightning-fast, multi-monitor screenshot capture
- **OpenCV (cv2)** - High-quality video recording
- **NumPy** - Efficient array operations for video frames

### Video Specifications

- **Format:** MP4 (MPEG-4)
- **Codec:** mp4v
- **Frame Rate:** 30 FPS
- **Quality:** High (no compression artifacts)
- **Audio:** Not recorded (video only)

## 🎨 Visual Differences

### Screenshot Mode
- **Dark overlay** with frozen screen
- **Blue border** around selection
- Shows what will be captured exactly

### Recording Mode  
- **Light overlay** (60% transparent)
- **Green border** around selection
- **LIVE screen** - not frozen!
- Perfect for setting up before recording

## ⚙️ Configuration

### Default Save Location
```
C:\Users\<YourUsername>\Pictures\Snippy\
```

Change via "📁 Change Save Location" button

### File Naming Convention
- **Screenshots:** `Snippy_YYYYMMDD_HHMMSS.png`
- **Recordings:** `Snippy_Recording_YYYYMMDD_HHMMSS.mp4`

Example: `Snippy_20250109_153045.png`

## 🐛 Troubleshooting

### Application won't start
```bash
# Reinstall dependencies
pip install -r requirements.txt --force-reinstall
```

### Screenshots are black/empty
- Run as administrator
- Check Windows display scaling settings
- Update graphics drivers

### Recording issues
- Ensure sufficient disk space
- Close other screen recording apps
- Try selecting a smaller area
- Update OpenCV: `pip install opencv-python --upgrade`

### Selection overlay not showing
- Check if running on correct monitor
- Try windowed mode first
- Restart the application

## 📝 Requirements

- **OS:** Windows 7/8/10/11
- **Python:** 3.7 or higher
- **RAM:** 2GB minimum (4GB recommended for recording)
- **Disk:** 100MB for app + space for recordings

## 🆚 Why Snippy vs Windows Snipping Tool?

| Feature | Snippy | Windows Snipping Tool |
|---------|--------|----------------------|
| Live recording preview | ✅ Yes | ❌ No |
| Area selection for recording | ✅ Yes | ❌ No |
| Modern dark UI | ✅ Yes | ❌ No |
| Screen recording | ✅ Yes | ⚠️ Limited |
| Real-time dimensions | ✅ Yes | ❌ No |
| System tray | ✅ Yes | ❌ No |
| Free-form capture | ✅ Yes | ✅ Yes |
| Delay timer | ✅ Yes | ✅ Yes |

## 📄 License

This project is open source and available for personal and educational use.

## 🤝 Contributing

Found a bug? Have a feature request? Feel free to create an issue!

## ❤️ Credits

Created with passion for modern Windows users who need a powerful, user-friendly snipping and recording tool.

**Enjoy capturing! 📸🎥**
