package io.github.mohmk10.changeloghub.git.model;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class GitFileContentTest {

    @Test
    void shouldCreateFileContent() {
        GitFileContent file = GitFileContent.builder()
            .path("src/main/api/openapi.yaml")
            .ref("main")
            .content("openapi: 3.0.0")
            .exists(true)
            .build();

        assertThat(file.getPath()).isEqualTo("src/main/api/openapi.yaml");
        assertThat(file.getRef()).isEqualTo("main");
        assertThat(file.getContent()).isEqualTo("openapi: 3.0.0");
        assertThat(file.exists()).isTrue();
    }

    @Test
    void shouldGetFileName() {
        GitFileContent file = GitFileContent.builder()
            .path("src/main/api/openapi.yaml")
            .build();

        assertThat(file.getFileName()).isEqualTo("openapi.yaml");
    }

    @Test
    void shouldGetFileNameWithoutPath() {
        GitFileContent file = GitFileContent.builder()
            .path("openapi.yaml")
            .build();

        assertThat(file.getFileName()).isEqualTo("openapi.yaml");
    }

    @Test
    void shouldGetExtension() {
        assertThat(GitFileContent.builder().path("file.yaml").build().getExtension()).isEqualTo("yaml");
        assertThat(GitFileContent.builder().path("file.json").build().getExtension()).isEqualTo("json");
        assertThat(GitFileContent.builder().path("file.graphql").build().getExtension()).isEqualTo("graphql");
        assertThat(GitFileContent.builder().path("file.proto").build().getExtension()).isEqualTo("proto");
    }

    @Test
    void shouldHandleNoExtension() {
        GitFileContent file = GitFileContent.builder()
            .path("Dockerfile")
            .build();

        assertThat(file.getExtension()).isNull();
    }

    @Test
    void shouldHandleDotFile() {
        GitFileContent file = GitFileContent.builder()
            .path(".gitignore")
            .build();

        assertThat(file.getExtension()).isNull();
    }

    @Test
    void shouldDetectTextFiles() {
        assertThat(GitFileContent.builder().path("api.yaml").build().isTextFile()).isTrue();
        assertThat(GitFileContent.builder().path("api.yml").build().isTextFile()).isTrue();
        assertThat(GitFileContent.builder().path("api.json").build().isTextFile()).isTrue();
        assertThat(GitFileContent.builder().path("schema.graphql").build().isTextFile()).isTrue();
        assertThat(GitFileContent.builder().path("service.proto").build().isTextFile()).isTrue();
        assertThat(GitFileContent.builder().path("Controller.java").build().isTextFile()).isTrue();
        assertThat(GitFileContent.builder().path("README.md").build().isTextFile()).isTrue();
    }

    @Test
    void shouldDetectNonTextFiles() {
        assertThat(GitFileContent.builder().path("image.png").build().isTextFile()).isFalse();
        assertThat(GitFileContent.builder().path("binary.exe").build().isTextFile()).isFalse();
    }

    @Test
    void shouldDetectEmptyFile() {
        GitFileContent empty1 = GitFileContent.builder()
            .path("file.txt")
            .exists(false)
            .build();

        GitFileContent empty2 = GitFileContent.builder()
            .path("file.txt")
            .exists(true)
            .content("")
            .build();

        GitFileContent notEmpty = GitFileContent.builder()
            .path("file.txt")
            .exists(true)
            .content("content")
            .build();

        assertThat(empty1.isEmpty()).isTrue();
        assertThat(empty2.isEmpty()).isTrue();
        assertThat(notEmpty.isEmpty()).isFalse();
    }

    @Test
    void shouldConvertBetweenContentAndBytes() {
        String content = "Hello, World!";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        GitFileContent fromContent = GitFileContent.builder()
            .path("file.txt")
            .content(content)
            .build();

        GitFileContent fromBytes = GitFileContent.builder()
            .path("file.txt")
            .bytes(bytes)
            .build();

        assertThat(fromContent.getBytes()).isEqualTo(bytes);
        assertThat(fromBytes.getContent()).isEqualTo(content);
    }

    @Test
    void shouldSetSizeFromContent() {
        GitFileContent file = GitFileContent.builder()
            .path("file.txt")
            .content("Hello")
            .build();

        assertThat(file.getSize()).isEqualTo(5);
    }

    @Test
    void shouldSetSizeFromBytes() {
        GitFileContent file = GitFileContent.builder()
            .path("file.txt")
            .bytes(new byte[100])
            .build();

        assertThat(file.getSize()).isEqualTo(100);
    }

    @Test
    void shouldSetCommitId() {
        GitFileContent file = GitFileContent.builder()
            .path("file.txt")
            .commitId("abc123")
            .build();

        assertThat(file.getCommitId()).isEqualTo("abc123");
    }

    @Test
    void shouldHandleNullPath() {
        GitFileContent file = new GitFileContent();
        assertThat(file.getFileName()).isNull();
        assertThat(file.getExtension()).isNull();
    }

    @Test
    void shouldImplementEquals() {
        GitFileContent file1 = GitFileContent.builder()
            .path("file.txt")
            .ref("main")
            .commitId("abc123")
            .build();

        GitFileContent file2 = GitFileContent.builder()
            .path("file.txt")
            .ref("main")
            .commitId("abc123")
            .build();

        GitFileContent file3 = GitFileContent.builder()
            .path("file.txt")
            .ref("develop")
            .commitId("abc123")
            .build();

        assertThat(file1).isEqualTo(file2);
        assertThat(file1.hashCode()).isEqualTo(file2.hashCode());
        assertThat(file1).isNotEqualTo(file3);
    }

    @Test
    void shouldImplementToString() {
        GitFileContent file = GitFileContent.builder()
            .path("api/openapi.yaml")
            .ref("main")
            .exists(true)
            .content("content")
            .build();

        String str = file.toString();
        assertThat(str).contains("api/openapi.yaml");
        assertThat(str).contains("main");
        assertThat(str).contains("exists=true");
    }
}
