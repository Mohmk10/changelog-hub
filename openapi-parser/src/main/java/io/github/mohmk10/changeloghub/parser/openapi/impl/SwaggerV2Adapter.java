package io.github.mohmk10.changeloghub.parser.openapi.impl;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.converter.SwaggerConverter;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.util.Optional;

public class SwaggerV2Adapter {

    private final SwaggerConverter converter;

    public SwaggerV2Adapter() {
        this.converter = new SwaggerConverter();
    }

    public Optional<OpenAPI> convert(String content) {
        try {
            ParseOptions options = new ParseOptions();
            options.setResolve(true);

            SwaggerParseResult result = converter.readContents(content, null, options);

            if (result != null && result.getOpenAPI() != null) {
                return Optional.of(result.getOpenAPI());
            }

            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean isSwagger2(String content) {
        if (content == null) {
            return false;
        }
        return content.contains("\"swagger\"") || content.contains("swagger:");
    }
}
