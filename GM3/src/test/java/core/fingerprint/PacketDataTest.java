package core.fingerprint;

import core.protocol.PayloadBuffer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.Cidr;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PacketData - packet metadata and payload wrapper")
class PacketDataTest {

    /**
     * Helper to create a PMetaData with specified flags and other defaults.
     */
    private PMetaData createMeta(Set<String> flags) {
        return new PMetaData(
                null,           // source (ImportItem)
                1000L,          // time
                1L,             // frame
                12345,          // sourcePort
                80,             // destPort
                (short) 6,      // transportProtocol (TCP)
                new Cidr("10.0.0.1"),   // sourceIp
                new byte[]{0, 0, 0, 0, 0, 1}, // sourceMac
                new Cidr("10.0.0.2"),   // destIp
                new byte[]{0, 0, 0, 0, 0, 2}, // destMac
                0L,             // ack
                100L,           // dSize
                0x0800,         // ethertype (IPv4)
                1460,           // mss
                1L,             // seqNum
                64,             // ttl
                65535,          // windowNum
                flags           // flags
        );
    }

    /**
     * Helper to create a PMetaData with default (no-arg) constructor.
     */
    private PMetaData createDefaultMeta() {
        return new PMetaData();
    }

    // ---------------------------------------------------------------
    // Constructor with payload
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Constructor with payload: hasPayload() returns true")
    void constructorWithPayload_hasPayload() {
        PayloadBuffer payload = new PayloadBuffer(new byte[]{0x01, 0x02, 0x03});
        PacketData pd = new PacketData(1, createDefaultMeta(), payload);
        assertTrue(pd.hasPayload());
    }

