package io.github.mohmk10.changeloghub.parser.grpc.analyzer;

import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoEnum;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoEnumValue;
import io.github.mohmk10.changeloghub.parser.grpc.util.ProtoConstants;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnumAnalyzer {

    private static final Pattern ENUM_VALUE_PATTERN = Pattern.compile(
            "^\\s*(\\w+)\\s*=\\s*(-?\\d+)\\s*(?:\\[([^\\]]+)\\])?\\s*;",
            Pattern.MULTILINE
    );

    private static final Pattern OPTION_PATTERN = Pattern.compile(
            "(\\w+)\\s*=\\s*([^,\\]]+)"
    );

    private static final Pattern ALLOW_ALIAS_PATTERN = Pattern.compile(
            "option\\s+allow_alias\\s*=\\s*true\\s*;"
    );

    public List<ProtoEnum> analyzeEnums(String content, String packageName) {
        List<ProtoEnum> enums = new ArrayList<>();

        if (content == null || content.isBlank()) {
            return enums;
        }

        String cleanContent = stripComments(content);

        Matcher enumMatcher = ProtoConstants.ENUM_PATTERN.matcher(cleanContent);
        while (enumMatcher.find()) {
            String enumName = enumMatcher.group(1);
            String enumBody = enumMatcher.group(2);

            ProtoEnum protoEnum = parseEnum(enumName, enumBody, packageName);
            enums.add(protoEnum);
        }

        return enums;
    }

    public ProtoEnum parseEnum(String name, String body, String packageName) {
        ProtoEnum.Builder builder = ProtoEnum.builder()
                .name(name);

        if (packageName != null && !packageName.isEmpty()) {
            builder.fullName(packageName + "." + name);
        }

        if (ALLOW_ALIAS_PATTERN.matcher(body).find()) {
            builder.allowAlias(true);
        }

        List<ProtoEnumValue> values = parseEnumValues(body);
        builder.values(values);

        Map<String, String> options = parseEnumOptions(body);
        builder.options(options);

        if (options.containsKey("deprecated") &&
            "true".equalsIgnoreCase(options.get("deprecated"))) {
            builder.deprecated(true);
        }

        return builder.build();
    }

    private List<ProtoEnumValue> parseEnumValues(String enumBody) {
        List<ProtoEnumValue> values = new ArrayList<>();

        Matcher valueMatcher = ENUM_VALUE_PATTERN.matcher(enumBody);
        while (valueMatcher.find()) {
            String name = valueMatcher.group(1);
            int number = Integer.parseInt(valueMatcher.group(2));
            String optionsStr = valueMatcher.group(3);

            ProtoEnumValue.Builder builder = ProtoEnumValue.builder()
                    .name(name)
                    .number(number);

            if (optionsStr != null && !optionsStr.isBlank()) {
                Map<String, String> options = parseOptions(optionsStr);
                builder.options(options);

                if (options.containsKey("deprecated") &&
                        "true".equalsIgnoreCase(options.get("deprecated"))) {
                    builder.deprecated(true);
                }
            }

            values.add(builder.build());
        }

        return values;
    }

    private Map<String, String> parseEnumOptions(String enumBody) {
        Map<String, String> options = new HashMap<>();

        Pattern enumOptionPattern = Pattern.compile(
                "option\\s+(\\w+)\\s*=\\s*([^;]+)\\s*;"
        );

        Matcher matcher = enumOptionPattern.matcher(enumBody);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2).trim();

            if (!"allow_alias".equals(key)) {
                
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                options.put(key, value);
            }
        }

        return options;
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

    public List<ProtoEnum> findNestedEnums(String messageBody, String parentFullName) {
        List<ProtoEnum> nestedEnums = new ArrayList<>();

        if (messageBody == null || messageBody.isBlank()) {
            return nestedEnums;
        }

        Matcher enumMatcher = ProtoConstants.ENUM_PATTERN.matcher(messageBody);
        while (enumMatcher.find()) {
            String enumName = enumMatcher.group(1);
            String enumBody = enumMatcher.group(2);

            String fullName = parentFullName + "." + enumName;
            ProtoEnum.Builder builder = ProtoEnum.builder()
                    .name(enumName)
                    .fullName(fullName);

            if (ALLOW_ALIAS_PATTERN.matcher(enumBody).find()) {
                builder.allowAlias(true);
            }

            List<ProtoEnumValue> values = parseEnumValues(enumBody);
            builder.values(values);

            nestedEnums.add(builder.build());
        }

        return nestedEnums;
    }

    private String stripComments(String content) {
        
        String result = content.replaceAll("/\\*[\\s\\S]*?\\*/", "");

        result = result.replaceAll("//[^\n]*", "");

        return result;
    }
}
