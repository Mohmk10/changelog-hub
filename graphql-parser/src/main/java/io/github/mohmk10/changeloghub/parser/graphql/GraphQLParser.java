package io.github.mohmk10.changeloghub.parser.graphql;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.parser.graphql.exception.GraphQLParseException;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLSchema;

import java.io.File;
import java.io.InputStream;

/**
 * Interface for parsing GraphQL schemas.
 */
public interface GraphQLParser {

    /**
     * Parses a GraphQL schema from SDL content.
     *
     * @param sdlContent the GraphQL SDL content
     * @return the parsed GraphQL schema
     * @throws GraphQLParseException if parsing fails
     */
    GraphQLSchema parse(String sdlContent) throws GraphQLParseException;

    /**
     * Parses a GraphQL schema from a file.
     *
     * @param file the file containing the GraphQL schema
     * @return the parsed GraphQL schema
     * @throws GraphQLParseException if parsing fails
     */
    GraphQLSchema parseFile(File file) throws GraphQLParseException;

    /**
     * Parses a GraphQL schema from a file path.
     *
     * @param filePath the path to the GraphQL schema file
     * @return the parsed GraphQL schema
     * @throws GraphQLParseException if parsing fails
     */
    GraphQLSchema parseFile(String filePath) throws GraphQLParseException;

    /**
     * Parses a GraphQL schema from an input stream.
     *
     * @param inputStream the input stream containing the GraphQL schema
     * @return the parsed GraphQL schema
     * @throws GraphQLParseException if parsing fails
     */
    GraphQLSchema parseStream(InputStream inputStream) throws GraphQLParseException;

    /**
     * Parses a GraphQL schema and converts it to an ApiSpec.
     *
     * @param sdlContent the GraphQL SDL content
     * @return the parsed API specification
     * @throws GraphQLParseException if parsing fails
     */
    ApiSpec parseToApiSpec(String sdlContent) throws GraphQLParseException;

    /**
     * Parses a GraphQL schema file and converts it to an ApiSpec.
     *
     * @param file the file containing the GraphQL schema
     * @return the parsed API specification
     * @throws GraphQLParseException if parsing fails
     */
    ApiSpec parseFileToApiSpec(File file) throws GraphQLParseException;

    /**
     * Parses a GraphQL schema file and converts it to an ApiSpec.
     *
     * @param filePath the path to the GraphQL schema file
     * @return the parsed API specification
     * @throws GraphQLParseException if parsing fails
     */
    ApiSpec parseFileToApiSpec(String filePath) throws GraphQLParseException;

    /**
     * Validates a GraphQL schema.
     *
     * @param sdlContent the GraphQL SDL content
     * @return true if the schema is valid
     * @throws GraphQLParseException if the schema is invalid
     */
    boolean validate(String sdlContent) throws GraphQLParseException;

    /**
     * Validates a GraphQL schema file.
     *
     * @param file the file containing the GraphQL schema
     * @return true if the schema is valid
     * @throws GraphQLParseException if the schema is invalid
     */
    boolean validateFile(File file) throws GraphQLParseException;
}
