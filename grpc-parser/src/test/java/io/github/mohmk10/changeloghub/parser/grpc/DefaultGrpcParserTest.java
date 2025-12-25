package io.github.mohmk10.changeloghub.parser.grpc;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.parser.grpc.exception.GrpcParseException;
import io.github.mohmk10.changeloghub.parser.grpc.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DefaultGrpcParser Tests")
class DefaultGrpcParserTest {

    private DefaultGrpcParser parser;

    @BeforeEach
    void setUp() {
        parser = new DefaultGrpcParser();
    }

    @Nested
    @DisplayName("Basic Parsing Tests")
    class BasicParsingTests {

        @Test
        @DisplayName("Should parse minimal proto3 file")
        void shouldParseMinimalProto3File() {
            String content = """
                syntax = "proto3";
                package test;
                """;

            ProtoFile result = parser.parse(content);

            assertEquals("proto3", result.getSyntax());
            assertEquals("test", result.getPackageName());
            assertTrue(result.isProto3());
            assertFalse(result.isProto2());
        }

        @Test
        @DisplayName("Should parse minimal proto2 file")
        void shouldParseMinimalProto2File() {
            String content = """
                syntax = "proto2";
                package legacy;
                """;

            ProtoFile result = parser.parse(content);

            assertEquals("proto2", result.getSyntax());
            assertEquals("legacy", result.getPackageName());
            assertTrue(result.isProto2());
        }

        @Test
        @DisplayName("Should throw exception for empty content")
        void shouldThrowExceptionForEmptyContent() {
            assertThrows(GrpcParseException.class, () -> parser.parse(""));
            assertThrows(GrpcParseException.class, () -> parser.parse("   "));
            assertThrows(GrpcParseException.class, () -> parser.parse(null));
        }

        @Test
        @DisplayName("Should parse file with options")
        void shouldParseFileWithOptions() {
            String content = """
                syntax = "proto3";
                package test;
                option java_package = "com.example.test";
                option java_multiple_files = true;
                """;

            ProtoFile result = parser.parse(content);

            assertEquals("com.example.test", result.getOption("java_package"));
            assertEquals("true", result.getOption("java_multiple_files"));
        }

        @Test
        @DisplayName("Should parse imports")
        void shouldParseImports() {
            String content = """
                syntax = "proto3";
                package test;
                import "google/protobuf/timestamp.proto";
                import public "common/types.proto";
                import "other.proto";
                """;

            ProtoFile result = parser.parse(content);

            assertEquals(2, result.getImports().size());
            assertTrue(result.getImports().contains("google/protobuf/timestamp.proto"));
            assertTrue(result.getImports().contains("other.proto"));
            assertEquals(1, result.getPublicImports().size());
            assertTrue(result.getPublicImports().contains("common/types.proto"));
        }

        @Test
        @DisplayName("Should set filename when provided")
        void shouldSetFilenameWhenProvided() {
            String content = """
                syntax = "proto3";
                package test;
                """;

            ProtoFile result = parser.parse(content, "test_service.proto");

            assertEquals("test_service.proto", result.getFileName());
        }
    }

    @Nested
    @DisplayName("Message Parsing Tests")
    class MessageParsingTests {

        @Test
        @DisplayName("Should parse simple message")
        void shouldParseSimpleMessage() {
            String content = """
                syntax = "proto3";
                package test;

                message User {
                    string id = 1;
                    string name = 2;
                    int32 age = 3;
                }
                """;

            ProtoFile result = parser.parse(content);

            assertEquals(1, result.getMessages().size());
            ProtoMessage user = result.getMessage("User").orElseThrow();
            assertEquals("User", user.getName());
            assertEquals(3, user.getFields().size());

            assertTrue(user.hasField("id"));
            assertTrue(user.hasField("name"));
            assertTrue(user.hasField("age"));
        }

        @Test
        @DisplayName("Should parse message with all field types")
        void shouldParseMessageWithAllFieldTypes() {
            String content = """
                syntax = "proto3";
                package test;

                message AllTypes {
                    double double_field = 1;
                    float float_field = 2;
                    int32 int32_field = 3;
                    int64 int64_field = 4;
                    bool bool_field = 5;
                    string string_field = 6;
                    bytes bytes_field = 7;
                    repeated string tags = 8;
                    map<string, int32> scores = 9;
                }
                """;

            ProtoFile result = parser.parse(content);
            ProtoMessage msg = result.getMessage("AllTypes").orElseThrow();

            assertEquals(9, msg.getFields().size());
            assertTrue(msg.getField("tags").orElseThrow().isRepeated());
            assertTrue(msg.getField("scores").orElseThrow().isMap());
        }

        @Test
        @DisplayName("Should parse nested messages")
        void shouldParseNestedMessages() {
            String content = """
                syntax = "proto3";
                package test;

                message Outer {
                    string name = 1;

                    message Inner {
                        int32 value = 1;
                    }

                    Inner inner = 2;
                }
                """;

            ProtoFile result = parser.parse(content);
            ProtoMessage outer = result.getMessage("Outer").orElseThrow();

            assertEquals(1, outer.getNestedMessages().size());
            ProtoMessage inner = outer.getNestedMessage("Inner").orElseThrow();
            assertEquals("Inner", inner.getName());
        }

