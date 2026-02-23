Runtime Analysis & Recommended Changes                                                   
                                                                                         
CRITICAL — Bugs Blocking Core Functionality                                              

1. Plugins not loading — breaks PCAP import and live capture
                                                                                         
The plugins are compiled to build/app/plugins/ but the app looks for ./plugins/ relative 
to the working directory. Since there's no GM3/plugins/ directory, all plugins fail to   
load, which means:                                                                       
- .pcap / .pcapng file import is completely broken (provided by iadgov.offlinepcap       
plugin)
- Live capture crashes with NPE in LivePCAPImport.java:24 because                        
processorForPath(".pcap") returns null                                                   
- SVG export is unavailable
- The user's test pcap was handled by the Cisco parser as a fallback (wrong parser)

Fix: Either symlink/copy built plugins to GM3/plugins/, or better — register PCAP import
as a built-in processor instead of relying on a plugin for core functionality.

2. LivePCAPImport.java:24 — NPE on null processor

Even with plugins loaded, this line has no null guard:
source =
ImportProcessors.newItem(ImportProcessors.processorForPath(dumpPath).getProcessor(), ...)
The developer even marked it // HACK. Needs a null check.

---
MEDIUM — Data & Configuration Issues

3. Fingerprint loader picks up Review_Comments.txt — Launcher.java iterates all files in
the fingerprint directory without filtering by .xml extension, causing a JAXB exception
on the text file.

4. MAC manufacturer database hardcoded to Wireshark path — /usr/share/wireshark/manuf
doesn't exist without Wireshark installed. There's an oui.csv in the repo root that could
 serve as a bundled fallback, but the parser (Manufacturer.java) only handles Wireshark's
 tab-separated format.

5. ~50 missing country flag icons — The GeoIP name sanitization strips accented
characters (e.g., "Curaçao" → "Curaao.png"), and many territories simply don't have flag
files. Only a cosmetic issue but creates log noise.

6. GeoIP continent IDs (6255147, 6255148) — These are continent-level entries in
geo_id_to_name.csv with empty country names. The loader correctly warns but could filter
these out.

7. Legacy log files in logs/ — Five old .txt log files from July 2025 (the old Launcher
custom logger format) are still on disk. Harmless but could be cleaned up.

---
Improvements Worth Considering

8. Make PCAP import built-in instead of plugin-only — Line 43 of ImportProcessors.java
has a commented-out built-in PCAP registration. Since the Pcap4J migration made the core
PcapFileParser fully functional, the offline pcap plugin is now redundant. Moving PCAP
import to a built-in processor eliminates the fragile plugin dependency.

9. Add .pcapng support to core parser — The plugin provides both .pcap and .pcapng
handlers. If making PCAP built-in, ensure PcapNG is also covered.

10. Filter dialog BPF validation — Should verify the new Pcap4J-based validation works
correctly at runtime (not just compilation).

11. Test with real PCAP data — The test suite covers unit tests well, but there's no
integration test that actually parses a PCAP file through the full pipeline.

---
Want me to start fixing the critical issues (plugin loading + PCAP import)? The quickest
win would be copying the built plugins into place and adding the null guard, but the
cleaner long-term fix would be making PCAP import a built-in processor.
