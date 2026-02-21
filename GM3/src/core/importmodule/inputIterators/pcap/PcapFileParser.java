package core.importmodule.inputIterators.pcap;

import core.Configuration;
import core.exec.IEEE802154Data;
import core.fingerprint.PMetaData;
import core.fingerprint.PacketData;
import core.importmodule.ImportItem;
import core.logging.Logger;
import core.logging.Severity;
import core.protocol.IEEE_802_15_4;
import core.protocol.PayloadBuffer;
import core.protocol.TcpFlags;
import core.protocol.Zep;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import org.pcap4j.packet.namednumber.DataLinkType;
import util.Cidr;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Class to import a pcap file to return an {@link Iterator} of {@link core.fingerprint.PacketData}
 */
public class PcapFileParser {

    private static final int PACKET_INTERVAL_MILLIS = 232;
    private static final int PACKET_INTERVAL_PACKETS = 4000;

    private long lastCheckTime;
    private int numPackets;

    protected Runnable fnOnNewPacket = null;

    /**
     * IANA id for TCP protocol.
     */
    public static final short TCP_ID = 6;
    /**
     * IANA id for UDP protocol.
     */
    public static final short UDP_ID = 17;
    /**
     * UNKNOWN protocol.
     */
    public static final short UNKNOWN_ID = -1;

    private final ImportItem source;

    protected final Path inPath;

    private boolean done;

    private BlockingQueue<Object> packetQueue;

    protected PcapHandle handle;

    protected PcapFileParser(ImportItem source, Path inPath) {
        this.source = source;
        this.inPath = inPath;
        this.packetQueue = new ArrayBlockingQueue<>(100);

        this.lastCheckTime = System.currentTimeMillis();
        numPackets = 0;
    }

    public static Iterator<Object> getPcapFileIterator(ImportItem source, Path inPath) throws IllegalStateException{
        PcapFileParser parser = new PcapFileParser(source, inPath);

        parser.parseSource();

        return parser.new LogicalIterator();
    }

    protected void parseSource() throws IllegalStateException{
        done = false;
        PcapHandle pcapHandle = getHandle();

        if (pcapHandle == null) {
            throw new IllegalStateException("Unable load pcap from " + this.inPath);
        }

        String txtFilter = Configuration.getPreferenceString(Configuration.Fields.PCAP_FILTER_STRING);
        if(txtFilter != null && !txtFilter.trim().equals("")) {
            try {
                pcapHandle.setFilter(txtFilter, BpfProgram.BpfCompileMode.OPTIMIZE);
                Logger.log(this, Severity.Information, "Using PCAP filter: '" + txtFilter + "'");
            } catch (PcapNativeException | NotOpenException e) {
                Logger.log(this, Severity.Warning, "Unable to initialize PCAP filter for '" + txtFilter + "' (" + e.getMessage() + ").  Filtering will not be performed.");
            }
        }

        Runnable loop = () -> {
            try {
                processPackets(pcapHandle);
            } finally {
                done = true;
                try {
                    pcapHandle.close();
                } catch (Exception e) {
                    // best effort
                }
            }
        };
        Thread loopThread = new Thread(loop, "pcap loop");
        loopThread.setDaemon(true);
        loopThread.start();
    }

    private void processPackets(PcapHandle pcapHandle) {
        long frameNumber = 0;
        while (true) {
            try {
                Packet packet = pcapHandle.getNextPacketEx();
                frameNumber++;

                if (numPackets++ == PACKET_INTERVAL_PACKETS) {
                    long sleepTime = lastCheckTime + PACKET_INTERVAL_MILLIS - System.currentTimeMillis();
                    if (sleepTime >= 0) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    lastCheckTime = System.currentTimeMillis();
                    numPackets = 0;
                }

                long timestampMs = pcapHandle.getTimestamp().getTime();
                int captureLength = packet.length();

                handlePacket(packet, timestampMs, frameNumber, captureLength);

            } catch (java.util.concurrent.TimeoutException e) {
                // No packet available, continue
            } catch (java.io.EOFException e) {
                // End of pcap file
                break;
            } catch (PcapNativeException | NotOpenException e) {
                Logger.log(this, Severity.Error, "Error reading pcap: " + e.getMessage());
                break;
            }
        }
    }

