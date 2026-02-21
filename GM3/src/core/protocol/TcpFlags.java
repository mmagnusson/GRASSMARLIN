package core.protocol;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Library-agnostic TCP flag abstraction, replacing dependency on org.jnetpcap.protocol.tcpip.Tcp.Flag.
 * Provides string-based flag names compatible with fingerprint matching in FProcessor.
 */
public enum TcpFlags {
    FIN(0x01),
    SYN(0x02),
    RST(0x04),
    PSH(0x08),
    ACK(0x10),
    URG(0x20),
    ECE(0x40),
    CWR(0x80);

    private final int bitmask;

    TcpFlags(int bitmask) {
        this.bitmask = bitmask;
    }

    public int getBitmask() {
        return bitmask;
    }

    /**
     * Converts a raw TCP flags bitmask byte into a Set of flag name strings.
     * The string names match what fingerprint XML files use for flag matching.
     *
     * @param rawFlags the raw TCP flags byte
     * @return unmodifiable set of flag name strings (e.g., "SYN", "ACK")
     */
    public static Set<String> fromBitmask(int rawFlags) {
        Set<String> flags = new HashSet<>();
        for (TcpFlags flag : values()) {
            if ((rawFlags & flag.bitmask) != 0) {
                flags.add(flag.name());
            }
        }
        return Collections.unmodifiableSet(flags);
    }
}
