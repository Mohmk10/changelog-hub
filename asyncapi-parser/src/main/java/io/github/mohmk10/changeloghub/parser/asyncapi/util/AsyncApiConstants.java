package io.github.mohmk10.changeloghub.parser.asyncapi.util;

import java.util.Set;
import java.util.regex.Pattern;

public final class AsyncApiConstants {

    private AsyncApiConstants() {
        
    }

    public static final String ASYNCAPI = "asyncapi";
    public static final String INFO = "info";
    public static final String SERVERS = "servers";
    public static final String CHANNELS = "channels";
    public static final String OPERATIONS = "operations";
    public static final String COMPONENTS = "components";
    public static final String TAGS = "tags";
    public static final String EXTERNAL_DOCS = "externalDocs";
    public static final String DEFAULT_CONTENT_TYPE = "defaultContentType";

    public static final String TITLE = "title";
    public static final String VERSION = "version";
    public static final String DESCRIPTION = "description";
    public static final String TERMS_OF_SERVICE = "termsOfService";
    public static final String CONTACT = "contact";
    public static final String LICENSE = "license";

    public static final String URL = "url";
    public static final String PROTOCOL = "protocol";
    public static final String PROTOCOL_VERSION = "protocolVersion";
    public static final String VARIABLES = "variables";
    public static final String SECURITY = "security";
    public static final String BINDINGS = "bindings";

    public static final String ADDRESS = "address";
    public static final String MESSAGES = "messages";
    public static final String PARAMETERS = "parameters";
    public static final String PUBLISH = "publish";
    public static final String SUBSCRIBE = "subscribe";

    public static final String OPERATION_ID = "operationId";
    public static final String SUMMARY = "summary";
    public static final String MESSAGE = "message";
    public static final String TRAITS = "traits";

    public static final String ACTION = "action";
    public static final String CHANNEL = "channel";
    public static final String REPLY = "reply";

    public static final String NAME = "name";
    public static final String CONTENT_TYPE = "contentType";
    public static final String PAYLOAD = "payload";
    public static final String HEADERS = "headers";
    public static final String CORRELATION_ID = "correlationId";
    public static final String SCHEMA_FORMAT = "schemaFormat";
    public static final String MESSAGE_ID = "messageId";

    public static final String TYPE = "type";
    public static final String PROPERTIES = "properties";
    public static final String REQUIRED = "required";
    public static final String ITEMS = "items";
    public static final String FORMAT = "format";
    public static final String ENUM = "enum";
    public static final String DEFAULT = "default";
    public static final String MINIMUM = "minimum";
    public static final String MAXIMUM = "maximum";
    public static final String MIN_LENGTH = "minLength";
    public static final String MAX_LENGTH = "maxLength";
    public static final String PATTERN = "pattern";
    public static final String ADDITIONAL_PROPERTIES = "additionalProperties";
    public static final String ALL_OF = "allOf";
    public static final String ONE_OF = "oneOf";
    public static final String ANY_OF = "anyOf";

    public static final String REF = "$ref";

    public static final String DEPRECATED = "deprecated";
    public static final String EXAMPLES = "examples";

    public static final String SCHEMAS = "schemas";
    public static final String SECURITY_SCHEMES = "securitySchemes";
    public static final String SERVER_VARIABLES = "serverVariables";
    public static final String CHANNEL_PARAMETERS = "parameters";
    public static final String OPERATION_TRAITS = "operationTraits";
    public static final String MESSAGE_TRAITS = "messageTraits";

    public static final String CORRELATION_IDS = "correlationIds";
    public static final String SERVER_BINDINGS = "serverBindings";
    public static final String CHANNEL_BINDINGS = "channelBindings";
    public static final String OPERATION_BINDINGS = "operationBindings";
    public static final String MESSAGE_BINDINGS = "messageBindings";

    public static final String EMAIL = "email";

    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_AVRO = "application/vnd.apache.avro+json";
    public static final String APPLICATION_PROTOBUF = "application/vnd.google.protobuf";

    public static final Set<String> PRIMITIVE_TYPES = Set.of(
            "string", "number", "integer", "boolean", "null"
    );

    public static final Set<String> COMPLEX_TYPES = Set.of(
            "object", "array"
    );

    public static final Pattern CHANNEL_PARAMETER_PATTERN = Pattern.compile("\\{([^}]+)\\}");

    public static final Pattern REF_PATTERN = Pattern.compile("^#/components/(\\w+)/(.+)$");

    public static boolean isPrimitiveType(String type) {
        return type != null && PRIMITIVE_TYPES.contains(type.toLowerCase());
    }

    public static boolean isComplexType(String type) {
        return type != null && COMPLEX_TYPES.contains(type.toLowerCase());
    }

    public static boolean isReference(String value) {
        return value != null && value.startsWith("#/");
    }

    public static String extractRefName(String ref) {
        if (ref == null) {
            return null;
        }
        int lastSlash = ref.lastIndexOf('/');
        return lastSlash >= 0 ? ref.substring(lastSlash + 1) : ref;
    }

    public static String extractRefType(String ref) {
        if (ref == null || !ref.startsWith("#/components/")) {
            return null;
        }
        String[] parts = ref.substring("#/components/".length()).split("/");
        return parts.length > 0 ? parts[0] : null;
    }
}
