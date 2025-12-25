package io.github.mohmk10.changeloghub.parser.openapi.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenApiVersionDetector {

    private static final Pattern OPENAPI_VERSION_PATTERN = Pattern.compile("\"?openapi\"?\\s*:\\s*[\"']?(3\\.\\d+\\.?\\d*)[\"']?");
    private static final Pattern SWAGGER_VERSION_PATTERN = Pattern.compile("\"?swagger\"?\\s*:\\s*[\"']?(2\\.\\d+)[\"']?");

    private OpenApiVersionDetector() {
    }

    public static OpenApiVersion detect(String content) {
        if (content == null || content.isBlank()) {
            return OpenApiVersion.UNKNOWN;
        }

        Matcher openApiMatcher = OPENAPI_VERSION_PATTERN.matcher(content);
        if (openApiMatcher.find()) {
            String version = openApiMatcher.group(1);
            if (version.startsWith("3.1")) {
                return OpenApiVersion.OPENAPI_3_1;
            } else if (version.startsWith("3.0")) {
                return OpenApiVersion.OPENAPI_3_0;
            }
        }

        Matcher swaggerMatcher = SWAGGER_VERSION_PATTERN.matcher(content);
        if (swaggerMatcher.find()) {
            String version = swaggerMatcher.group(1);
            if (version.startsWith("2.")) {
                return OpenApiVersion.SWAGGER_2_0;
            }
        }

        return OpenApiVersion.UNKNOWN;
    }

    public static boolean isSupported(OpenApiVersion version) {
        return version == OpenApiVersion.SWAGGER_2_0 ||
               version == OpenApiVersion.OPENAPI_3_0 ||
               version == OpenApiVersion.OPENAPI_3_1;
    }
}
