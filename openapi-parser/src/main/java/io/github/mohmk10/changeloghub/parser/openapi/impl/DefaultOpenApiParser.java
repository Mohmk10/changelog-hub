package io.github.mohmk10.changeloghub.parser.openapi.impl;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.parser.openapi.OpenApiParser;
import io.github.mohmk10.changeloghub.parser.openapi.exception.OpenApiParseException;
import io.github.mohmk10.changeloghub.parser.openapi.exception.UnsupportedVersionException;
import io.github.mohmk10.changeloghub.parser.openapi.mapper.OpenApiModelMapper;
import io.github.mohmk10.changeloghub.parser.openapi.util.OpenApiVersion;
import io.github.mohmk10.changeloghub.parser.openapi.util.OpenApiVersionDetector;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class DefaultOpenApiParser implements OpenApiParser {

    private static final Logger logger = LoggerFactory.getLogger(DefaultOpenApiParser.class);

    private final OpenApiModelMapper modelMapper;
    private final SwaggerV2Adapter swaggerAdapter;
    private final OpenAPIV3Parser openApiV3Parser;

    public DefaultOpenApiParser() {
        this.modelMapper = new OpenApiModelMapper();
        this.swaggerAdapter = new SwaggerV2Adapter();
        this.openApiV3Parser = new OpenAPIV3Parser();
    }

    public DefaultOpenApiParser(OpenApiModelMapper modelMapper, SwaggerV2Adapter swaggerAdapter) {
        this.modelMapper = modelMapper;
        this.swaggerAdapter = swaggerAdapter;
        this.openApiV3Parser = new OpenAPIV3Parser();
    }

    @Override
    public ApiSpec parse(String content) throws OpenApiParseException {
        if (content == null || content.isBlank()) {
            throw new OpenApiParseException("Content cannot be null or empty");
        }

        OpenApiVersion version = OpenApiVersionDetector.detect(content);
        logger.debug("Detected OpenAPI version: {}", version);

        if (!OpenApiVersionDetector.isSupported(version)) {
            throw new UnsupportedVersionException(version);
        }

        OpenAPI openApi = parseContent(content, version);
        return modelMapper.map(openApi);
    }

    @Override
    public ApiSpec parseFile(Path filePath) throws OpenApiParseException {
        if (filePath == null) {
            throw new OpenApiParseException("File path cannot be null");
        }

        if (!Files.exists(filePath)) {
            throw new OpenApiParseException("File does not exist: " + filePath);
        }

        try {
            String content = Files.readString(filePath);
            return parse(content);
        } catch (IOException e) {
            throw new OpenApiParseException("Failed to read file: " + filePath, e);
        }
    }

    @Override
    public ApiSpec parseUrl(String url) throws OpenApiParseException {
        if (url == null || url.isBlank()) {
            throw new OpenApiParseException("URL cannot be null or empty");
        }

        try {
            ParseOptions options = createParseOptions();
            SwaggerParseResult result = openApiV3Parser.readLocation(url, null, options);

            if (result == null) {
                throw new OpenApiParseException("Failed to parse URL: " + url);
            }

            if (result.getMessages() != null && !result.getMessages().isEmpty()) {
                logger.warn("Parse warnings for URL {}: {}", url, result.getMessages());
            }

            if (result.getOpenAPI() == null) {
                String errorMsg = result.getMessages() != null ?
                    String.join(", ", result.getMessages()) : "Unknown error";
                throw new OpenApiParseException("Failed to parse URL: " + url + ". Errors: " + errorMsg);
            }

            return modelMapper.map(result.getOpenAPI());
        } catch (OpenApiParseException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenApiParseException("Failed to parse URL: " + url, e);
        }
    }

    @Override
    public boolean supports(String content) {
        if (content == null || content.isBlank()) {
            return false;
        }

        OpenApiVersion version = OpenApiVersionDetector.detect(content);
        return OpenApiVersionDetector.isSupported(version);
    }

    private OpenAPI parseContent(String content, OpenApiVersion version) throws OpenApiParseException {
        if (version == OpenApiVersion.SWAGGER_2_0) {
            return parseSwagger2(content);
        } else {
            return parseOpenApi3(content);
        }
    }

    private OpenAPI parseSwagger2(String content) throws OpenApiParseException {
        Optional<OpenAPI> converted = swaggerAdapter.convert(content);

        if (converted.isPresent()) {
            return converted.get();
        }

        throw new OpenApiParseException("Failed to parse Swagger 2.0 content");
    }

    private OpenAPI parseOpenApi3(String content) throws OpenApiParseException {
        ParseOptions options = createParseOptions();
        SwaggerParseResult result = openApiV3Parser.readContents(content, null, options);

        if (result == null) {
            throw new OpenApiParseException("Failed to parse OpenAPI 3.x content");
        }

        List<String> messages = result.getMessages();
        if (messages != null && !messages.isEmpty()) {
            logger.warn("Parse warnings: {}", messages);
        }

        if (result.getOpenAPI() == null) {
            String errorMsg = messages != null ? String.join(", ", messages) : "Unknown error";
            throw new OpenApiParseException("Failed to parse OpenAPI 3.x content. Errors: " + errorMsg);
        }

        return result.getOpenAPI();
    }

    private ParseOptions createParseOptions() {
        ParseOptions options = new ParseOptions();
        options.setResolve(true);
        options.setResolveFully(false);
        return options;
    }
}
