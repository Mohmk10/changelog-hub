package io.github.mohmk10.changeloghub.parser.openapi.integration;

import io.github.mohmk10.changeloghub.core.comparator.ApiComparator;
import io.github.mohmk10.changeloghub.core.comparator.impl.DefaultApiComparator;
import io.github.mohmk10.changeloghub.core.detector.BreakingChangeDetector;
import io.github.mohmk10.changeloghub.core.detector.SeverityClassifier;
import io.github.mohmk10.changeloghub.core.detector.impl.DefaultBreakingChangeDetector;
import io.github.mohmk10.changeloghub.core.detector.impl.DefaultSeverityClassifier;
import io.github.mohmk10.changeloghub.core.generator.ChangelogGenerator;
import io.github.mohmk10.changeloghub.core.generator.impl.DefaultChangelogGenerator;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.reporter.ReportFormat;
import io.github.mohmk10.changeloghub.core.reporter.Reporter;
import io.github.mohmk10.changeloghub.core.reporter.ReporterFactory;
import io.github.mohmk10.changeloghub.core.service.AnalysisService;
import io.github.mohmk10.changeloghub.core.service.impl.DefaultAnalysisService;
import io.github.mohmk10.changeloghub.parser.openapi.OpenApiParser;
import io.github.mohmk10.changeloghub.parser.openapi.impl.DefaultOpenApiParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class IntegrationTestHelper {

    private static final String INTEGRATION_RESOURCE_PATH = "integration/";

    private final OpenApiParser parser;
    private final ApiComparator comparator;
    private final BreakingChangeDetector breakingChangeDetector;
    private final SeverityClassifier severityClassifier;
    private final AnalysisService analysisService;
    private final ChangelogGenerator changelogGenerator;

    public IntegrationTestHelper() {
        this.parser = new DefaultOpenApiParser();
        this.comparator = new DefaultApiComparator();
        this.breakingChangeDetector = new DefaultBreakingChangeDetector();
        this.severityClassifier = new DefaultSeverityClassifier();
        this.analysisService = new DefaultAnalysisService();
        this.changelogGenerator = new DefaultChangelogGenerator();
    }

    public String loadResource(String fileName) throws IOException {
        String resourcePath = INTEGRATION_RESOURCE_PATH + fileName;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public ApiSpec parseApiSpec(String fileName) throws IOException {
        String content = loadResource(fileName);
        return parser.parse(content);
    }

    public OpenApiParser getParser() {
        return parser;
    }

    public ApiComparator getComparator() {
        return comparator;
    }

    public BreakingChangeDetector getBreakingChangeDetector() {
        return breakingChangeDetector;
    }

    public SeverityClassifier getSeverityClassifier() {
        return severityClassifier;
    }

    public AnalysisService getAnalysisService() {
        return analysisService;
    }

    public ChangelogGenerator getChangelogGenerator() {
        return changelogGenerator;
    }

    public Reporter getReporter(ReportFormat format) {
        return ReporterFactory.create(format);
    }
}
