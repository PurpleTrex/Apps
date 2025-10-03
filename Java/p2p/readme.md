# P2P Messaging Service

A modern, feature-rich peer-to-peer messaging application built with Java and JavaFX.

## Features

### Core Messaging
- **Real-time P2P messaging** - Direct communication between peers without central servers
- **Multi-protocol support** - Compatible with RCS, iOS, and other messaging protocols
- **File sharing** - Send images, videos, audio files, documents, Excel files, and more
- **Message history** - Persistent conversation history
- **Delivery status** - Read receipts and delivery confirmations

### Modern GUI
- **Clean, responsive interface** - Built with JavaFX for a modern user experience
- **Tabbed conversations** - Multiple simultaneous chats
- **Peer discovery** - Automatic discovery of peers on local network
- **Status indicators** - Online/offline/away/busy status for peers
- **File type support** - Preview and send various file formats

### Network Features
- **Automatic peer discovery** - UDP broadcast for finding peers on local network
- **Direct P2P connections** - No central server required
- **Manual connection** - Connect directly to peers by IP address
- **Robust networking** - Built on Netty for reliable message delivery

### File Support
- **Images**: PNG, JPG, JPEG, GIF, BMP
- **Videos**: MP4, AVI, MOV, WMV, FLV
- **Audio**: MP3, WAV, OGG, M4A
- **Documents**: PDF, DOC, DOCX, TXT
- **Spreadsheets**: XLS, XLSX, CSV
- **Any file type** supported

## Requirements

- **Java 17** or higher
- **Maven 3.6+** for building
- **Network connectivity** for peer-to-peer communication

## Quick Start

### Windows Users

1. **Build the application:**
   ```cmd
   build.bat
   ```

2. **Run the application:**
   ```cmd
   run.bat
   ```

### Manual Build & Run

1. **Build with Maven:**
   ```bash
   mvn clean compile
   ```

2. **Run with JavaFX:**
   ```bash
   mvn javafx:run
   ```

3. **Create executable JAR:**
   ```bash
   mvn clean package
   ```

## How to Use

### Starting the Application
1. Launch the application using one of the methods above
2. The application will automatically start discovering peers on your local network
3. Your peer information will be displayed in the bottom-left corner

### Connecting to Peers
- **Automatic Discovery**: Peers on the same network will appear automatically in the peer list
- **Manual Connection**: Click "Connect to Peer" and enter IP address and port
- **Android Devices**: Click "Scan Android Devices" to find Android phones/tablets on your network
- **All Platforms**: Click "Discover All Platforms" to find all compatible devices and servers
- **Start Chatting**: Double-click any peer in the list to open a chat window

### Cross-Platform Connections
- **Android Support**: Scan for and connect to Android devices running P2P messaging apps
- **iOS Compatibility**: Connect to iOS devices using platform discovery
- **Web Server Integration**: Connect to web servers and Node.js applications
- **QR Code Sharing**: Generate QR codes for easy connection setup with mobile devices

### Sending Messages
- **Text Messages**: Type in the message box and press Enter or click Send
- **File Attachments**: Click the 📎 button to attach files
- **Multiple Chats**: Open multiple chat tabs for different conversations

### File Sharing
1. Click the attachment button (📎) in any chat
2. Select files using the file chooser
3. Files are sent directly to the peer
4. Supported formats are automatically detected and displayed appropriately

## Architecture

### Core Components

- **P2PMessagingApp**: Main application entry point
- **P2PNetworkManager**: Handles all network communications and peer management
- **PeerDiscoveryService**: UDP-based peer discovery on local network
- **MainWindow**: Primary user interface
- **ChatPane**: Individual chat conversation interface

### Network Protocol
- **Discovery**: UDP broadcast on port 8081 for peer announcement
- **Messaging**: TCP connections on port 8080 for message exchange
- **File Transfer**: Embedded in message protocol with base64 encoding for binary data

### Message Types
- `TEXT`: Plain text messages
- `IMAGE`: Image files with preview
- `VIDEO`: Video files
- `AUDIO`: Audio files  
- `DOCUMENT`: Document files (PDF, Word, etc.)
- `FILE`: Generic file type

## Configuration

### Network Ports
- **Default messaging port**: 8080
- **Discovery port**: 8081
- Ports can be configured in the connection dialog

### Logging
- Application logs are written to `logs/p2p-messaging.log`
- Log level can be configured in `src/main/resources/logback.xml`

## Development

### Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── com/p2p/messaging/
│   │       ├── P2PMessagingApp.java     # Main application
│   │       ├── model/                   # Data models
│   │       │   ├── Message.java
│   │       │   └── Peer.java
│   │       ├── network/                 # Network layer
│   │       │   ├── P2PNetworkManager.java
│   │       │   └── PeerDiscoveryService.java
│   │       └── ui/                      # User interface
│   │           ├── MainWindow.java
│   │           └── ChatPane.java
│   └── resources/
│       └── logback.xml                  # Logging configuration
├── pom.xml                              # Maven configuration
├── build.bat                            # Windows build script
└── run.bat                              # Windows run script
```

### Dependencies
- **JavaFX**: Modern UI framework
- **Netty**: High-performance networking
- **Jackson**: JSON serialization
- **Apache Commons IO**: File utilities
- **SLF4J + Logback**: Logging

### Building from Source
```bash
git clone <repository-url>
cd p2p-messaging
mvn clean compile
mvn javafx:run
```

## Future Enhancements

- [ ] End-to-end encryption
- [ ] Voice/video calling
- [ ] Group chat support
- [ ] Cloud backup integration
- [ ] Mobile app versions
- [ ] Custom emoji support
- [ ] Message search functionality
- [ ] Notification system

## Troubleshooting

### Common Issues

**Application won't start:**
- Ensure Java 17+ is installed
- Check that JavaFX modules are available
- Verify Maven dependencies are downloaded

**Peers not discovered:**
- Check firewall settings
- Ensure UDP port 8081 is not blocked
- Verify peers are on the same network subnet

**Connection failed:**
- Check if target peer is running
- Verify IP address and port are correct
- Ensure TCP port 8080 is accessible

**File transfer issues:**
- Check file size (large files may take time)
- Verify file is not corrupted
- Ensure sufficient disk space

## License

This project is provided as-is for educational and development purposes.

## Support

For issues and questions, please check the troubleshooting section above or review the application logs in the `logs/` directory.