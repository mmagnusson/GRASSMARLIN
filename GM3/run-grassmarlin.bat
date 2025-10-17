@echo off
REM GRASSMARLIN Launcher for Windows
REM Requires Java 11 or newer

echo Starting GRASSMARLIN...
echo.

REM Check if Java is available
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 11 or newer from:
    echo   - https://adoptium.net/ (recommended)
    echo   - https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

REM Display Java version
echo Using Java:
java -version 2>&1 | findstr "version"
echo.

REM Check if JAR exists
if not exist "build\app\GrassMarlin.jar" (
    echo ERROR: GrassMarlin.jar not found!
    echo Please build the project first:
    echo   cd GM3
    echo   ant -buildfile build-ant.xml jar
    pause
    exit /b 1
)

REM Running in offline mode (recommended for analyzing existing PCAP files)
echo Mode: OFFLINE (analyzing existing PCAP/data files)
echo.
echo Features available:
echo   - Import and analyze PCAP files
echo   - Import Bro/Zeek logs, Cisco configs, CSV data
echo   - Network topology visualization
echo   - Fingerprinting and reporting
echo.
echo Starting GRASSMARLIN GUI...
echo.

REM Set JavaFX module path for native libraries (required for Windows GUI)
set JAVAFX_PATH=C:\javafx\javafx-sdk-11.0.2\lib

java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing -jar build\app\GrassMarlin.jar -nopcap

echo.
echo GRASSMARLIN closed.
echo.
echo Note: Live packet capture is disabled. To enable live capture,
echo download jnetpcap.dll and place it in this directory, then
echo run: java -jar build\app\GrassMarlin.jar (without -nopcap)
pause

