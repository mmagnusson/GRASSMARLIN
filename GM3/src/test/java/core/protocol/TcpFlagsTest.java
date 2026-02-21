package core.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TcpFlags - TCP flag bitmask to string set conversion")
class TcpFlagsTest {

    // ---------------------------------------------------------------
    // Individual flag bitmask constants
    // ---------------------------------------------------------------

    @Test
    @DisplayName("FIN has bitmask 0x01")
    void fin_bitmask() {
        assertEquals(0x01, TcpFlags.FIN.getBitmask());
    }

    @Test
    @DisplayName("SYN has bitmask 0x02")
    void syn_bitmask() {
        assertEquals(0x02, TcpFlags.SYN.getBitmask());
    }

    @Test
    @DisplayName("RST has bitmask 0x04")
    void rst_bitmask() {
        assertEquals(0x04, TcpFlags.RST.getBitmask());
    }

    @Test
    @DisplayName("PSH has bitmask 0x08")
    void psh_bitmask() {
        assertEquals(0x08, TcpFlags.PSH.getBitmask());
    }

    @Test
    @DisplayName("ACK has bitmask 0x10")
    void ack_bitmask() {
        assertEquals(0x10, TcpFlags.ACK.getBitmask());
    }

    @Test
    @DisplayName("URG has bitmask 0x20")
    void urg_bitmask() {
        assertEquals(0x20, TcpFlags.URG.getBitmask());
    }

    @Test
    @DisplayName("ECE has bitmask 0x40")
    void ece_bitmask() {
        assertEquals(0x40, TcpFlags.ECE.getBitmask());
    }

    @Test
    @DisplayName("CWR has bitmask 0x80")
    void cwr_bitmask() {
        assertEquals(0x80, TcpFlags.CWR.getBitmask());
    }

    // ---------------------------------------------------------------
    // fromBitmask - single flags
    // ---------------------------------------------------------------

    @Test
    @DisplayName("fromBitmask(0x02) returns {SYN}")
    void fromBitmask_syn() {
        Set<String> flags = TcpFlags.fromBitmask(0x02);
        assertEquals(Set.of("SYN"), flags);
    }

    @Test
    @DisplayName("fromBitmask(0x01) returns {FIN}")
    void fromBitmask_fin() {
        Set<String> flags = TcpFlags.fromBitmask(0x01);
        assertEquals(Set.of("FIN"), flags);
    }

    @Test
    @DisplayName("fromBitmask(0x10) returns {ACK}")
    void fromBitmask_ack() {
        Set<String> flags = TcpFlags.fromBitmask(0x10);
        assertEquals(Set.of("ACK"), flags);
    }

    // ---------------------------------------------------------------
    // fromBitmask - combined flags
    // ---------------------------------------------------------------

    @Test
    @DisplayName("fromBitmask(0x12) returns {SYN, ACK}")
    void fromBitmask_synAck() {
        Set<String> flags = TcpFlags.fromBitmask(0x12);
        assertEquals(Set.of("SYN", "ACK"), flags);
    }

    @Test
    @DisplayName("fromBitmask(0x18) returns {PSH, ACK}")
    void fromBitmask_pshAck() {
        Set<String> flags = TcpFlags.fromBitmask(0x18);
        assertEquals(Set.of("PSH", "ACK"), flags);
    }

    @Test
    @DisplayName("fromBitmask(0x11) returns {FIN, ACK}")
    void fromBitmask_finAck() {
        Set<String> flags = TcpFlags.fromBitmask(0x11);
        assertEquals(Set.of("FIN", "ACK"), flags);
    }

    // ---------------------------------------------------------------
    // fromBitmask - edge cases
    // ---------------------------------------------------------------

    @Test
    @DisplayName("fromBitmask(0x00) returns empty set")
    void fromBitmask_noFlags() {
        Set<String> flags = TcpFlags.fromBitmask(0x00);
        assertTrue(flags.isEmpty());
    }

    @Test
    @DisplayName("fromBitmask(0xFF) returns all 8 flags")
    void fromBitmask_allFlags() {
        Set<String> flags = TcpFlags.fromBitmask(0xFF);
        assertEquals(8, flags.size());
        assertTrue(flags.contains("FIN"));
        assertTrue(flags.contains("SYN"));
        assertTrue(flags.contains("RST"));
        assertTrue(flags.contains("PSH"));
        assertTrue(flags.contains("ACK"));
        assertTrue(flags.contains("URG"));
        assertTrue(flags.contains("ECE"));
        assertTrue(flags.contains("CWR"));
    }

    // ---------------------------------------------------------------
    // fromBitmask - immutability
    // ---------------------------------------------------------------

    @Test
    @DisplayName("fromBitmask returns unmodifiable set")
    void fromBitmask_unmodifiable() {
        Set<String> flags = TcpFlags.fromBitmask(0x02);
        assertThrows(UnsupportedOperationException.class, () -> flags.add("EXTRA"));
    }

    // ---------------------------------------------------------------
    // Enum values()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("There are exactly 8 TCP flag values")
    void values_count() {
        assertEquals(8, TcpFlags.values().length);
    }

    @Test
    @DisplayName("Flag names match their enum constant names")
    void flagNames() {
        assertEquals("SYN", TcpFlags.SYN.name());
        assertEquals("ACK", TcpFlags.ACK.name());
        assertEquals("FIN", TcpFlags.FIN.name());
        assertEquals("RST", TcpFlags.RST.name());
        assertEquals("PSH", TcpFlags.PSH.name());
        assertEquals("URG", TcpFlags.URG.name());
        assertEquals("ECE", TcpFlags.ECE.name());
        assertEquals("CWR", TcpFlags.CWR.name());
    }
}
