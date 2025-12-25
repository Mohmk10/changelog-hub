package io.github.mohmk10.changeloghub.parser.spring.analyzer;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;
import io.github.mohmk10.changeloghub.parser.spring.extractor.TypeExtractor;

import java.util.Optional;

/**
 * Analyzer for Spring controller method responses.
 */
public class ResponseAnalyzer {

    private final RequestMappingAnalyzer requestMappingAnalyzer;
    private final TypeExtractor typeExtractor;

    public ResponseAnalyzer() {
        this.requestMappingAnalyzer = new RequestMappingAnalyzer();
        this.typeExtractor = new TypeExtractor();
    }

    /**
     * Get the HTTP status code from @ResponseStatus or infer from method.
     */
    public String getResponseStatus(MethodDeclaration method) {
        // Check for @ResponseStatus annotation
        Optional<String> annotatedStatus = requestMappingAnalyzer.getResponseStatus(method);
        if (annotatedStatus.isPresent()) {
            return annotatedStatus.get();
        }

        // Infer from HTTP method
        String httpMethod = requestMappingAnalyzer.getHttpMethod(method);
        Type returnType = method.getType();

        // POST typically returns 201 Created
        if ("POST".equals(httpMethod) && !returnType.isVoidType()) {
            return "201";
        }

        // DELETE with void return typically returns 204 No Content
        if ("DELETE".equals(httpMethod) && returnType.isVoidType()) {
            return "204";
        }

        // Default to 200 OK
        return "200";
    }

    /**
     * Get the return type of the method as a string.
     */
    public String getReturnType(MethodDeclaration method) {
        Type returnType = method.getType();

        if (returnType.isVoidType()) {
            return "void";
        }

        return returnType.asString();
    }

    /**
     * Get the actual response type (unwrapping ResponseEntity, Optional, etc.)
     */
    public String getActualReturnType(MethodDeclaration method) {
        Type returnType = method.getType();

        if (returnType.isVoidType()) {
            return "void";
        }

        String typeString = returnType.asString();

        // Unwrap ResponseEntity<X>
        if (typeExtractor.isResponseEntityType(typeString)) {
            Optional<String> genericType = typeExtractor.extractGenericType(typeString);
            if (genericType.isPresent()) {
                return genericType.get();
            }
        }

        // Unwrap Optional<X>
        if (typeExtractor.isOptionalType(typeString)) {
            Optional<String> genericType = typeExtractor.extractGenericType(typeString);
            if (genericType.isPresent()) {
                return genericType.get();
            }
        }

        return typeString;
    }

    /**
     * Get the API type for the return value.
     */
    public String getReturnApiType(MethodDeclaration method) {
        String actualType = getActualReturnType(method);
        return typeExtractor.javaTypeToApiType(actualType);
    }

    /**
     * Check if the method returns a collection.
     */
    public boolean returnsCollection(MethodDeclaration method) {
        String actualType = getActualReturnType(method);
        return typeExtractor.isCollectionType(typeExtractor.getSimpleTypeName(actualType));
    }

    /**
     * Get the item type if the method returns a collection.
     */
    public Optional<String> getCollectionItemType(MethodDeclaration method) {
        String actualType = getActualReturnType(method);

        if (typeExtractor.isCollectionType(typeExtractor.getSimpleTypeName(actualType))) {
            return typeExtractor.extractGenericType(actualType);
        }

        return Optional.empty();
    }
}