    @Test
    @DisplayName("Constructor with payload: size() returns payload length")
    void constructorWithPayload_size() {
        PayloadBuffer payload = new PayloadBuffer(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05});
        PacketData pd = new PacketData(1, createDefaultMeta(), payload);
        assertEquals(5, pd.size());
    }

    // ---------------------------------------------------------------
    // Constructor without payload
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Constructor without payload: hasPayload() returns false")
    void constructorWithoutPayload_hasPayload() {
        PacketData pd = new PacketData(1, createDefaultMeta());
        assertFalse(pd.hasPayload());
    }

    @Test
    @DisplayName("Constructor without payload: size() returns 0")
    void constructorWithoutPayload_size() {
        PacketData pd = new PacketData(1, createDefaultMeta());
        assertEquals(0, pd.size());
    }

    @Test
    @DisplayName("Constructor with null payload: hasPayload() returns false")
    void constructorNullPayload_hasPayload() {
        PacketData pd = new PacketData(1, createDefaultMeta(), null);
        assertFalse(pd.hasPayload());
    }

    // ---------------------------------------------------------------
    // getByte(int)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getByte delegates to PayloadBuffer correctly")
    void getByte_delegatesToPayload() {
        byte[] data = new byte[]{0x0A, 0x0B, 0x0C, 0x0D};
        PayloadBuffer payload = new PayloadBuffer(data);
        PacketData pd = new PacketData(1, createDefaultMeta(), payload);

        assertEquals(0x0A, pd.getByte(0));
        assertEquals(0x0B, pd.getByte(1));
        assertEquals(0x0C, pd.getByte(2));
        assertEquals(0x0D, pd.getByte(3));
    }

    @Test
    @DisplayName("getByte returns 0 when no payload")
    void getByte_noPayload() {
        PacketData pd = new PacketData(1, createDefaultMeta());
        assertEquals(0, pd.getByte(0));
        assertEquals(0, pd.getByte(100));
    }

    // ---------------------------------------------------------------
    // match(byte[], int, int)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("match finds byte pattern at beginning")
    void match_foundAtBeginning() {
        byte[] data = new byte[]{0x48, 0x45, 0x4C, 0x4C, 0x4F}; // "HELLO"
        PayloadBuffer payload = new PayloadBuffer(data);
        PacketData pd = new PacketData(1, createDefaultMeta(), payload);

        int pos = pd.match(new byte[]{0x48, 0x45}, 0, 5);
        assertEquals(0, pos);
    }

    @Test
    @DisplayName("match finds byte pattern in the middle")
    void match_foundInMiddle() {
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        PayloadBuffer payload = new PayloadBuffer(data);
        PacketData pd = new PacketData(1, createDefaultMeta(), payload);

        int pos = pd.match(new byte[]{0x03, 0x04}, 0, 6);
        assertEquals(2, pos);
    }

    @Test
    @DisplayName("match returns -1 when pattern not found")
    void match_notFound() {
        byte[] data = new byte[]{0x01, 0x02, 0x03};
        PayloadBuffer payload = new PayloadBuffer(data);
        PacketData pd = new PacketData(1, createDefaultMeta(), payload);

        int pos = pd.match(new byte[]{0x04, 0x05}, 0, 3);
        assertEquals(-1, pos);
    }

    @Test
    @DisplayName("match returns -1 when no payload")
    void match_noPayload() {
        PacketData pd = new PacketData(1, createDefaultMeta());
        int pos = pd.match(new byte[]{0x01}, 0, 1);
        assertEquals(-1, pos);
    }

    @Test
    @DisplayName("match with offset starts search at offset")
    void match_withOffset() {
        byte[] data = new byte[]{0x01, 0x02, 0x01, 0x02, 0x03};
        PayloadBuffer payload = new PayloadBuffer(data);
        PacketData pd = new PacketData(1, createDefaultMeta(), payload);

        // Search for {0x01, 0x02} starting at offset 1
        int pos = pd.match(new byte[]{0x01, 0x02}, 1, 4);
        assertEquals(2, pos);
    }

    @Test
    @DisplayName("match with negative offset clamps to 0")
    void match_negativeOffset() {
        byte[] data = new byte[]{0x01, 0x02, 0x03};
        PayloadBuffer payload = new PayloadBuffer(data);
        PacketData pd = new PacketData(1, createDefaultMeta(), payload);

        // Negative offset should be clamped to 0
        int pos = pd.match(new byte[]{0x01}, -5, 3);
        assertEquals(0, pos);
    }

    @Test
    @DisplayName("match with empty search array returns -1")
    void match_emptySearch() {
        byte[] data = new byte[]{0x01, 0x02, 0x03};
        PayloadBuffer payload = new PayloadBuffer(data);
        PacketData pd = new PacketData(1, createDefaultMeta(), payload);

        // Empty search array has length 0, loop does not execute
        int pos = pd.match(new byte[]{}, 0, 3);
        assertEquals(-1, pos);
    }

    // ---------------------------------------------------------------
    // extract(int, int, int)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("extract returns correct byte range")
    void extract_correctRange() {
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05};
        PayloadBuffer payload = new PayloadBuffer(data);
        PacketData pd = new PacketData(1, createDefaultMeta(), payload);

        byte[] result = pd.extract(1, 4, 3);
        assertArrayEquals(new byte[]{0x02, 0x03, 0x04}, result);
    }

    @Test
    @DisplayName("extract with from > to uses min/max logic")
    void extract_fromGreaterThanTo() {
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05};
        PayloadBuffer payload = new PayloadBuffer(data);
        PacketData pd = new PacketData(1, createDefaultMeta(), payload);

        // from=4, to=1 -> start=min(4,1)=1, end=min(1+3, max(1,4))=min(4,4)=4
        byte[] result = pd.extract(4, 1, 3);
        assertArrayEquals(new byte[]{0x02, 0x03, 0x04}, result);
    }

    @Test
    @DisplayName("extract returns empty array when from is out of bounds")
    void extract_outOfBounds() {
        byte[] data = new byte[]{0x01, 0x02, 0x03};
        PayloadBuffer payload = new PayloadBuffer(data);
        PacketData pd = new PacketData(1, createDefaultMeta(), payload);

        byte[] result = pd.extract(10, 15, 5);
        assertEquals(0, result.length);
    }

    @Test
    @DisplayName("extract returns empty array for negative from")
    void extract_negativeFrom() {
        byte[] data = new byte[]{0x01, 0x02, 0x03};
        PayloadBuffer payload = new PayloadBuffer(data);
        PacketData pd = new PacketData(1, createDefaultMeta(), payload);

        byte[] result = pd.extract(-1, 2, 3);
        assertEquals(0, result.length);
    }

    // ---------------------------------------------------------------
    // getFlags()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getFlags returns null when PMetaData has null flags")
    void getFlags_null() {
        PMetaData meta = createMeta(null);
        PacketData pd = new PacketData(1, meta);
        assertNull(pd.getFlags());
    }

    @Test
    @DisplayName("getFlags returns the Set<String> from PMetaData")
    void getFlags_withFlags() {
        Set<String> flags = new HashSet<>();
        flags.add("SYN");
        flags.add("ACK");
        PMetaData meta = createMeta(flags);
        PacketData pd = new PacketData(1, meta);

        Set<String> result = pd.getFlags();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("SYN"));
        assertTrue(result.contains("ACK"));
    }

    @Test
    @DisplayName("getFlags returns empty set when PMetaData has empty flags")
    void getFlags_emptySet() {
        Set<String> flags = new HashSet<>();
        PMetaData meta = createMeta(flags);
        PacketData pd = new PacketData(1, meta);

        Set<String> result = pd.getFlags();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getFlags returns unmodifiable set from PMetaData")
    void getFlags_unmodifiable() {
        Set<String> flags = new HashSet<>();
        flags.add("SYN");
        PMetaData meta = createMeta(flags);
        PacketData pd = new PacketData(1, meta);

        Set<String> result = pd.getFlags();
        assertThrows(UnsupportedOperationException.class, () -> result.add("ACK"));
    }

    // ---------------------------------------------------------------
    // Metadata access
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getSourcePort returns value from PMetaData")
    void getSourcePort() {
        PMetaData meta = createMeta(null);
        PacketData pd = new PacketData(1, meta);
        assertEquals(12345, pd.getSourcePort());
    }

    @Test
    @DisplayName("getDestPort returns value from PMetaData")
    void getDestPort() {
        PMetaData meta = createMeta(null);
        PacketData pd = new PacketData(1, meta);
        assertEquals(80, pd.getDestPort());
    }

    @Test
    @DisplayName("getTransportProtocol returns value from PMetaData")
    void getTransportProtocol() {
        PMetaData meta = createMeta(null);
        PacketData pd = new PacketData(1, meta);
        assertEquals(6, pd.getTransportProtocol());
    }

    @Test
    @DisplayName("getSourceIp returns value from PMetaData")
    void getSourceIp() {
        PMetaData meta = createMeta(null);
        PacketData pd = new PacketData(1, meta);
        assertEquals(new Cidr("10.0.0.1"), pd.getSourceIp());
    }

    @Test
    @DisplayName("getDestIp returns value from PMetaData")
    void getDestIp() {
        PMetaData meta = createMeta(null);
        PacketData pd = new PacketData(1, meta);
        assertEquals(new Cidr("10.0.0.2"), pd.getDestIp());
    }

    @Test
    @DisplayName("getCompletionUnits returns constructor value")
    void getCompletionUnits() {
        PacketData pd = new PacketData(42, createDefaultMeta());
        assertEquals(42, pd.getCompletionUnits());
    }

    @Test
    @DisplayName("getEthertype returns value from PMetaData")
    void getEthertype() {
        PMetaData meta = createMeta(null);
        PacketData pd = new PacketData(1, meta);
        assertEquals(0x0800, pd.getEthertype());
    }

    @Test
    @DisplayName("getTtl returns value from PMetaData")
    void getTtl() {
        PMetaData meta = createMeta(null);
        PacketData pd = new PacketData(1, meta);
        assertEquals(64, pd.getTtl());
    }

    // ---------------------------------------------------------------
    // getByteArray(int offset, int length) - with size check
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getByteArray returns correct subarray when within bounds")
    void getByteArray_withinBounds() {
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        PayloadBuffer payload = new PayloadBuffer(data);
        PacketData pd = new PacketData(1, createDefaultMeta(), payload);

        byte[] result = pd.getByteArray(1, 3);
        assertArrayEquals(new byte[]{0x02, 0x03, 0x04}, result);
    }

    @Test
    @DisplayName("getByteArray returns empty array when offset+length exceeds payload size")
    void getByteArray_outOfBounds() {
        byte[] data = new byte[]{0x01, 0x02, 0x03};
        PayloadBuffer payload = new PayloadBuffer(data);
        PacketData pd = new PacketData(1, createDefaultMeta(), payload);

        // offset(1) + length(3) = 4 > size(3), so condition payload.size() > offset + length fails
        // Actually the check is payload.size() > offset + length i.e. 3 > 4 which is false
        byte[] result = pd.getByteArray(1, 3);
        assertEquals(0, result.length);
    }

    @Test
    @DisplayName("getByteArray returns empty array when no payload")
    void getByteArray_noPayload() {
        PacketData pd = new PacketData(1, createDefaultMeta());
        byte[] result = pd.getByteArray(0, 1);
        assertEquals(0, result.length);
    }
}
