package util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cidr - IP address and CIDR range utilities")
class CidrTest {

    // ---------------------------------------------------------------
    // Static helper: toIp(long) -> String
    // ---------------------------------------------------------------

    @Test
    @DisplayName("toIp(long) converts 0 to 0.0.0.0")
    void toIpLong_zero() {
        assertEquals("0.0.0.0", Cidr.toIp(0L));
    }

    @Test
    @DisplayName("toIp(long) converts max IP to 255.255.255.255")
    void toIpLong_maxIp() {
        assertEquals("255.255.255.255", Cidr.toIp(0xFFFFFFFFL));
    }

    @Test
    @DisplayName("toIp(long) converts 10.0.0.1 correctly")
    void toIpLong_tenDotZeroDotZeroDotOne() {
        // 10.0.0.1 = (10 << 24) + 1 = 167772161
        assertEquals("10.0.0.1", Cidr.toIp(167772161L));
    }

    @Test
    @DisplayName("toIp(long) converts 192.168.1.100 correctly")
    void toIpLong_privateAddress() {
        long ip = (192L << 24) | (168L << 16) | (1L << 8) | 100L;
        assertEquals("192.168.1.100", Cidr.toIp(ip));
    }

    // ---------------------------------------------------------------
    // Static helper: toIp(String) -> long
    // ---------------------------------------------------------------

    @Test
    @DisplayName("toIp(String) converts '0.0.0.0' to 0")
    void toIpString_zero() {
        assertEquals(0L, Cidr.toIp("0.0.0.0"));
    }

    @Test
    @DisplayName("toIp(String) converts '255.255.255.255' to maxIP")
    void toIpString_max() {
        assertEquals(0xFFFFFFFFL, Cidr.toIp("255.255.255.255"));
    }

    @Test
    @DisplayName("toIp(String) converts '10.0.0.1' correctly")
    void toIpString_tenDotOne() {
        assertEquals(167772161L, Cidr.toIp("10.0.0.1"));
    }

    @Test
    @DisplayName("toIp(String) round-trips with toIp(long)")
    void toIpString_roundTrip() {
        String original = "172.16.254.3";
        assertEquals(original, Cidr.toIp(Cidr.toIp(original)));
    }

    @Test
    @DisplayName("toIp(String) throws on invalid IP with too few octets")
    void toIpString_tooFewOctets() {
        assertThrows(IllegalArgumentException.class, () -> Cidr.toIp("10.0.1"));
    }

    @Test
    @DisplayName("toIp(String) throws on invalid IP with too many octets")
    void toIpString_tooManyOctets() {
        assertThrows(IllegalArgumentException.class, () -> Cidr.toIp("10.0.0.1.2"));
    }

