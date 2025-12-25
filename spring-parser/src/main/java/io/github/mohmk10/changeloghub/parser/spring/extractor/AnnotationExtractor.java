package io.github.mohmk10.changeloghub.parser.spring.extractor;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utility class to extract values from annotations.
 */
public class AnnotationExtractor {

    /**
     * Check if a node has a specific annotation.
     */
    public boolean hasAnnotation(NodeWithAnnotations<?> node, String annotationName) {
        return node.getAnnotationByName(annotationName).isPresent();
    }

    /**
     * Get an annotation by name.
     */
    public Optional<AnnotationExpr> getAnnotation(NodeWithAnnotations<?> node, String annotationName) {
        return node.getAnnotationByName(annotationName);
    }

    /**
     * Get the value attribute from an annotation.
     * Handles both @Annotation("value") and @Annotation(value = "value")
     */
    public Optional<String> getValueAttribute(AnnotationExpr annotation) {
        return getStringAttribute(annotation, "value");
    }

    /**
     * Get a string attribute from an annotation.
     */
    public Optional<String> getStringAttribute(AnnotationExpr annotation, String attributeName) {
        if (annotation.isSingleMemberAnnotationExpr()) {
            SingleMemberAnnotationExpr single = annotation.asSingleMemberAnnotationExpr();
            if ("value".equals(attributeName)) {
                return extractStringValue(single.getMemberValue());
            }
        } else if (annotation.isNormalAnnotationExpr()) {
            NormalAnnotationExpr normal = annotation.asNormalAnnotationExpr();
            for (MemberValuePair pair : normal.getPairs()) {
                if (pair.getNameAsString().equals(attributeName)) {
                    return extractStringValue(pair.getValue());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Get a boolean attribute from an annotation.
     */
    public Optional<Boolean> getBooleanAttribute(AnnotationExpr annotation, String attributeName) {
        if (annotation.isNormalAnnotationExpr()) {
            NormalAnnotationExpr normal = annotation.asNormalAnnotationExpr();
            for (MemberValuePair pair : normal.getPairs()) {
                if (pair.getNameAsString().equals(attributeName)) {
                    return extractBooleanValue(pair.getValue());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Get a list of string values from an annotation attribute.
     * Handles both single value and array of values.
     */
    public List<String> getStringArrayAttribute(AnnotationExpr annotation, String attributeName) {
        List<String> result = new ArrayList<>();

        if (annotation.isSingleMemberAnnotationExpr() && "value".equals(attributeName)) {
            SingleMemberAnnotationExpr single = annotation.asSingleMemberAnnotationExpr();
            extractStringValues(single.getMemberValue(), result);
        } else if (annotation.isNormalAnnotationExpr()) {
            NormalAnnotationExpr normal = annotation.asNormalAnnotationExpr();
            for (MemberValuePair pair : normal.getPairs()) {
                if (pair.getNameAsString().equals(attributeName)) {
                    extractStringValues(pair.getValue(), result);
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Extract HTTP method from RequestMapping annotation.
     * Handles method = RequestMethod.GET syntax.
     */
    public Optional<String> getHttpMethodAttribute(AnnotationExpr annotation) {
        if (annotation.isNormalAnnotationExpr()) {
            NormalAnnotationExpr normal = annotation.asNormalAnnotationExpr();
            for (MemberValuePair pair : normal.getPairs()) {
                if ("method".equals(pair.getNameAsString())) {
                    return extractEnumValue(pair.getValue());
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> extractStringValue(Expression expr) {
        if (expr.isStringLiteralExpr()) {
            return Optional.of(expr.asStringLiteralExpr().getValue());
        } else if (expr.isNameExpr()) {
            // Handle constant references
            return Optional.of(expr.asNameExpr().getNameAsString());
        } else if (expr.isFieldAccessExpr()) {
            // Handle MediaType.APPLICATION_JSON_VALUE etc.
            return Optional.of(expr.asFieldAccessExpr().getNameAsString());
        }
        return Optional.empty();
    }

    private Optional<Boolean> extractBooleanValue(Expression expr) {
        if (expr.isBooleanLiteralExpr()) {
            return Optional.of(expr.asBooleanLiteralExpr().getValue());
        }
        return Optional.empty();
    }

    private Optional<String> extractEnumValue(Expression expr) {
        if (expr.isFieldAccessExpr()) {
            // RequestMethod.GET -> GET
            return Optional.of(expr.asFieldAccessExpr().getNameAsString());
        } else if (expr.isNameExpr()) {
            return Optional.of(expr.asNameExpr().getNameAsString());
        } else if (expr.isArrayInitializerExpr()) {
            // Handle method = {RequestMethod.GET, RequestMethod.POST}
            NodeList<Expression> values = expr.asArrayInitializerExpr().getValues();
            if (!values.isEmpty()) {
                return extractEnumValue(values.get(0));
            }
        }
        return Optional.empty();
    }

    private void extractStringValues(Expression expr, List<String> result) {
        if (expr.isStringLiteralExpr()) {
            result.add(expr.asStringLiteralExpr().getValue());
        } else if (expr.isArrayInitializerExpr()) {
            for (Expression value : expr.asArrayInitializerExpr().getValues()) {
                extractStringValue(value).ifPresent(result::add);
            }
        } else if (expr.isFieldAccessExpr()) {
            result.add(expr.asFieldAccessExpr().getNameAsString());
        }
    }
}
