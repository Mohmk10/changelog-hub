package io.github.mohmk10.changeloghub.parser.spring.extractor;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class to convert Java types to API types.
 */
public class TypeExtractor {

    private static final Map<String, String> PRIMITIVE_MAPPINGS = new HashMap<>();
    private static final Map<String, String> WRAPPER_MAPPINGS = new HashMap<>();
    private static final Map<String, String> COMMON_MAPPINGS = new HashMap<>();

    static {
        // Primitive type mappings
        PRIMITIVE_MAPPINGS.put("int", "integer");
        PRIMITIVE_MAPPINGS.put("long", "integer");
        PRIMITIVE_MAPPINGS.put("short", "integer");
        PRIMITIVE_MAPPINGS.put("byte", "integer");
        PRIMITIVE_MAPPINGS.put("float", "number");
        PRIMITIVE_MAPPINGS.put("double", "number");
        PRIMITIVE_MAPPINGS.put("boolean", "boolean");
        PRIMITIVE_MAPPINGS.put("char", "string");

        // Wrapper type mappings
        WRAPPER_MAPPINGS.put("Integer", "integer");
        WRAPPER_MAPPINGS.put("Long", "integer");
        WRAPPER_MAPPINGS.put("Short", "integer");
        WRAPPER_MAPPINGS.put("Byte", "integer");
        WRAPPER_MAPPINGS.put("Float", "number");
        WRAPPER_MAPPINGS.put("Double", "number");
        WRAPPER_MAPPINGS.put("Boolean", "boolean");
        WRAPPER_MAPPINGS.put("Character", "string");
        WRAPPER_MAPPINGS.put("String", "string");
        WRAPPER_MAPPINGS.put("BigDecimal", "number");
        WRAPPER_MAPPINGS.put("BigInteger", "integer");

        // Common Java types
        COMMON_MAPPINGS.put("Date", "string");
        COMMON_MAPPINGS.put("LocalDate", "string");
        COMMON_MAPPINGS.put("LocalDateTime", "string");
        COMMON_MAPPINGS.put("ZonedDateTime", "string");
        COMMON_MAPPINGS.put("Instant", "string");
        COMMON_MAPPINGS.put("UUID", "string");
        COMMON_MAPPINGS.put("Object", "object");
        COMMON_MAPPINGS.put("Map", "object");
        COMMON_MAPPINGS.put("HashMap", "object");
        COMMON_MAPPINGS.put("LinkedHashMap", "object");
        COMMON_MAPPINGS.put("void", "null");
        COMMON_MAPPINGS.put("Void", "null");
    }

    /**
     * Convert a Java type to an API type string.
     */
    public String javaTypeToApiType(Type javaType) {
        if (javaType == null) {
            return "object";
        }

        if (javaType.isPrimitiveType()) {
            PrimitiveType primitiveType = javaType.asPrimitiveType();
            return PRIMITIVE_MAPPINGS.getOrDefault(primitiveType.asString(), "string");
        }

        if (javaType.isVoidType()) {
            return "null";
        }

        if (javaType.isClassOrInterfaceType()) {
            return handleClassOrInterfaceType(javaType.asClassOrInterfaceType());
        }

        if (javaType.isArrayType()) {
            return "array";
        }

        return "object";
    }

    /**
     * Convert a Java type string to an API type string.
     */
    public String javaTypeToApiType(String javaTypeName) {
        if (javaTypeName == null || javaTypeName.isEmpty()) {
            return "object";
        }

        // Check primitive mappings
        if (PRIMITIVE_MAPPINGS.containsKey(javaTypeName)) {
            return PRIMITIVE_MAPPINGS.get(javaTypeName);
        }

        // Check wrapper mappings
        String simpleTypeName = getSimpleTypeName(javaTypeName);
        if (WRAPPER_MAPPINGS.containsKey(simpleTypeName)) {
            return WRAPPER_MAPPINGS.get(simpleTypeName);
        }

        // Check common mappings
        if (COMMON_MAPPINGS.containsKey(simpleTypeName)) {
            return COMMON_MAPPINGS.get(simpleTypeName);
        }

        // Check for collection types
        if (isCollectionType(simpleTypeName)) {
            return "array";
        }

        // Check for array notation
        if (javaTypeName.endsWith("[]")) {
            return "array";
        }

        // Default to object for complex types
        return "object";
    }

    /**
     * Extract the item type from a collection type.
     * List<User> -> User, Optional<String> -> String
     */
    public Optional<String> extractGenericType(String javaTypeName) {
        int startIndex = javaTypeName.indexOf('<');
        int endIndex = javaTypeName.lastIndexOf('>');

        if (startIndex > 0 && endIndex > startIndex) {
            String genericPart = javaTypeName.substring(startIndex + 1, endIndex);
            // Handle nested generics like Map<String, List<User>>
            if (!genericPart.contains("<")) {
                // For simple generics, try to get the last type (value type for Map)
                String[] parts = genericPart.split(",");
                return Optional.of(parts[parts.length - 1].trim());
            }
            return Optional.of(genericPart);
        }

        return Optional.empty();
    }

    /**
     * Check if a type represents a collection.
     */
    public boolean isCollectionType(String typeName) {
        String simple = getSimpleTypeName(typeName);
        return simple.equals("List") || simple.equals("Set") || simple.equals("Collection") ||
               simple.equals("ArrayList") || simple.equals("HashSet") || simple.equals("LinkedList") ||
               simple.equals("TreeSet") || simple.equals("Iterable");
    }

    /**
     * Check if a type represents an Optional.
     */
    public boolean isOptionalType(String typeName) {
        return getSimpleTypeName(typeName).equals("Optional");
    }

    /**
     * Check if a type represents ResponseEntity.
     */
    public boolean isResponseEntityType(String typeName) {
        return getSimpleTypeName(typeName).equals("ResponseEntity");
    }

    /**
     * Get the simple type name without package.
     */
    public String getSimpleTypeName(String fullTypeName) {
        if (fullTypeName == null) {
            return "";
        }

        // Remove generic part
        int genericIndex = fullTypeName.indexOf('<');
        String withoutGenerics = genericIndex > 0 ? fullTypeName.substring(0, genericIndex) : fullTypeName;

        // Get simple name
        int lastDot = withoutGenerics.lastIndexOf('.');
        return lastDot > 0 ? withoutGenerics.substring(lastDot + 1) : withoutGenerics;
    }

    private String handleClassOrInterfaceType(ClassOrInterfaceType type) {
        String typeName = type.getNameAsString();

        // Check wrapper mappings
        if (WRAPPER_MAPPINGS.containsKey(typeName)) {
            return WRAPPER_MAPPINGS.get(typeName);
        }

        // Check common mappings
        if (COMMON_MAPPINGS.containsKey(typeName)) {
            return COMMON_MAPPINGS.get(typeName);
        }

        // Check for collection types
        if (isCollectionType(typeName)) {
            return "array";
        }

        // Check for ResponseEntity - extract the generic type
        if ("ResponseEntity".equals(typeName)) {
            return type.getTypeArguments()
                    .flatMap(args -> args.isEmpty() ? Optional.empty() : Optional.of(args.get(0)))
                    .map(this::javaTypeToApiType)
                    .orElse("object");
        }

        // Check for Optional - extract the generic type
        if ("Optional".equals(typeName)) {
            return type.getTypeArguments()
                    .flatMap(args -> args.isEmpty() ? Optional.empty() : Optional.of(args.get(0)))
                    .map(this::javaTypeToApiType)
                    .orElse("object");
        }

        // Default to object for custom types
        return "object";
    }
}
