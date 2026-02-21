package core.importmodule.inputIterators.pcap;

import core.Configuration;
import core.importmodule.ImportItem;
import core.logging.Logger;
import core.logging.Severity;
import org.pcap4j.core.*;

import java.nio.file.Path;
import java.util.Iterator;

/**
 * Class to iterate through live pcap
 */
public class PcapLiveParser extends PcapFileParser {

    private final PcapNetworkInterface device;

    private PcapLiveParser(ImportItem source, Path dumpPath, PcapNetworkInterface device, Runnable fnOnNewPacket) {
        super(source, dumpPath);
        this.device = device;

        this.fnOnNewPacket = fnOnNewPacket;
    }

    public static PcapLiveParser getInstance(ImportItem source, Path dumpPath, PcapNetworkInterface device, Runnable fnOnNewPacket) {
        return new PcapLiveParser(source, dumpPath, device, fnOnNewPacket);
    }

    public Iterator<Object> start() {
        this.parseSource();

        return this.new LogicalIterator();
    }

    public void stop() {
        if (handle != null) {
            try {
                handle.breakLoop();
            } catch (NotOpenException e) {
                // already closed
            }
            handle.close();
            handle = null;
        }
    }

    @Override
    protected PcapHandle getHandle() {
        handle = null;

        int snaplen = (int) Configuration.getPreferenceLong(Configuration.Fields.PCAP_FLAG_SNAPLEN);
        int mode = (int) Configuration.getPreferenceLong(Configuration.Fields.PCAP_FLAG_MODE);
        int timeout = (int) Configuration.getPreferenceLong(Configuration.Fields.PCAP_FLAG_TIMEOUT);

        PcapNetworkInterface.PromiscuousMode promiscuousMode =
                mode != 0 ? PcapNetworkInterface.PromiscuousMode.PROMISCUOUS
                          : PcapNetworkInterface.PromiscuousMode.NONPROMISCUOUS;

        try {
            handle = device.openLive(snaplen, promiscuousMode, timeout);
        } catch (UnsatisfiedLinkError err) {
            Logger.log(this, Severity.Error, "Importing PCAP is disabled: " + err);
        } catch (PcapNativeException ex) {
            Logger.log(this, Severity.Error, "Failed to open live capture: " + ex.getMessage());
        }

        if (handle != null) {
            String nameDumpFile = inPath.toString();
            try {
                PcapDumper dumper = handle.dumpOpen(nameDumpFile);
                Logger.log(this, Severity.Information, "Live PCAP is being logged to " + nameDumpFile);
            } catch (PcapNativeException | NotOpenException ex) {
                Logger.log(this, Severity.Warning, "Unable to open dump file: " + ex.getMessage());
            }
        }
        return handle;
    }
}