    @Test
    @DisplayName("toIp(String) throws on octet > 255")
    void toIpString_octetOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> Cidr.toIp("256.0.0.1"));
    }

    @Test
    @DisplayName("toIp(String) throws on negative octet")
    void toIpString_negativeOctet() {
        assertThrows(IllegalArgumentException.class, () -> Cidr.toIp("-1.0.0.1"));
    }

    // ---------------------------------------------------------------
    // Constructor: Cidr(String) with plain IP
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Cidr(String) with plain IP '1.2.3.4' creates /32 host")
    void constructorString_plainIp() {
        Cidr cidr = new Cidr("1.2.3.4");
        assertEquals("1.2.3.4", cidr.toString());
        assertEquals(Cidr.toIp("1.2.3.4"), cidr.getFirstIp());
        assertEquals(Cidr.toIp("1.2.3.4"), cidr.getLastIp());
    }

    @Test
    @DisplayName("Cidr(String) with CIDR '10.0.0.0/8' creates correct range")
    void constructorString_cidr() {
        Cidr cidr = new Cidr("10.0.0.0/8");
        assertEquals("10.0.0.0/8", cidr.toString());
        assertEquals(Cidr.toIp("10.0.0.0"), cidr.getFirstIp());
        assertEquals(Cidr.toIp("10.255.255.255"), cidr.getLastIp());
    }

    @Test
    @DisplayName("Cidr(String) with invalid format throws")
    void constructorString_invalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> new Cidr("10.0.0.0/8/extra"));
    }

    // ---------------------------------------------------------------
    // Constructor: Cidr(long) and Cidr(long, short)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Cidr(long) creates a /32 host entry")
    void constructorLong_host() {
        long ip = Cidr.toIp("192.168.1.1");
        Cidr cidr = new Cidr(ip);
        assertEquals("192.168.1.1", cidr.toString());
        assertEquals(ip, cidr.getFirstIp());
        assertEquals(ip, cidr.getLastIp());
    }

    @Test
    @DisplayName("Cidr(long, short) creates correct CIDR")
    void constructorLongShort_cidr() {
        long ip = Cidr.toIp("172.16.0.0");
        Cidr cidr = new Cidr(ip, (short) 12);
        assertEquals("172.16.0.0/12", cidr.toString());
        assertEquals(Cidr.toIp("172.16.0.0"), cidr.getFirstIp());
        assertEquals(Cidr.toIp("172.31.255.255"), cidr.getLastIp());
    }

    @Test
    @DisplayName("Cidr(long) throws on negative IP")
    void constructorLong_negativeIp() {
        assertThrows(IllegalArgumentException.class, () -> new Cidr(-1L));
    }

    @Test
    @DisplayName("Cidr(long) throws on IP > maxIP")
    void constructorLong_overMaxIp() {
        assertThrows(IllegalArgumentException.class, () -> new Cidr(0x100000000L));
    }

    @Test
    @DisplayName("Cidr(long, short) throws on bits > 32")
    void constructorLongShort_bitsOver32() {
        assertThrows(IllegalArgumentException.class, () -> new Cidr(0L, (short) 33));
    }

    @Test
    @DisplayName("Cidr(long, short) throws on negative bits")
    void constructorLongShort_negativeBits() {
        assertThrows(IllegalArgumentException.class, () -> new Cidr(0L, (short) -1));
    }

    // ---------------------------------------------------------------
    // Constructor: Cidr(byte[])
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Cidr(byte[]) creates correct IP from byte array")
    void constructorByteArray() {
        byte[] bytes = new byte[]{10, 0, 0, 1};
        Cidr cidr = new Cidr(bytes);
        assertEquals("10.0.0.1", cidr.toString());
    }

    @Test
    @DisplayName("Cidr(byte[]) handles high byte values (sign extension)")
    void constructorByteArray_highBytes() {
        // 192.168.1.1 -> bytes are -64, -88, 1, 1 in signed form
        byte[] bytes = new byte[]{(byte) 192, (byte) 168, 1, 1};
        Cidr cidr = new Cidr(bytes);
        assertEquals("192.168.1.1", cidr.toString());
    }

    @Test
    @DisplayName("Cidr(byte[]) for 255.255.255.255")
    void constructorByteArray_allOnes() {
        byte[] bytes = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        Cidr cidr = new Cidr(bytes);
        assertEquals("255.255.255.255", cidr.toString());
    }

    // ---------------------------------------------------------------
    // contains(Cidr)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("CIDR contains an IP within its range")
    void contains_ipInRange() {
        Cidr network = new Cidr("10.0.0.0/8");
        Cidr host = new Cidr("10.1.2.3");
        assertTrue(network.contains(host));
    }

    @Test
    @DisplayName("CIDR does not contain an IP outside its range")
    void contains_ipOutOfRange() {
        Cidr network = new Cidr("10.0.0.0/8");
        Cidr host = new Cidr("11.0.0.1");
        assertFalse(network.contains(host));
    }

    @Test
    @DisplayName("CIDR contains a smaller CIDR")
    void contains_smallerCidr() {
        Cidr outer = new Cidr("10.0.0.0/8");
        Cidr inner = new Cidr("10.1.0.0/16");
        assertTrue(outer.contains(inner));
    }

    @Test
    @DisplayName("Smaller CIDR does not contain larger CIDR")
    void contains_smallerDoesNotContainLarger() {
        Cidr outer = new Cidr("10.0.0.0/8");
        Cidr inner = new Cidr("10.1.0.0/16");
        assertFalse(inner.contains(outer));
    }

    @Test
    @DisplayName("CIDR contains itself")
    void contains_self() {
        Cidr cidr = new Cidr("192.168.0.0/16");
        assertTrue(cidr.contains(cidr));
    }

    @Test
    @DisplayName("/32 host contains itself")
    void contains_hostSelf() {
        Cidr host = new Cidr("1.2.3.4");
        assertTrue(host.contains(host));
    }

    // ---------------------------------------------------------------
    // overlaps(Cidr)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Overlapping CIDRs detected")
    void overlaps_true() {
        Cidr a = new Cidr("10.0.0.0/8");
        Cidr b = new Cidr("10.1.0.0/16");
        assertTrue(a.overlaps(b));
        assertTrue(b.overlaps(a));
    }

    @Test
    @DisplayName("Non-overlapping CIDRs not detected")
    void overlaps_false() {
        Cidr a = new Cidr("10.0.0.0/8");
        Cidr b = new Cidr("172.16.0.0/12");
        assertFalse(a.overlaps(b));
        assertFalse(b.overlaps(a));
    }

    @Test
    @DisplayName("Adjacent CIDRs do not overlap")
    void overlaps_adjacent() {
        // 10.0.0.0/24 ends at 10.0.0.255, 10.0.1.0/24 starts at 10.0.1.0
        Cidr a = new Cidr("10.0.0.0/24");
        Cidr b = new Cidr("10.0.1.0/24");
        assertFalse(a.overlaps(b));
    }

    // ---------------------------------------------------------------
    // getFirstIp() and getLastIp()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getFirstIp and getLastIp for /24")
    void firstAndLastIp_slash24() {
        Cidr cidr = new Cidr("192.168.1.0/24");
        assertEquals(Cidr.toIp("192.168.1.0"), cidr.getFirstIp());
        assertEquals(Cidr.toIp("192.168.1.255"), cidr.getLastIp());
    }

    @Test
    @DisplayName("getFirstIp and getLastIp for /16")
    void firstAndLastIp_slash16() {
        Cidr cidr = new Cidr("172.16.0.0/16");
        assertEquals(Cidr.toIp("172.16.0.0"), cidr.getFirstIp());
        assertEquals(Cidr.toIp("172.16.255.255"), cidr.getLastIp());
    }

    @Test
    @DisplayName("getFirstIp and getLastIp for /32 host")
    void firstAndLastIp_host() {
        Cidr cidr = new Cidr("1.2.3.4");
        assertEquals(cidr.getFirstIp(), cidr.getLastIp());
    }

    @Test
    @DisplayName("getFirstIp and getLastIp for /0 (entire address space)")
    void firstAndLastIp_slashZero() {
        Cidr cidr = new Cidr(0L, (short) 0);
        assertEquals(0L, cidr.getFirstIp());
        assertEquals(0xFFFFFFFFL, cidr.getLastIp());
    }

    @Test
    @DisplayName("getFirstIpString and getLastIpString return formatted strings")
    void firstAndLastIpString() {
        Cidr cidr = new Cidr("10.0.0.0/8");
        assertEquals("10.0.0.0", cidr.getFirstIpString());
        assertEquals("10.255.255.255", cidr.getLastIpString());
    }

    // ---------------------------------------------------------------
    // toString()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("toString for /32 host returns IP without prefix")
    void toString_host() {
        Cidr cidr = new Cidr("1.2.3.4");
        assertEquals("1.2.3.4", cidr.toString());
    }

    @Test
    @DisplayName("toString for CIDR returns IP/bits format")
    void toString_cidr() {
        Cidr cidr = new Cidr("10.0.0.0/8");
        assertEquals("10.0.0.0/8", cidr.toString());
    }

    @Test
    @DisplayName("toString for /0 throws IllegalArgumentException (imask requires [1:32])")
    void toString_slashZero() {
        Cidr cidr = new Cidr(0L, (short) 0);
        assertThrows(IllegalArgumentException.class, cidr::toString);
    }

    // ---------------------------------------------------------------
    // equals() and hashCode()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("equals returns true for identical CIDRs")
    void equals_identical() {
        Cidr a = new Cidr("10.0.0.0/8");
        Cidr b = new Cidr("10.0.0.0/8");
        assertEquals(a, b);
    }

    @Test
    @DisplayName("equals returns false for different IPs same bits")
    void equals_differentIp() {
        Cidr a = new Cidr("10.0.0.0/8");
        Cidr b = new Cidr("172.16.0.0/8");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals returns false for same IP different bits")
    void equals_differentBits() {
        Cidr a = new Cidr("10.0.0.0/8");
        Cidr b = new Cidr("10.0.0.0/16");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals returns false for null")
    void equals_null() {
        Cidr cidr = new Cidr("10.0.0.0/8");
        assertNotEquals(null, cidr);
    }

    @Test
    @DisplayName("equals returns false for non-Cidr object")
    void equals_differentType() {
        Cidr cidr = new Cidr("10.0.0.0/8");
        assertNotEquals("10.0.0.0/8", cidr);
    }

    @Test
    @DisplayName("hashCode is consistent for equal CIDRs")
    void hashCode_consistent() {
        Cidr a = new Cidr("10.0.0.0/8");
        Cidr b = new Cidr("10.0.0.0/8");
        assertEquals(a.hashCode(), b.hashCode());
    }

    // ---------------------------------------------------------------
    // compareTo()
    // ---------------------------------------------------------------

    @Test
    @DisplayName("compareTo orders by first IP ascending")
    void compareTo_byFirstIp() {
        Cidr a = new Cidr("10.0.0.0/8");
        Cidr b = new Cidr("172.16.0.0/12");
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }

    @Test
    @DisplayName("compareTo orders larger network before smaller when same first IP")
    void compareTo_sameFirstIpDifferentBits() {
        // Same first IP, /8 has fewer bits (larger range) => bigger cidr.bits means smaller range
        // compareTo returns compare(cidr.bits - this.bits)
        // /8 vs /16: compare(16 - 8) = 1 means /8 > /16 (larger network sorts after smaller)
        Cidr larger = new Cidr("10.0.0.0/8");
        Cidr smaller = new Cidr("10.0.0.0/16");
        assertTrue(larger.compareTo(smaller) > 0);
        assertTrue(smaller.compareTo(larger) < 0);
    }

    @Test
    @DisplayName("compareTo returns 0 for equal CIDRs")
    void compareTo_equal() {
        Cidr a = new Cidr("10.0.0.0/8");
        Cidr b = new Cidr("10.0.0.0/8");
        assertEquals(0, a.compareTo(b));
    }

    @Test
    @DisplayName("compareTo returns positive when compared to null")
    void compareTo_null() {
        Cidr cidr = new Cidr("10.0.0.0/8");
        assertTrue(cidr.compareTo(null) > 0);
    }

    // ---------------------------------------------------------------
    // Edge cases: 0.0.0.0, 255.255.255.255
    // ---------------------------------------------------------------

    @Test
    @DisplayName("0.0.0.0 is valid")
    void edgeCase_allZeros() {
        Cidr cidr = new Cidr("0.0.0.0");
        assertEquals(0L, cidr.getFirstIp());
        assertEquals("0.0.0.0", cidr.toString());
    }

    @Test
    @DisplayName("255.255.255.255 is valid")
    void edgeCase_allOnes() {
        Cidr cidr = new Cidr("255.255.255.255");
        assertEquals(0xFFFFFFFFL, cidr.getFirstIp());
        assertEquals("255.255.255.255", cidr.toString());
    }

    // ---------------------------------------------------------------
    // toCidrs(long, long)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("toCidrs for a single IP returns one /32")
    void toCidrs_singleIp() {
        long ip = Cidr.toIp("10.0.0.1");
        List<Cidr> cidrs = Cidr.toCidrs(ip, ip);
        assertEquals(1, cidrs.size());
        assertEquals("10.0.0.1", cidrs.get(0).toString());
    }

    @Test
    @DisplayName("toCidrs for 10.0.0.0 - 10.0.0.255 returns 10.0.0.0/24")
    void toCidrs_fullSlash24() {
        long first = Cidr.toIp("10.0.0.0");
        long last = Cidr.toIp("10.0.0.255");
        List<Cidr> cidrs = Cidr.toCidrs(first, last);
        assertEquals(1, cidrs.size());
        assertEquals("10.0.0.0/24", cidrs.get(0).toString());
    }

    @Test
    @DisplayName("toCidrs for non-power-of-2 range returns multiple CIDRs")
    void toCidrs_nonPowerOfTwo() {
        // 10.0.0.0 - 10.0.0.2 = 3 addresses -> should produce /31 + /32
        long first = Cidr.toIp("10.0.0.0");
        long last = Cidr.toIp("10.0.0.2");
        List<Cidr> cidrs = Cidr.toCidrs(first, last);
        assertTrue(cidrs.size() >= 2, "Non-power-of-2 range should split into multiple CIDRs");

        // Verify all CIDRs collectively cover the range
        long coveredFirst = cidrs.get(0).getFirstIp();
        long coveredLast = cidrs.get(cidrs.size() - 1).getLastIp();
        assertEquals(first, coveredFirst);
        assertEquals(last, coveredLast);
    }

    @Test
    @DisplayName("toCidrs with String arguments")
    void toCidrs_stringArgs() {
        List<Cidr> cidrs = Cidr.toCidrs("10.0.0.0", "10.0.0.255");
        assertEquals(1, cidrs.size());
        assertEquals("10.0.0.0/24", cidrs.get(0).toString());
    }

    // ---------------------------------------------------------------
    // contains(long) and contains(String)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("contains(long) works for IP within range")
    void containsLong_inRange() {
        Cidr network = new Cidr("10.0.0.0/8");
        assertTrue(network.contains(Cidr.toIp("10.1.2.3")));
    }

    @Test
    @DisplayName("contains(String) works for IP within range")
    void containsString_inRange() {
        Cidr network = new Cidr("10.0.0.0/8");
        assertTrue(network.contains("10.1.2.3"));
    }

    @Test
    @DisplayName("contains(String) works for CIDR within range")
    void containsString_cidrInRange() {
        Cidr network = new Cidr("10.0.0.0/8");
        assertTrue(network.contains("10.1.0.0/16"));
    }

    // ---------------------------------------------------------------
    // maxIP and maxBits constants
    // ---------------------------------------------------------------

    @Test
    @DisplayName("maxIP is 0xFFFFFFFF")
    void maxIpConstant() {
        assertEquals(0xFFFFFFFFL, Cidr.maxIP);
    }

    @Test
    @DisplayName("maxBits is 32")
    void maxBitsConstant() {
        assertEquals(32, Cidr.maxBits);
    }
}
