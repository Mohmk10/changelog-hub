package io.github.mohmk10.changeloghub.parser.spring.impl;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.utils.SourceRoot;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.parser.spring.SpringParser;
import io.github.mohmk10.changeloghub.parser.spring.analyzer.ControllerAnalyzer;
import io.github.mohmk10.changeloghub.parser.spring.analyzer.ParameterAnalyzer;
import io.github.mohmk10.changeloghub.parser.spring.analyzer.RequestMappingAnalyzer;
import io.github.mohmk10.changeloghub.parser.spring.analyzer.ResponseAnalyzer;
import io.github.mohmk10.changeloghub.parser.spring.exception.SpringParseException;
import io.github.mohmk10.changeloghub.parser.spring.mapper.SpringModelMapper;
import io.github.mohmk10.changeloghub.parser.spring.model.SpringController;
import io.github.mohmk10.changeloghub.parser.spring.model.SpringMethod;
import io.github.mohmk10.changeloghub.parser.spring.model.SpringParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of SpringParser.
 * Uses JavaParser to parse Java source files and extract Spring annotations.
 */
public class DefaultSpringParser implements SpringParser {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSpringParser.class);

    private final ControllerAnalyzer controllerAnalyzer;
    private final RequestMappingAnalyzer requestMappingAnalyzer;
    private final ParameterAnalyzer parameterAnalyzer;
    private final ResponseAnalyzer responseAnalyzer;
    private final SpringModelMapper modelMapper;
    private final com.github.javaparser.JavaParser javaParser;

    public DefaultSpringParser() {
        this.controllerAnalyzer = new ControllerAnalyzer();
        this.requestMappingAnalyzer = new RequestMappingAnalyzer();
        this.parameterAnalyzer = new ParameterAnalyzer();
        this.responseAnalyzer = new ResponseAnalyzer();
        this.modelMapper = new SpringModelMapper();

        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        this.javaParser = new com.github.javaparser.JavaParser(config);
    }

    @Override
    public ApiSpec parse(Path sourceDirectory) throws SpringParseException {
        return parse(sourceDirectory, null, null);
    }

    @Override
    public ApiSpec parse(Path sourceDirectory, String apiName, String apiVersion) throws SpringParseException {
        if (!Files.exists(sourceDirectory)) {
            throw SpringParseException.directoryNotFound(sourceDirectory.toString());
        }

        if (!Files.isDirectory(sourceDirectory)) {
            throw new SpringParseException("Path is not a directory: " + sourceDirectory);
        }

        logger.info("Parsing Spring controllers from: {}", sourceDirectory);

        List<Path> javaFiles = findJavaFiles(sourceDirectory);
        logger.debug("Found {} Java files", javaFiles.size());

        List<SpringController> controllers = parseControllers(javaFiles);
        logger.info("Found {} Spring controllers", controllers.size());

        return modelMapper.mapToApiSpec(controllers, apiName, apiVersion);
    }

    @Override
    public ApiSpec parse(List<Path> javaFiles) throws SpringParseException {
        List<SpringController> controllers = parseControllers(javaFiles);
        return modelMapper.mapToApiSpec(controllers, null, null);
    }

    @Override
    public ApiSpec parseFile(Path javaFile) throws SpringParseException {
        if (!Files.exists(javaFile)) {
            throw SpringParseException.fileNotFound(javaFile.toString());
        }

        Optional<SpringController> controller = parseController(javaFile);

        if (controller.isEmpty()) {
            throw new SpringParseException("No Spring controller found in file: " + javaFile);
        }

        return modelMapper.mapToApiSpec(controller.get());
    }

    @Override
    public boolean isSpringController(Path javaFile) {
        try {
            Optional<CompilationUnit> cu = parseJavaFile(javaFile);
            if (cu.isEmpty()) {
                return false;
            }

            return cu.get().findAll(ClassOrInterfaceDeclaration.class).stream()
                    .anyMatch(controllerAnalyzer::isController);
        } catch (Exception e) {
            logger.debug("Error checking if file is controller: {}", javaFile, e);
            return false;
        }
    }

    private List<Path> findJavaFiles(Path directory) throws SpringParseException {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new SpringParseException("Failed to scan directory: " + directory, e);
        }
    }

    private List<SpringController> parseControllers(List<Path> javaFiles) throws SpringParseException {
        List<SpringController> controllers = new ArrayList<>();

        for (Path file : javaFiles) {
            try {
                Optional<SpringController> controller = parseController(file);
                controller.ifPresent(controllers::add);
            } catch (Exception e) {
                logger.warn("Failed to parse file: {}", file, e);
                // Continue with other files
            }
        }

        return controllers;
    }

    private Optional<SpringController> parseController(Path javaFile) throws SpringParseException {
        Optional<CompilationUnit> cuOpt = parseJavaFile(javaFile);

        if (cuOpt.isEmpty()) {
            return Optional.empty();
        }

        CompilationUnit cu = cuOpt.get();

        // Find controller classes
        List<ClassOrInterfaceDeclaration> controllerClasses = cu.findAll(ClassOrInterfaceDeclaration.class)
                .stream()
                .filter(controllerAnalyzer::isController)
                .collect(Collectors.toList());

        if (controllerClasses.isEmpty()) {
            return Optional.empty();
        }

        // Parse the first controller class
        ClassOrInterfaceDeclaration clazz = controllerClasses.get(0);

        SpringController controller = new SpringController();
        controller.setClassName(controllerAnalyzer.getClassName(clazz));
        controller.setPackageName(cu.getPackageDeclaration().map(pd -> pd.getNameAsString()).orElse(""));
        controller.setBasePath(controllerAnalyzer.getBasePath(clazz));
        controller.setDeprecated(controllerAnalyzer.isDeprecated(clazz));
        controller.setProduces(controllerAnalyzer.getProduces(clazz));
        controller.setConsumes(controllerAnalyzer.getConsumes(clazz));

        // Parse methods
        List<MethodDeclaration> endpointMethods = controllerAnalyzer.getEndpointMethods(clazz);
        for (MethodDeclaration method : endpointMethods) {
            SpringMethod springMethod = parseMethod(method, controller);
            controller.addMethod(springMethod);
        }

        logger.debug("Parsed controller: {} with {} endpoints", controller.getClassName(), controller.getMethods().size());

        return Optional.of(controller);
    }

    private SpringMethod parseMethod(MethodDeclaration method, SpringController controller) {
        SpringMethod springMethod = new SpringMethod();

        springMethod.setMethodName(requestMappingAnalyzer.getOperationId(method));
        springMethod.setHttpMethod(requestMappingAnalyzer.getHttpMethod(method));
        springMethod.setPath(requestMappingAnalyzer.getPath(method));
        springMethod.setDeprecated(requestMappingAnalyzer.isDeprecated(method));

        // Set produces/consumes (method level overrides controller level)
        List<String> produces = requestMappingAnalyzer.getProduces(method);
        springMethod.setProduces(produces.isEmpty() ? controller.getProduces() : produces);

        List<String> consumes = requestMappingAnalyzer.getConsumes(method);
        springMethod.setConsumes(consumes.isEmpty() ? controller.getConsumes() : consumes);

        // Response status
        requestMappingAnalyzer.getResponseStatus(method).ifPresent(springMethod::setResponseStatus);

        // Return type
        springMethod.setReturnType(responseAnalyzer.getActualReturnType(method));

        // Parameters
        List<SpringParameter> parameters = parameterAnalyzer.analyzeParameters(method);
        springMethod.setParameters(parameters);

        return springMethod;
    }

    private Optional<CompilationUnit> parseJavaFile(Path javaFile) throws SpringParseException {
        try {
            ParseResult<CompilationUnit> result = javaParser.parse(javaFile);

            if (result.isSuccessful() && result.getResult().isPresent()) {
                return result.getResult();
            }

            if (!result.getProblems().isEmpty()) {
                logger.debug("Parse problems in {}: {}", javaFile, result.getProblems());
            }

            return Optional.empty();
        } catch (IOException e) {
            throw SpringParseException.parseError(javaFile.toString(), e);
        }
    }
}
