package io.github.mohmk10.changeloghub.parser.spring;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.parser.spring.exception.SpringParseException;

import java.nio.file.Path;
import java.util.List;

/**
 * Parser for Spring Boot controller annotations.
 * Extracts API specification from @RestController, @GetMapping, @PostMapping, etc.
 */
public interface SpringParser {

    /**
     * Parse all Java files in a directory and extract API specification.
     *
     * @param sourceDirectory the root directory containing Java source files
     * @return the parsed API specification
     * @throws SpringParseException if parsing fails
     */
    ApiSpec parse(Path sourceDirectory) throws SpringParseException;

    /**
     * Parse all Java files in a directory with custom API name and version.
     *
     * @param sourceDirectory the root directory containing Java source files
     * @param apiName the name of the API
     * @param apiVersion the version of the API
     * @return the parsed API specification
     * @throws SpringParseException if parsing fails
     */
    ApiSpec parse(Path sourceDirectory, String apiName, String apiVersion) throws SpringParseException;

    /**
     * Parse a list of Java files and extract API specification.
     *
     * @param javaFiles the list of Java files to parse
     * @return the parsed API specification
     * @throws SpringParseException if parsing fails
     */
    ApiSpec parse(List<Path> javaFiles) throws SpringParseException;

    /**
     * Parse a single Java file and extract API specification.
     *
     * @param javaFile the Java file to parse
     * @return the parsed API specification
     * @throws SpringParseException if parsing fails
     */
    ApiSpec parseFile(Path javaFile) throws SpringParseException;

    /**
     * Check if a Java file contains a Spring controller.
     *
     * @param javaFile the Java file to check
     * @return true if the file contains a Spring controller
     */
    boolean isSpringController(Path javaFile);
}
