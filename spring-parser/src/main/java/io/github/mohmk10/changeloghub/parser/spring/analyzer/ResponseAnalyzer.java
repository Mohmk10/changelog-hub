package io.github.mohmk10.changeloghub.parser.spring.analyzer;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;
import io.github.mohmk10.changeloghub.parser.spring.extractor.TypeExtractor;

import java.util.Optional;

public class ResponseAnalyzer {

    private final RequestMappingAnalyzer requestMappingAnalyzer;
    private final TypeExtractor typeExtractor;

    public ResponseAnalyzer() {
        this.requestMappingAnalyzer = new RequestMappingAnalyzer();
        this.typeExtractor = new TypeExtractor();
    }

    public String getResponseStatus(MethodDeclaration method) {
        
        Optional<String> annotatedStatus = requestMappingAnalyzer.getResponseStatus(method);
        if (annotatedStatus.isPresent()) {
            return annotatedStatus.get();
        }

        String httpMethod = requestMappingAnalyzer.getHttpMethod(method);
        Type returnType = method.getType();

        if ("POST".equals(httpMethod) && !returnType.isVoidType()) {
            return "201";
        }

        if ("DELETE".equals(httpMethod) && returnType.isVoidType()) {
            return "204";
        }

        return "200";
    }

    public String getReturnType(MethodDeclaration method) {
        Type returnType = method.getType();

        if (returnType.isVoidType()) {
            return "void";
        }

        return returnType.asString();
    }

    public String getActualReturnType(MethodDeclaration method) {
        Type returnType = method.getType();

        if (returnType.isVoidType()) {
            return "void";
        }

        String typeString = returnType.asString();

        if (typeExtractor.isResponseEntityType(typeString)) {
            Optional<String> genericType = typeExtractor.extractGenericType(typeString);
            if (genericType.isPresent()) {
                return genericType.get();
            }
        }

        if (typeExtractor.isOptionalType(typeString)) {
            Optional<String> genericType = typeExtractor.extractGenericType(typeString);
            if (genericType.isPresent()) {
                return genericType.get();
            }
        }

        return typeString;
    }

    public String getReturnApiType(MethodDeclaration method) {
        String actualType = getActualReturnType(method);
        return typeExtractor.javaTypeToApiType(actualType);
    }

    public boolean returnsCollection(MethodDeclaration method) {
        String actualType = getActualReturnType(method);
        return typeExtractor.isCollectionType(typeExtractor.getSimpleTypeName(actualType));
    }

    public Optional<String> getCollectionItemType(MethodDeclaration method) {
        String actualType = getActualReturnType(method);

        if (typeExtractor.isCollectionType(typeExtractor.getSimpleTypeName(actualType))) {
            return typeExtractor.extractGenericType(actualType);
        }

        return Optional.empty();
    }
}