    private void handlePacket(Packet packet, long timestampMs, long frameNumber, int captureLength) {
        try {
            // Check for IP layer
            IpV4Packet ipPacket = packet.get(IpV4Packet.class);
            EthernetPacket ethPacket = packet.get(EthernetPacket.class);

            if (ipPacket == null || ethPacket == null) {
                source.recordTaskProgress(captureLength + 16);
                return;
            }

            IpV4Packet.IpV4Header ipHeader = ipPacket.getHeader();
            EthernetPacket.EthernetHeader ethHeader = ethPacket.getHeader();

            long srcIp = new BigInteger(1, ipHeader.getSrcAddr().getAddress()).longValue();
            long destIp = new BigInteger(1, ipHeader.getDstAddr().getAddress()).longValue();

            byte[] srcMac = ethHeader.getSrcAddr().getAddress();
            byte[] dstMac = ethHeader.getDstAddr().getAddress();

            // Associate the macs with the hosts
            try {
                final java.util.Map<String, String> propertiesSource = new java.util.HashMap<>();
                propertiesSource.put("MAC", new util.Mac(srcMac).toString());
                packetQueue.put(new core.importmodule.LogicalProcessor.Host(new Cidr(srcIp), propertiesSource, null));
                final java.util.Map<String, String> propertiesDest = new java.util.HashMap<>();
                propertiesDest.put("MAC", new util.Mac(dstMac).toString());
                packetQueue.put(new core.importmodule.LogicalProcessor.Host(new Cidr(destIp), propertiesDest, null));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            PacketData data = null;
            TcpPacket tcpPacket = ipPacket.get(TcpPacket.class);
            UdpPacket udpPacket = ipPacket.get(UdpPacket.class);

            if (tcpPacket != null) {
                TcpPacket.TcpHeader tcpHeader = tcpPacket.getHeader();
                byte[] payloadBytes;
                Packet tcpPayload = tcpPacket.getPayload();
                if (tcpPayload != null) {
                    payloadBytes = tcpPayload.getRawData();
                } else {
                    payloadBytes = new byte[0];
                }
                // Extra byte like original code: new JBuffer(tcp.getPayloadLength() + 1)
                byte[] bufferBytes = Arrays.copyOf(payloadBytes, payloadBytes.length + 1);
                PayloadBuffer temp = new PayloadBuffer(bufferBytes);

                int mss = -1;
                // Extract MSS from TCP options if present
                for (TcpPacket.TcpOption option : tcpHeader.getOptions()) {
                    if (option.getKind().value() == 2 && option.length() >= 4) { // MSS option
                        byte[] optData = option.getRawData();
                        if (optData.length >= 4) {
                            mss = ((optData[2] & 0xFF) << 8) | (optData[3] & 0xFF);
                        }
                    }
                }

                // Extract TCP flags as Set<String>
                int rawFlags = tcpHeader.getRawData()[13] & 0xFF;
                Set<String> flags = TcpFlags.fromBitmask(rawFlags);

                PMetaData meta = new PMetaData(source, timestampMs, frameNumber,
                        tcpHeader.getSrcPort().valueAsInt(), tcpHeader.getDstPort().valueAsInt(), TCP_ID,
                        new Cidr(srcIp), Arrays.copyOf(srcMac, srcMac.length),
                        new Cidr(destIp), Arrays.copyOf(dstMac, dstMac.length),
                        tcpHeader.getAcknowledgmentNumberAsLong(),
                        packet.length(), 2048,
                        mss, tcpHeader.getSequenceNumberAsLong(),
                        ipHeader.getTtlAsInt(),
                        tcpHeader.getWindowAsInt(), flags);
                data = new PacketData(captureLength + 16, meta, temp);

            } else if (udpPacket != null) {
                UdpPacket.UdpHeader udpHeader = udpPacket.getHeader();
                int srcPort = udpHeader.getSrcPort().valueAsInt();
                int dstPort = udpHeader.getDstPort().valueAsInt();

                byte[] payloadBytes;
                Packet udpPayload = udpPacket.getPayload();
                if (udpPayload != null) {
                    payloadBytes = udpPayload.getRawData();
                } else {
                    payloadBytes = new byte[0];
                }

                if (Zep.isZEPProtocol(srcPort, dstPort)) {
                    Zep zep = new Zep();
                    IEEE_802_15_4 ieee802154 = new IEEE_802_15_4();
                    zep.fromArray(Arrays.copyOf(payloadBytes, payloadBytes.length));
                    ieee802154.setBuffer(zep.getNextBuffer());
                    IEEE802154Data meshData = new IEEE802154Data();
                    meshData.setChannel(zep.getChannelID());
                    meshData.settDevice(zep.getDestinationDeviceID());
                    meshData.setsDevice(zep.getSourceDeviceID());
                    meshData.setSource(ieee802154.getSourceDeviceId());
                    meshData.setTarget(ieee802154.getDestinationDeviceId());
                    meshData.setTargetPan(ieee802154.getDestinationPanId());
                    meshData.setIntraPan(ieee802154.isIntraPan());
                    try {
                        packetQueue.put(meshData);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                // Extra byte like original code
                byte[] bufferBytes = Arrays.copyOf(payloadBytes, payloadBytes.length + 1);
                PayloadBuffer temp = new PayloadBuffer(bufferBytes);

                PMetaData meta = new PMetaData(source, timestampMs, frameNumber,
                        srcPort, dstPort, UDP_ID,
                        new Cidr(srcIp), Arrays.copyOf(srcMac, srcMac.length),
                        new Cidr(destIp), Arrays.copyOf(dstMac, dstMac.length),
                        -1, captureLength + 16, 2048,
                        -1, -1, ipHeader.getTtlAsInt(), -1, null);
                data = new PacketData(captureLength + 16, meta, temp);

            } else {
                PMetaData meta = new PMetaData(source, timestampMs, frameNumber,
                        -1, -1, UNKNOWN_ID,
                        new Cidr(srcIp), Arrays.copyOf(srcMac, srcMac.length),
                        new Cidr(destIp), Arrays.copyOf(dstMac, dstMac.length),
                        -1, packet.length(), 2048,
                        -1, -1, ipHeader.getTtlAsInt(), -1, null);
                data = new PacketData(captureLength + 16, meta);
            }

            if (data != null) {
                try {
                    packetQueue.put(data);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception ex) {
            Logger.log(this, Severity.Error, "Error processing packet: " + ex.getMessage());
        }
    }

    /**
     * Retrieves a new PCAP handle. May return null if Pcap4J is not
     * available. This is non-static so that Live PCAP capture can override it.
     *
     * @return PcapHandle to the pcap file that belongs to this ImportItem.
     */
    protected PcapHandle getHandle() {
        PcapHandle pcapHandle = null;
        try {
            pcapHandle = Pcaps.openOffline(this.inPath.toString());
        } catch (UnsatisfiedLinkError err) {
            Logger.log(this, Severity.Error, "Importing PCAP is disabled. " + err.getMessage());
        } catch (PcapNativeException ex) {
            Logger.log(this, Severity.Error, "Failed to import. Reason: " + ex.getMessage());
        }
        return pcapHandle;
    }

    protected class LogicalIterator implements Iterator<Object> {

        @Override
        public boolean hasNext() {
            return !(done && packetQueue.isEmpty());
        }

        @Override
        public Object next() {
            Object result = packetQueue.poll();
            if(result != null && fnOnNewPacket != null) {
                fnOnNewPacket.run();
            }
            return result;
        }
    }
}
