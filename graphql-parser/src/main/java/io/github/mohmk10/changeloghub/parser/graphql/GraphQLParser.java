package io.github.mohmk10.changeloghub.parser.graphql;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.parser.graphql.exception.GraphQLParseException;
import io.github.mohmk10.changeloghub.parser.graphql.model.GraphQLSchema;

import java.io.File;
import java.io.InputStream;

public interface GraphQLParser {

    GraphQLSchema parse(String sdlContent) throws GraphQLParseException;

    GraphQLSchema parseFile(File file) throws GraphQLParseException;

    GraphQLSchema parseFile(String filePath) throws GraphQLParseException;

    GraphQLSchema parseStream(InputStream inputStream) throws GraphQLParseException;

    ApiSpec parseToApiSpec(String sdlContent) throws GraphQLParseException;

    ApiSpec parseFileToApiSpec(File file) throws GraphQLParseException;

    ApiSpec parseFileToApiSpec(String filePath) throws GraphQLParseException;

    boolean validate(String sdlContent) throws GraphQLParseException;

    boolean validateFile(File file) throws GraphQLParseException;
}
