package io.github.mohmk10.changeloghub.parser.grpc.analyzer;

import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoField;
import io.github.mohmk10.changeloghub.parser.grpc.util.ProtoFieldRule;
import io.github.mohmk10.changeloghub.parser.grpc.util.ProtoFieldType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FieldAnalyzer Tests")
class FieldAnalyzerTest {

    private FieldAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new FieldAnalyzer();
    }

    @Nested
    @DisplayName("Basic Field Parsing")
    class BasicFieldParsing {

        @Test
        @DisplayName("Should parse simple fields")
        void shouldParseSimpleFields() {
            String body = """
                string name = 1;
                int32 age = 2;
                bool active = 3;
                """;

            List<ProtoField> fields = analyzer.analyzeFields(body);

            assertEquals(3, fields.size());

            ProtoField name = fields.get(0);
            assertEquals("name", name.getName());
            assertEquals(1, name.getNumber());
            assertEquals("string", name.getTypeName());
            assertEquals(ProtoFieldType.STRING, name.getType());

            ProtoField age = fields.get(1);
            assertEquals("age", age.getName());
            assertEquals(2, age.getNumber());
            assertEquals(ProtoFieldType.INT32, age.getType());
        }

        @Test
        @DisplayName("Should parse all scalar types")
        void shouldParseAllScalarTypes() {
            String body = """
                double d = 1;
                float f = 2;
                int32 i32 = 3;
                int64 i64 = 4;
                uint32 ui32 = 5;
                uint64 ui64 = 6;
                sint32 si32 = 7;
                sint64 si64 = 8;
                fixed32 fx32 = 9;
                fixed64 fx64 = 10;
                sfixed32 sfx32 = 11;
                sfixed64 sfx64 = 12;
                bool b = 13;
                string s = 14;
                bytes by = 15;
                """;

            List<ProtoField> fields = analyzer.analyzeFields(body);

            assertEquals(15, fields.size());

            assertEquals(ProtoFieldType.DOUBLE, fields.get(0).getType());
            assertEquals(ProtoFieldType.FLOAT, fields.get(1).getType());
            assertEquals(ProtoFieldType.INT32, fields.get(2).getType());
            assertEquals(ProtoFieldType.INT64, fields.get(3).getType());
            assertEquals(ProtoFieldType.BOOL, fields.get(12).getType());
            assertEquals(ProtoFieldType.STRING, fields.get(13).getType());
            assertEquals(ProtoFieldType.BYTES, fields.get(14).getType());
        }

        @Test
        @DisplayName("Should return empty list for empty body")
        void shouldReturnEmptyListForEmptyBody() {
            assertTrue(analyzer.analyzeFields("").isEmpty());
            assertTrue(analyzer.analyzeFields(null).isEmpty());
            assertTrue(analyzer.analyzeFields("   ").isEmpty());
        }
    }

    @Nested
    @DisplayName("Field Rules Parsing")
    class FieldRulesParsing {

        @Test
        @DisplayName("Should parse optional field")
        void shouldParseOptionalField() {
            String body = "optional string name = 1;";

            List<ProtoField> fields = analyzer.analyzeFields(body);

            assertEquals(1, fields.size());
            assertEquals(ProtoFieldRule.OPTIONAL, fields.get(0).getRule());
            assertTrue(fields.get(0).isOptional());
        }

        @Test
        @DisplayName("Should parse required field (proto2)")
        void shouldParseRequiredField() {
            String body = "required string name = 1;";

            List<ProtoField> fields = analyzer.analyzeFields(body);

            assertEquals(1, fields.size());
            assertEquals(ProtoFieldRule.REQUIRED, fields.get(0).getRule());
            assertTrue(fields.get(0).isRequired());
        }

        @Test
        @DisplayName("Should parse repeated field")
        void shouldParseRepeatedField() {
            String body = "repeated string tags = 1;";

            List<ProtoField> fields = analyzer.analyzeFields(body);

            assertEquals(1, fields.size());
            assertEquals(ProtoFieldRule.REPEATED, fields.get(0).getRule());
            assertTrue(fields.get(0).isRepeated());
        }

        @Test
        @DisplayName("Should parse field without rule as singular")
        void shouldParseFieldWithoutRuleAsSingular() {
            String body = "string name = 1;";

            List<ProtoField> fields = analyzer.analyzeFields(body);

            assertEquals(ProtoFieldRule.SINGULAR, fields.get(0).getRule());
        }
    }

    @Nested
    @DisplayName("Map Field Parsing")
    class MapFieldParsing {

        @Test
        @DisplayName("Should parse map field")
        void shouldParseMapField() {
            String body = "map<string, int32> scores = 1;";

            List<ProtoField> fields = analyzer.analyzeFields(body);

            assertEquals(1, fields.size());
            ProtoField field = fields.get(0);

            assertEquals("scores", field.getName());
            assertTrue(field.isMap());
            assertEquals("string", field.getMapKeyType().orElse(null));
            assertEquals("int32", field.getMapValueType().orElse(null));
        }

        @Test
        @DisplayName("Should parse map with message value type")
        void shouldParseMapWithMessageValueType() {
            String body = "map<string, User> users = 1;";

            List<ProtoField> fields = analyzer.analyzeFields(body);

            assertEquals(1, fields.size());
            ProtoField field = fields.get(0);

            assertTrue(field.isMap());
            assertEquals("string", field.getMapKeyType().orElse(null));
            assertEquals("User", field.getMapValueType().orElse(null));
        }
    }

    @Nested
    @DisplayName("Field Options Parsing")
    class FieldOptionsParsing {

        @Test
        @DisplayName("Should parse deprecated option")
        void shouldParseDeprecatedOption() {
            String body = "string old_field = 1 [deprecated = true];";

            List<ProtoField> fields = analyzer.analyzeFields(body);

            assertEquals(1, fields.size());
            assertTrue(fields.get(0).isDeprecated());
        }

        @Test
        @DisplayName("Should parse default option")
        void shouldParseDefaultOption() {
            String body = "int32 count = 1 [default = 10];";

            List<ProtoField> fields = analyzer.analyzeFields(body);

            assertEquals(1, fields.size());
            assertEquals("10", fields.get(0).getDefaultValue().orElse(null));
        }

        @Test
        @DisplayName("Should parse multiple options")
        void shouldParseMultipleOptions() {
            String body = "string field = 1 [deprecated = true, json_name = \"fieldName\"];";

            List<ProtoField> fields = analyzer.analyzeFields(body);

            assertEquals(1, fields.size());
            assertTrue(fields.get(0).isDeprecated());
            assertEquals("fieldName", fields.get(0).getOption("json_name"));
        }
    }

    @Nested
    @DisplayName("Message Type Fields")
    class MessageTypeFields {

        @Test
        @DisplayName("Should parse message type field")
        void shouldParseMessageTypeField() {
            String body = "Address address = 1;";

            List<ProtoField> fields = analyzer.analyzeFields(body);

            assertEquals(1, fields.size());
            ProtoField field = fields.get(0);

            assertEquals("Address", field.getTypeName());
            assertEquals(ProtoFieldType.MESSAGE, field.getType());
            assertFalse(field.isScalar());
        }

        @Test
        @DisplayName("Should parse fully qualified message type")
        void shouldParseFullyQualifiedMessageType() {
            String body = "google.protobuf.Timestamp created_at = 1;";

            List<ProtoField> fields = analyzer.analyzeFields(body);

            assertEquals(1, fields.size());
            assertEquals("google.protobuf.Timestamp", fields.get(0).getTypeName());
        }
    }

    @Nested
    @DisplayName("Oneof Parsing")
    class OneofParsing {

        @Test
        @DisplayName("Should get oneof names")
        void shouldGetOneofNames() {
            String body = """
                oneof result {
                    string success = 1;
                    string error = 2;
                }
                oneof identifier {
                    string id = 3;
                    string email = 4;
                }
                """;

            List<String> oneofNames = analyzer.getOneofNames(body);

            assertEquals(2, oneofNames.size());
            assertTrue(oneofNames.contains("result"));
            assertTrue(oneofNames.contains("identifier"));
        }

        @Test
        @DisplayName("Should return empty list when no oneofs")
        void shouldReturnEmptyListWhenNoOneofs() {
            String body = """
                string name = 1;
                int32 age = 2;
                """;

            List<String> oneofNames = analyzer.getOneofNames(body);

            assertTrue(oneofNames.isEmpty());
        }
    }

    @Nested
    @DisplayName("Reserved Parsing")
    class ReservedParsing {

        @Test
        @DisplayName("Should parse reserved numbers")
        void shouldParseReservedNumbers() {
            String body = """
                reserved 2, 15, 9;
                string name = 1;
                """;

            Set<Integer> reserved = analyzer.parseReservedNumbers(body);

            assertEquals(3, reserved.size());
            assertTrue(reserved.contains(2));
            assertTrue(reserved.contains(15));
            assertTrue(reserved.contains(9));
        }

        @Test
        @DisplayName("Should parse reserved range")
        void shouldParseReservedRange() {
            String body = """
                reserved 5 to 10;
                string name = 1;
                """;

            Set<Integer> reserved = analyzer.parseReservedNumbers(body);

            assertEquals(6, reserved.size());
            for (int i = 5; i <= 10; i++) {
                assertTrue(reserved.contains(i));
            }
        }

        @Test
        @DisplayName("Should parse reserved names")
        void shouldParseReservedNames() {
            String body = """
                reserved "old_field", "deprecated_field";
                string name = 1;
                """;

            Set<String> reserved = analyzer.parseReservedNames(body);

            assertTrue(reserved.contains("old_field"));
            assertTrue(reserved.contains("deprecated_field"));
        }
    }

    @Nested
    @DisplayName("Full Type Signature Tests")
    class FullTypeSignatureTests {

        @Test
        @DisplayName("Should generate correct signature for repeated")
        void shouldGenerateCorrectSignatureForRepeated() {
            String body = "repeated string tags = 1;";
            List<ProtoField> fields = analyzer.analyzeFields(body);

            assertEquals("repeated string", fields.get(0).getFullTypeSignature());
        }

        @Test
        @DisplayName("Should generate correct signature for map")
        void shouldGenerateCorrectSignatureForMap() {
            String body = "map<string, int32> scores = 1;";
            List<ProtoField> fields = analyzer.analyzeFields(body);

            assertEquals("map<string, int32>", fields.get(0).getFullTypeSignature());
        }

        @Test
        @DisplayName("Should generate correct signature for optional")
        void shouldGenerateCorrectSignatureForOptional() {
            String body = "optional string name = 1;";
            List<ProtoField> fields = analyzer.analyzeFields(body);

            assertEquals("optional string", fields.get(0).getFullTypeSignature());
        }
    }
}
