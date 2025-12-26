package io.github.mohmk10.changeloghub.parser.spring.extractor;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AnnotationExtractor {

    public boolean hasAnnotation(NodeWithAnnotations<?> node, String annotationName) {
        return node.getAnnotationByName(annotationName).isPresent();
    }

    public Optional<AnnotationExpr> getAnnotation(NodeWithAnnotations<?> node, String annotationName) {
        return node.getAnnotationByName(annotationName);
    }

    public Optional<String> getValueAttribute(AnnotationExpr annotation) {
        return getStringAttribute(annotation, "value");
    }

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
            
            return Optional.of(expr.asNameExpr().getNameAsString());
        } else if (expr.isFieldAccessExpr()) {
            
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
            
            return Optional.of(expr.asFieldAccessExpr().getNameAsString());
        } else if (expr.isNameExpr()) {
            return Optional.of(expr.asNameExpr().getNameAsString());
        } else if (expr.isArrayInitializerExpr()) {
            
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
