package io.github.mohmk10.changeloghub.git.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class GitTagTest {

    @Test
    void shouldCreateTag() {
        GitTag tag = GitTag.builder()
            .name("v1.0.0")
            .commitId("abc1234567890def")
            .annotated(true)
            .message("Release 1.0.0")
            .build();

        assertThat(tag.getName()).isEqualTo("v1.0.0");
        assertThat(tag.getCommitId()).isEqualTo("abc1234567890def");
        assertThat(tag.isAnnotated()).isTrue();
        assertThat(tag.isLightweight()).isFalse();
        assertThat(tag.getMessage()).isEqualTo("Release 1.0.0");
    }

    @Test
    void shouldDetectSemanticVersion() {
        assertThat(GitTag.builder().name("v1.0.0").build().isSemanticVersion()).isTrue();
        assertThat(GitTag.builder().name("1.0.0").build().isSemanticVersion()).isTrue();
        assertThat(GitTag.builder().name("v2.3.4").build().isSemanticVersion()).isTrue();
        assertThat(GitTag.builder().name("v1.0.0-alpha").build().isSemanticVersion()).isTrue();
        assertThat(GitTag.builder().name("v1.0.0-beta.1").build().isSemanticVersion()).isTrue();
    }

    @Test
    void shouldRejectNonSemanticVersions() {
        assertThat(GitTag.builder().name("release").build().isSemanticVersion()).isFalse();
        assertThat(GitTag.builder().name("latest").build().isSemanticVersion()).isFalse();
        assertThat(GitTag.builder().name("v1").build().isSemanticVersion()).isFalse();
        assertThat(GitTag.builder().name("v1.0").build().isSemanticVersion()).isFalse();
    }

    @Test
    void shouldGetVersionWithoutPrefix() {
        assertThat(GitTag.builder().name("v1.0.0").build().getVersion()).isEqualTo("1.0.0");
        assertThat(GitTag.builder().name("1.0.0").build().getVersion()).isEqualTo("1.0.0");
        assertThat(GitTag.builder().name("v2.3.4-beta").build().getVersion()).isEqualTo("2.3.4-beta");
    }

    @Test
    void shouldHandleNullName() {
        GitTag tag = new GitTag();
        assertThat(tag.isSemanticVersion()).isFalse();
        assertThat(tag.getVersion()).isNull();
    }

    @Test
    void shouldGetShortCommitId() {
        GitTag tag = GitTag.builder()
            .name("v1.0.0")
            .commitId("abc1234567890def1234567890abcdef12345678")
            .build();

        assertThat(tag.getShortCommitId()).isEqualTo("abc1234");
    }

    @Test
    void shouldSetTaggerInfo() {
        LocalDateTime now = LocalDateTime.now();
        GitTag tag = GitTag.builder()
            .name("v1.0.0")
            .tagger("John Doe")
            .taggerEmail("john@example.com")
            .date(now)
            .build();

        assertThat(tag.getTagger()).isEqualTo("John Doe");
        assertThat(tag.getTaggerEmail()).isEqualTo("john@example.com");
        assertThat(tag.getDate()).isEqualTo(now);
    }

    @Test
    void shouldDistinguishLightweightAndAnnotated() {
        GitTag annotated = GitTag.builder().name("v1.0.0").annotated(true).build();
        GitTag lightweight = GitTag.builder().name("v1.0.0").annotated(false).build();

        assertThat(annotated.isAnnotated()).isTrue();
        assertThat(annotated.isLightweight()).isFalse();
        assertThat(lightweight.isAnnotated()).isFalse();
        assertThat(lightweight.isLightweight()).isTrue();
    }

    @Test
    void shouldImplementEquals() {
        GitTag tag1 = GitTag.builder().name("v1.0.0").commitId("abc123").build();
        GitTag tag2 = GitTag.builder().name("v1.0.0").commitId("abc123").build();
        GitTag tag3 = GitTag.builder().name("v1.0.0").commitId("def456").build();

        assertThat(tag1).isEqualTo(tag2);
        assertThat(tag1.hashCode()).isEqualTo(tag2.hashCode());
        assertThat(tag1).isNotEqualTo(tag3);
    }

    @Test
    void shouldImplementToString() {
        GitTag tag = GitTag.builder()
            .name("v1.0.0")
            .commitId("abc1234567890")
            .annotated(true)
            .build();

        String str = tag.toString();
        assertThat(str).contains("v1.0.0");
        assertThat(str).contains("abc1234");
        assertThat(str).contains("annotated=true");
    }
}
