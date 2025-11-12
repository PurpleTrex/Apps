#!/bin/bash

# Setup script for 3DS CIA Decryptor Redux - Linux Port
# This script helps install the required dependencies

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo ""
echo "============================================"
echo " 3DS CIA Decryptor Redux - Setup Script"
echo "============================================"
echo ""

# Create directories
mkdir -p bin
mkdir -p log
mkdir -p build

echo -e "${CYAN}Setting up dependencies for Ubuntu/Linux...${NC}"
echo ""

# Check if running on Ubuntu/Debian
if ! command -v apt-get &> /dev/null; then
    echo -e "${YELLOW}Warning: This script is designed for Ubuntu/Debian systems${NC}"
    echo "You may need to manually install dependencies on other distributions"
    echo ""
fi

# Install build dependencies
echo "Installing build dependencies..."
echo ""

sudo apt-get update
sudo apt-get install -y \
    git \
    build-essential \
    cmake \
    libmbedtls-dev \
    zlib1g-dev \
    liblzma-dev \
    pkg-config \
    wget \
    unzip

echo ""
echo -e "${GREEN}Build dependencies installed${NC}"
echo ""

# Clone and build ctrtool and makerom (Project_CTR)
echo "Cloning Project_CTR (ctrtool and makerom)..."
cd build

if [ ! -d "Project_CTR" ]; then
    git clone https://github.com/3DSGuy/Project_CTR.git
fi

cd Project_CTR

# Build ctrtool
echo "Building ctrtool..."
cd ctrtool
make clean || true
make -j$(nproc)

if [ -f "bin/ctrtool" ]; then
    cp bin/ctrtool ../../../bin/
    echo -e "${GREEN}ctrtool installed successfully${NC}"
else
    echo -e "${RED}Failed to build ctrtool${NC}"
fi

cd ..

# Build makerom
echo "Building makerom..."
cd makerom
make clean || true
make -j$(nproc)

if [ -f "bin/makerom" ]; then
    cp bin/makerom ../../../bin/
    echo -e "${GREEN}makerom installed successfully${NC}"
else
    echo -e "${RED}Failed to build makerom${NC}"
fi

cd ../../..

# Download seeddb.bin
echo ""
echo "Downloading seeddb.bin..."
cd bin

if [ ! -f "seeddb.bin" ]; then
    wget -q "https://raw.githubusercontent.com/ihaveamac/3DS-rom-tools/master/seeddb/seeddb.bin" -O seeddb.bin
    if [ -f "seeddb.bin" ]; then
        echo -e "${GREEN}seeddb.bin downloaded successfully${NC}"
    else
        echo -e "${YELLOW}Warning: Could not download seeddb.bin${NC}"
        echo "You may need to download it manually from:"
        echo "https://github.com/ihaveamac/3DS-rom-tools/tree/master/seeddb"
    fi
else
    echo -e "${GREEN}seeddb.bin already exists${NC}"
fi

cd ..

# Make scripts executable
chmod +x 3ds-decryptor.sh
chmod +x setup.sh

echo ""
echo "============================================"
echo -e "${GREEN}Setup completed!${NC}"
echo "============================================"
echo ""
echo "You can now run the decryptor with:"
echo "  ./3ds-decryptor.sh"
echo ""
echo "Place your .cia or .3ds files in this directory before running."
echo ""
echo "Note: Some advanced features may require additional tools."
echo "      The basic decryption functionality should work with"
echo "      ctrtool and makerom."
echo ""
