package io.github.mohmk10.changeloghub.git.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitConstantsTest {

    @Test
    void shouldHaveCorrectRefsPrefixes() {
        assertThat(GitConstants.REFS_HEADS_PREFIX).isEqualTo("refs/heads/");
        assertThat(GitConstants.REFS_TAGS_PREFIX).isEqualTo("refs/tags/");
        assertThat(GitConstants.REFS_REMOTES_PREFIX).isEqualTo("refs/remotes/");
    }

    @Test
    void shouldHaveCommonRefs() {
        assertThat(GitConstants.HEAD).isEqualTo("HEAD");
        assertThat(GitConstants.MAIN).isEqualTo("main");
        assertThat(GitConstants.MASTER).isEqualTo("master");
        assertThat(GitConstants.DEVELOP).isEqualTo("develop");
        assertThat(GitConstants.ORIGIN).isEqualTo("origin");
    }

    @Test
    void shouldContainOpenApiExtensions() {
        assertThat(GitConstants.OPENAPI_EXTENSIONS).contains("yaml", "yml", "json");
    }

    @Test
    void shouldContainGraphQLExtensions() {
        assertThat(GitConstants.GRAPHQL_EXTENSIONS).contains("graphql", "gql", "graphqls");
    }

    @Test
    void shouldContainProtobufExtensions() {
        assertThat(GitConstants.PROTOBUF_EXTENSIONS).contains("proto");
    }

    @Test
    void shouldContainSpringExtensions() {
        assertThat(GitConstants.SPRING_EXTENSIONS).contains("java", "kt", "groovy");
    }

    @Test
    void shouldDetectSpecExtension() {
        assertThat(GitConstants.isSpecExtension("yaml")).isTrue();
        assertThat(GitConstants.isSpecExtension("json")).isTrue();
        assertThat(GitConstants.isSpecExtension("graphql")).isTrue();
        assertThat(GitConstants.isSpecExtension("proto")).isTrue();
        assertThat(GitConstants.isSpecExtension("java")).isTrue();

        assertThat(GitConstants.isSpecExtension("txt")).isFalse();
        assertThat(GitConstants.isSpecExtension("md")).isFalse();
        assertThat(GitConstants.isSpecExtension(null)).isFalse();
    }

    @Test
    void shouldDetectIgnoredDirectory() {
        assertThat(GitConstants.isIgnoredDirectory(".git")).isTrue();
        assertThat(GitConstants.isIgnoredDirectory("node_modules")).isTrue();
        assertThat(GitConstants.isIgnoredDirectory("target")).isTrue();
        assertThat(GitConstants.isIgnoredDirectory("build")).isTrue();
        assertThat(GitConstants.isIgnoredDirectory("vendor")).isTrue();

        assertThat(GitConstants.isIgnoredDirectory("src")).isFalse();
        assertThat(GitConstants.isIgnoredDirectory("api")).isFalse();
        assertThat(GitConstants.isIgnoredDirectory(null)).isFalse();
    }

    @Test
    void shouldDetectSpecDirectory() {
        assertThat(GitConstants.isSpecDirectory("api")).isTrue();
        assertThat(GitConstants.isSpecDirectory("specs")).isTrue();
        assertThat(GitConstants.isSpecDirectory("openapi")).isTrue();
        assertThat(GitConstants.isSpecDirectory("graphql")).isTrue();
        assertThat(GitConstants.isSpecDirectory("proto")).isTrue();
        assertThat(GitConstants.isSpecDirectory("asyncapi")).isTrue();

        assertThat(GitConstants.isSpecDirectory("src")).isFalse();
        assertThat(GitConstants.isSpecDirectory("lib")).isFalse();
        assertThat(GitConstants.isSpecDirectory(null)).isFalse();
    }

    @Test
    void shouldValidateSemanticVersion() {
        assertThat(GitConstants.isSemanticVersion("1.0.0")).isTrue();
        assertThat(GitConstants.isSemanticVersion("v1.0.0")).isTrue();
        assertThat(GitConstants.isSemanticVersion("v2.3.4")).isTrue();
        assertThat(GitConstants.isSemanticVersion("1.0.0-alpha")).isTrue();
        assertThat(GitConstants.isSemanticVersion("1.0.0-beta.1")).isTrue();
        assertThat(GitConstants.isSemanticVersion("1.0.0+build")).isTrue();

        assertThat(GitConstants.isSemanticVersion("1.0")).isFalse();
        assertThat(GitConstants.isSemanticVersion("v1")).isFalse();
        assertThat(GitConstants.isSemanticVersion("release")).isFalse();
        assertThat(GitConstants.isSemanticVersion(null)).isFalse();
    }

    @Test
    void shouldValidateCommitSha() {
        assertThat(GitConstants.isCommitSha("abc1234")).isTrue();
        assertThat(GitConstants.isCommitSha("abc1234567890def1234567890abcdef12345678")).isTrue();

        assertThat(GitConstants.isCommitSha("abc")).isFalse();
        assertThat(GitConstants.isCommitSha("ghijkl")).isFalse();
        assertThat(GitConstants.isCommitSha(null)).isFalse();
    }

    @Test
    void shouldValidateConventionalCommitType() {
        assertThat(GitConstants.isConventionalCommitType("feat")).isTrue();
        assertThat(GitConstants.isConventionalCommitType("fix")).isTrue();
        assertThat(GitConstants.isConventionalCommitType("docs")).isTrue();
        assertThat(GitConstants.isConventionalCommitType("chore")).isTrue();
        assertThat(GitConstants.isConventionalCommitType("refactor")).isTrue();
        assertThat(GitConstants.isConventionalCommitType("test")).isTrue();
        assertThat(GitConstants.isConventionalCommitType("ci")).isTrue();
        assertThat(GitConstants.isConventionalCommitType("build")).isTrue();

        assertThat(GitConstants.isConventionalCommitType("random")).isFalse();
        assertThat(GitConstants.isConventionalCommitType(null)).isFalse();
    }

    @Test
    void shouldHaveDefaultValues() {
        assertThat(GitConstants.DEFAULT_COMMIT_LIMIT).isEqualTo(100);
        assertThat(GitConstants.DEFAULT_SHORT_SHA_LENGTH).isEqualTo(7);
        assertThat(GitConstants.DEFAULT_ENCODING).isEqualTo("UTF-8");
    }

    @Test
    void shouldContainConventionalCommitTypes() {
        assertThat(GitConstants.CONVENTIONAL_COMMIT_TYPES).contains(
            "feat", "feature", "fix", "bugfix", "docs", "style",
            "refactor", "perf", "test", "tests", "build", "ci", "chore", "revert"
        );
    }

    @Test
    void shouldContainBreakingChangeKeywords() {
        assertThat(GitConstants.BREAKING_CHANGE_KEYWORDS).contains(
            "BREAKING CHANGE", "BREAKING-CHANGE", "BREAKING CHANGES"
        );
    }
}
