package io.github.mohmk10.changeloghub.parser.grpc.analyzer;

import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoEnum;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoField;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoMessage;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Analyzer for parsing Protocol Buffers message definitions.
 */
public class MessageAnalyzer {

    private final FieldAnalyzer fieldAnalyzer;
    private final EnumAnalyzer enumAnalyzer;

    // Pattern to match message blocks with nested content handling
    private static final Pattern MESSAGE_START_PATTERN = Pattern.compile(
            "message\\s+(\\w+)\\s*\\{"
    );

    private static final Pattern DEPRECATED_OPTION_PATTERN = Pattern.compile(
            "option\\s+deprecated\\s*=\\s*true\\s*;"
    );

    public MessageAnalyzer() {
        this.fieldAnalyzer = new FieldAnalyzer();
        this.enumAnalyzer = new EnumAnalyzer();
    }

    public MessageAnalyzer(FieldAnalyzer fieldAnalyzer, EnumAnalyzer enumAnalyzer) {
        this.fieldAnalyzer = fieldAnalyzer;
        this.enumAnalyzer = enumAnalyzer;
    }

    /**
     * Parse all top-level messages from proto content.
     */
    public List<ProtoMessage> analyzeMessages(String content, String packageName) {
        List<ProtoMessage> messages = new ArrayList<>();

        if (content == null || content.isBlank()) {
            return messages;
        }

        // Strip comments before parsing
        String cleanContent = stripComments(content);

        // Find all top-level message blocks
        List<MessageBlock> messageBlocks = findMessageBlocks(cleanContent);

        for (MessageBlock block : messageBlocks) {
            ProtoMessage message = parseMessage(block.name, block.body, packageName);
            messages.add(message);
        }

        return messages;
    }

    /**
     * Parse a single message definition.
     */
    public ProtoMessage parseMessage(String name, String body, String packageName) {
        ProtoMessage.Builder builder = ProtoMessage.builder()
                .name(name);

        // Set full name
        String fullName = (packageName != null && !packageName.isEmpty())
                ? packageName + "." + name
                : name;
        builder.fullName(fullName);

        // Parse fields
        List<ProtoField> fields = fieldAnalyzer.analyzeFields(body);
        builder.fields(fields);

        // Parse oneof names
        List<String> oneofNames = fieldAnalyzer.getOneofNames(body);
        builder.oneofNames(oneofNames);

        // Parse reserved numbers and names
        builder.reservedNumbers(fieldAnalyzer.parseReservedNumbers(body));
        builder.reservedNames(fieldAnalyzer.parseReservedNames(body));

        // Parse nested messages
        List<ProtoMessage> nestedMessages = findNestedMessages(body, fullName);
        builder.nestedMessages(nestedMessages);

        // Parse nested enums
        List<ProtoEnum> nestedEnums = enumAnalyzer.findNestedEnums(body, fullName);
        builder.nestedEnums(nestedEnums);

        // Check for deprecated option
        if (DEPRECATED_OPTION_PATTERN.matcher(body).find()) {
            builder.deprecated(true);
        }

        // Parse message-level options
        Map<String, String> options = parseMessageOptions(body);
        builder.options(options);

        return builder.build();
    }

    /**
     * Find message blocks handling nested braces.
     */
    private List<MessageBlock> findMessageBlocks(String content) {
        List<MessageBlock> blocks = new ArrayList<>();

        Matcher startMatcher = MESSAGE_START_PATTERN.matcher(content);
        while (startMatcher.find()) {
            String messageName = startMatcher.group(1);
            int startIndex = startMatcher.end() - 1; // Position of opening brace

            // Find the matching closing brace
            int braceCount = 1;
            int endIndex = startIndex + 1;

            while (endIndex < content.length() && braceCount > 0) {
                char c = content.charAt(endIndex);
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                }
                endIndex++;
            }

            if (braceCount == 0) {
                // Extract the body (between braces)
                String body = content.substring(startIndex + 1, endIndex - 1);
                blocks.add(new MessageBlock(messageName, body));
            }
        }

        // Filter out nested messages (only keep top-level)
        return filterTopLevelMessages(blocks, content);
    }

    /**
     * Filter to keep only top-level message blocks.
     */
    private List<MessageBlock> filterTopLevelMessages(List<MessageBlock> blocks, String content) {
        List<MessageBlock> topLevel = new ArrayList<>();

        for (MessageBlock block : blocks) {
            // Check if this message is at top level by looking at the position
            String searchStr = "message " + block.name + " {";
            int pos = content.indexOf(searchStr);

            if (pos >= 0) {
                // Count braces before this position
                int braceCount = 0;
                for (int i = 0; i < pos; i++) {
                    char c = content.charAt(i);
                    if (c == '{') braceCount++;
                    else if (c == '}') braceCount--;
                }

                // If brace count is 0, it's a top-level message
                if (braceCount == 0) {
                    topLevel.add(block);
                }
            }
        }

        return topLevel;
    }

    /**
     * Find nested messages within a message body.
     */
    private List<ProtoMessage> findNestedMessages(String messageBody, String parentFullName) {
        List<ProtoMessage> nestedMessages = new ArrayList<>();

        List<MessageBlock> nestedBlocks = findMessageBlocks(messageBody);

        for (MessageBlock block : nestedBlocks) {
            String nestedFullName = parentFullName + "." + block.name;

            ProtoMessage.Builder builder = ProtoMessage.builder()
                    .name(block.name)
                    .fullName(nestedFullName);

            // Parse fields for nested message
            List<ProtoField> fields = fieldAnalyzer.analyzeFields(block.body);
            builder.fields(fields);

            // Parse oneof names
            List<String> oneofNames = fieldAnalyzer.getOneofNames(block.body);
            builder.oneofNames(oneofNames);

            // Recursively find nested messages
            List<ProtoMessage> deepNested = findNestedMessages(block.body, nestedFullName);
            builder.nestedMessages(deepNested);

            // Find nested enums
            List<ProtoEnum> nestedEnums = enumAnalyzer.findNestedEnums(block.body, nestedFullName);
            builder.nestedEnums(nestedEnums);

            nestedMessages.add(builder.build());
        }

        return nestedMessages;
    }

    /**
     * Parse message-level options.
     */
    private Map<String, String> parseMessageOptions(String messageBody) {
        Map<String, String> options = new HashMap<>();

        Pattern optionPattern = Pattern.compile(
                "option\\s+(\\w+)\\s*=\\s*([^;]+)\\s*;"
        );

        Matcher matcher = optionPattern.matcher(messageBody);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2).trim();

            // Remove quotes from string values
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            options.put(key, value);
        }

        return options;
    }

    /**
     * Strip comments from proto content.
     */
    private String stripComments(String content) {
        // Remove multi-line comments /* ... */
        String result = content.replaceAll("/\\*[\\s\\S]*?\\*/", "");

        // Remove single-line comments // ...
        result = result.replaceAll("//[^\n]*", "");

        return result;
    }

    /**
     * Internal class to hold message block data.
     */
    private static class MessageBlock {
        final String name;
        final String body;

        MessageBlock(String name, String body) {
            this.name = name;
            this.body = body;
        }
    }
}
