package io.github.mohmk10.changeloghub.parser.openapi;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.parser.openapi.exception.OpenApiParseException;

import java.nio.file.Path;

public interface OpenApiParser {

    ApiSpec parse(String content) throws OpenApiParseException;

    ApiSpec parseFile(Path filePath) throws OpenApiParseException;

    ApiSpec parseUrl(String url) throws OpenApiParseException;

    boolean supports(String content);
}
