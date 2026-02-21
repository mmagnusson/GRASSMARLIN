package core.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PayloadBuffer - byte array backed buffer")
class PayloadBufferTest {

    // ---------------------------------------------------------------
    // size()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("size() returns the length of the backing array")
    void size_returnsLength() {
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05};
        PayloadBuffer buffer = new PayloadBuffer(data);
        assertEquals(5, buffer.size());
    }

    @Test
    @DisplayName("size() returns 0 for empty array")
    void size_empty() {
        PayloadBuffer buffer = new PayloadBuffer(new byte[0]);
        assertEquals(0, buffer.size());
    }

    @Test
    @DisplayName("size(int) constructor creates buffer of specified size")
    void size_intConstructor() {
        PayloadBuffer buffer = new PayloadBuffer(10);
        assertEquals(10, buffer.size());
    }

    // ---------------------------------------------------------------
    // getByte(int)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getByte returns correct byte at each index")
    void getByte_correctBytes() {
        byte[] data = new byte[]{0x0A, 0x0B, 0x0C, 0x0D};
        PayloadBuffer buffer = new PayloadBuffer(data);

        assertEquals(0x0A, buffer.getByte(0));
        assertEquals(0x0B, buffer.getByte(1));
        assertEquals(0x0C, buffer.getByte(2));
        assertEquals(0x0D, buffer.getByte(3));
    }

    @Test
    @DisplayName("getByte handles negative byte values (high bit set)")
    void getByte_highBitSet() {
        byte[] data = new byte[]{(byte) 0xFF, (byte) 0x80};
        PayloadBuffer buffer = new PayloadBuffer(data);

        assertEquals((byte) 0xFF, buffer.getByte(0));
        assertEquals((byte) 0x80, buffer.getByte(1));
    }

    @Test
    @DisplayName("getByte throws ArrayIndexOutOfBoundsException for out-of-range index")
    void getByte_outOfBounds() {
        PayloadBuffer buffer = new PayloadBuffer(new byte[]{0x01, 0x02});
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> buffer.getByte(2));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> buffer.getByte(-1));
    }

    // ---------------------------------------------------------------
    // getByteArray(int offset, int length)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getByteArray(offset, length) returns correct subarray")
    void getByteArray_subarray() {
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05};
        PayloadBuffer buffer = new PayloadBuffer(data);

        byte[] result = buffer.getByteArray(1, 3);
        assertArrayEquals(new byte[]{0x02, 0x03, 0x04}, result);
    }

    @Test
    @DisplayName("getByteArray(offset, length) from start")
    void getByteArray_fromStart() {
        byte[] data = new byte[]{0x0A, 0x0B, 0x0C};
        PayloadBuffer buffer = new PayloadBuffer(data);

        byte[] result = buffer.getByteArray(0, 2);
        assertArrayEquals(new byte[]{0x0A, 0x0B}, result);
    }

    @Test
    @DisplayName("getByteArray(offset, length) returns copy, not reference")
    void getByteArray_returnsCopy() {
        byte[] data = new byte[]{0x01, 0x02, 0x03};
        PayloadBuffer buffer = new PayloadBuffer(data);

        byte[] result = buffer.getByteArray(0, 3);
        result[0] = 0x7F;  // modify the copy
        assertEquals(0x01, buffer.getByte(0));  // original unchanged
    }

    @Test
    @DisplayName("getByteArray with zero length returns empty array")
    void getByteArray_zeroLength() {
        PayloadBuffer buffer = new PayloadBuffer(new byte[]{0x01, 0x02});
        byte[] result = buffer.getByteArray(0, 0);
        assertEquals(0, result.length);
    }

    // ---------------------------------------------------------------
    // getByteArray(int index, byte[] dest, int offset, int length)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getByteArray with destination copies correctly")
    void getByteArray_withDest() {
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05};
        PayloadBuffer buffer = new PayloadBuffer(data);

        byte[] dest = new byte[5];
        byte[] returned = buffer.getByteArray(1, dest, 0, 3);

        assertSame(dest, returned);
        assertEquals(0x02, dest[0]);
        assertEquals(0x03, dest[1]);
        assertEquals(0x04, dest[2]);
    }

    @Test
    @DisplayName("getByteArray with destination offset copies to correct position")
    void getByteArray_withDestOffset() {
        byte[] data = new byte[]{0x0A, 0x0B, 0x0C, 0x0D};
        PayloadBuffer buffer = new PayloadBuffer(data);

        byte[] dest = new byte[6];
        buffer.getByteArray(0, dest, 2, 3);

        assertEquals(0, dest[0]);
        assertEquals(0, dest[1]);
        assertEquals(0x0A, dest[2]);
        assertEquals(0x0B, dest[3]);
        assertEquals(0x0C, dest[4]);
        assertEquals(0, dest[5]);
    }

    // ---------------------------------------------------------------
    // getRawData()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getRawData returns the backing array")
    void getRawData_returnsBackingArray() {
        byte[] data = new byte[]{0x01, 0x02, 0x03};
        PayloadBuffer buffer = new PayloadBuffer(data);

        assertSame(data, buffer.getRawData());
    }

    @Test
    @DisplayName("getRawData for int-constructed buffer returns zero-filled array")
    void getRawData_intConstructor() {
        PayloadBuffer buffer = new PayloadBuffer(3);
        byte[] raw = buffer.getRawData();

        assertEquals(3, raw.length);
        assertEquals(0, raw[0]);
        assertEquals(0, raw[1]);
        assertEquals(0, raw[2]);
    }

    // ---------------------------------------------------------------
    // Boundary conditions
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Single-byte buffer works correctly")
    void singleByte() {
        PayloadBuffer buffer = new PayloadBuffer(new byte[]{0x42});
        assertEquals(1, buffer.size());
        assertEquals(0x42, buffer.getByte(0));
        assertArrayEquals(new byte[]{0x42}, buffer.getByteArray(0, 1));
    }

    @Test
    @DisplayName("Large buffer preserves all data")
    void largeBuffer() {
        byte[] data = new byte[4096];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        PayloadBuffer buffer = new PayloadBuffer(data);

        assertEquals(4096, buffer.size());
        assertEquals((byte) 0, buffer.getByte(0));
        assertEquals((byte) 255, buffer.getByte(255));
        assertEquals((byte) 0, buffer.getByte(256));
    }
}
