# Snippy Executable Usage Guide

## 🎉 Build Complete!

Your Snippy application has been successfully compiled into a standalone executable!

## 📂 Location

The executable is located at: `dist/Snippy.exe`

## 📝 File Details

- **File Size**: ~101 MB
- **Type**: Standalone Windows Executable
- **Dependencies**: None required (all bundled)

## 🚀 How to Run

1. **Direct Launch**: Double-click `Snippy.exe` in the `dist` folder
2. **Command Line**: Navigate to the `dist` folder and run `.\Snippy.exe`
3. **Desktop Shortcut**: Create a shortcut to `Snippy.exe` for easy access

## 🔧 Rebuilding

To rebuild the executable after making changes to your code:

1. Run the build script: `.\build_exe.bat`
2. Or use the command directly:
   ```batch
   C:/Users/purple/AppData/Local/Microsoft/WindowsApps/python3.10.exe -m PyInstaller --clean --noconfirm Snippy.spec
   ```

## 📋 Features

Your Snippy tool includes:
- ✅ Screenshot capture with area selection
- ✅ Screen recording functionality
- ✅ Modern PyQt5-based UI
- ✅ Image editing capabilities
- ✅ Save/export options

## 🐛 Troubleshooting

If the executable doesn't run:

1. **Check Windows Defender**: The exe might be flagged - add it to exclusions
2. **Run as Administrator**: Right-click and "Run as administrator"
3. **Check Dependencies**: All required libraries are bundled, but some Windows updates might be needed

## 🔄 Updating

After making code changes:
1. Save your changes
2. Run `.\build_exe.bat`
3. The new executable will replace the old one in the `dist` folder

## 📦 Distribution

You can distribute just the `Snippy.exe` file - no installation required!
Users don't need Python or any dependencies installed.

---
*Built with PyInstaller 6.3.0 on Python 3.10.11*