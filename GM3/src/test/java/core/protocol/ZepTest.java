package core.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Zep - ZigBee Encapsulation Protocol")
class ZepTest {

    // ---------------------------------------------------------------
    // isZEPProtocol
    // ---------------------------------------------------------------

    @Test
    @DisplayName("isZEPProtocol returns true for ZEP port (17754) on both src and dst")
    void isZepProtocol_true() {
        assertTrue(Zep.isZEPProtocol(17754, 17754));
    }

    @Test
    @DisplayName("isZEPProtocol returns false for HTTP ports")
    void isZepProtocol_httpFalse() {
        assertFalse(Zep.isZEPProtocol(80, 80));
    }

    @Test
    @DisplayName("isZEPProtocol returns false when only src is ZEP port")
    void isZepProtocol_onlySrc() {
        assertFalse(Zep.isZEPProtocol(17754, 80));
    }

    @Test
    @DisplayName("isZEPProtocol returns false when only dst is ZEP port")
    void isZepProtocol_onlyDst() {
        assertFalse(Zep.isZEPProtocol(80, 17754));
    }

    @Test
    @DisplayName("isZEPProtocol returns false for zero ports")
    void isZepProtocol_zeroPorts() {
        assertFalse(Zep.isZEPProtocol(0, 0));
    }

    // ---------------------------------------------------------------
    // fromArray with valid ZEP v1 header
    // ---------------------------------------------------------------

    @Test
    @DisplayName("fromArray parses a valid ZEP v1 header")
    void fromArray_v1Header() {
        // Build a minimal ZEP v1 packet:
        // Offset  Field              Size  Value
        // 0-1     Preamble ("EX")    2     0x45, 0x58
        // 2       Version            1     0x01
        // 3       Channel ID         1     0x0B (channel 11)
        // 4-5     Device ID          2     0x00, 0x01
        // 6       CRC/LQI Mode       1     0x01 (CRC)
        // 7       LQI Value          1     0x64 (100)
        // 8-14    Reserved           7     zeros
        // 15      Length             1     0x10 (16 bytes payload)
        byte[] header = new byte[16];
        header[0] = 'E';
        header[1] = 'X';
        header[2] = 1;   // version 1
        header[3] = 11;  // channel 11
        header[4] = 0;   // device ID high
        header[5] = 1;   // device ID low
        header[6] = 1;   // CRC mode
        header[7] = 100; // LQI value
        // 8-14 reserved (zeros by default)
        header[15] = 16; // length

        Zep zep = new Zep();
        zep.fromArray(header);

        assertEquals(1, zep.getVersion());
        assertEquals(11, zep.getChannelID());
        assertEquals(1, zep.getMode());
        assertEquals(100, zep.getLqiValue());
        assertEquals(16, zep.getLength());
    }

    @Test
    @DisplayName("fromArray with v1 header sets v1 flag correctly")
    void fromArray_v1FlagTrue() {
        byte[] header = new byte[16];
        header[0] = 'E';
        header[1] = 'X';
        header[2] = 1; // version 1

        Zep zep = new Zep();
        zep.fromArray(header);

        assertTrue(zep.v1);
        assertEquals(1, zep.getVersion());
    }

    // ---------------------------------------------------------------
    // fromArray with valid ZEP v2 header
    // ---------------------------------------------------------------

    @Test
    @DisplayName("fromArray parses a valid ZEP v2 data header")
    void fromArray_v2DataHeader() {
        // Build a minimal ZEP v2 data packet:
        // Offset  Field              Size  Value
        // 0-1     Preamble ("EX")    2     0x45, 0x58
        // 2       Version            1     0x02
        // 3       Type               1     0x01 (DATA)
        // 4       Channel ID         1     0x0F (channel 15)
        // 5-6     Device ID          2     0x00, 0x02
        // 7       CRC/LQI Mode       1     0x01
        // 8       LQI Value          1     0x50
        // 9-16    NTP Timestamp      8     zeros
        // 17-20   Seq Number         4     zeros
        // 21-30   Reserved           10    zeros
        // 31      Length             1     0x20
        byte[] header = new byte[32];
        header[0] = 'E';
        header[1] = 'X';
        header[2] = 2;   // version 2
        header[3] = 1;   // type = DATA
        header[4] = 15;  // channel 15
        header[5] = 0;   // device ID high
        header[6] = 2;   // device ID low
        header[7] = 1;   // CRC mode
        header[8] = 80;  // LQI value
        // 9-30 zeros
        header[31] = 32; // length

        Zep zep = new Zep();
        zep.fromArray(header);

        assertEquals(2, zep.getVersion());
        assertFalse(zep.v1);
        assertTrue(zep.data);
        assertEquals(15, zep.getChannelID());
        assertEquals(1, zep.getType());
        assertEquals(1, zep.getMode());
        assertEquals(80, zep.getLqiValue());
        assertEquals(32, zep.getLength());
    }

    @Test
    @DisplayName("fromArray parses a valid ZEP v2 ACK header")
    void fromArray_v2AckHeader() {
        // ZEP v2 ACK: Type = 2 (ACK)
        byte[] header = new byte[32];
        header[0] = 'E';
        header[1] = 'X';
        header[2] = 2; // version 2
        header[3] = 2; // type = ACK

        Zep zep = new Zep();
        zep.fromArray(header);

        assertEquals(2, zep.getVersion());
        assertFalse(zep.v1);
        assertFalse(zep.data);
        assertEquals(2, zep.getType());
    }

    // ---------------------------------------------------------------
    // getVersion()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getVersion returns version 1 for v1 packet")
    void getVersion_v1() {
        byte[] header = new byte[16];
        header[0] = 'E';
        header[1] = 'X';
        header[2] = 1;

        Zep zep = new Zep();
        zep.fromArray(header);
        assertEquals(1, zep.getVersion());
    }

    @Test
    @DisplayName("getVersion returns version 2 for v2 packet")
    void getVersion_v2() {
        byte[] header = new byte[32];
        header[0] = 'E';
        header[1] = 'X';
        header[2] = 2;
        header[3] = 1; // DATA type

        Zep zep = new Zep();
        zep.fromArray(header);
        assertEquals(2, zep.getVersion());
    }

    // ---------------------------------------------------------------
    // getPreamble()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getPreamble returns 'EX' for a valid ZEP packet")
    void getPreamble_valid() {
        byte[] header = new byte[16];
        header[0] = 'E';
        header[1] = 'X';
        header[2] = 1;

        Zep zep = new Zep();
        zep.fromArray(header);
        assertEquals("EX", zep.getPreamble());
    }

    // ---------------------------------------------------------------
    // Default constructor
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Default constructor creates a Zep object")
    void defaultConstructor() {
        Zep zep = new Zep();
        assertNotNull(zep);
    }

    // ---------------------------------------------------------------
    // ZEP_PORTS constant
    // ---------------------------------------------------------------

    @Test
    @DisplayName("ZEP_PORTS constant is 17754")
    void zepPortsConstant() {
        assertEquals(17754, Zep.ZEP_PORTS);
    }
}
