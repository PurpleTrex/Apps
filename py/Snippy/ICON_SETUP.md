# Icon Setup Instructions

## Adding an Icon to Your Snippy Executable

### Step 1: Get Your Icon File
1. Create or find an icon file in `.ico` format
2. Recommended size: 256x256 pixels (Windows will scale automatically)
3. Name it something like `snippy_icon.ico` or `app_icon.ico`

### Step 2: Place the Icon
Put your `.ico` file in the project root directory:
```
c:\Users\purple\Desktop\Dev\py\Snippy\
├── main.py
├── Snippy.spec
├── your_icon.ico  ← Place it here
└── ...
```

### Step 3: Update Snippy.spec
The icon line in your spec file should look like:
```python
icon='your_icon.ico',  # Replace with your actual icon filename
```

### Step 4: Rebuild
Run `.\build_exe.bat` to rebuild with the new icon.

## Quick Icon Creation Options:

1. **Online Converter**: Convert PNG/JPG to ICO at https://convertio.co/png-ico/
2. **GIMP/Photoshop**: Export as ICO format
3. **Windows**: Right-click any image → "Convert to ICO" (with appropriate software)

## Example Icon Names:
- `app_icon.ico`
- `snippy_icon.ico`
- `camera_icon.ico`
- `screenshot_icon.ico`