package io.github.mohmk10.changeloghub.parser.grpc;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.parser.grpc.exception.GrpcParseException;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoFile;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface for parsing Protocol Buffers (.proto) files.
 * Supports parsing from strings, files, input streams, and URLs.
 */
public interface GrpcParser {

    /**
     * Parse a Protocol Buffers file from a string content.
     *
     * @param content the proto file content as a string
     * @return the parsed ProtoFile
     * @throws GrpcParseException if parsing fails
     */
    ProtoFile parse(String content) throws GrpcParseException;

    /**
     * Parse a Protocol Buffers file from a string content with a filename hint.
     *
     * @param content  the proto file content as a string
     * @param fileName the filename (used for naming and version extraction)
     * @return the parsed ProtoFile
     * @throws GrpcParseException if parsing fails
     */
    ProtoFile parse(String content, String fileName) throws GrpcParseException;

    /**
     * Parse a Protocol Buffers file from a file path.
     *
     * @param filePath the path to the proto file
     * @return the parsed ProtoFile
     * @throws GrpcParseException if parsing fails
     */
    ProtoFile parseFile(String filePath) throws GrpcParseException;

    /**
     * Parse a Protocol Buffers file from a File object.
     *
     * @param file the proto file
     * @return the parsed ProtoFile
     * @throws GrpcParseException if parsing fails
     */
    ProtoFile parseFile(File file) throws GrpcParseException;

    /**
     * Parse a Protocol Buffers file from a Path.
     *
     * @param path the path to the proto file
     * @return the parsed ProtoFile
     * @throws GrpcParseException if parsing fails
     */
    ProtoFile parseFile(Path path) throws GrpcParseException;

    /**
     * Parse a Protocol Buffers file from an InputStream.
     *
     * @param inputStream the input stream containing proto content
     * @return the parsed ProtoFile
     * @throws GrpcParseException if parsing fails
     */
    ProtoFile parseStream(InputStream inputStream) throws GrpcParseException;

    /**
     * Parse a Protocol Buffers file from an InputStream with a filename hint.
     *
     * @param inputStream the input stream containing proto content
     * @param fileName    the filename hint
     * @return the parsed ProtoFile
     * @throws GrpcParseException if parsing fails
     */
    ProtoFile parseStream(InputStream inputStream, String fileName) throws GrpcParseException;

    /**
     * Parse a Protocol Buffers file from a URL.
     *
     * @param url the URL to fetch the proto file from
     * @return the parsed ProtoFile
     * @throws GrpcParseException if parsing fails
     */
    ProtoFile parseUrl(URL url) throws GrpcParseException;

    /**
     * Parse a Protocol Buffers file from a URL string.
     *
     * @param urlString the URL string to fetch the proto file from
     * @return the parsed ProtoFile
     * @throws GrpcParseException if parsing fails
     */
    ProtoFile parseUrl(String urlString) throws GrpcParseException;

    /**
     * Parse multiple Protocol Buffers files from a directory.
     *
     * @param directoryPath the directory containing proto files
     * @param recursive     whether to search subdirectories
     * @return list of parsed ProtoFiles
     * @throws GrpcParseException if parsing fails
     */
    List<ProtoFile> parseDirectory(String directoryPath, boolean recursive) throws GrpcParseException;

    /**
     * Parse multiple Protocol Buffers files from a directory.
     *
     * @param directory the directory containing proto files
     * @param recursive whether to search subdirectories
     * @return list of parsed ProtoFiles
     * @throws GrpcParseException if parsing fails
     */
    List<ProtoFile> parseDirectory(File directory, boolean recursive) throws GrpcParseException;

    /**
     * Convert a ProtoFile to an ApiSpec (core model).
     *
     * @param protoFile the parsed proto file
     * @return the ApiSpec representation
     */
    ApiSpec toApiSpec(ProtoFile protoFile);

    /**
     * Convert multiple ProtoFiles to a single ApiSpec.
     *
     * @param protoFiles list of parsed proto files
     * @return the combined ApiSpec representation
     */
    ApiSpec toApiSpec(List<ProtoFile> protoFiles);

    /**
     * Check if the parser supports the given syntax version.
     *
     * @param syntax the syntax version (e.g., "proto2", "proto3")
     * @return true if supported
     */
    boolean supportsSyntax(String syntax);

    /**
     * Get the supported syntax versions.
     *
     * @return list of supported syntax versions
     */
    List<String> getSupportedSyntaxVersions();
}