        @Test
        @DisplayName("Should parse message with oneof")
        void shouldParseMessageWithOneof() {
            String content = """
                syntax = "proto3";
                package test;

                message Result {
                    oneof result {
                        string success_message = 1;
                        string error_message = 2;
                    }
                }
                """;

            ProtoFile result = parser.parse(content);
            ProtoMessage msg = result.getMessage("Result").orElseThrow();

            assertTrue(msg.getOneofNames().contains("result"));
        }
    }

    @Nested
    @DisplayName("Enum Parsing Tests")
    class EnumParsingTests {

        @Test
        @DisplayName("Should parse simple enum")
        void shouldParseSimpleEnum() {
            String content = """
                syntax = "proto3";
                package test;

                enum Status {
                    STATUS_UNSPECIFIED = 0;
                    STATUS_ACTIVE = 1;
                    STATUS_INACTIVE = 2;
                }
                """;

            ProtoFile result = parser.parse(content);

            assertEquals(1, result.getEnums().size());
            ProtoEnum status = result.getEnum("Status").orElseThrow();
            assertEquals(3, status.getValues().size());
            assertTrue(status.hasValue("STATUS_UNSPECIFIED"));
            assertTrue(status.hasValue("STATUS_ACTIVE"));
        }

        @Test
        @DisplayName("Should parse enum with allow_alias")
        void shouldParseEnumWithAllowAlias() {
            String content = """
                syntax = "proto3";
                package test;

                enum Priority {
                    option allow_alias = true;
                    PRIORITY_UNSPECIFIED = 0;
                    PRIORITY_LOW = 1;
                    PRIORITY_NORMAL = 1;
                    PRIORITY_HIGH = 2;
                }
                """;

            ProtoFile result = parser.parse(content);
            ProtoEnum priority = result.getEnum("Priority").orElseThrow();

            assertTrue(priority.isAllowAlias());
        }
    }

    @Nested
    @DisplayName("Service Parsing Tests")
    class ServiceParsingTests {

        @Test
        @DisplayName("Should parse service with unary RPC")
        void shouldParseServiceWithUnaryRpc() {
            String content = """
                syntax = "proto3";
                package test;

                message Request { string id = 1; }
                message Response { string data = 1; }

                service TestService {
                    rpc GetData(Request) returns (Response);
                }
                """;

            ProtoFile result = parser.parse(content);

            assertEquals(1, result.getServices().size());
            ProtoService service = result.getService("TestService").orElseThrow();
            assertEquals(1, service.getMethods().size());

            ProtoRpcMethod method = service.getMethod("GetData").orElseThrow();
            assertEquals("Request", method.getInputType());
            assertEquals("Response", method.getOutputType());
            assertFalse(method.isClientStreaming());
            assertFalse(method.isServerStreaming());
        }

        @Test
        @DisplayName("Should parse service with streaming RPCs")
        void shouldParseServiceWithStreamingRpcs() {
            String content = """
                syntax = "proto3";
                package test;

                message Request { string id = 1; }
                message Response { string data = 1; }

                service StreamService {
                    rpc ServerStream(Request) returns (stream Response);
                    rpc ClientStream(stream Request) returns (Response);
                    rpc BidiStream(stream Request) returns (stream Response);
                }
                """;

            ProtoFile result = parser.parse(content);
            ProtoService service = result.getService("StreamService").orElseThrow();

            ProtoRpcMethod serverStream = service.getMethod("ServerStream").orElseThrow();
            assertFalse(serverStream.isClientStreaming());
            assertTrue(serverStream.isServerStreaming());

            ProtoRpcMethod clientStream = service.getMethod("ClientStream").orElseThrow();
            assertTrue(clientStream.isClientStreaming());
            assertFalse(clientStream.isServerStreaming());

            ProtoRpcMethod bidiStream = service.getMethod("BidiStream").orElseThrow();
            assertTrue(bidiStream.isClientStreaming());
            assertTrue(bidiStream.isServerStreaming());
        }

        @Test
        @DisplayName("Should parse deprecated RPC")
        void shouldParseDeprecatedRpc() {
            String content = """
                syntax = "proto3";
                package test;

                message Request { string id = 1; }
                message Response { string data = 1; }

                service TestService {
                    rpc OldMethod(Request) returns (Response) {
                        option deprecated = true;
                    }
                }
                """;

            ProtoFile result = parser.parse(content);
            ProtoService service = result.getService("TestService").orElseThrow();
            ProtoRpcMethod method = service.getMethod("OldMethod").orElseThrow();

            assertTrue(method.isDeprecated());
        }
    }

    @Nested
    @DisplayName("Stream Parsing Tests")
    class StreamParsingTests {

