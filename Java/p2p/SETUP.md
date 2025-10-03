# Development Environment Setup Guide

## Prerequisites Installation

### 1. Install Java 17 or Higher

#### Option A: Oracle JDK
1. Download from: https://www.oracle.com/java/technologies/downloads/
2. Install the JDK
3. Add Java to your PATH environment variable

#### Option B: OpenJDK
1. Download from: https://adoptium.net/
2. Install the JDK  
3. Add Java to your PATH environment variable

#### Verify Java Installation
```cmd
java -version
javac -version
```

### 2. Install Apache Maven

#### Download and Install
1. Download from: https://maven.apache.org/download.cgi
2. Extract to a directory (e.g., C:\Apache\maven)
3. Add Maven bin directory to your PATH environment variable
   - Add: C:\Apache\maven\bin (adjust path as needed)

#### Verify Maven Installation
```cmd
mvn -version
```

### 3. Add to System PATH (Windows)

#### Using System Properties:
1. Right-click "This PC" → Properties
2. Click "Advanced system settings"
3. Click "Environment Variables"
4. Under "System Variables", find and select "Path"
5. Click "Edit" → "New"
6. Add Java bin directory: `C:\Program Files\Java\jdk-17\bin`
7. Add Maven bin directory: `C:\Apache\maven\bin`
8. Click OK to save

#### Using PowerShell (Alternative):
```powershell
# Add Java to PATH (adjust path as needed)
$env:PATH += ";C:\Program Files\Java\jdk-17\bin"

# Add Maven to PATH (adjust path as needed)  
$env:PATH += ";C:\Apache\maven\bin"

# Make permanent (requires restart)
[Environment]::SetEnvironmentVariable("PATH", $env:PATH, "Machine")
```

## IDE Setup (Optional but Recommended)

### IntelliJ IDEA
1. Download from: https://www.jetbrains.com/idea/
2. Import the project as a Maven project
3. Set Project SDK to Java 17+
4. Run configuration: Main class = `com.p2p.messaging.P2PMessagingApp`

### Eclipse
1. Download Eclipse IDE for Java Developers
2. Install JavaFX plugin if needed
3. Import as Maven project
4. Set Java Build Path to JDK 17+

### Visual Studio Code
1. Install Java Extension Pack
2. Install Maven extension  
3. Open project folder
4. Set Java home in settings

## Building and Running

Once Java and Maven are installed and in PATH:

### Build the Application
```cmd
cd path\to\p2p\project
build.bat
```

### Run the Application  
```cmd
run.bat
```

### Manual Maven Commands
```cmd
# Build
mvn clean compile

# Run
mvn javafx:run

# Package (creates executable JAR)
mvn clean package

# Run tests
mvn test
```

## Troubleshooting

### "mvn is not recognized"
- Maven is not installed or not in PATH
- Follow Maven installation steps above
- Restart command prompt after adding to PATH

### "'java' is not recognized"
- Java is not installed or not in PATH  
- Follow Java installation steps above
- Restart command prompt after adding to PATH

### JavaFX Module Issues
- Ensure using Java 17+ (JavaFX is included)
- If using older Java, add JavaFX modules manually

### Build Fails - Dependencies
- Run: `mvn clean install -U` to force update dependencies
- Check internet connection for downloading dependencies

### Port Already in Use
- Default ports: 8080 (messaging), 8081 (discovery)
- Close other applications using these ports
- Or modify port settings in code

### Firewall Issues
- Allow Java applications through Windows Firewall
- Open ports 8080-8081 for peer discovery and messaging

## Project Structure After Build

```
p2p/
├── src/                     # Source code
├── target/                  # Compiled classes & JARs
├── logs/                    # Application logs
├── pom.xml                  # Maven configuration  
├── build.bat               # Build script
├── run.bat                 # Run script
└── README.md               # This file
```

## Quick Start Checklist

- [ ] Java 17+ installed and in PATH
- [ ] Maven installed and in PATH  
- [ ] Project downloaded/cloned
- [ ] Run `build.bat` successfully
- [ ] Run `run.bat` to start application
- [ ] Application window opens
- [ ] Check logs/ directory for any issues

## Getting Help

If you encounter issues:

1. Check the logs in the `logs/` directory
2. Verify Java and Maven installations
3. Ensure Windows Firewall allows the application
4. Try running with an IDE if command line fails
5. Check the main README.md for application usage instructions