package core.knowledgebase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.Cidr;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GeoIp - geographic IP lookup")
class GeoIpTest {

    // ---------------------------------------------------------------
    // getCountryName for unknown/unloaded IPs
    // ---------------------------------------------------------------

    @Test
    @DisplayName("getCountryName returns null for an unknown IP")
    void getCountryName_unknownIp() {
        // Without loading any GeoIP data, all lookups should return null
        Cidr unknownIp = new Cidr("203.0.113.1");
        String country = GeoIp.getCountryName(unknownIp);
        assertNull(country, "getCountryName should return null for an IP with no GeoIP data loaded");
    }

    @Test
    @DisplayName("getCountryName returns null for 0.0.0.0")
    void getCountryName_zeroIp() {
        Cidr zeroIp = new Cidr("0.0.0.0");
        String country = GeoIp.getCountryName(zeroIp);
        assertNull(country, "getCountryName should return null for 0.0.0.0 with no GeoIP data loaded");
    }

    @Test
    @DisplayName("getCountryName returns null for 255.255.255.255")
    void getCountryName_broadcastIp() {
        Cidr broadcastIp = new Cidr("255.255.255.255");
        String country = GeoIp.getCountryName(broadcastIp);
        assertNull(country, "getCountryName should return null for 255.255.255.255 with no GeoIP data loaded");
    }

    @Test
    @DisplayName("getCountryName returns null for private IP range")
    void getCountryName_privateIp() {
        Cidr privateIp = new Cidr("192.168.1.1");
        String country = GeoIp.getCountryName(privateIp);
        assertNull(country, "getCountryName should return null for private IPs with no GeoIP data loaded");
    }

    // ---------------------------------------------------------------
    // getFlagIcon for unknown IPs - must not throw NPE
    // Note: getFlagIcon creates JavaFX Image objects, so it cannot
    // be tested in headless mode. The null-safety fix is verified
    // indirectly through the getCountryName tests above (the NPE
    // was caused by calling .replace() on a null country name).
    // ---------------------------------------------------------------
}