        @Test
        @DisplayName("Should parse from InputStream")
        void shouldParseFromInputStream() {
            String content = """
                syntax = "proto3";
                package test;
                message Test { string value = 1; }
                """;

            InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            ProtoFile result = parser.parseStream(stream);

            assertEquals("test", result.getPackageName());
            assertTrue(result.hasMessage("Test"));
        }

        @Test
        @DisplayName("Should parse from InputStream with filename")
        void shouldParseFromInputStreamWithFilename() {
            String content = """
                syntax = "proto3";
                package test;
                message Test { string value = 1; }
                """;

            InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            ProtoFile result = parser.parseStream(stream, "test.proto");

            assertEquals("test.proto", result.getFileName());
        }
    }

    @Nested
    @DisplayName("API Spec Conversion Tests")
    class ApiSpecConversionTests {

        @Test
        @DisplayName("Should convert ProtoFile to ApiSpec")
        void shouldConvertProtoFileToApiSpec() {
            String content = """
                syntax = "proto3";
                package test.api;

                message GetRequest { string id = 1; }
                message GetResponse { string data = 1; }

                service DataService {
                    rpc GetData(GetRequest) returns (GetResponse);
                }
                """;

            ProtoFile protoFile = parser.parse(content);
            ApiSpec apiSpec = parser.toApiSpec(protoFile);

            assertNotNull(apiSpec);
            assertNotNull(apiSpec.getName());
            assertFalse(apiSpec.getEndpoints().isEmpty());
            assertNotNull(apiSpec.getMetadata().get("schemas"));
        }

        @Test
        @DisplayName("Should convert multiple ProtoFiles to ApiSpec")
        void shouldConvertMultipleProtoFilesToApiSpec() {
            String content1 = """
                syntax = "proto3";
                package test.api.v1;
                message User { string id = 1; }
                service UserService {
                    rpc GetUser(User) returns (User);
                }
                """;

            String content2 = """
                syntax = "proto3";
                package test.api.v1;
                message Order { string id = 1; }
                service OrderService {
                    rpc GetOrder(Order) returns (Order);
                }
                """;

            ProtoFile proto1 = parser.parse(content1, "user.proto");
            ProtoFile proto2 = parser.parse(content2, "order.proto");

            ApiSpec apiSpec = parser.toApiSpec(List.of(proto1, proto2));

            assertNotNull(apiSpec);
            assertEquals(2, apiSpec.getEndpoints().size());
        }
    }

    @Nested
    @DisplayName("Comment Handling Tests")
    class CommentHandlingTests {

        @Test
        @DisplayName("Should ignore single-line comments")
        void shouldIgnoreSingleLineComments() {
            String content = """
                syntax = "proto3";
                // This is a comment
                package test;

                // Another comment
                message User {
                    string id = 1; // Inline comment
                }
                """;

            ProtoFile result = parser.parse(content);

            assertEquals("test", result.getPackageName());
            assertTrue(result.hasMessage("User"));
        }

        @Test
        @DisplayName("Should ignore multi-line comments")
        void shouldIgnoreMultiLineComments() {
            String content = """
                syntax = "proto3";
                /*
                 * This is a
                 * multi-line comment
                 */
                package test;

                message User {
                    string id = 1;
                }
                """;

            ProtoFile result = parser.parse(content);

            assertEquals("test", result.getPackageName());
            assertTrue(result.hasMessage("User"));
        }
    }

    @Nested
    @DisplayName("Supported Syntax Tests")
    class SupportedSyntaxTests {

        @Test
        @DisplayName("Should support proto2 and proto3")
        void shouldSupportProto2AndProto3() {
            assertTrue(parser.supportsSyntax("proto2"));
            assertTrue(parser.supportsSyntax("proto3"));
            assertFalse(parser.supportsSyntax("proto4"));
        }

        @Test
        @DisplayName("Should return supported versions")
        void shouldReturnSupportedVersions() {
            List<String> versions = parser.getSupportedSyntaxVersions();

            assertEquals(2, versions.size());
            assertTrue(versions.contains("proto2"));
            assertTrue(versions.contains("proto3"));
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should calculate correct statistics")
        void shouldCalculateCorrectStatistics() {
            String content = """
                syntax = "proto3";
                package test;

                enum Status { UNKNOWN = 0; ACTIVE = 1; }

                message User {
                    string id = 1;
                    string name = 2;
                    Status status = 3;
                }

                message Request { string id = 1; }
                message Response { User user = 1; }

                service UserService {
                    rpc GetUser(Request) returns (Response);
                    rpc UpdateUser(User) returns (Response);
                }
                """;

            ProtoFile result = parser.parse(content);
            var stats = result.getStatistics();

            assertEquals(1, stats.get("services"));
            assertEquals(3, stats.get("messages"));
            assertEquals(1, stats.get("enums"));
            assertEquals(2, stats.get("rpcMethods"));
            assertEquals(5, stats.get("fields")); // 3 + 1 + 1
        }
    }
}
