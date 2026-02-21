package core.protocol;

import java.util.Arrays;

/**
 * Pure-Java byte[]-backed buffer replacing JNetPcap's JBuffer.
 * Provides the same API surface used by PacketData: getByte(), getByteArray(), size().
 */
public class PayloadBuffer {
    private final byte[] data;

    public PayloadBuffer(byte[] data) {
        this.data = data;
    }

    public PayloadBuffer(int size) {
        this.data = new byte[size];
    }

    public byte getByte(int index) {
        return data[index];
    }

    public byte[] getByteArray(int offset, int length) {
        return Arrays.copyOfRange(data, offset, offset + length);
    }

    public byte[] getByteArray(int index, byte[] dest, int offset, int length) {
        System.arraycopy(data, index, dest, offset, length);
        return dest;
    }

    public int size() {
        return data.length;
    }

    public byte[] getRawData() {
        return data;
    }
}
