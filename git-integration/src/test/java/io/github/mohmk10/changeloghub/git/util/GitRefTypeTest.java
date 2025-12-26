package io.github.mohmk10.changeloghub.git.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitRefTypeTest {

    @Test
    void shouldDetectBranchFromRefsHeads() {
        assertThat(GitRefType.fromRef("refs/heads/main")).isEqualTo(GitRefType.BRANCH);
        assertThat(GitRefType.fromRef("refs/heads/feature/test")).isEqualTo(GitRefType.BRANCH);
    }

    @Test
    void shouldDetectBranchFromRefsRemotes() {
        assertThat(GitRefType.fromRef("refs/remotes/origin/main")).isEqualTo(GitRefType.BRANCH);
    }

    @Test
    void shouldDetectTagFromRefsTags() {
        assertThat(GitRefType.fromRef("refs/tags/v1.0.0")).isEqualTo(GitRefType.TAG);
        assertThat(GitRefType.fromRef("refs/tags/release-1.0")).isEqualTo(GitRefType.TAG);
    }

    @Test
    void shouldDetectCommitFromSha() {
        assertThat(GitRefType.fromRef("abc1234")).isEqualTo(GitRefType.COMMIT);
        assertThat(GitRefType.fromRef("abc1234567890")).isEqualTo(GitRefType.COMMIT);
        assertThat(GitRefType.fromRef("abc1234567890def1234567890abcdef12345678")).isEqualTo(GitRefType.COMMIT);
    }

    @Test
    void shouldDefaultToBranchForNamedRefs() {
        assertThat(GitRefType.fromRef("main")).isEqualTo(GitRefType.BRANCH);
        assertThat(GitRefType.fromRef("develop")).isEqualTo(GitRefType.BRANCH);
        assertThat(GitRefType.fromRef("feature/something")).isEqualTo(GitRefType.BRANCH);
    }

    @Test
    void shouldHandleNullAndEmpty() {
        assertThat(GitRefType.fromRef(null)).isEqualTo(GitRefType.COMMIT);
        assertThat(GitRefType.fromRef("")).isEqualTo(GitRefType.COMMIT);
    }

    @Test
    void shouldValidateCommitSha() {
        assertThat(GitRefType.isCommitSha("abc1234")).isTrue();
        assertThat(GitRefType.isCommitSha("ABC1234")).isTrue();
        assertThat(GitRefType.isCommitSha("abc1234567890def1234567890abcdef12345678")).isTrue();

        assertThat(GitRefType.isCommitSha("abc123")).isFalse(); // too short
        assertThat(GitRefType.isCommitSha("ghijkl")).isFalse(); // invalid hex
        assertThat(GitRefType.isCommitSha(null)).isFalse();
    }

    @Test
    void shouldValidateFullCommitSha() {
        assertThat(GitRefType.isFullCommitSha("abc1234567890def1234567890abcdef12345678")).isTrue();
        assertThat(GitRefType.isFullCommitSha("ABC1234567890DEF1234567890ABCDEF12345678")).isTrue();

        assertThat(GitRefType.isFullCommitSha("abc1234")).isFalse(); // too short
        assertThat(GitRefType.isFullCommitSha("abc1234567890def1234567890abcdef1234567890")).isFalse(); // too long
    }

    @Test
    void shouldValidateShortCommitSha() {
        assertThat(GitRefType.isShortCommitSha("abc1234")).isTrue();
        assertThat(GitRefType.isShortCommitSha("abc1234567890")).isTrue();
        assertThat(GitRefType.isShortCommitSha("abc1234567890def1234567890abcdef1234567")).isTrue(); // 39 chars

        assertThat(GitRefType.isShortCommitSha("abc123")).isFalse(); // 6 chars, too short
        assertThat(GitRefType.isShortCommitSha("abc1234567890def1234567890abcdef12345678")).isFalse(); // 40 chars, full SHA
    }

    @Test
    void shouldHaveCorrectDisplayNames() {
        assertThat(GitRefType.BRANCH.getDisplayName()).isEqualTo("branch");
        assertThat(GitRefType.TAG.getDisplayName()).isEqualTo("tag");
        assertThat(GitRefType.COMMIT.getDisplayName()).isEqualTo("commit");
    }

    @Test
    void shouldHaveCorrectRefPrefixes() {
        assertThat(GitRefType.BRANCH.getRefPrefix()).isEqualTo("refs/heads/");
        assertThat(GitRefType.TAG.getRefPrefix()).isEqualTo("refs/tags/");
        assertThat(GitRefType.COMMIT.getRefPrefix()).isEmpty();
    }
}
