package io.github.mohmk10.changeloghub.parser.spring.analyzer;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import io.github.mohmk10.changeloghub.parser.spring.extractor.AnnotationExtractor;
import io.github.mohmk10.changeloghub.parser.spring.util.SpringAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Analyzer for Spring controller classes.
 */
public class ControllerAnalyzer {

    private final AnnotationExtractor annotationExtractor;
    private final RequestMappingAnalyzer requestMappingAnalyzer;

    public ControllerAnalyzer() {
        this.annotationExtractor = new AnnotationExtractor();
        this.requestMappingAnalyzer = new RequestMappingAnalyzer();
    }

    /**
     * Check if a class is a Spring controller.
     * A controller is annotated with @RestController or @Controller.
     */
    public boolean isController(ClassOrInterfaceDeclaration clazz) {
        return annotationExtractor.hasAnnotation(clazz, SpringAnnotations.REST_CONTROLLER) ||
               (annotationExtractor.hasAnnotation(clazz, SpringAnnotations.CONTROLLER) &&
                annotationExtractor.hasAnnotation(clazz, SpringAnnotations.RESPONSE_BODY));
    }

    /**
     * Check if a class is a REST controller (produces JSON/XML responses).
     */
    public boolean isRestController(ClassOrInterfaceDeclaration clazz) {
        return annotationExtractor.hasAnnotation(clazz, SpringAnnotations.REST_CONTROLLER);
    }

    /**
     * Get the base path from class-level @RequestMapping.
     */
    public String getBasePath(ClassOrInterfaceDeclaration clazz) {
        Optional<AnnotationExpr> requestMapping = annotationExtractor.getAnnotation(clazz, SpringAnnotations.REQUEST_MAPPING);

        if (requestMapping.isPresent()) {
            // Try 'value' attribute first, then 'path'
            Optional<String> value = annotationExtractor.getValueAttribute(requestMapping.get());
            if (value.isPresent()) {
                return value.get();
            }

            Optional<String> path = annotationExtractor.getStringAttribute(requestMapping.get(), SpringAnnotations.PATH);
            if (path.isPresent()) {
                return path.get();
            }

            // Check for array value
            List<String> values = annotationExtractor.getStringArrayAttribute(requestMapping.get(), SpringAnnotations.VALUE);
            if (!values.isEmpty()) {
                return values.get(0);
            }
        }

        return "";
    }

    /**
     * Get all endpoint methods from a controller.
     */
    public List<MethodDeclaration> getEndpointMethods(ClassOrInterfaceDeclaration clazz) {
        List<MethodDeclaration> endpoints = new ArrayList<>();

        for (MethodDeclaration method : clazz.getMethods()) {
            if (requestMappingAnalyzer.isEndpointMethod(method)) {
                endpoints.add(method);
            }
        }

        return endpoints;
    }

    /**
     * Check if the controller is deprecated.
     */
    public boolean isDeprecated(ClassOrInterfaceDeclaration clazz) {
        return annotationExtractor.hasAnnotation(clazz, SpringAnnotations.DEPRECATED);
    }

    /**
     * Get the produces media types from class-level @RequestMapping.
     */
    public List<String> getProduces(ClassOrInterfaceDeclaration clazz) {
        Optional<AnnotationExpr> requestMapping = annotationExtractor.getAnnotation(clazz, SpringAnnotations.REQUEST_MAPPING);

        if (requestMapping.isPresent()) {
            return annotationExtractor.getStringArrayAttribute(requestMapping.get(), SpringAnnotations.PRODUCES);
        }

        return new ArrayList<>();
    }

    /**
     * Get the consumes media types from class-level @RequestMapping.
     */
    public List<String> getConsumes(ClassOrInterfaceDeclaration clazz) {
        Optional<AnnotationExpr> requestMapping = annotationExtractor.getAnnotation(clazz, SpringAnnotations.REQUEST_MAPPING);

        if (requestMapping.isPresent()) {
            return annotationExtractor.getStringArrayAttribute(requestMapping.get(), SpringAnnotations.CONSUMES);
        }

        return new ArrayList<>();
    }

    /**
     * Get the controller class name.
     */
    public String getClassName(ClassOrInterfaceDeclaration clazz) {
        return clazz.getNameAsString();
    }
}
