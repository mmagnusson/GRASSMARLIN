package core;

import core.logging.Logger;
import core.logging.Severity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.pcap4j.core.PcapAddress;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class PcapDeviceList {
    public static class DeviceEntry {
        private PcapNetworkInterface device;

        public DeviceEntry(PcapNetworkInterface device) {
            this.device = device;
        }

        public PcapNetworkInterface getDevice() {
            return device;
        }

        @Override
        public String toString() {
            if(device == null) {
                return "[null]";
            } else {
                String text = device.getDescription();
                if(text == null || text.isEmpty()) {
                    text = device.getName();
                }
                if(text == null || text.isEmpty()) {
                    List<PcapAddress> addresses = device.getAddresses();
                    if (addresses != null && !addresses.isEmpty()) {
                        text = addresses.get(0).getAddress().getHostAddress();
                    }
                }
                if(text == null || text.isEmpty()) {
                    text = "[Unnamed Device]";
                }

                return text;
            }
        }
    }

    public static ObservableList<DeviceEntry> get() {
        ObservableList<DeviceEntry> result = FXCollections.observableList(new CopyOnWriteArrayList<>());
        try {
            List<PcapNetworkInterface> devices = Pcaps.findAllDevs();
            for(PcapNetworkInterface device : devices) {
                result.add(new DeviceEntry(device));
            }
        } catch (java.lang.UnsatisfiedLinkError ex) {
            result.clear();
            Logger.log(PcapDeviceList.class, Severity.Error, "Live capture is unavailable due to insufficient permissions or a missing PCAP library.");
        } catch(PcapNativeException ex) {
            result.clear();
            Logger.log(PcapDeviceList.class, Severity.Error, "Live capture is unavailable: " + ex.getMessage());
        } catch(Exception ex) {
            result.clear();
            Logger.log(PcapDeviceList.class, Severity.Error, "Live capture is unavailable.");
        }

        return result;
    }
}
