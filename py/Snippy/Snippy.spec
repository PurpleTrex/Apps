# -*- mode: python ; coding: utf-8 -*-

block_cipher = None

a = Analysis(
    ['main.py'],
    pathex=[],
    binaries=[],
    datas=[
        ('icon.ico', '.'),
        ('icon.png', '.'),
        ('action.png', '.'),
        ('capture.png', '.'),
        ('exit.png', '.'),
        ('folder.png', '.'),
        ('freeform.png', '.'),
        ('fullscreen.png', '.'),
        ('quick_capture.png', '.'),
        ('record.png', '.'),
        ('rectangular.png', '.'),
        ('save.png', '.'),
        ('settings.png', '.'),
        ('show.png', '.'),
        ('up.png', '.'),
        ('window.png', '.'),
    ],
    hiddenimports=[
        'PyQt5.QtCore',
        'PyQt5.QtGui',
        'PyQt5.QtWidgets',
        'PIL',
        'PIL.Image',
        'cv2',
        'numpy',
        'mss',
        'pyautogui',
    ],
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludes=[],
    win_no_prefer_redirects=False,
    win_private_assemblies=False,
    cipher=block_cipher,
    noarchive=False,
)

pyz = PYZ(a.pure, a.zipped_data, cipher=block_cipher)

exe = EXE(
    pyz,
    a.scripts,
    a.binaries,
    a.zipfiles,
    a.datas,
    [],
    name='Snippy',
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=True,
    upx_exclude=[],
    runtime_tmpdir=None,
    console=False,  # This prevents the CMD window from appearing!
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
    icon='icon.ico',  # Using the converted icon file
    version_file=None,
)
