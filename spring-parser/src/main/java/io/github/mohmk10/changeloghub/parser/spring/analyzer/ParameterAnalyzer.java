package io.github.mohmk10.changeloghub.parser.spring.analyzer;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import io.github.mohmk10.changeloghub.parser.spring.extractor.AnnotationExtractor;
import io.github.mohmk10.changeloghub.parser.spring.model.SpringParameter;
import io.github.mohmk10.changeloghub.parser.spring.util.SpringAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Analyzer for Spring controller method parameters.
 */
public class ParameterAnalyzer {

    private final AnnotationExtractor annotationExtractor;

    public ParameterAnalyzer() {
        this.annotationExtractor = new AnnotationExtractor();
    }

    /**
     * Analyze all parameters of a method.
     */
    public List<SpringParameter> analyzeParameters(MethodDeclaration method) {
        List<SpringParameter> result = new ArrayList<>();

        for (Parameter param : method.getParameters()) {
            analyzeParameter(param).ifPresent(result::add);
        }

        return result;
    }

    /**
     * Analyze a single parameter.
     * Returns empty if the parameter has no Spring annotation.
     */
    public Optional<SpringParameter> analyzeParameter(Parameter param) {
        SpringParameter.Location location = getLocation(param);

        // If no Spring annotation, skip this parameter (it might be injected by Spring)
        if (location == null) {
            return Optional.empty();
        }

        SpringParameter springParam = new SpringParameter();
        springParam.setLocation(location);
        springParam.setName(getParameterName(param));
        springParam.setJavaType(param.getTypeAsString());
        springParam.setRequired(isRequired(param));
        springParam.setDefaultValue(getDefaultValue(param).orElse(null));

        return Optional.of(springParam);
    }

    /**
     * Get the parameter location based on annotations.
     */
    public SpringParameter.Location getLocation(Parameter param) {
        if (annotationExtractor.hasAnnotation(param, SpringAnnotations.PATH_VARIABLE)) {
            return SpringParameter.Location.PATH;
        }
        if (annotationExtractor.hasAnnotation(param, SpringAnnotations.REQUEST_PARAM)) {
            return SpringParameter.Location.QUERY;
        }
        if (annotationExtractor.hasAnnotation(param, SpringAnnotations.REQUEST_HEADER)) {
            return SpringParameter.Location.HEADER;
        }
        if (annotationExtractor.hasAnnotation(param, SpringAnnotations.COOKIE_VALUE)) {
            return SpringParameter.Location.COOKIE;
        }
        if (annotationExtractor.hasAnnotation(param, SpringAnnotations.REQUEST_BODY)) {
            return SpringParameter.Location.BODY;
        }
        return null;
    }

    /**
     * Get the parameter name from annotation or parameter name.
     */
    public String getParameterName(Parameter param) {
        // Check annotations for explicit name
        for (String annotationName : List.of(
                SpringAnnotations.PATH_VARIABLE,
                SpringAnnotations.REQUEST_PARAM,
                SpringAnnotations.REQUEST_HEADER,
                SpringAnnotations.COOKIE_VALUE)) {

            Optional<AnnotationExpr> annotation = annotationExtractor.getAnnotation(param, annotationName);
            if (annotation.isPresent()) {
                // Try 'value' attribute
                Optional<String> value = annotationExtractor.getValueAttribute(annotation.get());
                if (value.isPresent() && !value.get().isEmpty()) {
                    return value.get();
                }

                // Try 'name' attribute
                Optional<String> name = annotationExtractor.getStringAttribute(annotation.get(), SpringAnnotations.NAME);
                if (name.isPresent() && !name.get().isEmpty()) {
                    return name.get();
                }
            }
        }

        // Fall back to parameter name
        return param.getNameAsString();
    }

    /**
     * Check if the parameter is required.
     */
    public boolean isRequired(Parameter param) {
        // @RequestBody is required by default
        if (annotationExtractor.hasAnnotation(param, SpringAnnotations.REQUEST_BODY)) {
            Optional<AnnotationExpr> annotation = annotationExtractor.getAnnotation(param, SpringAnnotations.REQUEST_BODY);
            if (annotation.isPresent()) {
                Optional<Boolean> required = annotationExtractor.getBooleanAttribute(annotation.get(), SpringAnnotations.REQUIRED);
                return required.orElse(true);
            }
            return true;
        }

        // @PathVariable is required by default
        if (annotationExtractor.hasAnnotation(param, SpringAnnotations.PATH_VARIABLE)) {
            Optional<AnnotationExpr> annotation = annotationExtractor.getAnnotation(param, SpringAnnotations.PATH_VARIABLE);
            if (annotation.isPresent()) {
                Optional<Boolean> required = annotationExtractor.getBooleanAttribute(annotation.get(), SpringAnnotations.REQUIRED);
                return required.orElse(true);
            }
            return true;
        }

        // Check other annotations
        for (String annotationName : List.of(
                SpringAnnotations.REQUEST_PARAM,
                SpringAnnotations.REQUEST_HEADER,
                SpringAnnotations.COOKIE_VALUE)) {

            Optional<AnnotationExpr> annotation = annotationExtractor.getAnnotation(param, annotationName);
            if (annotation.isPresent()) {
                Optional<Boolean> required = annotationExtractor.getBooleanAttribute(annotation.get(), SpringAnnotations.REQUIRED);
                // @RequestParam defaults to required=true, but if defaultValue is set, it's optional
                if (required.isEmpty()) {
                    // Check if defaultValue is set
                    Optional<String> defaultValue = annotationExtractor.getStringAttribute(annotation.get(), SpringAnnotations.DEFAULT_VALUE);
                    return defaultValue.isEmpty();
                }
                return required.get();
            }
        }

        return true;
    }

    /**
     * Get the default value for the parameter.
     */
    public Optional<String> getDefaultValue(Parameter param) {
        for (String annotationName : List.of(
                SpringAnnotations.REQUEST_PARAM,
                SpringAnnotations.REQUEST_HEADER,
                SpringAnnotations.COOKIE_VALUE)) {

            Optional<AnnotationExpr> annotation = annotationExtractor.getAnnotation(param, annotationName);
            if (annotation.isPresent()) {
                return annotationExtractor.getStringAttribute(annotation.get(), SpringAnnotations.DEFAULT_VALUE);
            }
        }

        return Optional.empty();
    }
}
