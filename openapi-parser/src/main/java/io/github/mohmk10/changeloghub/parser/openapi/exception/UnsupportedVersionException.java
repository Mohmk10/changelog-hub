package io.github.mohmk10.changeloghub.parser.openapi.exception;

import io.github.mohmk10.changeloghub.parser.openapi.util.OpenApiVersion;

public class UnsupportedVersionException extends OpenApiParseException {

    private final OpenApiVersion detectedVersion;

    public UnsupportedVersionException(OpenApiVersion detectedVersion) {
        super("Unsupported OpenAPI version: " + detectedVersion);
        this.detectedVersion = detectedVersion;
    }

    public UnsupportedVersionException(String versionString) {
        super("Unsupported OpenAPI version: " + versionString);
        this.detectedVersion = OpenApiVersion.UNKNOWN;
    }

    public OpenApiVersion getDetectedVersion() {
        return detectedVersion;
    }
}
