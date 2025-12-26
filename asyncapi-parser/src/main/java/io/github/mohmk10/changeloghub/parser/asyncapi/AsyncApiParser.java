package io.github.mohmk10.changeloghub.parser.asyncapi;

import io.github.mohmk10.changeloghub.parser.asyncapi.exception.AsyncApiParseException;
import io.github.mohmk10.changeloghub.parser.asyncapi.model.AsyncApiSpec;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

public interface AsyncApiParser {

    AsyncApiSpec parse(String content) throws AsyncApiParseException;

    AsyncApiSpec parseFile(File file) throws AsyncApiParseException;

    AsyncApiSpec parseFile(String filePath) throws AsyncApiParseException;

    AsyncApiSpec parseUrl(URL url) throws AsyncApiParseException;

    AsyncApiSpec parseUrl(String urlString) throws AsyncApiParseException;

    AsyncApiSpec parse(InputStream inputStream) throws AsyncApiParseException;

    boolean isValid(String content);

    String detectVersion(String content);
}
