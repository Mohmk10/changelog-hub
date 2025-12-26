package io.github.mohmk10.changeloghub.git.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GitCommitTest {

    @Test
    void shouldCreateCommit() {
        GitCommit commit = GitCommit.builder()
            .id("abc1234567890def1234567890abcdef12345678")
            .message("feat: add new feature")
            .author("John Doe")
            .email("john@example.com")
            .build();

        assertThat(commit.getId()).isEqualTo("abc1234567890def1234567890abcdef12345678");
        assertThat(commit.getShortId()).isEqualTo("abc1234");
        assertThat(commit.getMessage()).isEqualTo("feat: add new feature");
        assertThat(commit.getAuthor()).isEqualTo("John Doe");
        assertThat(commit.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void shouldGetMessageFirstLine() {
        GitCommit commit = GitCommit.builder()
            .id("abc123")
            .message("feat: add feature\n\nThis is the body of the commit")
            .build();

        assertThat(commit.getMessageFirstLine()).isEqualTo("feat: add feature");
    }

    @Test
    void shouldHandleSingleLineMessage() {
        GitCommit commit = GitCommit.builder()
            .id("abc123")
            .message("fix: simple fix")
            .build();

        assertThat(commit.getMessageFirstLine()).isEqualTo("fix: simple fix");
    }

    @Test
    void shouldHandleNullMessage() {
        GitCommit commit = new GitCommit();
        assertThat(commit.getMessageFirstLine()).isNull();
    }

    @Test
    void shouldDetectMergeCommit() {
        GitCommit commit = GitCommit.builder()
            .id("abc123")
            .parentIds(Arrays.asList("parent1", "parent2"))
            .build();

        assertThat(commit.isMergeCommit()).isTrue();
    }

    @Test
    void shouldDetectNonMergeCommit() {
        GitCommit commit = GitCommit.builder()
            .id("abc123")
            .parentIds(Arrays.asList("parent1"))
            .build();

        assertThat(commit.isMergeCommit()).isFalse();
    }

    @Test
    void shouldHandleChangedFiles() {
        GitCommit commit = new GitCommit();
        commit.addChangedFile("file1.txt");
        commit.addChangedFile("file2.txt");

        assertThat(commit.getChangedFiles()).containsExactly("file1.txt", "file2.txt");
    }

    @Test
    void shouldSetChangedFiles() {
        GitCommit commit = new GitCommit();
        commit.setChangedFiles(Arrays.asList("a.txt", "b.txt"));

        assertThat(commit.getChangedFiles()).containsExactly("a.txt", "b.txt");
    }

    @Test
    void shouldSetDate() {
        LocalDateTime now = LocalDateTime.now();
        GitCommit commit = GitCommit.builder()
            .id("abc123")
            .date(now)
            .build();

        assertThat(commit.getDate()).isEqualTo(now);
    }

    @Test
    void shouldImplementEqualsBasedOnId() {
        GitCommit commit1 = GitCommit.builder().id("abc123").message("msg1").build();
        GitCommit commit2 = GitCommit.builder().id("abc123").message("msg2").build();
        GitCommit commit3 = GitCommit.builder().id("def456").message("msg1").build();

        assertThat(commit1).isEqualTo(commit2);
        assertThat(commit1.hashCode()).isEqualTo(commit2.hashCode());
        assertThat(commit1).isNotEqualTo(commit3);
    }

    @Test
    void shouldImplementToString() {
        GitCommit commit = GitCommit.builder()
            .id("abc1234567890")
            .author("John")
            .message("feat: something")
            .build();

        String str = commit.toString();
        assertThat(str).contains("abc1234");
        assertThat(str).contains("John");
    }
}
