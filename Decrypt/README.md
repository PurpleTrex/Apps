# 3DS CIA Decryptor Redux - Linux Port

A bash script for decrypting Nintendo 3DS games and applications (3DS/CIA files) on Linux/Ubuntu systems.

This is a Linux port of the [Batch CIA 3DS Decryptor Redux](https://github.com/xxmichibxx/Batch-CIA-3DS-Decryptor-Redux) by xxmichibxx, which itself is based on the original Batch CIA 3DS Decryptor by matiffeder.

## Features

* **CIA File Decryption**: Decrypt encrypted CIA files (games, updates, DLC, system titles)
* **3DS File Decryption**: Decrypt and process 3DS ROM files
* **Auto-detection**: Automatically detects CIA types (DLC/Patch/Game/System)
* **Comprehensive Logging**: Detailed logs of all operations in `log/programlog.txt`
* **Title Information**: Extracts and logs Title ID and version information
* **Linux Native**: Fully ported to work on Ubuntu and other Linux distributions

## Original Features from Redux Version

* Improved error handling: Invalid and already decrypted CIAs will be detected
* Improved script logging: Logging title, title version details
* Proper CIA versioning: Decrypted files use the same version as source
* Fixed decryption for CIA Demo, System and TWL titles
* Support for crypto seed titles (introduced in firmware 9.6.0-24)

## Requirements

### System Requirements
* Ubuntu 20.04 or higher (or compatible Debian-based distribution)
* 64-bit system (x86_64 or ARM64)
* At least 2GB of free disk space

### Software Dependencies
* git
* build-essential
* cmake
* libmbedtls-dev
* zlib1g-dev
* liblzma-dev
* pkg-config
* wget

These will be automatically installed by the setup script.

## Installation

### Quick Setup

1. Clone or download this repository:
```bash
cd /path/to/Decrypt
```

2. Run the setup script to install dependencies and build tools:
```bash
chmod +x setup.sh
./setup.sh
```

The setup script will:
- Install required system packages
- Download and compile `ctrtool` and `makerom` from Project_CTR
- Download the `seeddb.bin` database for seed crypto games
- Set up the directory structure

### Manual Installation

If you prefer to install dependencies manually:

1. Install system packages:
```bash
sudo apt-get update
sudo apt-get install git build-essential cmake libmbedtls-dev zlib1g-dev liblzma-dev pkg-config
```

2. Build ctrtool and makerom:
```bash
git clone https://github.com/3DSGuy/Project_CTR.git
cd Project_CTR/ctrtool && make
cp bin/ctrtool /path/to/Decrypt/bin/
cd ../makerom && make
cp bin/makerom /path/to/Decrypt/bin/
```

3. Download seeddb.bin:
```bash
wget https://raw.githubusercontent.com/ihaveamac/3DS-rom-tools/master/seeddb/seeddb.bin -O bin/seeddb.bin
```

## Usage

1. Place your CIA or 3DS files in the Decrypt directory:
```bash
cp /path/to/your/game.cia ./
```

2. Run the decryptor:
```bash
./3ds-decryptor.sh
```

3. Follow the on-screen prompts:
   - You'll be asked if you want to convert CIA files to CCI format
   - The script will process all files and show progress
   - Results will be displayed at the end

4. Find your decrypted files in the same directory:
   - CIA files: `filename Type-decrypted.cia`
   - 3DS files: `filename-decrypted.cci`

5. Check the log file for detailed information:
```bash
cat log/programlog.txt
```

## Supported File Types

### CIA Files
* **Game/eShop Titles** (0004000000xxxxxx): Full games from retail or eShop
* **Updates** (0004000e00xxxxxx): Game updates and patches
* **DLC** (0004008c00xxxxxx): Downloadable content
* **Demos** (0004000200xxxxxx): Demo versions
* **System Applications** (0004001000xxxxxx): System apps
* **System Data Archives** (0004001b00xxxxxx, 000400db00xxxxxx)
* **System Applets** (0004003000xxxxxx)
* **TWL/DSi Titles** (0004800x00xxxxxx): DSiWare titles

### 3DS Files
* Standard 3DS ROM files
* Cartridge dumps (CCI format)

## Notes and Limitations

### Important Notes
* **Backup originals**: Always keep backups of your original files
* **Legal use only**: Only decrypt games you legally own
* **File organization**: Move processed files to avoid conflicts with future runs
* **Already decrypted files**: The tool will skip files that are already decrypted

### Current Limitations
* **CCI Conversion**: Not all CIA types can be converted to CCI format (DLC, updates, system titles, TWL titles, demos are not supported for CCI conversion)
* **TWL CIA**: TWL (DSiWare) CIAs can be decrypted but are best used on retail hardware. Use DSi emulators for playing these titles.
* **Special characters**: Files with special characters in names will be automatically renamed
* **3DS decryption**: Full 3DS file decryption may require additional manual steps depending on the ROM type

### Compatibility Notes
* Decrypted files work with 3DS emulators (Citra, Citra MMJ, etc.)
* Some system titles may not work properly in emulators
* TWL titles require DSi emulators (melonDS recommended)

## Directory Structure

```
Decrypt/
├── 3ds-decryptor.sh    # Main decryption script
├── setup.sh            # Setup and dependency installation script
├── README.md           # This file
├── bin/                # Binary tools directory
│   ├── ctrtool         # CTR tool for 3DS file analysis
│   ├── makerom         # ROM building tool
│   └── seeddb.bin      # Seed database for crypto
├── log/                # Log files
│   └── programlog.txt  # Detailed operation log
└── build/              # Temporary build directory (created by setup)
```

## Troubleshooting

### Script won't run
```bash
chmod +x 3ds-decryptor.sh
chmod +x setup.sh
```

### Missing dependencies error
Run the setup script again:
```bash
./setup.sh
```

### "ctrtool not found" error
Make sure the setup script completed successfully and check that `bin/ctrtool` exists and is executable:
```bash
ls -la bin/
chmod +x bin/ctrtool bin/makerom
```

### Decryption fails for specific files
* Check the log file: `cat log/programlog.txt`
* Ensure the file is not already decrypted
* Verify the file is not corrupted
* Some files may require `seeddb.bin` - ensure it's present in the bin directory

### Build errors during setup
Make sure all development packages are installed:
```bash
sudo apt-get install build-essential cmake libmbedtls-dev zlib1g-dev liblzma-dev
```

## Credits and Acknowledgments

### Original Windows Version
* **Batch CIA 3DS Decryptor Redux** - [xxmichibxx](https://github.com/xxmichibxx/Batch-CIA-3DS-Decryptor-Redux)
* **Batch CIA 3DS Decryptor** - [matiffeder](https://github.com/matiffeder/3DS-stuff)

### Tools and Libraries
* **CTRTool/MakeROM** - [3DSGuy](https://github.com/3DSGuy/Project_CTR)
* **seeddb.bin** - [ihaveamac](https://github.com/ihaveamac/3DS-rom-tools/tree/master/seeddb)

### Linux Port
* Ported to Linux/Bash by Claude (Anthropic)

## License

This is a community tool for homebrew and backup purposes. Use responsibly and only with content you legally own.

## Changelog

### v1.0.7-linux (Linux Port)
* Complete rewrite from Windows Batch to Linux Bash
* Replaced Windows .exe files with Linux-native builds
* Added automated setup script for dependency installation
* Improved cross-platform compatibility
* Added colored terminal output
* Enhanced error handling and logging
* Maintained all functionality from original Redux version

## Support

For issues specific to the Linux port, please check:
1. The log file at `log/programlog.txt`
2. Ensure all dependencies are properly installed
3. Verify that binary tools in `bin/` are executable

For issues with the original Windows version, visit:
* [Original Redux Repository](https://github.com/xxmichibxx/Batch-CIA-3DS-Decryptor-Redux)
* [GBAtemp Thread](https://gbatemp.net/threads/batch-cia-3ds-decryptor-a-simple-batch-file-to-decrypt-cia-3ds.512385/)

## Disclaimer

This tool is for educational and backup purposes only. Users are responsible for complying with all applicable laws regarding game backups and decryption in their jurisdiction. Only use this tool with content you have legally obtained and own.
