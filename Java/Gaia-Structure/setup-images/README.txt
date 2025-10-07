Inno Setup Icon Configuration
=============================

The Gaia installer now uses the existing app icon from:
src\main\resources\images\app-icon.png

Inno Setup automatically handles:
✅ Resizing the icon for different contexts
✅ Converting PNG to the required formats
✅ Using the icon for:
   - Installer window icon
   - Wizard header image  
   - Small wizard icon
   - Installed application icon
   - Uninstaller icon

This approach is much simpler than creating separate BMP files!

The setup-images directory is no longer needed and can be deleted.