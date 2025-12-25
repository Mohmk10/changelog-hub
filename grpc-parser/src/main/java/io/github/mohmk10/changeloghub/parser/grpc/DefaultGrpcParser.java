package io.github.mohmk10.changeloghub.parser.grpc;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.parser.grpc.analyzer.EnumAnalyzer;
import io.github.mohmk10.changeloghub.parser.grpc.analyzer.MessageAnalyzer;
import io.github.mohmk10.changeloghub.parser.grpc.analyzer.ServiceAnalyzer;
import io.github.mohmk10.changeloghub.parser.grpc.exception.GrpcParseException;
import io.github.mohmk10.changeloghub.parser.grpc.mapper.GrpcModelMapper;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoEnum;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoFile;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoMessage;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoService;
import io.github.mohmk10.changeloghub.parser.grpc.util.ProtoConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Default implementation of GrpcParser using regex-based parsing.
 * Supports proto2 and proto3 syntax without requiring the protobuf compiler.
 */
public class DefaultGrpcParser implements GrpcParser {

    private static final Logger logger = LoggerFactory.getLogger(DefaultGrpcParser.class);

    private static final List<String> SUPPORTED_SYNTAXES = List.of(
            ProtoConstants.SYNTAX_PROTO2,
            ProtoConstants.SYNTAX_PROTO3
    );

    private final ServiceAnalyzer serviceAnalyzer;
    private final MessageAnalyzer messageAnalyzer;
    private final EnumAnalyzer enumAnalyzer;
    private final GrpcModelMapper modelMapper;

    public DefaultGrpcParser() {
        this.serviceAnalyzer = new ServiceAnalyzer();
        this.messageAnalyzer = new MessageAnalyzer();
        this.enumAnalyzer = new EnumAnalyzer();
        this.modelMapper = new GrpcModelMapper();
    }

    public DefaultGrpcParser(ServiceAnalyzer serviceAnalyzer, MessageAnalyzer messageAnalyzer,
                             EnumAnalyzer enumAnalyzer, GrpcModelMapper modelMapper) {
        this.serviceAnalyzer = serviceAnalyzer;
        this.messageAnalyzer = messageAnalyzer;
        this.enumAnalyzer = enumAnalyzer;
        this.modelMapper = modelMapper;
    }

    @Override
    public ProtoFile parse(String content) throws GrpcParseException {
        return parse(content, null);
    }

    @Override
    public ProtoFile parse(String content, String fileName) throws GrpcParseException {
        if (content == null || content.isBlank()) {
            throw GrpcParseException.emptyContent();
        }

        logger.debug("Parsing proto content{}", fileName != null ? " from " + fileName : "");

        try {
            // Remove comments
            String cleanContent = removeComments(content);

            // Extract syntax
            String syntax = extractSyntax(cleanContent);
            if (syntax != null && !supportsSyntax(syntax)) {
                throw GrpcParseException.unsupportedSyntax(syntax);
            }

            // Extract package
            String packageName = extractPackage(cleanContent);

            // Extract imports
            List<String> imports = extractImports(cleanContent);
            List<String> publicImports = extractPublicImports(cleanContent);

            // Extract options
            var options = extractOptions(cleanContent);

            // Parse services
            List<ProtoService> services = serviceAnalyzer.analyzeServices(cleanContent, packageName);

            // Parse messages
            List<ProtoMessage> messages = messageAnalyzer.analyzeMessages(cleanContent, packageName);

            // Parse top-level enums
            List<ProtoEnum> enums = enumAnalyzer.analyzeEnums(cleanContent, packageName);

            // Build ProtoFile
            ProtoFile.Builder builder = ProtoFile.builder()
                    .fileName(fileName)
                    .syntax(syntax != null ? syntax : ProtoConstants.SYNTAX_PROTO3)
                    .packageName(packageName)
                    .imports(imports)
                    .publicImports(publicImports)
                    .services(services)
                    .messages(messages)
                    .enums(enums)
                    .options(options);

            ProtoFile protoFile = builder.build();

            logger.info("Parsed proto file: {} services, {} messages, {} enums",
                    services.size(), messages.size(), enums.size());

            return protoFile;

        } catch (GrpcParseException e) {
            throw e;
        } catch (Exception e) {
            throw GrpcParseException.parseError("Unexpected error during parsing", e);
        }
    }

    @Override
    public ProtoFile parseFile(String filePath) throws GrpcParseException {
        return parseFile(new File(filePath));
    }

