package io.github.mohmk10.changeloghub.parser.asyncapi.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class AsyncApiVersionTest {

    @Test
    @DisplayName("Should parse version 2.0.0")
    void testParseV2_0() {
        AsyncApiVersion version = AsyncApiVersion.fromString("2.0.0");
        assertEquals(AsyncApiVersion.V2_0, version);
        assertTrue(version.isV2());
        assertFalse(version.isV3());
        assertTrue(version.isSupported());
    }

    @Test
    @DisplayName("Should parse version 2.6.0")
    void testParseV2_6() {
        AsyncApiVersion version = AsyncApiVersion.fromString("2.6.0");
        assertEquals(AsyncApiVersion.V2_6, version);
        assertTrue(version.isV2());
        assertFalse(version.isV3());
        assertTrue(version.isSupported());
    }

    @Test
    @DisplayName("Should parse version 3.0.0")
    void testParseV3_0() {
        AsyncApiVersion version = AsyncApiVersion.fromString("3.0.0");
        assertEquals(AsyncApiVersion.V3_0, version);
        assertFalse(version.isV2());
        assertTrue(version.isV3());
        assertTrue(version.isSupported());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2.0.0", "2.1.0", "2.2.0", "2.3.0", "2.4.0", "2.5.0", "2.6.0", "3.0.0"})
    @DisplayName("Should support all valid versions")
    void testSupportedVersions(String versionStr) {
        AsyncApiVersion version = AsyncApiVersion.fromString(versionStr);
        assertNotNull(version);
        assertTrue(version.isSupported());
    }

    @Test
    @DisplayName("Should return UNKNOWN for unsupported version")
    void testUnsupportedVersion() {
        AsyncApiVersion version = AsyncApiVersion.fromString("1.0.0");
        assertEquals(AsyncApiVersion.UNKNOWN, version);
        assertFalse(version.isSupported());
    }

    @Test
    @DisplayName("Should return UNKNOWN for null input")
    void testNullInput() {
        AsyncApiVersion version = AsyncApiVersion.fromString(null);
        assertEquals(AsyncApiVersion.UNKNOWN, version);
        assertFalse(version.isSupported());
    }

    @Test
    @DisplayName("Should return UNKNOWN for empty input")
    void testEmptyInput() {
        AsyncApiVersion version = AsyncApiVersion.fromString("");
        assertEquals(AsyncApiVersion.UNKNOWN, version);
        assertFalse(version.isSupported());
    }

    @Test
    @DisplayName("Should return correct version string")
    void testGetVersion() {
        assertEquals("2.6.0", AsyncApiVersion.V2_6.getVersion());
        assertEquals("3.0.0", AsyncApiVersion.V3_0.getVersion());
    }

    @Test
    @DisplayName("Should check major version correctly")
    void testMajorVersionCheck() {
        assertTrue(AsyncApiVersion.V2_0.isV2());
        assertTrue(AsyncApiVersion.V2_3.isV2());
        assertTrue(AsyncApiVersion.V2_6.isV2());
        assertFalse(AsyncApiVersion.V3_0.isV2());

        assertFalse(AsyncApiVersion.V2_0.isV3());
        assertFalse(AsyncApiVersion.V2_6.isV3());
        assertTrue(AsyncApiVersion.V3_0.isV3());
    }
}
