# Running GRASSMARLIN on Windows 11

## Prerequisites

### 1. **Java 11 or Newer (Required)**

GRASSMARLIN requires Java 11 or newer to run. Choose one:

**Recommended: Eclipse Temurin (AdoptiumOpenJDK)**
- Download: https://adoptium.net/
- Select: Java 11, 17, or 21 LTS
- Windows x64 installer (.msi)
- Install and ensure "Add to PATH" is checked

**Alternative: Oracle JDK**
- Download: https://www.oracle.com/java/technologies/downloads/
- Select Java 11 or newer

### 2. **Verify Java Installation**

Open Command Prompt or PowerShell and run:
```cmd
java -version
```

You should see version 11 or higher, e.g.:
```
openjdk version "11.0.28" 2025-07-15
```

If Java is not found, restart your terminal or add Java to your PATH.

---

## Running GRASSMARLIN

### **Option 1: Using the Batch Script (Easiest)**

1. Navigate to the `GM3` directory
2. Double-click `run-grassmarlin.bat`

The script will:
- Check if Java is installed
- Check if the JAR file exists
- Automatically detect if live packet capture is available
- Launch GRASSMARLIN

### **Option 2: Command Line**

Open Command Prompt or PowerShell:

```cmd
cd GM3
java -jar build\app\GrassMarlin.jar
```

**For offline mode (without live packet capture):**
```cmd
java -jar build\app\GrassMarlin.jar -nopcap
```

### **Option 3: PowerShell**

```powershell
cd GM3
java -jar build/app/GrassMarlin.jar
```

---

## Live Packet Capture (Optional)

### **For Live Network Capture**

GRASSMARLIN can capture live network traffic if you have the JNetPcap native library:

1. **Download JNetPcap DLL**:
   - For 64-bit Windows: `jnetpcap-64.dll`
   - For 32-bit Windows: `jnetpcap-32.dll`
   - Source: jnetpcap.com or from GRASSMARLIN releases

2. **Install**:
   - Rename to `jnetpcap.dll`
   - Place in the `GM3` directory (same folder as the JAR)

3. **Run normally** (without -nopcap flag):
   ```cmd
   java -jar build\app\GrassMarlin.jar
   ```

### **For Offline Analysis Only**

If you only need to analyze existing PCAP files (not capture live traffic):
- No JNetPcap required
- Always run with `-nopcap` flag
- Import PCAP files through File → Import Files menu

---

## Building from Source

If the JAR doesn't exist, build it first:

### **Using Apache Ant**

1. **Download Apache Ant** (if not installed):
   - https://ant.apache.org/bindownload.cgi
   - Extract to `C:\ant`

2. **Build**:
   ```cmd
   cd GM3
   C:\ant\apache-ant-1.10.15\bin\ant.bat -buildfile build-ant.xml jar
   ```

3. **Run**:
   ```cmd
   java -jar build\app\GrassMarlin.jar
   ```

---

## Troubleshooting

### **Error: "cannot find symbol" during build**
- Ensure you have JDK 11+ (not just JRE)
- Set JAVA_HOME to point to JDK directory

### **Error: "UnsupportedClassVersionError"**
- The JAR was compiled with a newer Java version
- Install Java 11 or newer
- Rebuild with your Java version

### **Application won't start**
1. Check Java version: `java -version` (must be 11+)
2. Verify JAR exists: `GM3\build\app\GrassMarlin.jar`
3. Try offline mode: `java -jar build\app\GrassMarlin.jar -nopcap`
4. Check logs in `GM3\logs\` directory

### **"Unable to initialize JNetPCap" warning**
- This is normal if jnetpcap.dll is not present
- Application will run in offline mode
- You can still import and analyze PCAP files

### **"Cannot locate Wireshark" warning**
- This is normal on first run
- Set Wireshark path in Tools → Preferences menu
- Wireshark is optional (only needed for MAC address lookups)

---

## Command Line Options

- `-nopcap` - Disable live packet capture (offline mode only)
- `-noplugins` - Disable plugin loading

---

## System Requirements

- **OS**: Windows 11, 10, 8.1, 7 (64-bit or 32-bit)
- **Java**: Java 11 or newer (11, 17 LTS, 21 LTS recommended)
- **RAM**: 4 GB minimum, 8 GB+ recommended for large PCAP files
- **Disk**: 500 MB for application + space for captured data

---

## Quick Start

```cmd
cd C:\Users\mmagnusson\GITHUB_STUFF\GRASSMARLIN\GM3
run-grassmarlin.bat
```

That's it! The application should launch with a JavaFX GUI.

For detailed usage instructions, see the User Guide in the Help menu or at:
`GM3\data\reference\GRASSMARLIN_User_Guide3.2.pdf`

