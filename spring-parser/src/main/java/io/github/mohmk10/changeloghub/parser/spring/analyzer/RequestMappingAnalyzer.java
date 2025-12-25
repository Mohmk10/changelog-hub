package io.github.mohmk10.changeloghub.parser.spring.analyzer;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import io.github.mohmk10.changeloghub.parser.spring.extractor.AnnotationExtractor;
import io.github.mohmk10.changeloghub.parser.spring.util.SpringAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Analyzer for Spring request mapping annotations on methods.
 */
public class RequestMappingAnalyzer {

    private final AnnotationExtractor annotationExtractor;

    public RequestMappingAnalyzer() {
        this.annotationExtractor = new AnnotationExtractor();
    }

    /**
     * Check if a method is an endpoint (has request mapping annotation).
     */
    public boolean isEndpointMethod(MethodDeclaration method) {
        return annotationExtractor.hasAnnotation(method, SpringAnnotations.REQUEST_MAPPING) ||
               annotationExtractor.hasAnnotation(method, SpringAnnotations.GET_MAPPING) ||
               annotationExtractor.hasAnnotation(method, SpringAnnotations.POST_MAPPING) ||
               annotationExtractor.hasAnnotation(method, SpringAnnotations.PUT_MAPPING) ||
               annotationExtractor.hasAnnotation(method, SpringAnnotations.DELETE_MAPPING) ||
               annotationExtractor.hasAnnotation(method, SpringAnnotations.PATCH_MAPPING);
    }

    /**
     * Get the HTTP method from the mapping annotation.
     */
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

        // For @RequestMapping, check the method attribute
        Optional<AnnotationExpr> requestMapping = annotationExtractor.getAnnotation(method, SpringAnnotations.REQUEST_MAPPING);
        if (requestMapping.isPresent()) {
            Optional<String> httpMethod = annotationExtractor.getHttpMethodAttribute(requestMapping.get());
            if (httpMethod.isPresent()) {
                return httpMethod.get();
            }
        }

        // Default to GET if no method specified
        return SpringAnnotations.HTTP_GET;
    }

    /**
     * Get the path from the mapping annotation.
     */
    public String getPath(MethodDeclaration method) {
        Optional<AnnotationExpr> mapping = getMappingAnnotation(method);

        if (mapping.isPresent()) {
            // Try 'value' attribute first
            Optional<String> value = annotationExtractor.getValueAttribute(mapping.get());
            if (value.isPresent()) {
                return value.get();
            }

            // Try 'path' attribute
            Optional<String> path = annotationExtractor.getStringAttribute(mapping.get(), SpringAnnotations.PATH);
            if (path.isPresent()) {
                return path.get();
            }

            // Check for array value
            List<String> values = annotationExtractor.getStringArrayAttribute(mapping.get(), SpringAnnotations.VALUE);
            if (!values.isEmpty()) {
                return values.get(0);
            }
        }

        return "";
    }

    /**
     * Get the operation ID (method name).
     */
    public String getOperationId(MethodDeclaration method) {
        return method.getNameAsString();
    }

    /**
     * Check if the method is deprecated.
     */
    public boolean isDeprecated(MethodDeclaration method) {
        return annotationExtractor.hasAnnotation(method, SpringAnnotations.DEPRECATED);
    }

    /**
     * Get the produces media types from the method annotation.
     */
    public List<String> getProduces(MethodDeclaration method) {
        Optional<AnnotationExpr> mapping = getMappingAnnotation(method);

        if (mapping.isPresent()) {
            return annotationExtractor.getStringArrayAttribute(mapping.get(), SpringAnnotations.PRODUCES);
        }

        return new ArrayList<>();
    }

    /**
     * Get the consumes media types from the method annotation.
     */
    public List<String> getConsumes(MethodDeclaration method) {
        Optional<AnnotationExpr> mapping = getMappingAnnotation(method);

        if (mapping.isPresent()) {
            return annotationExtractor.getStringArrayAttribute(mapping.get(), SpringAnnotations.CONSUMES);
        }

        return new ArrayList<>();
    }

    /**
     * Get the response status from @ResponseStatus annotation.
     */
    public Optional<String> getResponseStatus(MethodDeclaration method) {
        Optional<AnnotationExpr> responseStatus = annotationExtractor.getAnnotation(method, SpringAnnotations.RESPONSE_STATUS);

        if (responseStatus.isPresent()) {
            // Try 'value' or 'code' attribute
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
        // Check specific mapping annotations first
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
        // Handle HttpStatus enum values like CREATED, NO_CONTENT
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
                // If it's already a number, return as is
                if (statusValue.matches("\\d+")) {
                    return statusValue;
                }
                return "200";
        }
    }
}
