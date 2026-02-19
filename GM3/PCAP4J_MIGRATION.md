# JNetPcap to Pcap4J Migration Guide

## Status: PLANNED (Pcap4J dependency added to Gradle build)

The `build.gradle` already includes Pcap4J dependencies. The JNetPcap JAR in `lib/` is
retained for backward compatibility until the migration is complete.

## Why Migrate

- JNetPcap is **abandoned** (no updates since ~2015)
- Requires platform-specific native DLLs (jnetpcap-32.dll, jnetpcap-64.dll)
- No Java module system support
- Pcap4J is actively maintained, pure-Java API with native bridge via JNA

## Files to Migrate

### Core (must change):
1. `src/core/PcapDeviceList.java` - Device enumeration
2. `src/core/importmodule/LivePCAPImport.java` - Live capture entry point
3. `src/core/importmodule/inputIterators/pcap/PcapFileParser.java` - **Largest change** (packet dissection)
4. `src/core/importmodule/inputIterators/pcap/PcapLiveParser.java` - Live capture loop

### Plugins:
5. `data/plugins/iadgov.offlinepcap/PcapFileParser.java` - Offline pcap plugin
6. `data/plugins/iadgov.offlinepcap/PcapNgFileParser.java` - PcapNG support
7. `data/plugins/iadgov.offlinepcap/PacketHandler.java` - Packet handling

### Supporting (import references):
8. `src/core/fingerprint/PMetaData.java`
9. `src/core/fingerprint/PacketData.java`
10. `src/core/protocol/Zep.java`
11. `src/ui/dialog/FilterDialogFx.java`
12. `src/util/Launcher.java` - `System.loadLibrary("jnetpcap")` call
13. `data/plugins/iadgov.csvimport/CsvFileIterator.java`

## API Mapping

| JNetPcap | Pcap4J |
|----------|--------|
| `Pcap.openOffline(path, err)` | `Pcaps.openOffline(path)` |
| `Pcap.openLive(dev, snap, mode, timeout, err)` | `nif.openLive(snap, mode, timeout)` |
| `Pcap.findAllDevs(list, err)` | `Pcaps.findAllDevs()` |
| `pcap.loop(count, handler, user)` | `handle.loop(count, listener)` |
| `pcap.compile(prog, filter, opt, mask)` | `handle.setFilter(filter, mode)` |
| `pcap.breakloop()` | `handle.breakLoop()` |
| `pcap.close()` | `handle.close()` |
| `PcapIf` | `PcapNetworkInterface` |
| `PcapBpfProgram` | Built into `setFilter()` |
| `PcapDumper` | `PcapDumper` (similar) |
| `packet.hasHeader(ip4)` | `packet.get(IpV4Packet.class) != null` |
| `ip4.source()` | `ipPacket.getHeader().getSrcAddr()` |
| `tcp.source()` | `tcpPacket.getHeader().getSrcPort()` |
| `eth.source()` | `ethPacket.getHeader().getSrcAddr()` |
| `JBuffer` / `JPacket` | `Packet` / `byte[]` |

## Migration Steps

1. Create adapter interfaces that abstract pcap operations
2. Implement Pcap4J adapter
3. Update PcapDeviceList to use Pcap4J device enumeration
4. Rewrite PcapFileParser packet dissection using Pcap4J packet classes
5. Update PcapLiveParser for live capture
6. Update plugins
7. Remove JNetPcap JAR and native DLL references
8. Test with sample PCAP files (TCP, UDP, ZEP/802.15.4 protocols)
