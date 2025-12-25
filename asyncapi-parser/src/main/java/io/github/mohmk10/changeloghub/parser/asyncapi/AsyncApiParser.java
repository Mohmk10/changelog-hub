package io.github.mohmk10.changeloghub.parser.asyncapi;

import io.github.mohmk10.changeloghub.parser.asyncapi.exception.AsyncApiParseException;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncApiSpec;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * Interface for parsing AsyncAPI specifications.
 * Supports AsyncAPI 2.x and 3.x versions in YAML and JSON formats.
 */
public interface AsyncApiParser {

    /**
     * Parse AsyncAPI spec from string content.
     *
     * @param content the YAML or JSON content
     * @return parsed AsyncAPI specification
     * @throws AsyncApiParseException if parsing fails
     */
    AsyncApiSpec parse(String content) throws AsyncApiParseException;

    /**
     * Parse AsyncAPI spec from file.
     *
     * @param file the spec file (YAML or JSON)
     * @return parsed AsyncAPI specification
     * @throws AsyncApiParseException if parsing fails
     */
    AsyncApiSpec parseFile(File file) throws AsyncApiParseException;

    /**
     * Parse AsyncAPI spec from file path.
     *
     * @param filePath the path to the spec file
     * @return parsed AsyncAPI specification
     * @throws AsyncApiParseException if parsing fails
     */
    AsyncApiSpec parseFile(String filePath) throws AsyncApiParseException;

    /**
     * Parse AsyncAPI spec from URL.
     *
     * @param url the URL to the spec
     * @return parsed AsyncAPI specification
     * @throws AsyncApiParseException if parsing fails
     */
    AsyncApiSpec parseUrl(URL url) throws AsyncApiParseException;

    /**
     * Parse AsyncAPI spec from URL string.
     *
     * @param urlString the URL string to the spec
     * @return parsed AsyncAPI specification
     * @throws AsyncApiParseException if parsing fails
     */
    AsyncApiSpec parseUrl(String urlString) throws AsyncApiParseException;

    /**
     * Parse AsyncAPI spec from input stream.
     *
     * @param inputStream the input stream containing spec content
     * @return parsed AsyncAPI specification
     * @throws AsyncApiParseException if parsing fails
     */
    AsyncApiSpec parse(InputStream inputStream) throws AsyncApiParseException;

    /**
     * Check if content is valid AsyncAPI format.
     *
     * @param content the content to validate
     * @return true if valid AsyncAPI format
     */
    boolean isValid(String content);

    /**
     * Detect AsyncAPI version from content.
     *
     * @param content the spec content
     * @return the detected version string or null if not detected
     */
    String detectVersion(String content);
}
