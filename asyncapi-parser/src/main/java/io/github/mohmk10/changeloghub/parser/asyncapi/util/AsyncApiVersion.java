package io.github.mohmk10.changeloghub.parser.asyncapi.util;

/**
 * Enum representing AsyncAPI specification versions.
 */
public enum AsyncApiVersion {
    V2_0("2.0.0", 2, 0),
    V2_1("2.1.0", 2, 1),
    V2_2("2.2.0", 2, 2),
    V2_3("2.3.0", 2, 3),
    V2_4("2.4.0", 2, 4),
    V2_5("2.5.0", 2, 5),
    V2_6("2.6.0", 2, 6),
    V3_0("3.0.0", 3, 0),
    UNKNOWN("unknown", 0, 0);

    private final String version;
    private final int major;
    private final int minor;

    AsyncApiVersion(String version, int major, int minor) {
        this.version = version;
        this.major = major;
        this.minor = minor;
    }

    public String getVersion() {
        return version;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public boolean isV2() {
        return major == 2;
    }

    public boolean isV3() {
        return major == 3;
    }

    /**
     * Parse a version string to AsyncApiVersion.
     */
    public static AsyncApiVersion fromString(String versionStr) {
        if (versionStr == null || versionStr.isBlank()) {
            return UNKNOWN;
        }

        String normalized = versionStr.trim();

        // Exact match first
        for (AsyncApiVersion v : values()) {
            if (v.version.equals(normalized)) {
                return v;
            }
        }

        // Try matching major.minor pattern
        if (normalized.startsWith("2.0")) return V2_0;
        if (normalized.startsWith("2.1")) return V2_1;
        if (normalized.startsWith("2.2")) return V2_2;
        if (normalized.startsWith("2.3")) return V2_3;
        if (normalized.startsWith("2.4")) return V2_4;
        if (normalized.startsWith("2.5")) return V2_5;
        if (normalized.startsWith("2.6")) return V2_6;
        if (normalized.startsWith("3.0")) return V3_0;

        // Fallback to major version
        if (normalized.startsWith("2.")) return V2_6; // Default to latest 2.x
        if (normalized.startsWith("3.")) return V3_0;

        return UNKNOWN;
    }

    /**
     * Check if this version is supported.
     */
    public boolean isSupported() {
        return this != UNKNOWN && (isV2() || isV3());
    }

    @Override
    public String toString() {
        return version;
    }
}
