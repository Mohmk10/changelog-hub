package io.github.mohmk10.changeloghub.git.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

class GitConfigTest {

    @Test
    void shouldCreateWithDefaults() {
        GitConfig config = new GitConfig();

        assertThat(config.getDefaultBranch()).isEqualTo("main");
        assertThat(config.getMaxCommitDepth()).isEqualTo(100);
        assertThat(config.getMaxFileSizeBytes()).isEqualTo(10 * 1024 * 1024);
        assertThat(config.isFollowRenames()).isTrue();
        assertThat(config.isIncludeRemoteBranches()).isFalse();
    }

    @Test
    void shouldBuildWithCustomValues() {
        GitConfig config = GitConfig.builder()
            .repositoryPath("/path/to/repo")
            .defaultBranch("master")
            .maxCommitDepth(50)
            .maxFileSizeBytes(5 * 1024 * 1024)
            .followRenames(false)
            .includeRemoteBranches(true)
            .build();

        assertThat(config.getRepositoryPath()).isEqualTo(Path.of("/path/to/repo"));
        assertThat(config.getDefaultBranch()).isEqualTo("master");
        assertThat(config.getMaxCommitDepth()).isEqualTo(50);
        assertThat(config.getMaxFileSizeBytes()).isEqualTo(5 * 1024 * 1024);
        assertThat(config.isFollowRenames()).isFalse();
        assertThat(config.isIncludeRemoteBranches()).isTrue();
    }

    @Test
    void shouldSetSpecFilePatterns() {
        GitConfig config = new GitConfig();
        config.setSpecFilePatterns(new HashSet<>(Arrays.asList("*.yaml", "*.json")));

        assertThat(config.getSpecFilePatterns()).containsExactlyInAnyOrder("*.yaml", "*.json");
    }

    @Test
    void shouldAddSpecFilePattern() {
        GitConfig config = new GitConfig();
        config.addSpecFilePattern("*.custom");

        assertThat(config.getSpecFilePatterns()).contains("*.custom");
    }

    @Test
    void shouldSetIgnoredPaths() {
        GitConfig config = new GitConfig();
        config.setIgnoredPaths(new HashSet<>(Arrays.asList("node_modules", "vendor")));

        assertThat(config.getIgnoredPaths()).containsExactlyInAnyOrder("node_modules", "vendor");
    }

    @Test
    void shouldAddIgnoredPath() {
        GitConfig config = new GitConfig();
        config.addIgnoredPath("custom_dir");

        assertThat(config.getIgnoredPaths()).contains("custom_dir");
    }

    @Test
    void shouldSetSpecDirectories() {
        GitConfig config = new GitConfig();
        config.setSpecDirectories(Arrays.asList("api", "specs"));

        assertThat(config.getSpecDirectories()).containsExactly("api", "specs");
    }

    @Test
    void shouldAddSpecDirectory() {
        GitConfig config = new GitConfig();
        config.addSpecDirectory("custom_api");

        assertThat(config.getSpecDirectories()).contains("custom_api");
    }

    @Test
    void shouldCheckIfPathShouldBeIgnored() {
        GitConfig config = new GitConfig();
        config.setIgnoredPaths(new HashSet<>(Arrays.asList("node_modules", "vendor")));

        assertThat(config.shouldIgnore("node_modules/package/file.js")).isTrue();
        assertThat(config.shouldIgnore("src/vendor/lib.js")).isTrue();
        assertThat(config.shouldIgnore("src/main/api.yaml")).isFalse();
    }

    @Test
    void shouldMatchSpecPattern() {
        GitConfig config = new GitConfig();
        config.setSpecFilePatterns(new HashSet<>(Arrays.asList("*.yaml", "*.json", "openapi.yml")));

        assertThat(config.matchesSpecPattern("api.yaml")).isTrue();
        assertThat(config.matchesSpecPattern("spec.json")).isTrue();
        assertThat(config.matchesSpecPattern("openapi.yml")).isTrue();
        assertThat(config.matchesSpecPattern("readme.md")).isFalse();
    }

    @Test
    void shouldConfigureCredentials() {
        GitConfig.CredentialsConfig creds = new GitConfig.CredentialsConfig();
        creds.setUsername("user");
        creds.setPassword("pass");

        GitConfig config = new GitConfig();
        config.setCredentials(creds);

        assertThat(config.hasCredentials()).isTrue();
        assertThat(config.getCredentials().isBasicAuth()).isTrue();
    }

    @Test
    void shouldConfigureTokenAuth() {
        GitConfig.CredentialsConfig creds = new GitConfig.CredentialsConfig();
        creds.setAccessToken("token123");

        assertThat(creds.isValid()).isTrue();
        assertThat(creds.isTokenAuth()).isTrue();
        assertThat(creds.isBasicAuth()).isFalse();
    }

    @Test
    void shouldConfigureSshAuth() {
        GitConfig.CredentialsConfig creds = new GitConfig.CredentialsConfig();
        creds.setSshKeyPath("/path/to/key");
        creds.setSshPassphrase("passphrase");

        assertThat(creds.isValid()).isTrue();
        assertThat(creds.isSshAuth()).isTrue();
    }

    @Test
    void shouldConfigureProxy() {
        GitConfig.ProxyConfig proxy = new GitConfig.ProxyConfig();
        proxy.setHost("proxy.example.com");
        proxy.setPort(8080);
        proxy.setUsername("proxyuser");
        proxy.setPassword("proxypass");

        GitConfig config = new GitConfig();
        config.setProxy(proxy);

        assertThat(config.hasProxy()).isTrue();
        assertThat(config.getProxy().isValid()).isTrue();
        assertThat(config.getProxy().hasAuthentication()).isTrue();
    }

    @Test
    void shouldImplementEquals() {
        GitConfig config1 = GitConfig.builder()
            .repositoryPath("/path")
            .defaultBranch("main")
            .build();

        GitConfig config2 = GitConfig.builder()
            .repositoryPath("/path")
            .defaultBranch("main")
            .build();

        GitConfig config3 = GitConfig.builder()
            .repositoryPath("/other")
            .defaultBranch("main")
            .build();

        assertThat(config1).isEqualTo(config2);
        assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
        assertThat(config1).isNotEqualTo(config3);
    }

    @Test
    void shouldImplementToString() {
        GitConfig config = GitConfig.builder()
            .repositoryPath("/path/to/repo")
            .defaultBranch("main")
            .build();

        String str = config.toString();
        assertThat(str).contains("repositoryPath");
        assertThat(str).contains("main");
    }
}
