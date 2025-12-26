package io.github.mohmk10.changeloghub.parser.grpc;

import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.parser.grpc.exception.GrpcParseException;
import io.github.mohmk10.changeloghub.parser.grpc.model.ProtoFile;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public interface GrpcParser {

    ProtoFile parse(String content) throws GrpcParseException;

    ProtoFile parse(String content, String fileName) throws GrpcParseException;

    ProtoFile parseFile(String filePath) throws GrpcParseException;

    ProtoFile parseFile(File file) throws GrpcParseException;

    ProtoFile parseFile(Path path) throws GrpcParseException;

    ProtoFile parseStream(InputStream inputStream) throws GrpcParseException;

    ProtoFile parseStream(InputStream inputStream, String fileName) throws GrpcParseException;

    ProtoFile parseUrl(URL url) throws GrpcParseException;

    ProtoFile parseUrl(String urlString) throws GrpcParseException;

    List<ProtoFile> parseDirectory(String directoryPath, boolean recursive) throws GrpcParseException;

    List<ProtoFile> parseDirectory(File directory, boolean recursive) throws GrpcParseException;

    ApiSpec toApiSpec(ProtoFile protoFile);

    ApiSpec toApiSpec(List<ProtoFile> protoFiles);

    boolean supportsSyntax(String syntax);

    List<String> getSupportedSyntaxVersions();
}
