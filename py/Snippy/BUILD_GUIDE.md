# Building Snippy as an Executable

## Quick Build

### Option 1: Use the Build Script (Easiest!)
1. Double-click `build_exe.bat`
2. Wait for the build to complete
3. Find your executable in `dist\Snippy.exe`

### Option 2: Manual Build
```bash
# Install PyInstaller
pip install pyinstaller

# Build the executable
pyinstaller --clean --noconfirm Snippy.spec
```

## What Gets Created

```
dist/
  └── Snippy.exe          ← Your standalone executable!
```

## Important Features

### ✅ No CMD Window!
The `.spec` file is configured with `console=False`, which means:
- No black command prompt window appears
- Clean, professional launch
- Just your GUI window

### ✅ Single File
Everything is packed into one `.exe` file:
- All Python code
- All dependencies (PyQt5, OpenCV, etc.)
- All libraries
- ~200-300 MB total size

### ✅ Portable
- Copy `Snippy.exe` anywhere
- No Python installation needed
- No dependencies needed
- Runs on any Windows PC

## Distribution

### To Share Snippy:
1. Build the executable
2. Copy `dist\Snippy.exe` 
3. Send to anyone!

They can just double-click and run it. No installation needed!

### Creating a Desktop Shortcut:
1. Right-click `dist\Snippy.exe`
2. Choose "Create shortcut"
3. Drag shortcut to Desktop
4. Optional: Rename to just "Snippy"

## Build Settings Explained

```python
# Snippy.spec file
console=False          # ← NO CMD WINDOW!
name='Snippy'          # ← Output filename
upx=True              # ← Compress the exe
onefile=True          # ← Single exe file
```

## Troubleshooting

### Build Fails
```bash
# Clean previous builds
rmdir /s /q build dist
del Snippy.spec

# Rebuild
pyinstaller --clean --noconfirm Snippy.spec
```

### Missing Modules
If you get "module not found" errors, add to `Snippy.spec`:
```python
hiddenimports=[
    'your_module_here',
]
```

### Antivirus False Positive
Some antivirus may flag the exe (false positive):
- Add exception in your antivirus
- This is normal for PyInstaller exes
- Not a virus - just packed Python code

### Large File Size
The exe will be 200-300 MB because it includes:
- Python runtime
- PyQt5 (large GUI library)
- OpenCV (video library)
- NumPy and other dependencies

This is normal and unavoidable.

## Advanced Options

### Add an Icon
1. Create or download a `.ico` file
2. Save as `icon.ico` in the Snippy folder
3. Update `Snippy.spec`:
```python
icon='icon.ico'
```

### Add Version Info
Create `version_info.txt`:
```
VSVersionInfo(
  ffi=FixedFileInfo(
    filevers=(1, 0, 0, 0),
    prodvers=(1, 0, 0, 0),
    mask=0x3f,
    flags=0x0,
    OS=0x40004,
    fileType=0x1,
    subtype=0x0,
    date=(0, 0)
  ),
  kids=[
    StringFileInfo([
      StringTable('040904B0', [
        StringStruct('CompanyName', 'Your Name'),
        StringStruct('FileDescription', 'Snippy - Modern Snipping Tool'),
        StringStruct('FileVersion', '1.0.0'),
        StringStruct('ProductName', 'Snippy'),
        StringStruct('ProductVersion', '1.0.0')
      ])
    ]),
    VarFileInfo([VarStruct('Translation', [1033, 1200])])
  ]
)
```

Then update spec:
```python
version_file='version_info.txt'
```

## Build Times

- First build: 3-5 minutes
- Subsequent builds: 1-2 minutes
- Depends on your PC speed

## File Locations

After building:
```
Snippy/
├── build/              ← Temporary build files (can delete)
├── dist/
│   └── Snippy.exe     ← YOUR EXE IS HERE! 
├── Snippy.spec        ← Build configuration
└── build_exe.bat      ← Build script
```

## Testing the Executable

1. Navigate to `dist\Snippy.exe`
2. Double-click it
3. Should launch with NO CMD window
4. All features should work
5. Creates save folder automatically

## Distribution Checklist

Before sharing:
- [x] Build completes successfully
- [x] No CMD window appears
- [x] All features work (screenshot, record)
- [x] Save location works
- [x] Tray icon appears
- [x] No errors in normal use

## File Size Optimization

Current: ~250 MB
Cannot reduce much because:
- PyQt5: ~150 MB
- OpenCV: ~50 MB
- Python runtime: ~30 MB
- Other libraries: ~20 MB

If size is critical:
- Use `upx=True` (already enabled)
- Cannot remove dependencies
- Consider 7-Zip self-extracting archive

## Auto-Start on Windows

To make Snippy start with Windows:
1. Press Win+R
2. Type: `shell:startup`
3. Create shortcut to `Snippy.exe` there

## Updating

When you make code changes:
1. Update your Python files
2. Run `build_exe.bat` again
3. New exe in `dist\Snippy.exe`
4. Replace old version

## Common Issues

### "VCRUNTIME140.dll not found"
Solution: Install Microsoft Visual C++ Redistributable
https://aka.ms/vs/17/release/vc_redist.x64.exe

### Exe won't run on other PCs
- Check Windows version compatibility
- Install VC++ Redistributable
- Run as administrator

### Slow startup
- First launch is slower (Windows checks signature)
- Subsequent launches are faster
- Normal behavior for PyInstaller

## Success!

When build completes:
```
dist\Snippy.exe  ← 250 MB, ready to run!
```

✨ No Python needed
✨ No CMD window  
✨ Runs anywhere
✨ Professional look

Enjoy your standalone Snippy! 🚀
