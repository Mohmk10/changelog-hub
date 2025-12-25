package io.github.mohmk10.changeloghub.parser.spring.extractor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class TypeExtractorTest {

    private TypeExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new TypeExtractor();
    }

    @Test
    void testConvertStringType() {
        assertThat(extractor.javaTypeToApiType("String")).isEqualTo("string");
    }

    @Test
    void testConvertIntegerType() {
        assertThat(extractor.javaTypeToApiType("Integer")).isEqualTo("integer");
        assertThat(extractor.javaTypeToApiType("int")).isEqualTo("integer");
    }

    @Test
    void testConvertLongType() {
        assertThat(extractor.javaTypeToApiType("Long")).isEqualTo("integer");
        assertThat(extractor.javaTypeToApiType("long")).isEqualTo("integer");
    }

    @Test
    void testConvertDoubleType() {
        assertThat(extractor.javaTypeToApiType("Double")).isEqualTo("number");
        assertThat(extractor.javaTypeToApiType("double")).isEqualTo("number");
    }

    @Test
    void testConvertFloatType() {
        assertThat(extractor.javaTypeToApiType("Float")).isEqualTo("number");
        assertThat(extractor.javaTypeToApiType("float")).isEqualTo("number");
    }

    @Test
    void testConvertBooleanType() {
        assertThat(extractor.javaTypeToApiType("Boolean")).isEqualTo("boolean");
        assertThat(extractor.javaTypeToApiType("boolean")).isEqualTo("boolean");
    }

    @Test
    void testConvertListType() {
        assertThat(extractor.javaTypeToApiType("List<String>")).isEqualTo("array");
        assertThat(extractor.javaTypeToApiType("List<User>")).isEqualTo("array");
    }

    @Test
    void testConvertSetType() {
        assertThat(extractor.javaTypeToApiType("Set<String>")).isEqualTo("array");
    }

    @Test
    void testConvertCollectionType() {
        assertThat(extractor.javaTypeToApiType("Collection<Item>")).isEqualTo("array");
    }

    @Test
    void testConvertMapType() {
        assertThat(extractor.javaTypeToApiType("Map<String, Object>")).isEqualTo("object");
    }

    @Test
    void testConvertVoidType() {
        assertThat(extractor.javaTypeToApiType("void")).isEqualTo("null");
        assertThat(extractor.javaTypeToApiType("Void")).isEqualTo("null");
    }

    @Test
    void testConvertObjectType() {
        assertThat(extractor.javaTypeToApiType("UserDto")).isEqualTo("object");
        assertThat(extractor.javaTypeToApiType("CreateUserRequest")).isEqualTo("object");
    }

    @Test
    void testExtractGenericType() {
        Optional<String> result = extractor.extractGenericType("List<String>");
        assertThat(result).isPresent().contains("String");

        result = extractor.extractGenericType("ResponseEntity<UserDto>");
        assertThat(result).isPresent().contains("UserDto");
    }

    @Test
    void testExtractGenericTypeNoGeneric() {
        Optional<String> result = extractor.extractGenericType("String");
        assertThat(result).isEmpty();
    }

    @Test
    void testIsCollectionType() {
        assertThat(extractor.isCollectionType("List")).isTrue();
        assertThat(extractor.isCollectionType("Set")).isTrue();
        assertThat(extractor.isCollectionType("Collection")).isTrue();
        assertThat(extractor.isCollectionType("String")).isFalse();
        assertThat(extractor.isCollectionType("User")).isFalse();
    }

    @Test
    void testConvertBigDecimalType() {
        assertThat(extractor.javaTypeToApiType("BigDecimal")).isEqualTo("number");
    }

    @Test
    void testConvertBigIntegerType() {
        assertThat(extractor.javaTypeToApiType("BigInteger")).isEqualTo("integer");
    }

    @Test
    void testConvertArrayType() {
        assertThat(extractor.javaTypeToApiType("String[]")).isEqualTo("array");
        assertThat(extractor.javaTypeToApiType("int[]")).isEqualTo("array");
    }

    @Test
    void testConvertDateType() {
        assertThat(extractor.javaTypeToApiType("Date")).isEqualTo("string");
        assertThat(extractor.javaTypeToApiType("LocalDate")).isEqualTo("string");
        assertThat(extractor.javaTypeToApiType("LocalDateTime")).isEqualTo("string");
    }

    @Test
    void testConvertUuidType() {
        assertThat(extractor.javaTypeToApiType("UUID")).isEqualTo("string");
    }

    @Test
    void testGetSimpleTypeName() {
        assertThat(extractor.getSimpleTypeName("java.lang.String")).isEqualTo("String");
        assertThat(extractor.getSimpleTypeName("List<User>")).isEqualTo("List");
        assertThat(extractor.getSimpleTypeName("String")).isEqualTo("String");
    }

    @Test
    void testIsOptionalType() {
        assertThat(extractor.isOptionalType("Optional<User>")).isTrue();
        assertThat(extractor.isOptionalType("String")).isFalse();
    }

    @Test
    void testIsResponseEntityType() {
        assertThat(extractor.isResponseEntityType("ResponseEntity<User>")).isTrue();
        assertThat(extractor.isResponseEntityType("User")).isFalse();
    }

    @Test
    void testConvertNullType() {
        assertThat(extractor.javaTypeToApiType((String) null)).isEqualTo("object");
    }

    @Test
    void testConvertEmptyType() {
        assertThat(extractor.javaTypeToApiType("")).isEqualTo("object");
    }
}
