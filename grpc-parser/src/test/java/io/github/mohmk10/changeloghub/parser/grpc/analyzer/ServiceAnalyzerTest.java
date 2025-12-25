package io.github.mohmk10.changeloghub.parser.grpc.analyzer;

import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoRpcMethod;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoService;
import io.github.mohmk10.changeloghub.parser.grpc.util.StreamType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ServiceAnalyzer Tests")
class ServiceAnalyzerTest {

    private ServiceAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new ServiceAnalyzer();
    }

    @Nested
    @DisplayName("Basic Service Parsing")
    class BasicServiceParsing {

        @Test
        @DisplayName("Should parse simple service")
        void shouldParseSimpleService() {
            String content = """
                message Request { string id = 1; }
                message Response { string data = 1; }

                service TestService {
                    rpc GetData(Request) returns (Response);
                }
                """;

            List<ProtoService> services = analyzer.analyzeServices(content, "test");

            assertEquals(1, services.size());
            ProtoService service = services.get(0);
            assertEquals("TestService", service.getName());
            assertEquals("test.TestService", service.getFullName());
            assertEquals(1, service.getMethodCount());
        }

        @Test
        @DisplayName("Should parse multiple services")
        void shouldParseMultipleServices() {
            String content = """
                message Request { string id = 1; }
                message Response { string data = 1; }

                service UserService {
                    rpc GetUser(Request) returns (Response);
                }

                service OrderService {
                    rpc GetOrder(Request) returns (Response);
                }
                """;

            List<ProtoService> services = analyzer.analyzeServices(content, "test");

            assertEquals(2, services.size());
            assertTrue(services.stream().anyMatch(s -> s.getName().equals("UserService")));
            assertTrue(services.stream().anyMatch(s -> s.getName().equals("OrderService")));
        }

        @Test
        @DisplayName("Should return empty list for no services")
        void shouldReturnEmptyListForNoServices() {
            String content = """
                message Request { string id = 1; }
                message Response { string data = 1; }
                """;

            List<ProtoService> services = analyzer.analyzeServices(content, "test");

            assertTrue(services.isEmpty());
        }
    }

    @Nested
    @DisplayName("RPC Method Parsing")
    class RpcMethodParsing {

        @Test
        @DisplayName("Should parse unary RPC")
        void shouldParseUnaryRpc() {
            String content = """
                service TestService {
                    rpc UnaryMethod(Request) returns (Response);
                }
                """;

            List<ProtoService> services = analyzer.analyzeServices(content, "test");
            ProtoRpcMethod method = services.get(0).getMethod("UnaryMethod").orElseThrow();

            assertEquals("UnaryMethod", method.getName());
            assertEquals("Request", method.getInputType());
            assertEquals("Response", method.getOutputType());
            assertEquals(StreamType.UNARY, method.getStreamType());
            assertFalse(method.isStreaming());
        }

        @Test
        @DisplayName("Should parse server streaming RPC")
        void shouldParseServerStreamingRpc() {
            String content = """
                service TestService {
                    rpc ServerStream(Request) returns (stream Response);
                }
                """;

            List<ProtoService> services = analyzer.analyzeServices(content, "test");
            ProtoRpcMethod method = services.get(0).getMethod("ServerStream").orElseThrow();

            assertEquals(StreamType.SERVER_STREAMING, method.getStreamType());
            assertFalse(method.isClientStreaming());
            assertTrue(method.isServerStreaming());
            assertTrue(method.isStreaming());
        }

        @Test
        @DisplayName("Should parse client streaming RPC")
        void shouldParseClientStreamingRpc() {
            String content = """
                service TestService {
                    rpc ClientStream(stream Request) returns (Response);
                }
                """;

            List<ProtoService> services = analyzer.analyzeServices(content, "test");
            ProtoRpcMethod method = services.get(0).getMethod("ClientStream").orElseThrow();

            assertEquals(StreamType.CLIENT_STREAMING, method.getStreamType());
            assertTrue(method.isClientStreaming());
            assertFalse(method.isServerStreaming());
            assertTrue(method.isStreaming());
        }

        @Test
        @DisplayName("Should parse bidirectional streaming RPC")
        void shouldParseBidirectionalStreamingRpc() {
            String content = """
                service TestService {
                    rpc BidiStream(stream Request) returns (stream Response);
                }
                """;

            List<ProtoService> services = analyzer.analyzeServices(content, "test");
            ProtoRpcMethod method = services.get(0).getMethod("BidiStream").orElseThrow();

            assertEquals(StreamType.BIDIRECTIONAL, method.getStreamType());
            assertTrue(method.isClientStreaming());
            assertTrue(method.isServerStreaming());
            assertTrue(method.isStreaming());
        }

        @Test
        @DisplayName("Should parse all streaming types in one service")
        void shouldParseAllStreamingTypesInOneService() {
            String content = """
                service CompleteService {
                    rpc Unary(Request) returns (Response);
                    rpc ServerStream(Request) returns (stream Response);
                    rpc ClientStream(stream Request) returns (Response);
                    rpc BidiStream(stream Request) returns (stream Response);
                }
                """;

            List<ProtoService> services = analyzer.analyzeServices(content, "test");
            ProtoService service = services.get(0);

            assertEquals(4, service.getMethodCount());

            assertEquals(StreamType.UNARY,
                    service.getMethod("Unary").orElseThrow().getStreamType());
            assertEquals(StreamType.SERVER_STREAMING,
                    service.getMethod("ServerStream").orElseThrow().getStreamType());
            assertEquals(StreamType.CLIENT_STREAMING,
                    service.getMethod("ClientStream").orElseThrow().getStreamType());
            assertEquals(StreamType.BIDIRECTIONAL,
                    service.getMethod("BidiStream").orElseThrow().getStreamType());
        }
    }

    @Nested
    @DisplayName("Service Options Parsing")
    class ServiceOptionsParsing {

        @Test
        @DisplayName("Should parse deprecated service")
        void shouldParseDeprecatedService() {
            String content = """
                service OldService {
                    option deprecated = true;
                    rpc GetData(Request) returns (Response);
                }
                """;

            List<ProtoService> services = analyzer.analyzeServices(content, "test");

            assertTrue(services.get(0).isDeprecated());
        }

        @Test
        @DisplayName("Should parse deprecated RPC method")
        void shouldParseDeprecatedRpcMethod() {
            String content = """
                service TestService {
                    rpc OldMethod(Request) returns (Response) {
                        option deprecated = true;
                    }
                    rpc NewMethod(Request) returns (Response);
                }
                """;

            List<ProtoService> services = analyzer.analyzeServices(content, "test");
            ProtoService service = services.get(0);

            assertTrue(service.getMethod("OldMethod").orElseThrow().isDeprecated());
            assertFalse(service.getMethod("NewMethod").orElseThrow().isDeprecated());
        }
    }

    @Nested
    @DisplayName("gRPC Path Generation")
    class GrpcPathGeneration {

        @Test
        @DisplayName("Should generate correct gRPC path with package")
        void shouldGenerateCorrectGrpcPathWithPackage() {
            String content = """
                service UserService {
                    rpc GetUser(Request) returns (Response);
                }
                """;

            List<ProtoService> services = analyzer.analyzeServices(content, "com.example.api");
            ProtoService service = services.get(0);
            ProtoRpcMethod method = service.getMethod("GetUser").orElseThrow();

            String path = method.getGrpcPath("com.example.api", "UserService");
            assertEquals("/com.example.api.UserService/GetUser", path);
        }

        @Test
        @DisplayName("Should generate correct gRPC path without package")
        void shouldGenerateCorrectGrpcPathWithoutPackage() {
            String content = """
                service UserService {
                    rpc GetUser(Request) returns (Response);
                }
                """;

            List<ProtoService> services = analyzer.analyzeServices(content, null);
            ProtoService service = services.get(0);
            ProtoRpcMethod method = service.getMethod("GetUser").orElseThrow();

            String path = method.getGrpcPath(null, "UserService");
            assertEquals("/UserService/GetUser", path);
        }

        @Test
        @DisplayName("Should get all gRPC paths for service")
        void shouldGetAllGrpcPathsForService() {
            String content = """
                service UserService {
                    rpc GetUser(Request) returns (Response);
                    rpc CreateUser(Request) returns (Response);
                    rpc DeleteUser(Request) returns (Response);
                }
                """;

            List<ProtoService> services = analyzer.analyzeServices(content, "api");
            ProtoService service = services.get(0);

            List<String> paths = service.getGrpcPaths("api");

            assertEquals(3, paths.size());
            assertTrue(paths.contains("/api.UserService/GetUser"));
            assertTrue(paths.contains("/api.UserService/CreateUser"));
            assertTrue(paths.contains("/api.UserService/DeleteUser"));
        }
    }

    @Nested
    @DisplayName("Referenced Message Types")
    class ReferencedMessageTypes {

        @Test
        @DisplayName("Should get all referenced message types")
        void shouldGetAllReferencedMessageTypes() {
            String content = """
                service UserService {
                    rpc GetUser(GetUserRequest) returns (GetUserResponse);
                    rpc CreateUser(CreateUserRequest) returns (CreateUserResponse);
                    rpc UpdateUser(UpdateUserRequest) returns (UpdateUserResponse);
                }
                """;

            List<ProtoService> services = analyzer.analyzeServices(content, "test");
            Set<String> types = analyzer.getReferencedMessageTypes(services);

            assertEquals(6, types.size());
            assertTrue(types.contains("GetUserRequest"));
            assertTrue(types.contains("GetUserResponse"));
            assertTrue(types.contains("CreateUserRequest"));
            assertTrue(types.contains("CreateUserResponse"));
            assertTrue(types.contains("UpdateUserRequest"));
            assertTrue(types.contains("UpdateUserResponse"));
        }
    }

    @Nested
    @DisplayName("Method Signature")
    class MethodSignature {

        @Test
        @DisplayName("Should generate correct signature for unary")
        void shouldGenerateCorrectSignatureForUnary() {
            String content = """
                service TestService {
                    rpc GetData(Request) returns (Response);
                }
                """;

            List<ProtoService> services = analyzer.analyzeServices(content, "test");
            ProtoRpcMethod method = services.get(0).getMethod("GetData").orElseThrow();

            assertEquals("rpc GetData(Request) returns (Response)", method.getSignature());
        }

        @Test
        @DisplayName("Should generate correct signature for streaming")
        void shouldGenerateCorrectSignatureForStreaming() {
            String content = """
                service TestService {
                    rpc StreamData(stream Request) returns (stream Response);
                }
                """;

            List<ProtoService> services = analyzer.analyzeServices(content, "test");
            ProtoRpcMethod method = services.get(0).getMethod("StreamData").orElseThrow();

            assertEquals("rpc StreamData(stream Request) returns (stream Response)", method.getSignature());
        }
    }
}
