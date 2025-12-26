package io.github.mohmk10.changeloghub.parser.spring;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.parser.spring.exception.SpringParseException;

import java.nio.file.Path;
import java.util.List;

public interface SpringParser {

    ApiSpec parse(Path sourceDirectory) throws SpringParseException;

    ApiSpec parse(Path sourceDirectory, String apiName, String apiVersion) throws SpringParseException;

    ApiSpec parse(List<Path> javaFiles) throws SpringParseException;

    ApiSpec parseFile(Path javaFile) throws SpringParseException;

    boolean isSpringController(Path javaFile);
}
