package io.github.mohmk10.changeloghub.parser.grpc.analyzer;

import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoEnum;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoField;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoMessage;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageAnalyzer {

    private final FieldAnalyzer fieldAnalyzer;
    private final EnumAnalyzer enumAnalyzer;

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

    public List<ProtoMessage> analyzeMessages(String content, String packageName) {
        List<ProtoMessage> messages = new ArrayList<>();

        if (content == null || content.isBlank()) {
            return messages;
        }

        String cleanContent = stripComments(content);

        List<MessageBlock> messageBlocks = findMessageBlocks(cleanContent);

        for (MessageBlock block : messageBlocks) {
            ProtoMessage message = parseMessage(block.name, block.body, packageName);
            messages.add(message);
        }

        return messages;
    }

    public ProtoMessage parseMessage(String name, String body, String packageName) {
        ProtoMessage.Builder builder = ProtoMessage.builder()
                .name(name);

        String fullName = (packageName != null && !packageName.isEmpty())
                ? packageName + "." + name
                : name;
        builder.fullName(fullName);

        List<ProtoField> fields = fieldAnalyzer.analyzeFields(body);
        builder.fields(fields);

        List<String> oneofNames = fieldAnalyzer.getOneofNames(body);
        builder.oneofNames(oneofNames);

        builder.reservedNumbers(fieldAnalyzer.parseReservedNumbers(body));
        builder.reservedNames(fieldAnalyzer.parseReservedNames(body));

        List<ProtoMessage> nestedMessages = findNestedMessages(body, fullName);
        builder.nestedMessages(nestedMessages);

        List<ProtoEnum> nestedEnums = enumAnalyzer.findNestedEnums(body, fullName);
        builder.nestedEnums(nestedEnums);

        if (DEPRECATED_OPTION_PATTERN.matcher(body).find()) {
            builder.deprecated(true);
        }

        Map<String, String> options = parseMessageOptions(body);
        builder.options(options);

        return builder.build();
    }

    private List<MessageBlock> findMessageBlocks(String content) {
        List<MessageBlock> blocks = new ArrayList<>();

        Matcher startMatcher = MESSAGE_START_PATTERN.matcher(content);
        while (startMatcher.find()) {
            String messageName = startMatcher.group(1);
            int startIndex = startMatcher.end() - 1; 

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
                
                String body = content.substring(startIndex + 1, endIndex - 1);
                blocks.add(new MessageBlock(messageName, body));
            }
        }

        return filterTopLevelMessages(blocks, content);
    }

    private List<MessageBlock> filterTopLevelMessages(List<MessageBlock> blocks, String content) {
        List<MessageBlock> topLevel = new ArrayList<>();

        for (MessageBlock block : blocks) {
            
            String searchStr = "message " + block.name + " {";
            int pos = content.indexOf(searchStr);

            if (pos >= 0) {
                
                int braceCount = 0;
                for (int i = 0; i < pos; i++) {
                    char c = content.charAt(i);
                    if (c == '{') braceCount++;
                    else if (c == '}') braceCount--;
                }

                if (braceCount == 0) {
                    topLevel.add(block);
                }
            }
        }

        return topLevel;
    }

    private List<ProtoMessage> findNestedMessages(String messageBody, String parentFullName) {
        List<ProtoMessage> nestedMessages = new ArrayList<>();

        List<MessageBlock> nestedBlocks = findMessageBlocks(messageBody);

        for (MessageBlock block : nestedBlocks) {
            String nestedFullName = parentFullName + "." + block.name;

            ProtoMessage.Builder builder = ProtoMessage.builder()
                    .name(block.name)
                    .fullName(nestedFullName);

            List<ProtoField> fields = fieldAnalyzer.analyzeFields(block.body);
            builder.fields(fields);

            List<String> oneofNames = fieldAnalyzer.getOneofNames(block.body);
            builder.oneofNames(oneofNames);

            List<ProtoMessage> deepNested = findNestedMessages(block.body, nestedFullName);
            builder.nestedMessages(deepNested);

            List<ProtoEnum> nestedEnums = enumAnalyzer.findNestedEnums(block.body, nestedFullName);
            builder.nestedEnums(nestedEnums);

            nestedMessages.add(builder.build());
        }

        return nestedMessages;
    }

    private Map<String, String> parseMessageOptions(String messageBody) {
        Map<String, String> options = new HashMap<>();

        Pattern optionPattern = Pattern.compile(
                "option\\s+(\\w+)\\s*=\\s*([^;]+)\\s*;"
        );

        Matcher matcher = optionPattern.matcher(messageBody);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2).trim();

            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            options.put(key, value);
        }

        return options;
    }

    private String stripComments(String content) {
        
        String result = content.replaceAll("/\\*[\\s\\S]*?\\*/", "");

        result = result.replaceAll("//[^\n]*", "");

        return result;
    }

    private static class MessageBlock {
        final String name;
        final String body;

        MessageBlock(String name, String body) {
            this.name = name;
            this.body = body;
        }
    }
}