    @Override
    public ProtoFile parseFile(File file) throws GrpcParseException {
        if (!file.exists()) {
            throw GrpcParseException.fileNotFound(file.getAbsolutePath());
        }

        if (!file.getName().endsWith(ProtoConstants.PROTO_EXTENSION)) {
            logger.warn("File does not have .proto extension: {}", file.getName());
        }

        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            return parse(content, file.getName());
        } catch (IOException e) {
            throw GrpcParseException.ioError(file.getAbsolutePath(), e);
        }
    }

    @Override
    public ProtoFile parseFile(Path path) throws GrpcParseException {
        return parseFile(path.toFile());
    }

    @Override
    public ProtoFile parseStream(InputStream inputStream) throws GrpcParseException {
        return parseStream(inputStream, null);
    }

    @Override
    public ProtoFile parseStream(InputStream inputStream, String fileName) throws GrpcParseException {
        try {
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return parse(content, fileName);
        } catch (IOException e) {
            throw GrpcParseException.ioError("InputStream", e);
        }
    }

    @Override
    public ProtoFile parseUrl(URL url) throws GrpcParseException {
        try (InputStream inputStream = url.openStream()) {
            String fileName = extractFileNameFromUrl(url);
            return parseStream(inputStream, fileName);
        } catch (IOException e) {
            throw GrpcParseException.ioError(url.toString(), e);
        }
    }

    @Override
    public ProtoFile parseUrl(String urlString) throws GrpcParseException {
        try {
            URL url = new URL(urlString);
            return parseUrl(url);
        } catch (Exception e) {
            throw GrpcParseException.parseError("Invalid URL: " + urlString, e);
        }
    }

    @Override
    public List<ProtoFile> parseDirectory(String directoryPath, boolean recursive) throws GrpcParseException {
        return parseDirectory(new File(directoryPath), recursive);
    }

    @Override
    public List<ProtoFile> parseDirectory(File directory, boolean recursive) throws GrpcParseException {
        if (!directory.exists()) {
            throw GrpcParseException.fileNotFound(directory.getAbsolutePath());
        }

        if (!directory.isDirectory()) {
            throw GrpcParseException.invalidProto(directory.getAbsolutePath() + " is not a directory");
        }

        List<ProtoFile> protoFiles = new ArrayList<>();

        try (Stream<Path> paths = recursive
                ? Files.walk(directory.toPath())
                : Files.list(directory.toPath())) {

            List<Path> protoFilePaths = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(ProtoConstants.PROTO_EXTENSION))
                    .toList();

            for (Path path : protoFilePaths) {
                try {
                    protoFiles.add(parseFile(path));
                } catch (GrpcParseException e) {
                    logger.warn("Failed to parse {}: {}", path, e.getMessage());
                }
            }

        } catch (IOException e) {
            throw GrpcParseException.ioError(directory.getAbsolutePath(), e);
        }

        logger.info("Parsed {} proto files from directory {}", protoFiles.size(), directory.getAbsolutePath());
        return protoFiles;
    }

    @Override
    public ApiSpec toApiSpec(ProtoFile protoFile) {
        return modelMapper.mapProtoFile(protoFile);
    }

    @Override
    public ApiSpec toApiSpec(List<ProtoFile> protoFiles) {
        return modelMapper.mapProtoFiles(protoFiles);
    }

    @Override
    public boolean supportsSyntax(String syntax) {
        return SUPPORTED_SYNTAXES.contains(syntax);
    }

    @Override
    public List<String> getSupportedSyntaxVersions() {
        return SUPPORTED_SYNTAXES;
    }

    // Private helper methods

    /**
     * Remove single-line and multi-line comments from the content.
     */
    private String removeComments(String content) {
        // Remove single-line comments
        String noSingleLine = content.replaceAll("//.*$", "");

        // Remove multi-line comments
        return noSingleLine.replaceAll("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/", "");
    }

    /**
     * Extract syntax declaration.
     */
    private String extractSyntax(String content) {
        Matcher matcher = ProtoConstants.SYNTAX_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Extract package declaration.
     */
    private String extractPackage(String content) {
        Matcher matcher = ProtoConstants.PACKAGE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Extract import statements.
     */
    private List<String> extractImports(String content) {
        List<String> imports = new ArrayList<>();
        Matcher matcher = ProtoConstants.IMPORT_PATTERN.matcher(content);
        while (matcher.find()) {
            String modifier = matcher.group(1); // public or weak
            if (modifier == null) { // regular import
                imports.add(matcher.group(2));
            }
        }
        return imports;
    }

    /**
     * Extract public import statements.
     */
    private List<String> extractPublicImports(String content) {
        List<String> imports = new ArrayList<>();
        Matcher matcher = ProtoConstants.IMPORT_PATTERN.matcher(content);
        while (matcher.find()) {
            String modifier = matcher.group(1);
            if ("public".equals(modifier)) {
                imports.add(matcher.group(2));
            }
        }
        return imports;
    }

    /**
     * Extract file-level options.
     */
    private java.util.Map<String, String> extractOptions(String content) {
        java.util.Map<String, String> options = new java.util.LinkedHashMap<>();
        Matcher matcher = ProtoConstants.OPTION_PATTERN.matcher(content);
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
     * Extract filename from a URL.
     */
    private String extractFileNameFromUrl(URL url) {
        String path = url.getPath();
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
}
