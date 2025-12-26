package io.github.mohmk10.changeloghub.git.extractor;

import io.github.mohmk10.changeloghub.git.config.GitConfig;
import io.github.mohmk10.changeloghub.git.model.GitFileContent;
import io.github.mohmk10.changeloghub.git.util.GitConstants;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class SpecFileDetector {

    private static final Logger logger = LoggerFactory.getLogger(SpecFileDetector.class);

    private final FileExtractor fileExtractor;
    private final GitConfig config;

    private static final Pattern OPENAPI_PATTERN = Pattern.compile(
        "(?s)(openapi|swagger)\\s*:\\s*['\"]?[23]\\.",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern ASYNCAPI_PATTERN = Pattern.compile(
        "(?s)asyncapi\\s*:\\s*['\"]?[23]\\.",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern GRAPHQL_PATTERN = Pattern.compile(
        "(?s)(type\\s+(Query|Mutation|Subscription|\\w+)\\s*\\{|schema\\s*\\{|directive\\s+@)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PROTOBUF_PATTERN = Pattern.compile(
        "(?m)^\\s*syntax\\s*=\\s*['\"]proto[23]['\"]",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SPRING_CONTROLLER_PATTERN = Pattern.compile(
        "@(RestController|Controller|RequestMapping|GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)",
        Pattern.CASE_INSENSITIVE
    );

    public SpecFileDetector(Repository repository) {
        this(repository, new GitConfig());
    }

    public SpecFileDetector(Repository repository, GitConfig config) {
        this.fileExtractor = new FileExtractor(repository, config);
        this.config = config;
    }

    public SpecFileDetector(FileExtractor fileExtractor, GitConfig config) {
        this.fileExtractor = fileExtractor;
        this.config = config;
    }

    public Map<SpecType, List<String>> detectSpecs(String ref) {
        Map<SpecType, List<String>> result = new EnumMap<>(SpecType.class);
        for (SpecType type : SpecType.values()) {
            result.put(type, new ArrayList<>());
        }

        List<String> allFiles = fileExtractor.listAllFiles(ref);
        logger.debug("Found {} files at ref {}", allFiles.size(), ref);

        for (String filePath : allFiles) {
            SpecType type = detectSpecType(filePath, ref);
            if (type != null) {
                result.get(type).add(filePath);
                logger.debug("Detected {} spec: {}", type, filePath);
            }
        }

        return result;
    }

    public SpecType detectSpecType(String filePath, String ref) {
        String extension = getExtension(filePath);
        String fileName = getFileName(filePath);

        SpecType byExtension = detectByExtension(extension, fileName);
        if (byExtension == SpecType.GRAPHQL || byExtension == SpecType.PROTOBUF) {
            return byExtension; 
        }

        if (isYamlOrJson(extension)) {
            return detectByContent(filePath, ref);
        }

        if ("java".equals(extension)) {
            return detectSpringController(filePath, ref);
        }

        return byExtension;
    }

    private SpecType detectByExtension(String extension, String fileName) {
        if (extension == null) return null;

        switch (extension.toLowerCase()) {
            case "graphql":
            case "gql":
            case "graphqls":
                return SpecType.GRAPHQL;

            case "proto":
                return SpecType.PROTOBUF;

            case "yaml":
            case "yml":
            case "json":
                
                String lowerName = fileName.toLowerCase();
                if (lowerName.startsWith("openapi") || lowerName.startsWith("swagger") ||
                    lowerName.contains("openapi") || lowerName.contains("swagger")) {
                    return SpecType.OPENAPI;
                }
                if (lowerName.startsWith("asyncapi") || lowerName.contains("asyncapi")) {
                    return SpecType.ASYNCAPI;
                }
                
                return null;

            case "java":
            case "kt":
            case "groovy":
                return null; 

            default:
                return null;
        }
    }

    private SpecType detectByContent(String filePath, String ref) {
        Optional<String> content = fileExtractor.getFileContentAsString(filePath, ref);
        if (content.isEmpty()) {
            return null;
        }

        String text = content.get();

        if (OPENAPI_PATTERN.matcher(text).find()) {
            return SpecType.OPENAPI;
        }

        if (ASYNCAPI_PATTERN.matcher(text).find()) {
            return SpecType.ASYNCAPI;
        }

        return null;
    }

    private SpecType detectSpringController(String filePath, String ref) {
        Optional<String> content = fileExtractor.getFileContentAsString(filePath, ref);
        if (content.isEmpty()) {
            return null;
        }

        if (SPRING_CONTROLLER_PATTERN.matcher(content.get()).find()) {
            return SpecType.SPRING;
        }

        return null;
    }

    public List<String> findOpenApiSpecs(String ref) {
        return detectSpecs(ref).get(SpecType.OPENAPI);
    }

    public List<String> findGraphQLSchemas(String ref) {
        return detectSpecs(ref).get(SpecType.GRAPHQL);
    }

    public List<String> findProtobufFiles(String ref) {
        return detectSpecs(ref).get(SpecType.PROTOBUF);
    }

    public List<String> findAsyncApiSpecs(String ref) {
        return detectSpecs(ref).get(SpecType.ASYNCAPI);
    }

    public List<String> findSpringControllers(String ref) {
        return detectSpecs(ref).get(SpecType.SPRING);
    }

    public List<SpecFile> findAllSpecs(String ref) {
        List<SpecFile> specs = new ArrayList<>();
        Map<SpecType, List<String>> detected = detectSpecs(ref);

        for (Map.Entry<SpecType, List<String>> entry : detected.entrySet()) {
            for (String path : entry.getValue()) {
                specs.add(new SpecFile(path, entry.getKey(), ref));
            }
        }

        return specs;
    }

    public boolean isInSpecDirectory(String filePath) {
        if (filePath == null) return false;

        for (String specDir : config.getSpecDirectories()) {
            if (filePath.contains("/" + specDir + "/") ||
                filePath.startsWith(specDir + "/")) {
                return true;
            }
        }

        return GitConstants.SPEC_DIRECTORIES.stream()
            .anyMatch(dir -> filePath.contains("/" + dir + "/") ||
                           filePath.startsWith(dir + "/"));
    }

    private String getExtension(String path) {
        if (path == null) return null;
        int lastDot = path.lastIndexOf('.');
        return lastDot > 0 ? path.substring(lastDot + 1) : null;
    }

    private String getFileName(String path) {
        if (path == null) return null;
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    private boolean isYamlOrJson(String extension) {
        return "yaml".equalsIgnoreCase(extension) ||
               "yml".equalsIgnoreCase(extension) ||
               "json".equalsIgnoreCase(extension);
    }

    public enum SpecType {
        OPENAPI("OpenAPI/Swagger"),
        GRAPHQL("GraphQL"),
        PROTOBUF("Protocol Buffers/gRPC"),
        ASYNCAPI("AsyncAPI"),
        SPRING("Spring Boot");

        private final String displayName;

        SpecType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public static class SpecFile {
        private final String path;
        private final SpecType type;
        private final String ref;

        public SpecFile(String path, SpecType type, String ref) {
            this.path = path;
            this.type = type;
            this.ref = ref;
        }

        public String getPath() {
            return path;
        }

        public SpecType getType() {
            return type;
        }

        public String getRef() {
            return ref;
        }

        public String getFileName() {
            int lastSlash = path.lastIndexOf('/');
            return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SpecFile specFile = (SpecFile) o;
            return Objects.equals(path, specFile.path) &&
                   type == specFile.type &&
                   Objects.equals(ref, specFile.ref);
        }

        @Override
        public int hashCode() {
            return Objects.hash(path, type, ref);
        }

        @Override
        public String toString() {
            return "SpecFile{" +
                   "path='" + path + '\'' +
                   ", type=" + type +
                   ", ref='" + ref + '\'' +
                   '}';
        }
    }
}
