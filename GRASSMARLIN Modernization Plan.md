Ready to code?
                                                                                           
 Here is Claude's plan:
╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌
 GRASSMARLIN Modernization Plan                         

 Context

 GRASSMARLIN is an NSA-developed ICS/SCADA passive network mapping tool (~234 Java source
 files, JavaFX UI). Originally written for Java 8, a partial migration to Java 21 was
 started (Jakarta XML namespace migration, updated build.gradle), but significant work
 remains. The most critical issue is the continued dependency on JNetPcap, an abandoned
 native library (no updates since ~2015). There are no automated tests, no CI/CD, a
 fragmented logging system, 13 MB of duplicate JARs checked into git, and numerous code
 quality issues.

 This plan addresses all 10 improvement areas in dependency order.

 ---
 Phase 1: Foundation — Gradle Wrapper + Build Cleanup

 Dependencies: None
 Goal: Reproducible builds and test infrastructure baseline.

 Changes

 1. Generate Gradle wrapper — Run gradle wrapper --gradle-version 8.11 in GM3/ to create
 gradlew, gradlew.bat, gradle/wrapper/*
 2. Update .gitignore (/home/mmagnusson/code/GRASSMARLIN/.gitignore) — Currently only
 /Installers. Add:
   - GM3/build/, GM3/.gradle/
   - IDE metadata (.idea/, *.iml, .classpath, .project, .settings/)
   - OS files (.DS_Store, Thumbs.db)
 3. Add JUnit 5 to build.gradle (GM3/build.gradle) — Add test dependencies,
 useJUnitPlatform(), and src/test/java source set
 4. Create empty test directory — GM3/src/test/java/ and GM3/src/test/resources/

 Verification

 - cd GM3 && ./gradlew --version succeeds
 - ./gradlew build compiles existing code
 - ./gradlew test reports 0 tests, 0 failures

 ---
 Phase 2: Remove Checked-in JARs

 Dependencies: Phase 1
 Goal: Eliminate 13 MB of binary blobs duplicated by Gradle dependencies.

 Delete

 - All JARs from GM3/lib/ except jnetpcap.jar (retained until Phase 5):
   - antlr-4.5-complete.jar, commons-io-2.4.jar, commons-lang3-3.3.2.jar
   - istack-commons-runtime-3.0.8.jar, jakarta.activation-api-2.1.0.jar,
 jakarta.xml.bind-api-3.0.1.jar
   - jaxb-core-3.0.2.jar, jaxb-impl-3.0.2.jar
   - All 8 javafx.*.jar files
 - Entire GM3/resource/jaxb/ directory (~11 MB, only needed for Ant XJC code generation
 which is already done)

 Verification

 - ./gradlew clean build succeeds using only Maven dependencies + jnetpcap.jar
 - du -sh GM3/lib/ shows only ~500 KB (jnetpcap.jar)

 ---
 Phase 3: Bug Fixes — Null Safety + Resource Leaks

 Dependencies: Phase 1
 Goal: Fix known defects without changing public APIs.

 Fix 1: GeoIp.java NPE (line 104)

 File: GM3/src/core/knowledgebase/GeoIp.java
 getCountryName(ip) can return null; .replace() called on result without null check. Add
 null guard before the string operations.

 Fix 2: PcapFileParser null check ordering (lines 90–105)

 File: GM3/src/core/importmodule/inputIterators/pcap/PcapFileParser.java
 getHandle() can return null but pcap.compile() is called on line 97 before the null check
  on line 105. Move the null check to immediately after getHandle().

 Fix 3: EmbeddedIcons InputStream leak (line 127)

 File: GM3/src/ui/EmbeddedIcons.java
 Wrap getResourceAsStream() in try-with-resources.

 Fix 4: URLClassLoader never closed (line 383)

 File: GM3/src/util/Launcher.java
 Add a shutdown hook to close all plugin URLClassLoaders.

 Fix 5: Empty catch block (SaveTask.java:66)

 File: GM3/src/core/document/serialization/SaveTask.java
 Add Thread.currentThread().interrupt() in the InterruptedException catch.

 Fix 6: InterruptedException handling (multiple files)

 All catch (InterruptedException) blocks that swallow the exception must restore the
 interrupt flag. Files:
 - TaskDispatcher.java (7 locations)
 - PcapFileParser.java (4 locations)
 - BroFileIterator.java (2 locations)
 - Bro2JsonIterator.java (1 location)

 Verification

 - ./gradlew build compiles
 - Manual test: application handles unknown IPs without NPE

 ---
 Phase 4: Logging Cleanup

 Dependencies: Phase 1, Phase 3
 Goal: Consolidate all logging through the existing Logger→SLF4J→Logback pipeline. Remove
 the duplicate file-logging system in Launcher.java.

 Changes to GM3/src/util/Launcher.java

 - Remove the custom file logging system (lines ~201–268): pathLogFile, writerLogFile,
 getLogFilePath(), InitializeLogging(), Handle_writeLogMessageToDisk(),
 TerminateLogging(), all RecordLogMessage() overloads
 - Update main(): Remove calls to InitializeLogging() and TerminateLogging(). Replace
 RecordLogMessage() calls with Logger.log()

 Replace all printStackTrace() (20 instances) with Logger.log()

 Key files: FProcessor.java, TaskDispatcher.java (3), GrassMarlinFx.java, CellGroup.java
 (3), FilterRow.java (2), IDraggable.java (2), PcapFileParser.java, RateLimitedTask.java,
 CiscoFileIterator.java, and others.

 Replace all System.out/err (33 instances) with Logger.log()

 Heaviest concentrations: LayoutPhysicalRadial.java (15 debug printlns),
 CsvFileIterator.java (8), ImportItem.java (3), SetConfiguration.java (3).

 Update GeoIp.java

 Replace Launcher.RecordLogMessage() calls (lines 47, 53) with Logger.log().

 Add/update Logback configuration

 Ensure GM3/src/logback.xml has console and rolling file appenders with ISO8601
 timestamps.

 Verification

 - ./gradlew build compiles
 - grep -r "System.out\|System.err\|printStackTrace\|RecordLogMessage" GM3/src/ returns
 zero hits
 - Application logs to console and file via Logback

 ---
 Phase 5: Pcap4J Migration (Largest Phase)

 Dependencies: Phase 1, Phase 3 (null check fix in PcapFileParser)
 Goal: Replace all JNetPcap usage with Pcap4J. Pcap4J dependency already exists in
 build.gradle.

 Step 5.1: Create TCP Flag Abstraction

 New file: GM3/src/core/protocol/TcpFlags.java
 Library-agnostic enum with fromBitmask(int) → Set<String>. Replaces dependency on
 org.jnetpcap.protocol.tcpip.Tcp.Flag.

 Step 5.2: Create Payload Buffer Abstraction

 New file: GM3/src/core/protocol/PayloadBuffer.java
 Pure-Java byte[]-backed buffer replacing JBuffer. Provides same API: getByte(),
 getByteArray(), size().

 Step 5.3: Migrate PMetaData

 File: GM3/src/core/fingerprint/PMetaData.java
 Change Set<Tcp.Flag> flags → Set<String> flags. Remove org.jnetpcap import. This actually
  fixes the fingerprint flag matching which was broken (FProcessor calls contains(String)
 on an EnumSet<Tcp.Flag>).

 Step 5.4: Migrate PacketData

 File: GM3/src/core/fingerprint/PacketData.java
 Change JBuffer payload → PayloadBuffer payload. Change Set<Tcp.Flag> → Set<String>.
 Method bodies unchanged (same API surface).

 Step 5.5: Migrate Zep.java

 File: GM3/src/core/protocol/Zep.java
 Remove Udp-dependent methods (hasProtocol(Udp), isProtocol(Udp), isZEPProtocol(Udp)).
 Keep isZEPProtocol(int, int) and fromArray(byte[]). Remove org.jnetpcap import.

 Step 5.6: Migrate PcapDeviceList.java

 File: GM3/src/core/PcapDeviceList.java
 Replace Pcap.findAllDevs()/PcapIf with Pcaps.findAllDevs()/PcapNetworkInterface.

 Step 5.7: Migrate Core PcapFileParser (heaviest)

 File: GM3/src/core/importmodule/inputIterators/pcap/PcapFileParser.java
 - getHandle(): Pcap.openOffline() → Pcaps.openOffline(), returns PcapHandle
 - BPF filter: PcapBpfProgram + pcap.compile() → handle.setFilter(filter,
 BpfCompileMode.OPTIMIZE)
 - Packet loop: JPacketHandler callback → handle.getNextPacketEx() loop with
 handle.getTimestamp() for timestamps
 - Protocol dissection: packet.hasHeader(ip4) → packet.get(IpV4Packet.class), immutable
 packet model
 - Payload extraction: packet.transferTo(JBuffer) → tcpPacket.getPayload().getRawData() →
 PayloadBuffer
 - TCP flags: tcp.flagsEnum() → TcpFlags.fromBitmask(tcpHeader raw flags byte)
 - Frame numbers: manual counter (Pcap4J doesn't track frame numbers)

 Step 5.8: Migrate PcapLiveParser.java

 File: GM3/src/core/importmodule/inputIterators/pcap/PcapLiveParser.java
 Pcap.openLive() → device.openLive(snaplen, PromiscuousMode, timeout). breakloop() →
 handle.breakLoop().

 Step 5.9: Migrate LivePCAPImport.java

 File: GM3/src/core/importmodule/LivePCAPImport.java
 Change PcapIf parameter → PcapNetworkInterface.

 Step 5.10: Migrate FilterDialogFx.java

 File: GM3/src/ui/dialog/FilterDialogFx.java
 BPF validation: Pcap.openDead() + compile() → Pcaps.openDead() + handle.setFilter() in
 try/catch.

 Step 5.11: Migrate Launcher.java

 File: GM3/src/util/Launcher.java
 Replace System.loadLibrary("jnetpcap") + Pcap.libVersion() with Pcaps.findAllDevs()
 availability check.

 Step 5.12: Migrate CsvFileIterator.java (plugin)

 File: GM3/data/plugins/iadgov.csvimport/CsvFileIterator.java
 Delete unused org.jnetpcap.protocol.tcpip.Tcp import.

 Step 5.13: Migrate Plugin PacketHandler.java

 File: GM3/data/plugins/iadgov.offlinepcap/PacketHandler.java
 Replace JBuffer → PayloadBuffer (3 usages).

 Step 5.14: Migrate Plugin PcapFileParser.java + PcapNgFileParser.java

 Files: GM3/data/plugins/iadgov.offlinepcap/PcapFileParser.java, PcapNgFileParser.java
 Remove unused JBuffer imports after PacketHandler migration.

 Step 5.15: Remove JNetPcap

 - Delete GM3/lib/jnetpcap.jar
 - Remove implementation files('lib/jnetpcap.jar') from build.gradle

 Verification

 - ./gradlew clean build with zero JNetPcap references
 - grep -r "org.jnetpcap\|JBuffer" GM3/src GM3/data returns zero
 - Manual test: import known PCAP, verify node/edge counts
 - Manual test: BPF filter validation in FilterDialogFx
 - Manual test: live capture (if hardware available)
 - Manual test: PcapNG import via offline plugin

 ---
 Phase 6: Test Suite

 Dependencies: Phase 1, Phase 5
 Goal: JUnit 5 tests for critical non-UI code paths.

 Test files to create in GM3/src/test/java/

 Test File: util/CidrTest.java
 Targets: IP parsing, CIDR math, contains, overlaps, edge cases
 ────────────────────────────────────────
 Test File: core/knowledgebase/GeoIpTest.java
 Targets: Null handling, flag icon lookup
 ────────────────────────────────────────
 Test File: core/protocol/PayloadBufferTest.java
 Targets: getByte, getByteArray, size, boundaries
 ────────────────────────────────────────
 Test File: core/protocol/TcpFlagsTest.java
 Targets: fromBitmask for all flag combinations
 ────────────────────────────────────────
 Test File: core/fingerprint/PacketDataTest.java
 Targets: Payload access, match, extract
 ────────────────────────────────────────
 Test File: core/protocol/ZepTest.java
 Targets: Protocol detection, field parsing
 ────────────────────────────────────────
 Test File: core/document/serialization/XmlSecurityTest.java
 Targets: XXE protection verification

 Test resources in GM3/src/test/resources/

 - test-packets.pcap — small PCAP with known TCP/UDP/ZEP packets
 - test-cidr-id.csv, test-id-name.csv — small GeoIP test data

 Build config

 Add headless JavaFX system properties to the test task in build.gradle for CI
 compatibility.

 Verification

 - ./gradlew test — all tests pass
 - Test report at build/reports/tests/test/index.html

 ---
 Phase 7: CI/CD Pipeline

 Dependencies: Phase 1, Phase 6
 Goal: GitHub Actions for automated build + test on push/PR.

 Create .github/workflows/ci.yml

 - Java 21 (Temurin) setup
 - ./gradlew build and ./gradlew test
 - Upload test report artifacts
 - Dependency listing for audit

 Verification

 - Push to feature branch, verify workflow runs
 - Break a test intentionally, confirm CI reports failure

 ---
 Phase 8: Build System Cleanup

 Dependencies: Phase 2, Phase 5, Phase 7
 Goal: Remove legacy Ant build, fix Gradle deprecations, proper plugin compilation.

 Changes

 - Delete or archive GM3/build-ant.xml (references obsolete JNetPcap DLLs, WIX, Java 8)
 - Refactor build.gradle:
   - Replace ant.javac-based plugin compilation with proper Gradle source sets
   - Fix deprecated buildDir → layout.buildDirectory
   - Fix deprecated task syntax → tasks.register()
 - Update PCAP4J_MIGRATION.md — mark as COMPLETE

 Verification

 - ./gradlew clean build buildPlugins succeeds
 - ./gradlew --warning-mode all build — no deprecation warnings

 ---
 Phase 9: Java 21 Modernization

 Dependencies: Phase 6, Phase 8
 Goal: Leverage Java 21 features for readability. No new functionality.

 Candidates

 - Records: PMetaData (18 fields), Logger.Message, PcapDeviceList.DeviceEntry
 - Pattern matching: Replace instanceof + cast with binding variables throughout
 - Switch expressions: Logger.java severity mapping, Launcher.java arg parsing
 - Sealed interfaces: Consider for Plugin subtypes

 Verification

 - ./gradlew test — all tests still pass

 ---
 Phase 10: Packaging Modernization

 Dependencies: Phase 5, Phase 8
 Goal: Replace WIX-based MSI installers with jpackage cross-platform packaging.

 Changes

 - Add jpackage task to build.gradle
 - Delete GM3/Installers/ directory (WIX scripts, jnetpcap DLLs)
 - Remove EOL distro targets (CentOS 6, Ubuntu 14.04, Debian 8)

 Verification

 - ./gradlew package produces a working app-image
 - Application launches from the packaged image

 ---
 Dependency Graph

 Phase 1 (Foundation)
 ├── Phase 2 (Remove JARs) ──────────────────┐
 ├── Phase 3 (Bug fixes) ──┬── Phase 4 (Logging)  │
 │                          └── Phase 5 (Pcap4J) ──┤
 │                               └── Phase 6 (Tests)│
 │                                    └── Phase 7 (CI)
 │                                         └── Phase 8 (Build cleanup) ─┬── Phase 9
 (Modernize)
 │                                              ▲                       └── Phase 10
 (Packaging)
 │                                              │
 └──────────────────────────────────────────────┘

 Phases 2, 3, 4, and 5 can proceed in parallel after Phase 1 (they touch largely disjoint
 files).
╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌
