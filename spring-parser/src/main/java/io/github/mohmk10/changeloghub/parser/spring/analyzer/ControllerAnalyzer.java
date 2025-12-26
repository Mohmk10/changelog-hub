package io.github.mohmk10.changeloghub.parser.spring.analyzer;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import io.github.mohmk10.changeloghub.parser.spring.extractor.AnnotationExtractor;
import io.github.mohmk10.changeloghub.parser.spring.util.SpringAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ControllerAnalyzer {

    private final AnnotationExtractor annotationExtractor;
    private final RequestMappingAnalyzer requestMappingAnalyzer;

    public ControllerAnalyzer() {
        this.annotationExtractor = new AnnotationExtractor();
        this.requestMappingAnalyzer = new RequestMappingAnalyzer();
    }

    public boolean isController(ClassOrInterfaceDeclaration clazz) {
        return annotationExtractor.hasAnnotation(clazz, SpringAnnotations.REST_CONTROLLER) ||
               (annotationExtractor.hasAnnotation(clazz, SpringAnnotations.CONTROLLER) &&
                annotationExtractor.hasAnnotation(clazz, SpringAnnotations.RESPONSE_BODY));
    }

    public boolean isRestController(ClassOrInterfaceDeclaration clazz) {
        return annotationExtractor.hasAnnotation(clazz, SpringAnnotations.REST_CONTROLLER);
    }

    public String getBasePath(ClassOrInterfaceDeclaration clazz) {
        Optional<AnnotationExpr> requestMapping = annotationExtractor.getAnnotation(clazz, SpringAnnotations.REQUEST_MAPPING);

        if (requestMapping.isPresent()) {
            
            Optional<String> value = annotationExtractor.getValueAttribute(requestMapping.get());
            if (value.isPresent()) {
                return value.get();
            }

            Optional<String> path = annotationExtractor.getStringAttribute(requestMapping.get(), SpringAnnotations.PATH);
            if (path.isPresent()) {
                return path.get();
            }

            List<String> values = annotationExtractor.getStringArrayAttribute(requestMapping.get(), SpringAnnotations.VALUE);
            if (!values.isEmpty()) {
                return values.get(0);
            }
        }

        return "";
    }

    public List<MethodDeclaration> getEndpointMethods(ClassOrInterfaceDeclaration clazz) {
        List<MethodDeclaration> endpoints = new ArrayList<>();

        for (MethodDeclaration method : clazz.getMethods()) {
            if (requestMappingAnalyzer.isEndpointMethod(method)) {
                endpoints.add(method);
            }
        }

        return endpoints;
    }

    public boolean isDeprecated(ClassOrInterfaceDeclaration clazz) {
        return annotationExtractor.hasAnnotation(clazz, SpringAnnotations.DEPRECATED);
    }

    public List<String> getProduces(ClassOrInterfaceDeclaration clazz) {
        Optional<AnnotationExpr> requestMapping = annotationExtractor.getAnnotation(clazz, SpringAnnotations.REQUEST_MAPPING);

        if (requestMapping.isPresent()) {
            return annotationExtractor.getStringArrayAttribute(requestMapping.get(), SpringAnnotations.PRODUCES);
        }

        return new ArrayList<>();
    }

    public List<String> getConsumes(ClassOrInterfaceDeclaration clazz) {
        Optional<AnnotationExpr> requestMapping = annotationExtractor.getAnnotation(clazz, SpringAnnotations.REQUEST_MAPPING);

        if (requestMapping.isPresent()) {
            return annotationExtractor.getStringArrayAttribute(requestMapping.get(), SpringAnnotations.CONSUMES);
        }

        return new ArrayList<>();
    }

    public String getClassName(ClassOrInterfaceDeclaration clazz) {
        return clazz.getNameAsString();
    }
}
