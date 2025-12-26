package io.github.mohmk10.changeloghub.parser.grpc.analyzer;

import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoField;
import io.github.mohmk10.changeloghub.parser.grpc.util.ProtoConstants;
import io.github.mohmk10.changeloghub.parser.grpc.util.ProtoFieldRule;
import io.github.mohmk10.changeloghub.parser.grpc.util.ProtoFieldType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldAnalyzer {

    private static final Pattern FIELD_PATTERN = Pattern.compile(
            "^\\s*(optional|required|repeated)?\\s*([\\w.<>,\\s]+)\\s+(\\w+)\\s*=\\s*(\\d+)\\s*(?:\\[([^\\]]+)\\])?\\s*;",
            Pattern.MULTILINE
    );

    private static final Pattern MAP_PATTERN = Pattern.compile(
            "map\\s*<\\s*(\\w+)\\s*,\\s*([\\w.]+)\\s*>"
    );

    private static final Pattern OPTION_PATTERN = Pattern.compile(
            "(\\w+)\\s*=\\s*([^,\\]]+)"
    );

    public List<ProtoField> analyzeFields(String messageBody) {
        List<ProtoField> fields = new ArrayList<>();

        if (messageBody == null || messageBody.isBlank()) {
            return fields;
        }

        String cleanedBody = removeNestedTypes(messageBody);

        Matcher fieldMatcher = FIELD_PATTERN.matcher(cleanedBody);
        while (fieldMatcher.find()) {
            String rule = fieldMatcher.group(1);
            String typeName = fieldMatcher.group(2).trim();
            String name = fieldMatcher.group(3);
            int number = Integer.parseInt(fieldMatcher.group(4));
            String optionsStr = fieldMatcher.group(5);

            ProtoField.Builder builder = ProtoField.builder()
                    .name(name)
                    .number(number)
                    .typeName(typeName)
                    .rule(ProtoFieldRule.fromString(rule));

            Matcher mapMatcher = MAP_PATTERN.matcher(typeName);
            if (mapMatcher.matches()) {
                builder.asMap(mapMatcher.group(1), mapMatcher.group(2));
            } else {
                builder.type(ProtoFieldType.fromString(typeName));
            }

            if (optionsStr != null && !optionsStr.isBlank()) {
                Map<String, String> options = parseOptions(optionsStr);
                builder.options(options);

                if (options.containsKey("deprecated") &&
                    "true".equalsIgnoreCase(options.get("deprecated"))) {
                    builder.deprecated(true);
                }

                if (options.containsKey("default")) {
                    builder.defaultValue(options.get("default"));
                }
            }

            fields.add(builder.build());
        }

        fields.addAll(parseOneofFields(messageBody));

        return fields;
    }

    private List<ProtoField> parseOneofFields(String messageBody) {
        List<ProtoField> fields = new ArrayList<>();

        Matcher oneofMatcher = ProtoConstants.ONEOF_PATTERN.matcher(messageBody);
        while (oneofMatcher.find()) {
            String oneofName = oneofMatcher.group(1);
            String oneofBody = oneofMatcher.group(2);

            Pattern oneofFieldPattern = Pattern.compile(
                    "^\\s*([\\w.]+)\\s+(\\w+)\\s*=\\s*(\\d+)\\s*(?:\\[([^\\]]+)\\])?\\s*;",
                    Pattern.MULTILINE
            );

            Matcher fieldMatcher = oneofFieldPattern.matcher(oneofBody);
            while (fieldMatcher.find()) {
                String typeName = fieldMatcher.group(1);
                String name = fieldMatcher.group(2);
                int number = Integer.parseInt(fieldMatcher.group(3));
                String optionsStr = fieldMatcher.group(4);

                ProtoField.Builder builder = ProtoField.builder()
                        .name(name)
                        .number(number)
                        .typeName(typeName)
                        .type(ProtoFieldType.fromString(typeName))
                        .rule(ProtoFieldRule.OPTIONAL)
                        .oneofName(oneofName);

                if (optionsStr != null && !optionsStr.isBlank()) {
                    Map<String, String> options = parseOptions(optionsStr);
                    builder.options(options);

                    if (options.containsKey("deprecated") &&
                            "true".equalsIgnoreCase(options.get("deprecated"))) {
                        builder.deprecated(true);
                    }
                }

                fields.add(builder.build());
            }
        }

        return fields;
    }

    private Map<String, String> parseOptions(String optionsStr) {
        Map<String, String> options = new HashMap<>();

        Matcher optionMatcher = OPTION_PATTERN.matcher(optionsStr);
        while (optionMatcher.find()) {
            String key = optionMatcher.group(1).trim();
            String value = optionMatcher.group(2).trim();
            
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            options.put(key, value);
        }

        return options;
    }

    private String removeNestedTypes(String body) {
        
        StringBuilder result = new StringBuilder();
        int braceDepth = 0;
        boolean inNestedType = false;
        String[] lines = body.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();

            if (!inNestedType &&
                (trimmed.startsWith("message ") || trimmed.startsWith("enum ")) &&
                trimmed.contains("{")) {
                inNestedType = true;
                braceDepth = 1;
                continue;
            }

            if (inNestedType) {
                for (char c : trimmed.toCharArray()) {
                    if (c == '{') braceDepth++;
                    else if (c == '}') braceDepth--;
                }
                if (braceDepth == 0) {
                    inNestedType = false;
                }
                continue;
            }

            if (trimmed.startsWith("oneof ")) {
                inNestedType = true;
                braceDepth = 1;
                continue;
            }

            result.append(line).append("\n");
        }

        return result.toString();
    }

    public List<String> getOneofNames(String messageBody) {
        List<String> names = new ArrayList<>();

        if (messageBody == null || messageBody.isBlank()) {
            return names;
        }

        Matcher matcher = ProtoConstants.ONEOF_PATTERN.matcher(messageBody);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }

        return names;
    }

    public Set<Integer> parseReservedNumbers(String messageBody) {
        Set<Integer> reserved = new LinkedHashSet<>();

        if (messageBody == null || messageBody.isBlank()) {
            return reserved;
        }

        Pattern reservedPattern = Pattern.compile(
                "reserved\\s+(\\d+(?:\\s*,\\s*\\d+|\\s+to\\s+\\d+)*)\\s*;",
                Pattern.MULTILINE
        );

        Matcher matcher = reservedPattern.matcher(messageBody);
        while (matcher.find()) {
            String reservedStr = matcher.group(1);

            if (reservedStr.contains("to")) {
                String[] parts = reservedStr.split("\\s+to\\s+");
                int start = Integer.parseInt(parts[0].trim());
                int end = Integer.parseInt(parts[1].trim());
                for (int i = start; i <= end; i++) {
                    reserved.add(i);
                }
            } else {
                
                String[] numbers = reservedStr.split("\\s*,\\s*");
                for (String num : numbers) {
                    reserved.add(Integer.parseInt(num.trim()));
                }
            }
        }

        return reserved;
    }

    public Set<String> parseReservedNames(String messageBody) {
        Set<String> reserved = new LinkedHashSet<>();

        if (messageBody == null || messageBody.isBlank()) {
            return reserved;
        }

        Pattern reservedPattern = Pattern.compile(
                "reserved\\s+\"([^\"]+)\"(?:\\s*,\\s*\"([^\"]+)\")*\\s*;",
                Pattern.MULTILINE
        );

        Matcher matcher = reservedPattern.matcher(messageBody);
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String name = matcher.group(i);
                if (name != null) {
                    reserved.add(name);
                }
            }
        }

        return reserved;
    }
}
