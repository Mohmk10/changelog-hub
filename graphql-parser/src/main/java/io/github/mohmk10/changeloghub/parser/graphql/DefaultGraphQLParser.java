package io.github.mohmk10.changeloghub.parser.graphql;

import graphql.language.Definition;
import graphql.language.Document;
import graphql.language.SDLDefinition;
import graphql.parser.Parser;
import graphql.parser.ParserOptions;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.errors.SchemaProblem;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.parser.graphql.analyzer.OperationAnalyzer;
import io.github.mohmk10.changeloghub.parser.graphql.analyzer.TypeAnalyzer;
import io.github.mohmk10.changeloghub.parser.graphql.exception.GraphQLParseException;
import io.github.mohmk10.changeloghub.parser.graphql.mapper.GraphQLModelMapper;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLOperation;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLSchema;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultGraphQLParser implements GraphQLParser {

    private static final Logger logger = LoggerFactory.getLogger(DefaultGraphQLParser.class);

    private final SchemaParser schemaParser;
    private final TypeAnalyzer typeAnalyzer;
    private final OperationAnalyzer operationAnalyzer;
    private final GraphQLModelMapper modelMapper;

    public DefaultGraphQLParser() {
        this.schemaParser = new SchemaParser();
        this.typeAnalyzer = new TypeAnalyzer();
        this.operationAnalyzer = new OperationAnalyzer();
        this.modelMapper = new GraphQLModelMapper();
    }

    @Override
    public GraphQLSchema parse(String sdlContent) throws GraphQLParseException {
        if (sdlContent == null || sdlContent.isBlank()) {
            throw GraphQLParseException.emptySchema();
        }

        try {
            logger.debug("Parsing GraphQL schema from SDL content");

            TypeDefinitionRegistry registry = schemaParser.parse(sdlContent);

            return buildSchema(registry, sdlContent);

        } catch (SchemaProblem e) {
            logger.error("Failed to parse GraphQL schema: {}", e.getMessage());
            throw GraphQLParseException.parseError(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error parsing GraphQL schema: {}", e.getMessage());
            throw GraphQLParseException.parseError("Unexpected error: " + e.getMessage(), e);
        }
    }

    @Override
    public GraphQLSchema parseFile(File file) throws GraphQLParseException {
        validateFile(file);

        try {
            logger.debug("Parsing GraphQL schema from file: {}", file.getAbsolutePath());
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            GraphQLSchema schema = parse(content);
            schema.setSourceFile(file.getAbsolutePath());
            return schema;
        } catch (IOException e) {
            throw GraphQLParseException.fileNotFound(file.getAbsolutePath());
        }
    }

    @Override
    public GraphQLSchema parseFile(String filePath) throws GraphQLParseException {
        return parseFile(new File(filePath));
    }

    @Override
    public GraphQLSchema parseStream(InputStream inputStream) throws GraphQLParseException {
        if (inputStream == null) {
            throw GraphQLParseException.parseError("Input stream is null", null);
        }

        try {
            String content = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
            return parse(content);
        } catch (Exception e) {
            throw GraphQLParseException.parseError("Failed to read input stream: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiSpec parseToApiSpec(String sdlContent) throws GraphQLParseException {
        GraphQLSchema schema = parse(sdlContent);
        return modelMapper.mapToApiSpec(schema);
    }

    @Override
    public ApiSpec parseFileToApiSpec(File file) throws GraphQLParseException {
        GraphQLSchema schema = parseFile(file);
        return modelMapper.mapToApiSpec(schema);
    }

    @Override
    public ApiSpec parseFileToApiSpec(String filePath) throws GraphQLParseException {
        return parseFileToApiSpec(new File(filePath));
    }

    @Override
    public boolean validate(String sdlContent) throws GraphQLParseException {
        if (sdlContent == null || sdlContent.isBlank()) {
            throw GraphQLParseException.emptySchema();
        }

        try {
            schemaParser.parse(sdlContent);
            return true;
        } catch (SchemaProblem e) {
            throw GraphQLParseException.validationError(e.getMessage());
        }
    }

    @Override
    public boolean validateFile(File file) throws GraphQLParseException {
        if (file == null) {
            throw GraphQLParseException.parseError("File is null", null);
        }
        if (!file.exists()) {
            throw GraphQLParseException.fileNotFound(file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw GraphQLParseException.parseError("Path is not a file: " + file.getAbsolutePath(), null);
        }
        if (!file.canRead()) {
            throw GraphQLParseException.parseError("Cannot read file: " + file.getAbsolutePath(), null);
        }

        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            return validate(content);
        } catch (IOException e) {
            throw GraphQLParseException.fileNotFound(file.getAbsolutePath());
        }
    }

    private GraphQLSchema buildSchema(TypeDefinitionRegistry registry, String sdlContent) {
        GraphQLSchema schema = new GraphQLSchema();

        extractSchemaMetadata(schema, sdlContent);

        List<GraphQLType> types = typeAnalyzer.analyzeTypes(registry);
        Map<String, GraphQLType> typeMap = types.stream()
                .collect(Collectors.toMap(GraphQLType::getName, t -> t, (a, b) -> a));
        schema.setTypes(typeMap);

        List<GraphQLOperation> allOperations = operationAnalyzer.analyzeOperations(registry);

        List<GraphQLOperation> queries = allOperations.stream()
                .filter(GraphQLOperation::isQuery)
                .toList();
        List<GraphQLOperation> mutations = allOperations.stream()
                .filter(GraphQLOperation::isMutation)
                .toList();
        List<GraphQLOperation> subscriptions = allOperations.stream()
                .filter(GraphQLOperation::isSubscription)
                .toList();

        schema.setQueries(queries);
        schema.setMutations(mutations);
        schema.setSubscriptions(subscriptions);

        logger.info("Parsed GraphQL schema: {} types, {} queries, {} mutations, {} subscriptions",
                types.size(), queries.size(), mutations.size(), subscriptions.size());

        return schema;
    }

    private void extractSchemaMetadata(GraphQLSchema schema, String sdlContent) {
        
        String[] lines = sdlContent.split("\n");
        StringBuilder description = new StringBuilder();
        boolean inDescription = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("\"\"\"")) {
                if (inDescription) {
                    inDescription = false;
                    schema.setDescription(description.toString().trim());
                    break;
                } else {
                    inDescription = true;
                    
                    if (trimmed.length() > 6 && trimmed.endsWith("\"\"\"")) {
                        schema.setDescription(trimmed.substring(3, trimmed.length() - 3).trim());
                        break;
                    }
                }
            } else if (inDescription) {
                description.append(trimmed).append(" ");
            } else if (trimmed.startsWith("#")) {
                
                String comment = trimmed.substring(1).trim();
                if (comment.toLowerCase().contains("api") || comment.toLowerCase().contains("schema")) {
                    if (schema.getName() == null) {
                        schema.setName(comment);
                    }
                }
            } else if (!trimmed.isEmpty() && !trimmed.startsWith("\"")) {
                
                break;
            }
        }

        if (schema.getName() == null) {
            schema.setName("GraphQL API");
        }
        if (schema.getVersion() == null) {
            schema.setVersion("1.0.0");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean lenientParsing = false;

        public Builder lenientParsing(boolean lenient) {
            this.lenientParsing = lenient;
            return this;
        }

        public DefaultGraphQLParser build() {
            return new DefaultGraphQLParser();
        }
    }
}
