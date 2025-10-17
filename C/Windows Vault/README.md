# Windows Vault - Modern Media Management Application

A powerful, modern Windows application for organizing, tagging, and managing your media files with a beautiful Windows 11-inspired interface.

## Features

### 🗂️ **Advanced Media Management**
- Support for images (JPG, PNG, GIF, BMP, TIFF, WebP, SVG)
- Support for videos (MP4, AVI, MKV, MOV, WMV, FLV, WebM, M4V)
- Support for audio files (MP3, WAV, FLAC, AAC, OGG, WMA, M4A)
- Automatic thumbnail generation for all media types
- Duplicate detection and prevention
- Smart file organization with date-based folders

### 🏷️ **Powerful Tagging System**
- Create unlimited custom tags with color coding
- System tags (Favorites, Recent, High Quality, etc.)
- Many-to-many relationships between files and tags
- Tag-based filtering and search
- Usage count tracking for tags

### 🔍 **Advanced Search & Filtering**
- Real-time search across filenames, descriptions, and tags
- Multi-tag filtering with AND/OR logic
- Media type filtering (Images, Videos, Audio)
- Sort by name, size, date, rating
- Favorites system

### 🎨 **Modern Windows 11 UI**
- Fluent Design with acrylic effects
- Modern WPF controls with Windows 11 styling
- Responsive grid and list views
- Customizable thumbnail sizes
- Dark/Light theme support

### 🔧 **Robust Architecture**
- SQLite database with Entity Framework Core
- MVVM pattern with CommunityToolkit.Mvvm
- Dependency Injection with Microsoft.Extensions
- Comprehensive error handling
- Settings persistence

## Installation

### Prerequisites
- Windows 10/11
- .NET 8.0 Runtime

### Building from Source
1. Clone the repository
2. Open in Visual Studio 2022
3. Restore NuGet packages
4. Build and run

```bash
dotnet restore
dotnet build
dotnet run
```

## Usage

### Adding Media Files
1. **Individual Files**: Click "Add Media" and select files
2. **Folder Import**: Use "Import Folder" to add entire directories
3. **Drag & Drop**: Drag files directly into the application (coming soon)

### Organizing with Tags
1. Create custom tags with colors and descriptions
2. Assign multiple tags to any media file
3. Use tag filters to quickly find content
4. System tags are automatically applied

### Advanced Features
- **External App Integration**: Double-click to open files in default applications
- **Thumbnail Management**: Automatic generation and caching
- **Vault Organization**: Files are copied to organized folder structure
- **Backup System**: Configurable automatic backups

## Project Structure

```
WindowsVault/
├── Models/           # Entity Framework models
├── Data/             # Database context and configuration
├── Services/         # Business logic layer
├── ViewModels/       # MVVM ViewModels
├── Views/            # WPF Views and UserControls
├── Converters/       # Value converters for data binding
├── Styles/           # XAML styles and themes
└── Assets/           # Icons and resources
```

## Technical Architecture

### Database Schema
- **MediaFiles**: Core media file information
- **Tags**: Tag definitions with colors and descriptions
- **MediaFileTags**: Many-to-many relationship table

### Services Layer
- **MediaFileService**: Media file CRUD operations
- **TagService**: Tag management and relationships
- **ThumbnailService**: Thumbnail generation and caching
- **FileSystemService**: Vault organization and file operations
- **SettingsService**: Application configuration

### Key Technologies
- **WPF + ModernWPF**: Modern Windows UI framework
- **Entity Framework Core**: Data access layer
- **SQLite**: Embedded database
- **CommunityToolkit.Mvvm**: MVVM helpers
- **System.Drawing**: Image processing
- **FFMpegCore**: Video processing (future enhancement)

## Configuration

### Vault Settings (appsettings.json)
```json
{
  "VaultSettings": {
    "DefaultVaultPath": "%USERPROFILE%\\Documents\\WindowsVault",
    "ThumbnailSize": 200,
    "MaxFileSize": 2147483648,
    "EnableAutoBackup": true,
    "BackupIntervalHours": 24
  }
}
```

### User Settings
User preferences are stored in:
`%LOCALAPPDATA%\\WindowsVault\\settings.json`

## Performance Features

- **Lazy Loading**: Media files loaded on-demand
- **Thumbnail Caching**: Generated once, reused everywhere  
- **Database Indexing**: Optimized queries for fast search
- **Async Operations**: Non-blocking UI with progress reporting

## Future Enhancements

### Planned Features
- [ ] Drag & drop file import
- [ ] Advanced tag management dialog
- [ ] Settings configuration UI
- [ ] Batch operations (tag assignment, deletion)
- [ ] Media file rating system
- [ ] Export/Import functionality
- [ ] Cloud backup integration
- [ ] Plugin system for external tools

### Advanced Media Features
- [ ] Video frame extraction with FFMpeg
- [ ] EXIF data extraction and display
- [ ] Facial recognition tagging
- [ ] Automatic tag suggestions
- [ ] Smart collections based on content

## Development

### Contributing
1. Fork the repository
2. Create a feature branch
3. Make changes with tests
4. Submit a pull request

### Code Style
- Follow C# coding conventions
- Use async/await for I/O operations
- Implement proper error handling
- Add XML documentation for public APIs

## License

MIT License - see LICENSE file for details

## Support

For issues and feature requests, please use the GitHub Issues page.

---

**Windows Vault** - Organize your media, your way. 🗂️✨