# GRASSMARLIN Quick Start Guide - Windows 11

## Running GRASSMARLIN (Offline Mode)

### **Easiest Method:**

1. Open File Explorer
2. Navigate to: `C:\Users\mmagnusson\GITHUB_STUFF\GRASSMARLIN\GM3`
3. **Double-click** `run-grassmarlin.bat`

That's it! GRASSMARLIN will launch in offline mode.

---

### **Alternative: PowerShell or Command Prompt**

```cmd
cd C:\Users\mmagnusson\GITHUB_STUFF\GRASSMARLIN\GM3
run-grassmarlin.bat
```

Or run directly with Java:
```cmd
java -jar build\app\GrassMarlin.jar -nopcap
```

---

## What You Can Do in Offline Mode

### ✅ **Full Functionality (Except Live Capture)**

- **Import PCAP Files**: File → Import Files... or drag-and-drop
- **Import Other Formats**:
  - Bro/Zeek connection logs
  - Cisco show commands output
  - CSV host data
  - PcapNG files
  
- **Network Visualization**:
  - Logical network topology
  - Physical device mapping
  - Mesh network view
  
- **Analysis Features**:
  - Device fingerprinting
  - Protocol analysis
  - Network reports
  - Connection tracking
  
- **Export**:
  - Save sessions (.gm3 files)
  - Export to SVG graphics
  - Generate CSV reports

### ❌ **NOT Available (Offline Mode)**

- Live network traffic capture
- Real-time packet sniffing

---

## Quick Workflow Example

1. **Launch GRASSMARLIN** (double-click `run-grassmarlin.bat`)

2. **Import a PCAP file**:
   - Click File → Import Files...
   - Select your .pcap or .pcapng file
   - Click Import

3. **View the Results**:
   - Switch between tabs: Logical, Physical, Mesh
   - Right-click nodes for details
   - View → Reports for summaries

4. **Save Your Work**:
   - File → Save Session
   - Choose location and filename (.gm3)
   - Reopen later with File → Open Session

---

## First Time Setup

### **Configure Wireshark Path (Optional)**

For MAC address lookups:
1. Tools → Preferences
2. Set Wireshark Installation Path
3. OK

### **Import Sample Data**

Try importing:
- Any .pcap or .pcapng file
- Network traffic captures
- Connection logs

---

## System Info

- **Your Java**: 11.0.28 (OpenJDK) ✅
- **Required**: Java 11 or newer ✅
- **Built JAR**: `GM3\build\app\GrassMarlin.jar` (13.3 MB) ✅
- **Mode**: Offline (no live capture)

---

## Need Help?

- **User Guide**: Help → User Guide (in GRASSMARLIN)
- **PDF Manual**: `GM3\data\reference\GRASSMARLIN_User_Guide3.2.pdf`
- **Logs**: Check `GM3\logs\` for error messages
- **Migration Details**: See `GM3\MIGRATION_SUMMARY.md`

---

## Pro Tips

1. **Drag and Drop**: Drag PCAP files directly onto the GRASSMARLIN window to import
2. **Keyboard Shortcuts**:
   - Ctrl+I: Import Files
   - Ctrl+S: Save Session
   - Ctrl+O: Open Session
   - Ctrl+R: Start Live Capture (if enabled)
3. **Memory**: Large PCAP files need adequate RAM (8GB+ recommended)
4. **Session Files**: Save your analysis as .gm3 files to resume later

---

**You're all set!** Just double-click `run-grassmarlin.bat` to start analyzing network data. 🚀

