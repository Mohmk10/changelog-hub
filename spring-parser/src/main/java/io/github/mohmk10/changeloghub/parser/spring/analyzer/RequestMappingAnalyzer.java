package io.github.mohmk10.changeloghub.parser.spring.analyzer;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import io.github.mohmk10.changeloghub.parser.spring.extractor.AnnotationExtractor;
import io.github.mohmk10.changeloghub.parser.spring.util.SpringAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RequestMappingAnalyzer {

    private final AnnotationExtractor annotationExtractor;

    public RequestMappingAnalyzer() {
        this.annotationExtractor = new AnnotationExtractor();
    }

    public boolean isEndpointMethod(MethodDeclaration method) {
        return annotationExtractor.hasAnnotation(method, SpringAnnotations.REQUEST_MAPPING) ||
               annotationExtractor.hasAnnotation(method, SpringAnnotations.GET_MAPPING) ||
               annotationExtractor.hasAnnotation(method, SpringAnnotations.POST_MAPPING) ||
               annotationExtractor.hasAnnotation(method, SpringAnnotations.PUT_MAPPING) ||
               annotationExtractor.hasAnnotation(method, SpringAnnotations.DELETE_MAPPING) ||
               annotationExtractor.hasAnnotation(method, SpringAnnotations.PATCH_MAPPING);
    }

    public String getHttpMethod(MethodDeclaration method) {
        if (annotationExtractor.hasAnnotation(method, SpringAnnotations.GET_MAPPING)) {
            return SpringAnnotations.HTTP_GET;
        }
        if (annotationExtractor.hasAnnotation(method, SpringAnnotations.POST_MAPPING)) {
            return SpringAnnotations.HTTP_POST;
        }
        if (annotationExtractor.hasAnnotation(method, SpringAnnotations.PUT_MAPPING)) {
            return SpringAnnotations.HTTP_PUT;
        }
        if (annotationExtractor.hasAnnotation(method, SpringAnnotations.DELETE_MAPPING)) {
            return SpringAnnotations.HTTP_DELETE;
        }
        if (annotationExtractor.hasAnnotation(method, SpringAnnotations.PATCH_MAPPING)) {
            return SpringAnnotations.HTTP_PATCH;
        }

        Optional<AnnotationExpr> requestMapping = annotationExtractor.getAnnotation(method, SpringAnnotations.REQUEST_MAPPING);
        if (requestMapping.isPresent()) {
            Optional<String> httpMethod = annotationExtractor.getHttpMethodAttribute(requestMapping.get());
            if (httpMethod.isPresent()) {
                return httpMethod.get();
            }
        }

        return SpringAnnotations.HTTP_GET;
    }

    public String getPath(MethodDeclaration method) {
        Optional<AnnotationExpr> mapping = getMappingAnnotation(method);

        if (mapping.isPresent()) {
            
            Optional<String> value = annotationExtractor.getValueAttribute(mapping.get());
            if (value.isPresent()) {
                return value.get();
            }

            Optional<String> path = annotationExtractor.getStringAttribute(mapping.get(), SpringAnnotations.PATH);
            if (path.isPresent()) {
                return path.get();
            }

            List<String> values = annotationExtractor.getStringArrayAttribute(mapping.get(), SpringAnnotations.VALUE);
            if (!values.isEmpty()) {
                return values.get(0);
            }
        }

        return "";
    }

    public String getOperationId(MethodDeclaration method) {
        return method.getNameAsString();
    }

    public boolean isDeprecated(MethodDeclaration method) {
        return annotationExtractor.hasAnnotation(method, SpringAnnotations.DEPRECATED);
    }

    public List<String> getProduces(MethodDeclaration method) {
        Optional<AnnotationExpr> mapping = getMappingAnnotation(method);

        if (mapping.isPresent()) {
            return annotationExtractor.getStringArrayAttribute(mapping.get(), SpringAnnotations.PRODUCES);
        }

        return new ArrayList<>();
    }

    public List<String> getConsumes(MethodDeclaration method) {
        Optional<AnnotationExpr> mapping = getMappingAnnotation(method);

        if (mapping.isPresent()) {
            return annotationExtractor.getStringArrayAttribute(mapping.get(), SpringAnnotations.CONSUMES);
        }

        return new ArrayList<>();
    }

    public Optional<String> getResponseStatus(MethodDeclaration method) {
        Optional<AnnotationExpr> responseStatus = annotationExtractor.getAnnotation(method, SpringAnnotations.RESPONSE_STATUS);

        if (responseStatus.isPresent()) {
            
            Optional<String> value = annotationExtractor.getValueAttribute(responseStatus.get());
            if (value.isPresent()) {
                return Optional.of(extractStatusCode(value.get()));
            }

            Optional<String> code = annotationExtractor.getStringAttribute(responseStatus.get(), SpringAnnotations.CODE);
            if (code.isPresent()) {
                return Optional.of(extractStatusCode(code.get()));
            }
        }

        return Optional.empty();
    }

    private Optional<AnnotationExpr> getMappingAnnotation(MethodDeclaration method) {
        
        for (String annotationName : List.of(
                SpringAnnotations.GET_MAPPING,
                SpringAnnotations.POST_MAPPING,
                SpringAnnotations.PUT_MAPPING,
                SpringAnnotations.DELETE_MAPPING,
                SpringAnnotations.PATCH_MAPPING,
                SpringAnnotations.REQUEST_MAPPING)) {

            Optional<AnnotationExpr> annotation = annotationExtractor.getAnnotation(method, annotationName);
            if (annotation.isPresent()) {
                return annotation;
            }
        }

        return Optional.empty();
    }

    private String extractStatusCode(String statusValue) {
        
        switch (statusValue.toUpperCase()) {
            case "OK":
                return "200";
            case "CREATED":
                return "201";
            case "ACCEPTED":
                return "202";
            case "NO_CONTENT":
                return "204";
            case "BAD_REQUEST":
                return "400";
            case "UNAUTHORIZED":
                return "401";
            case "FORBIDDEN":
                return "403";
            case "NOT_FOUND":
                return "404";
            case "CONFLICT":
                return "409";
            case "INTERNAL_SERVER_ERROR":
                return "500";
            default:
                
                if (statusValue.matches("\\d+")) {
                    return statusValue;
                }
                return "200";
        }
    }
}
