package io.github.mohmk10.changeloghub.parser.grpc.util;

import java.util.Set;
import java.util.regex.Pattern;

public final class ProtoConstants {

    private ProtoConstants() {
        
    }

    public static final String SYNTAX_PROTO2 = "proto2";
    public static final String SYNTAX_PROTO3 = "proto3";

    public static final String KEYWORD_SYNTAX = "syntax";
    public static final String KEYWORD_PACKAGE = "package";
    public static final String KEYWORD_IMPORT = "import";
    public static final String KEYWORD_OPTION = "option";
    public static final String KEYWORD_MESSAGE = "message";
    public static final String KEYWORD_ENUM = "enum";
    public static final String KEYWORD_SERVICE = "service";
    public static final String KEYWORD_RPC = "rpc";
    public static final String KEYWORD_RETURNS = "returns";
    public static final String KEYWORD_STREAM = "stream";
    public static final String KEYWORD_ONEOF = "oneof";
    public static final String KEYWORD_MAP = "map";
    public static final String KEYWORD_RESERVED = "reserved";
    public static final String KEYWORD_EXTENSIONS = "extensions";
    public static final String KEYWORD_EXTEND = "extend";

    public static final String RULE_OPTIONAL = "optional";
    public static final String RULE_REQUIRED = "required";
    public static final String RULE_REPEATED = "repeated";

    public static final String OPTION_DEPRECATED = "deprecated";
    public static final String OPTION_JAVA_PACKAGE = "java_package";
    public static final String OPTION_JAVA_OUTER_CLASSNAME = "java_outer_classname";
    public static final String OPTION_JAVA_MULTIPLE_FILES = "java_multiple_files";
    public static final String OPTION_GO_PACKAGE = "go_package";
    public static final String OPTION_CSHARP_NAMESPACE = "csharp_namespace";

    public static final Set<String> SCALAR_TYPES = Set.of(
            "double", "float",
            "int32", "int64",
            "uint32", "uint64",
            "sint32", "sint64",
            "fixed32", "fixed64",
            "sfixed32", "sfixed64",
            "bool", "string", "bytes"
    );

    public static final Set<String> WELL_KNOWN_TYPES = Set.of(
            "google.protobuf.Any",
            "google.protobuf.Timestamp",
            "google.protobuf.Duration",
            "google.protobuf.Empty",
            "google.protobuf.Struct",
            "google.protobuf.Value",
            "google.protobuf.ListValue",
            "google.protobuf.FieldMask",
            "google.protobuf.BoolValue",
            "google.protobuf.BytesValue",
            "google.protobuf.DoubleValue",
            "google.protobuf.FloatValue",
            "google.protobuf.Int32Value",
            "google.protobuf.Int64Value",
            "google.protobuf.StringValue",
            "google.protobuf.UInt32Value",
            "google.protobuf.UInt64Value"
    );

    public static final Pattern SYNTAX_PATTERN = Pattern.compile(
            "syntax\\s*=\\s*[\"']([^\"']+)[\"']\\s*;");

    public static final Pattern PACKAGE_PATTERN = Pattern.compile(
            "package\\s+([\\w.]+)\\s*;");

    public static final Pattern IMPORT_PATTERN = Pattern.compile(
            "import\\s+(?:(public|weak)\\s+)?[\"']([^\"']+)[\"']\\s*;");

    public static final Pattern OPTION_PATTERN = Pattern.compile(
            "option\\s+([\\w.]+)\\s*=\\s*([^;]+)\\s*;");

    public static final Pattern SERVICE_PATTERN = Pattern.compile(
            "service\\s+(\\w+)\\s*\\{([^}]*)\\}", Pattern.DOTALL);

    public static final Pattern RPC_PATTERN = Pattern.compile(
            "rpc\\s+(\\w+)\\s*\\(\\s*(stream\\s+)?(\\w+)\\s*\\)\\s*returns\\s*\\(\\s*(stream\\s+)?(\\w+)\\s*\\)");

    public static final Pattern MESSAGE_PATTERN = Pattern.compile(
            "message\\s+(\\w+)\\s*\\{([^}]*)\\}", Pattern.DOTALL);

    public static final Pattern ENUM_PATTERN = Pattern.compile(
            "enum\\s+(\\w+)\\s*\\{([^}]*)\\}", Pattern.DOTALL);

    public static final Pattern FIELD_PATTERN = Pattern.compile(
            "^\\s*(optional|required|repeated)?\\s*([\\w.<>,\\s]+)\\s+(\\w+)\\s*=\\s*(\\d+)\\s*(?:\\[([^\\]]+)\\])?\\s*;",
            Pattern.MULTILINE);

    public static final Pattern ENUM_VALUE_PATTERN = Pattern.compile(
            "^\\s*(\\w+)\\s*=\\s*(-?\\d+)\\s*(?:\\[([^\\]]+)\\])?\\s*;",
            Pattern.MULTILINE);

    public static final Pattern MAP_TYPE_PATTERN = Pattern.compile(
            "map\\s*<\\s*(\\w+)\\s*,\\s*([\\w.]+)\\s*>");

    public static final Pattern ONEOF_PATTERN = Pattern.compile(
            "oneof\\s+(\\w+)\\s*\\{([^}]*)\\}", Pattern.DOTALL);

    public static final String PROTO_EXTENSION = ".proto";

    public static final String GRPC_PATH_FORMAT = "/%s.%s/%s";
}
