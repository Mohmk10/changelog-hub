package io.github.mohmk10.changeloghub.git.model;

import io.github.mohmk10.changeloghub.git.util.GitRefType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class GitRefTest {

    @Test
    void shouldCreateBranchRef() {
        GitRef ref = GitRef.builder()
            .name("main")
            .type(GitRefType.BRANCH)
            .commitId("abc1234567890def")
            .build();

        assertThat(ref.getName()).isEqualTo("main");
        assertThat(ref.getType()).isEqualTo(GitRefType.BRANCH);
        assertThat(ref.isBranch()).isTrue();
        assertThat(ref.isTag()).isFalse();
        assertThat(ref.isCommit()).isFalse();
    }

    @Test
    void shouldCreateTagRef() {
        GitRef ref = GitRef.builder()
            .name("v1.0.0")
            .type(GitRefType.TAG)
            .commitId("abc1234567890def")
            .build();

        assertThat(ref.getName()).isEqualTo("v1.0.0");
        assertThat(ref.getType()).isEqualTo(GitRefType.TAG);
        assertThat(ref.isBranch()).isFalse();
        assertThat(ref.isTag()).isTrue();
        assertThat(ref.isCommit()).isFalse();
    }

    @Test
    void shouldCreateCommitRef() {
        GitRef ref = GitRef.builder()
            .name("abc1234567890def")
            .type(GitRefType.COMMIT)
            .commitId("abc1234567890def")
            .build();

        assertThat(ref.isCommit()).isTrue();
        assertThat(ref.isBranch()).isFalse();
        assertThat(ref.isTag()).isFalse();
    }

    @Test
    void shouldGetShortCommitId() {
        GitRef ref = GitRef.builder()
            .name("main")
            .type(GitRefType.BRANCH)
            .commitId("abc1234567890def1234567890abcdef12345678")
            .build();

        assertThat(ref.getShortCommitId()).isEqualTo("abc1234");
    }

    @Test
    void shouldHandleShortCommitId() {
        GitRef ref = GitRef.builder()
            .name("main")
            .type(GitRefType.BRANCH)
            .commitId("abc")
            .build();

        assertThat(ref.getShortCommitId()).isEqualTo("abc");
    }

    @Test
    void shouldHandleNullCommitId() {
        GitRef ref = GitRef.builder()
            .name("main")
            .type(GitRefType.BRANCH)
            .build();

        assertThat(ref.getShortCommitId()).isNull();
    }

    @Test
    void shouldSetDate() {
        LocalDateTime now = LocalDateTime.now();
        GitRef ref = GitRef.builder()
            .name("main")
            .type(GitRefType.BRANCH)
            .date(now)
            .build();

        assertThat(ref.getDate()).isEqualTo(now);
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        GitRef ref1 = GitRef.builder()
            .name("main")
            .type(GitRefType.BRANCH)
            .commitId("abc123")
            .build();

        GitRef ref2 = GitRef.builder()
            .name("main")
            .type(GitRefType.BRANCH)
            .commitId("abc123")
            .build();

        GitRef ref3 = GitRef.builder()
            .name("develop")
            .type(GitRefType.BRANCH)
            .commitId("abc123")
            .build();

        assertThat(ref1).isEqualTo(ref2);
        assertThat(ref1.hashCode()).isEqualTo(ref2.hashCode());
        assertThat(ref1).isNotEqualTo(ref3);
    }

    @Test
    void shouldImplementToString() {
        GitRef ref = GitRef.builder()
            .name("main")
            .type(GitRefType.BRANCH)
            .commitId("abc1234567890")
            .build();

        String str = ref.toString();
        assertThat(str).contains("main");
        assertThat(str).contains("BRANCH");
    }
}
