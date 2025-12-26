package io.github.mohmk10.changeloghub.git.extractor;

import io.github.mohmk10.changeloghub.core.generator.ChangelogGenerator;
import io.github.mohmk10.changeloghub.core.generator.impl.DefaultChangelogGenerator;
import io.github.mohmk10.changeloghub.core.model.ApiSpec;
import io.github.mohmk10.changeloghub.core.model.Changelog;
import io.github.mohmk10.changeloghub.git.config.GitConfig;
import io.github.mohmk10.changeloghub.git.model.GitDiff;
import io.github.mohmk10.changeloghub.git.model.GitFileContent;
import io.github.mohmk10.changeloghub.parser.asyncapi.DefaultAsyncApiParser;
import io.github.mohmk10.changeloghub.parser.asyncapi.mapper.AsyncApiModelMapper;
import io.github.mohmk10.changeloghub.parser.graphql.DefaultGraphQLParser;
import io.github.mohmk10.changeloghub.parser.graphql.mapper.GraphQLModelMapper;
import io.github.mohmk10.changeloghub.parser.grpc.DefaultGrpcParser;
import io.github.mohmk10.changeloghub.parser.grpc.mapper.GrpcModelMapper;
import io.github.mohmk10.changeloghub.parser.openapi.OpenApiParser;
import io.github.mohmk10.changeloghub.parser.openapi.impl.DefaultOpenApiParser;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ChangelogExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ChangelogExtractor.class);

    private final Repository repository;
    private final FileExtractor fileExtractor;
    private final SpecFileDetector specFileDetector;
    private final GitConfig config;

    private final OpenApiParser openApiParser;
    private final DefaultGraphQLParser graphQLParser;
    private final DefaultGrpcParser grpcParser;
    private final DefaultAsyncApiParser asyncApiParser;

    private final GraphQLModelMapper graphQLModelMapper;
    private final GrpcModelMapper grpcModelMapper;
    private final AsyncApiModelMapper asyncApiModelMapper;

    private final ChangelogGenerator changelogGenerator;

    public ChangelogExtractor(Repository repository) {
        this(repository, new GitConfig());
    }

    public ChangelogExtractor(Repository repository, GitConfig config) {
        this.repository = repository;
        this.config = config;
        this.fileExtractor = new FileExtractor(repository, config);
        this.specFileDetector = new SpecFileDetector(repository, config);

        this.openApiParser = new DefaultOpenApiParser();
        this.graphQLParser = new DefaultGraphQLParser();
        this.grpcParser = new DefaultGrpcParser();
        this.asyncApiParser = new DefaultAsyncApiParser();

        this.graphQLModelMapper = new GraphQLModelMapper();
        this.grpcModelMapper = new GrpcModelMapper();
        this.asyncApiModelMapper = new AsyncApiModelMapper();

        this.changelogGenerator = new DefaultChangelogGenerator();
    }

    public List<SpecChangelog> extractChangelogs(String oldRef, String newRef) {
        List<SpecChangelog> changelogs = new ArrayList<>();

        Map<SpecFileDetector.SpecType, List<String>> oldSpecs = specFileDetector.detectSpecs(oldRef);
        Map<SpecFileDetector.SpecType, List<String>> newSpecs = specFileDetector.detectSpecs(newRef);

        Set<String> allSpecFiles = new HashSet<>();
        oldSpecs.values().forEach(allSpecFiles::addAll);
        newSpecs.values().forEach(allSpecFiles::addAll);

        logger.info("Found {} spec files to compare between {} and {}", allSpecFiles.size(), oldRef, newRef);

        for (String specPath : allSpecFiles) {
            try {
                SpecChangelog changelog = extractChangelogForFile(specPath, oldRef, newRef);
                if (changelog != null && changelog.hasChanges()) {
                    changelogs.add(changelog);
                }
            } catch (Exception e) {
                logger.warn("Failed to extract changelog for: {}", specPath, e);
            }
        }

        return changelogs;
    }

    public SpecChangelog extractChangelogForFile(String specPath, String oldRef, String newRef) {
        SpecFileDetector.SpecType specType = specFileDetector.detectSpecType(specPath, newRef);
        if (specType == null) {
            specType = specFileDetector.detectSpecType(specPath, oldRef);
        }

        if (specType == null) {
            logger.debug("Could not detect spec type for: {}", specPath);
            return null;
        }

        Optional<GitFileContent> oldContent = fileExtractor.extractFile(specPath, oldRef);
        Optional<GitFileContent> newContent = fileExtractor.extractFile(specPath, newRef);

        boolean oldExists = oldContent.isPresent() && oldContent.get().exists();
        boolean newExists = newContent.isPresent() && newContent.get().exists();

        if (!oldExists && !newExists) {
            return null;
        }

        try {
            ApiSpec oldSpec = oldExists ? parseSpec(specType, oldContent.get()) : null;
            ApiSpec newSpec = newExists ? parseSpec(specType, newContent.get()) : null;

            Changelog changelog = changelogGenerator.generate(oldSpec, newSpec);

            return new SpecChangelog(
                specPath,
                specType,
                oldRef,
                newRef,
                changelog,
                !oldExists, 
                !newExists  
            );
        } catch (Exception e) {
            logger.warn("Failed to parse spec: {} - {}", specPath, e.getMessage());
            return null;
        }
    }

    public List<SpecChangelog> extractChangelogsForChangedFiles(GitDiff diff, String oldRef, String newRef) {
        List<SpecChangelog> changelogs = new ArrayList<>();

        Set<String> changedSpecFiles = new HashSet<>();

        for (String file : diff.getAddedFiles()) {
            if (isSpecFile(file, newRef)) {
                changedSpecFiles.add(file);
            }
        }

        for (String file : diff.getModifiedFiles()) {
            if (isSpecFile(file, newRef) || isSpecFile(file, oldRef)) {
                changedSpecFiles.add(file);
            }
        }

        for (String file : diff.getDeletedFiles()) {
            if (isSpecFile(file, oldRef)) {
                changedSpecFiles.add(file);
            }
        }

        logger.info("Found {} changed spec files", changedSpecFiles.size());

        for (String specPath : changedSpecFiles) {
            try {
                SpecChangelog changelog = extractChangelogForFile(specPath, oldRef, newRef);
                if (changelog != null) {
                    changelogs.add(changelog);
                }
            } catch (Exception e) {
                logger.warn("Failed to extract changelog for changed file: {}", specPath, e);
            }
        }

        return changelogs;
    }

    public ChangelogSummary summarize(List<SpecChangelog> changelogs) {
        int totalChanges = 0;
        int breakingChanges = 0;
        int newSpecs = 0;
        int deletedSpecs = 0;

        Map<SpecFileDetector.SpecType, Integer> changesByType = new EnumMap<>(SpecFileDetector.SpecType.class);

        for (SpecChangelog sc : changelogs) {
            if (sc.isNew()) newSpecs++;
            if (sc.isDeleted()) deletedSpecs++;

            Changelog cl = sc.getChangelog();
            if (cl != null) {
                totalChanges += cl.getChanges().size();
                breakingChanges += cl.getBreakingChanges().size();
            }

            changesByType.merge(sc.getSpecType(), 1, Integer::sum);
        }

        return new ChangelogSummary(
            changelogs.size(),
            totalChanges,
            breakingChanges,
            newSpecs,
            deletedSpecs,
            changesByType
        );
    }

    private boolean isSpecFile(String filePath, String ref) {
        SpecFileDetector.SpecType type = specFileDetector.detectSpecType(filePath, ref);
        return type != null;
    }

    private ApiSpec parseSpec(SpecFileDetector.SpecType type, GitFileContent content) {
        String specContent = content.getContent();
        if (specContent == null || specContent.isEmpty()) {
            return null;
        }

        switch (type) {
            case OPENAPI:
                return openApiParser.parse(specContent);

            case GRAPHQL:
                return graphQLModelMapper.mapToApiSpec(graphQLParser.parse(specContent));

            case PROTOBUF:
                return grpcModelMapper.mapProtoFile(grpcParser.parse(specContent));

            case ASYNCAPI:
                return asyncApiModelMapper.map(asyncApiParser.parse(specContent));

            case SPRING:

                logger.debug("Spring controller parsing not yet supported via Git extraction");
                return null;

            default:
                return null;
        }
    }

    public static class SpecChangelog {
        private final String specPath;
        private final SpecFileDetector.SpecType specType;
        private final String oldRef;
        private final String newRef;
        private final Changelog changelog;
        private final boolean isNew;
        private final boolean isDeleted;

        public SpecChangelog(String specPath, SpecFileDetector.SpecType specType,
                            String oldRef, String newRef, Changelog changelog,
                            boolean isNew, boolean isDeleted) {
            this.specPath = specPath;
            this.specType = specType;
            this.oldRef = oldRef;
            this.newRef = newRef;
            this.changelog = changelog;
            this.isNew = isNew;
            this.isDeleted = isDeleted;
        }

        public String getSpecPath() {
            return specPath;
        }

        public SpecFileDetector.SpecType getSpecType() {
            return specType;
        }

        public String getOldRef() {
            return oldRef;
        }

        public String getNewRef() {
            return newRef;
        }

        public Changelog getChangelog() {
            return changelog;
        }

        public boolean isNew() {
            return isNew;
        }

        public boolean isDeleted() {
            return isDeleted;
        }

        public boolean hasChanges() {
            return isNew || isDeleted ||
                   (changelog != null && !changelog.getChanges().isEmpty());
        }

        public boolean hasBreakingChanges() {
            return isDeleted ||
                   (changelog != null && !changelog.getBreakingChanges().isEmpty());
        }

        @Override
        public String toString() {
            return "SpecChangelog{" +
                   "specPath='" + specPath + '\'' +
                   ", specType=" + specType +
                   ", isNew=" + isNew +
                   ", isDeleted=" + isDeleted +
                   ", changes=" + (changelog != null ? changelog.getChanges().size() : 0) +
                   '}';
        }
    }

    public static class ChangelogSummary {
        private final int totalSpecs;
        private final int totalChanges;
        private final int breakingChanges;
        private final int newSpecs;
        private final int deletedSpecs;
        private final Map<SpecFileDetector.SpecType, Integer> changesByType;

        public ChangelogSummary(int totalSpecs, int totalChanges, int breakingChanges,
                               int newSpecs, int deletedSpecs,
                               Map<SpecFileDetector.SpecType, Integer> changesByType) {
            this.totalSpecs = totalSpecs;
            this.totalChanges = totalChanges;
            this.breakingChanges = breakingChanges;
            this.newSpecs = newSpecs;
            this.deletedSpecs = deletedSpecs;
            this.changesByType = changesByType;
        }

        public int getTotalSpecs() {
            return totalSpecs;
        }

        public int getTotalChanges() {
            return totalChanges;
        }

        public int getBreakingChanges() {
            return breakingChanges;
        }

        public int getNewSpecs() {
            return newSpecs;
        }

        public int getDeletedSpecs() {
            return deletedSpecs;
        }

        public Map<SpecFileDetector.SpecType, Integer> getChangesByType() {
            return Collections.unmodifiableMap(changesByType);
        }

        public boolean hasBreakingChanges() {
            return breakingChanges > 0 || deletedSpecs > 0;
        }

        @Override
        public String toString() {
            return "ChangelogSummary{" +
                   "totalSpecs=" + totalSpecs +
                   ", totalChanges=" + totalChanges +
                   ", breakingChanges=" + breakingChanges +
                   ", newSpecs=" + newSpecs +
                   ", deletedSpecs=" + deletedSpecs +
                   '}';
        }
    }
}
