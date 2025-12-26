package io.github.mohmk10.changeloghub.git.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class GitBranchTest {

    @Test
    void shouldCreateLocalBranch() {
        GitBranch branch = GitBranch.builder()
            .name("refs/heads/main")
            .commitId("abc123")
            .remote(false)
            .build();

        assertThat(branch.getName()).isEqualTo("refs/heads/main");
        assertThat(branch.getShortName()).isEqualTo("main");
        assertThat(branch.isRemote()).isFalse();
        assertThat(branch.isLocal()).isTrue();
    }

    @Test
    void shouldCreateRemoteBranch() {
        GitBranch branch = GitBranch.builder()
            .name("refs/remotes/origin/main")
            .commitId("abc123")
            .remote(true)
            .remoteName("origin")
            .build();

        assertThat(branch.getShortName()).isEqualTo("origin/main");
        assertThat(branch.isRemote()).isTrue();
        assertThat(branch.isLocal()).isFalse();
        assertThat(branch.getRemoteName()).isEqualTo("origin");
    }

    @Test
    void shouldDetectMainBranch() {
        assertThat(GitBranch.builder().name("refs/heads/main").build().isMainBranch()).isTrue();
        assertThat(GitBranch.builder().name("refs/heads/master").build().isMainBranch()).isTrue();
        assertThat(GitBranch.builder().name("main").build().isMainBranch()).isTrue();
        assertThat(GitBranch.builder().name("master").build().isMainBranch()).isTrue();
    }

    @Test
    void shouldDetectFeatureBranch() {
        assertThat(GitBranch.builder().name("refs/heads/feature/new-feature").build().isFeatureBranch()).isTrue();
        assertThat(GitBranch.builder().name("refs/heads/feat/something").build().isFeatureBranch()).isTrue();
        assertThat(GitBranch.builder().name("feature/test").build().isFeatureBranch()).isTrue();
    }

    @Test
    void shouldDetectReleaseBranch() {
        assertThat(GitBranch.builder().name("refs/heads/release/1.0.0").build().isReleaseBranch()).isTrue();
        assertThat(GitBranch.builder().name("release/v2.0").build().isReleaseBranch()).isTrue();
    }

    @Test
    void shouldNotMisidentifyBranches() {
        GitBranch develop = GitBranch.builder().name("refs/heads/develop").build();
        assertThat(develop.isMainBranch()).isFalse();
        assertThat(develop.isFeatureBranch()).isFalse();
        assertThat(develop.isReleaseBranch()).isFalse();
    }

    @Test
    void shouldGetShortCommitId() {
        GitBranch branch = GitBranch.builder()
            .name("main")
            .commitId("abc1234567890def1234567890abcdef12345678")
            .build();

        assertThat(branch.getShortCommitId()).isEqualTo("abc1234");
    }

    @Test
    void shouldSetLastCommitDate() {
        LocalDateTime now = LocalDateTime.now();
        GitBranch branch = GitBranch.builder()
            .name("main")
            .lastCommitDate(now)
            .build();

        assertThat(branch.getLastCommitDate()).isEqualTo(now);
    }

    @Test
    void shouldMarkAsDefault() {
        GitBranch branch = GitBranch.builder()
            .name("main")
            .isDefault(true)
            .build();

        assertThat(branch.isDefault()).isTrue();
    }

    @Test
    void shouldHandleNullName() {
        GitBranch branch = new GitBranch();
        assertThat(branch.getShortName()).isNull();
        assertThat(branch.isMainBranch()).isFalse();
        assertThat(branch.isFeatureBranch()).isFalse();
    }

    @Test
    void shouldImplementEquals() {
        GitBranch branch1 = GitBranch.builder().name("main").build();
        GitBranch branch2 = GitBranch.builder().name("main").build();
        GitBranch branch3 = GitBranch.builder().name("develop").build();

        assertThat(branch1).isEqualTo(branch2);
        assertThat(branch1.hashCode()).isEqualTo(branch2.hashCode());
        assertThat(branch1).isNotEqualTo(branch3);
    }

    @Test
    void shouldImplementToString() {
        GitBranch branch = GitBranch.builder()
            .name("refs/heads/main")
            .remote(false)
            .isDefault(true)
            .build();

        String str = branch.toString();
        assertThat(str).contains("main");
        assertThat(str).contains("remote=false");
        assertThat(str).contains("default=true");
    }
}
