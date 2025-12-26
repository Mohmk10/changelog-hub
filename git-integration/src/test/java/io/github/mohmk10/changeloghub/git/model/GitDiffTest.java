package io.github.mohmk10.changeloghub.git.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GitDiffTest {

    @Test
    void shouldCreateEmptyDiff() {
        GitDiff diff = new GitDiff();

        assertThat(diff.getAddedFiles()).isEmpty();
        assertThat(diff.getModifiedFiles()).isEmpty();
        assertThat(diff.getDeletedFiles()).isEmpty();
        assertThat(diff.getRenamedFiles()).isEmpty();
        assertThat(diff.hasChanges()).isFalse();
        assertThat(diff.getTotalChanges()).isZero();
    }

    @Test
    void shouldTrackAddedFiles() {
        GitDiff diff = GitDiff.builder()
            .addedFiles(Arrays.asList("file1.txt", "file2.txt"))
            .build();

        assertThat(diff.getAddedFiles()).containsExactly("file1.txt", "file2.txt");
        assertThat(diff.hasChanges()).isTrue();
        assertThat(diff.getTotalChanges()).isEqualTo(2);
    }

    @Test
    void shouldTrackModifiedFiles() {
        GitDiff diff = GitDiff.builder()
            .modifiedFiles(Arrays.asList("modified.txt"))
            .build();

        assertThat(diff.getModifiedFiles()).containsExactly("modified.txt");
        assertThat(diff.hasChanges()).isTrue();
    }

    @Test
    void shouldTrackDeletedFiles() {
        GitDiff diff = GitDiff.builder()
            .deletedFiles(Arrays.asList("deleted.txt"))
            .build();

        assertThat(diff.getDeletedFiles()).containsExactly("deleted.txt");
        assertThat(diff.hasChanges()).isTrue();
    }

    @Test
    void shouldTrackRenamedFiles() {
        GitDiff diff = GitDiff.builder()
            .renamedFiles(Arrays.asList("old.txt -> new.txt"))
            .build();

        assertThat(diff.getRenamedFiles()).containsExactly("old.txt -> new.txt");
        assertThat(diff.hasChanges()).isTrue();
    }

    @Test
    void shouldCalculateTotalChanges() {
        GitDiff diff = GitDiff.builder()
            .addedFiles(Arrays.asList("a.txt", "b.txt"))
            .modifiedFiles(Arrays.asList("c.txt"))
            .deletedFiles(Arrays.asList("d.txt", "e.txt", "f.txt"))
            .renamedFiles(Arrays.asList("g.txt -> h.txt"))
            .build();

        assertThat(diff.getTotalChanges()).isEqualTo(7);
    }

    @Test
    void shouldGetAllChangedFiles() {
        GitDiff diff = GitDiff.builder()
            .addedFiles(Arrays.asList("added.txt"))
            .modifiedFiles(Arrays.asList("modified.txt"))
            .deletedFiles(Arrays.asList("deleted.txt"))
            .renamedFiles(Arrays.asList("renamed.txt"))
            .build();

        List<String> all = diff.getAllChangedFiles();
        assertThat(all).containsExactlyInAnyOrder(
            "added.txt", "modified.txt", "deleted.txt", "renamed.txt"
        );
    }

    @Test
    void shouldCheckIfFileChanged() {
        GitDiff diff = GitDiff.builder()
            .addedFiles(Arrays.asList("added.txt"))
            .modifiedFiles(Arrays.asList("modified.txt"))
            .build();

        assertThat(diff.hasFileChanged("added.txt")).isTrue();
        assertThat(diff.hasFileChanged("modified.txt")).isTrue();
        assertThat(diff.hasFileChanged("other.txt")).isFalse();
    }

    @Test
    void shouldSetRefs() {
        GitDiff diff = GitDiff.builder()
            .oldRef("v1.0.0")
            .newRef("v2.0.0")
            .oldCommitId("abc123")
            .newCommitId("def456")
            .build();

        assertThat(diff.getOldRef()).isEqualTo("v1.0.0");
        assertThat(diff.getNewRef()).isEqualTo("v2.0.0");
        assertThat(diff.getOldCommitId()).isEqualTo("abc123");
        assertThat(diff.getNewCommitId()).isEqualTo("def456");
    }

    @Test
    void shouldAddFilesIndividually() {
        GitDiff diff = new GitDiff();
        diff.addAddedFile("a.txt");
        diff.addModifiedFile("b.txt");
        diff.addDeletedFile("c.txt");
        diff.addRenamedFile("d.txt -> e.txt");

        assertThat(diff.getAddedFiles()).containsExactly("a.txt");
        assertThat(diff.getModifiedFiles()).containsExactly("b.txt");
        assertThat(diff.getDeletedFiles()).containsExactly("c.txt");
        assertThat(diff.getRenamedFiles()).containsExactly("d.txt -> e.txt");
    }

    @Test
    void shouldHandleNullListsGracefully() {
        GitDiff diff = new GitDiff();
        diff.setAddedFiles(null);
        diff.setModifiedFiles(null);
        diff.setDeletedFiles(null);
        diff.setRenamedFiles(null);

        assertThat(diff.getAddedFiles()).isEmpty();
        assertThat(diff.getModifiedFiles()).isEmpty();
        assertThat(diff.getDeletedFiles()).isEmpty();
        assertThat(diff.getRenamedFiles()).isEmpty();
    }

    @Test
    void shouldImplementToString() {
        GitDiff diff = GitDiff.builder()
            .oldRef("v1.0.0")
            .newRef("v2.0.0")
            .addedFiles(Arrays.asList("a.txt"))
            .modifiedFiles(Arrays.asList("b.txt", "c.txt"))
            .build();

        String str = diff.toString();
        assertThat(str).contains("v1.0.0");
        assertThat(str).contains("v2.0.0");
        assertThat(str).contains("added=1");
        assertThat(str).contains("modified=2");
    }
}
