# GRASSMARLIN

GRASSMARLIN provides IP network situational awareness of industrial control systems (ICS) and Supervisory Control and Data Acquisition (SCADA) networks to support network security. Passively map, and visually display, an ICS/SCADA network topology while safely conducting device discovery, accounting, and reporting on these critical cyber-physical systems.

## Getting Started

### Prerequisites

| Requirement | Linux | Windows |
|---|---|---|
| **Java 21 LTS** (JDK, not JRE) | [Adoptium Temurin](https://adoptium.net/) or distro package | [Adoptium Temurin installer](https://adoptium.net/) |
| **libpcap** (for live capture) | `libpcap-dev` / `libpcap-devel` | [Npcap](https://npcap.com/) (install with "WinPcap API-compatible mode") |
| **Git** | distro package | [Git for Windows](https://git-scm.com/download/win) |

Live packet capture requires the native pcap library. If you only need to analyze saved `.pcap` files, you can skip libpcap/Npcap and launch with the `-nopcap` flag.

---

### Linux

**1. Install dependencies**

Debian / Ubuntu:
```bash
sudo apt update
sudo apt install -y openjdk-21-jdk libpcap-dev git
```

Fedora / RHEL:
```bash
sudo dnf install -y java-21-openjdk-devel libpcap-devel git
```

Arch:
```bash
sudo pacman -S jdk21-openjdk libpcap git
```

Verify Java is version 21+:
```bash
java -version
```

**2. Clone and build**

```bash
git clone https://github.com/iadgov/GRASSMARLIN.git
cd GRASSMARLIN/GM3
./gradlew build
```

The Gradle wrapper (`./gradlew`) downloads Gradle automatically -- no separate install needed.

**3. Run**

Quick start (development mode):
```bash
./gradlew run
```

Or build a standalone distribution:
```bash
./gradlew distribution
cd build/app
java -jar grassmarlin-3.2.1.jar
```

**4. (Optional) Build a native application image**

```bash
./gradlew jpackage
```

Creates a self-contained app in `build/jpackage/GRASSMARLIN/` that includes its own JVM.

---

### Windows

**1. Install Java 21**

Download and run the Adoptium Temurin JDK 21 installer from https://adoptium.net/. During install, check the option to **set JAVA_HOME** and **add to PATH**.

Open a new Command Prompt or PowerShell and verify:
```
java -version
```

**2. Install Npcap (for live capture)**

Download from https://npcap.com/ and install. Check **"Install Npcap in WinPcap API-compatible Mode"** during setup. A reboot may be required.

If you only plan to analyze saved `.pcap` files, you can skip this step.

**3. Clone and build**

```
git clone https://github.com/iadgov/GRASSMARLIN.git
cd GRASSMARLIN\GM3
gradlew.bat build
```

**4. Run**

Quick start (development mode):
```
gradlew.bat run
```

Or build a standalone distribution:
```
gradlew.bat distribution
cd build\app
java -jar grassmarlin-3.2.1.jar
```

**5. (Optional) Build a native application image**

```
gradlew.bat jpackage
```

Creates a self-contained app in `build\jpackage\GRASSMARLIN\` with a `GRASSMARLIN.exe` launcher.

---

## Command-Line Options

| Flag | Effect |
|---|---|
| `-nopcap` | Disable live capture (skip pcap library initialization) |
| `-noplugins` | Disable loading of plugin JARs |

Example:
```bash
java -jar grassmarlin-3.2.1.jar -nopcap
```

## Gradle Tasks Reference

All commands are run from the `GM3/` directory.

| Command | Description |
|---|---|
| `./gradlew build` | Compile source, run tests |
| `./gradlew run` | Build and launch the application |
| `./gradlew buildPlugins` | Compile plugin JARs (CSV import, SVG export, etc.) |
| `./gradlew distribution` | Assemble standalone app directory in `build/app/` |
| `./gradlew jpackage` | Create platform-native application image |
| `./gradlew test` | Run the test suite |
| `./gradlew clean` | Delete all build outputs |

## Troubleshooting

**"Unable to initialize Pcap4J; packet capture functionality will be disabled."**
The native pcap library is missing or not found. Install `libpcap-dev` (Linux) or Npcap (Windows). On Windows, ensure WinPcap-compatible mode was selected during Npcap install.

**"Unable to load MAC manufacturer reference"**
Wireshark's OUI database was not found. This is cosmetic -- MAC vendor lookup is disabled but everything else works. Install Wireshark or set the path in Tools > Preferences.

**"Cannot locate Wireshark"**
Same as above -- optional, only needed for MAC vendor lookup and the "Open in Wireshark" feature.

**Application doesn't launch (no window appears)**
Ensure you are running a JDK (not just a JRE) and that you have a graphical desktop environment. GRASSMARLIN requires JavaFX, which needs a display server.

## Documentation

GrassMarlin v3.2 User Guide:
* [Download PDF](https://github.com/iadgov/GRASSMARLIN/raw/master/GRASSMARLIN%20User%20Guide.pdf)
* [View PDF on GitHub](https://github.com/iadgov/GRASSMARLIN/blob/master/GRASSMARLIN%20User%20Guide.pdf)

A [presentation on GRASSMARLIN](https://github.com/iadgov/GRASSMARLIN/blob/master/GRASSMARLIN_Briefing_20170210.pptx) is also available.

## License

See [LICENSE.md](./LICENSE.md).

## Disclaimer

See [DISCLAIMER.md](./DISCLAIMER.md).
