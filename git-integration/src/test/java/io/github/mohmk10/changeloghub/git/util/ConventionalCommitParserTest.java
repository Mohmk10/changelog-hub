package io.github.mohmk10.changeloghub.git.util;

import io.github.mohmk10.changeloghub.git.util.ConventionalCommitParser.ConventionalCommit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConventionalCommitParserTest {

    @Test
    void shouldParseSimpleCommit() {
        ConventionalCommit commit = ConventionalCommitParser.parse("feat: add new feature");

        assertThat(commit).isNotNull();
        assertThat(commit.getType()).isEqualTo("feat");
        assertThat(commit.getScope()).isNull();
        assertThat(commit.getDescription()).isEqualTo("add new feature");
        assertThat(commit.isBreaking()).isFalse();
    }

    @Test
    void shouldParseCommitWithScope() {
        ConventionalCommit commit = ConventionalCommitParser.parse("fix(auth): resolve login issue");

        assertThat(commit).isNotNull();
        assertThat(commit.getType()).isEqualTo("fix");
        assertThat(commit.getScope()).isEqualTo("auth");
        assertThat(commit.getDescription()).isEqualTo("resolve login issue");
        assertThat(commit.hasScope()).isTrue();
    }

    @Test
    void shouldParseBreakingChangeWithBang() {
        ConventionalCommit commit = ConventionalCommitParser.parse("feat!: breaking change");

        assertThat(commit).isNotNull();
        assertThat(commit.getType()).isEqualTo("feat");
        assertThat(commit.isBreaking()).isTrue();
    }

    @Test
    void shouldParseBreakingChangeWithScopeAndBang() {
        ConventionalCommit commit = ConventionalCommitParser.parse("feat(api)!: breaking api change");

        assertThat(commit).isNotNull();
        assertThat(commit.getType()).isEqualTo("feat");
        assertThat(commit.getScope()).isEqualTo("api");
        assertThat(commit.isBreaking()).isTrue();
    }

    @Test
    void shouldDetectBreakingChangeInFooter() {
        String message = "feat: add new feature\n\nBREAKING CHANGE: old api removed";
        ConventionalCommit commit = ConventionalCommitParser.parse(message);

        assertThat(commit).isNotNull();
        assertThat(commit.isBreaking()).isTrue();
    }

    @Test
    void shouldDetectBreakingChangeWithHyphen() {
        String message = "feat: add new feature\n\nBREAKING-CHANGE: old api removed";
        ConventionalCommit commit = ConventionalCommitParser.parse(message);

        assertThat(commit).isNotNull();
        assertThat(commit.isBreaking()).isTrue();
    }

    @Test
    void shouldParseMultilineMessage() {
        String message = "feat: add feature\n\nThis is the body with more details";
        ConventionalCommit commit = ConventionalCommitParser.parse(message);

        assertThat(commit).isNotNull();
        assertThat(commit.getDescription()).isEqualTo("add feature");
        assertThat(commit.getBody()).isEqualTo("This is the body with more details");
    }

    @Test
    void shouldReturnNullForNonConventionalCommit() {
        assertThat(ConventionalCommitParser.parse("random commit message")).isNull();
        assertThat(ConventionalCommitParser.parse("Update README")).isNull();
        assertThat(ConventionalCommitParser.parse("")).isNull();
        assertThat(ConventionalCommitParser.parse(null)).isNull();
    }

    @Test
    void shouldCheckIfConventionalCommit() {
        assertThat(ConventionalCommitParser.isConventionalCommit("feat: something")).isTrue();
        assertThat(ConventionalCommitParser.isConventionalCommit("fix: bug")).isTrue();
        assertThat(ConventionalCommitParser.isConventionalCommit("not conventional")).isFalse();
    }

    @Test
    void shouldGetType() {
        assertThat(ConventionalCommitParser.getType("feat: feature")).isEqualTo("feat");
        assertThat(ConventionalCommitParser.getType("fix: bug")).isEqualTo("fix");
        assertThat(ConventionalCommitParser.getType("not conventional")).isNull();
    }

    @Test
    void shouldDetectFeature() {
        assertThat(ConventionalCommitParser.isFeature("feat: something")).isTrue();
        assertThat(ConventionalCommitParser.isFeature("feature: something")).isTrue();
        assertThat(ConventionalCommitParser.isFeature("fix: something")).isFalse();
    }

    @Test
    void shouldDetectFix() {
        assertThat(ConventionalCommitParser.isFix("fix: bug")).isTrue();
        assertThat(ConventionalCommitParser.isFix("bugfix: something")).isTrue();
        assertThat(ConventionalCommitParser.isFix("feat: something")).isFalse();
    }

    @Test
    void shouldDetectBreakingChange() {
        assertThat(ConventionalCommitParser.isBreakingChange("feat!: breaking")).isTrue();
        assertThat(ConventionalCommitParser.isBreakingChange("feat: normal")).isFalse();
    }

    @Test
    void shouldDetectCommitTypes() {
        ConventionalCommit feat = ConventionalCommitParser.parse("feat: x");
        ConventionalCommit fix = ConventionalCommitParser.parse("fix: x");
        ConventionalCommit chore = ConventionalCommitParser.parse("chore: x");
        ConventionalCommit docs = ConventionalCommitParser.parse("docs: x");
        ConventionalCommit refactor = ConventionalCommitParser.parse("refactor: x");
        ConventionalCommit test = ConventionalCommitParser.parse("test: x");
        ConventionalCommit style = ConventionalCommitParser.parse("style: x");
        ConventionalCommit perf = ConventionalCommitParser.parse("perf: x");
        ConventionalCommit ci = ConventionalCommitParser.parse("ci: x");
        ConventionalCommit build = ConventionalCommitParser.parse("build: x");

        assertThat(feat.isFeature()).isTrue();
        assertThat(fix.isFix()).isTrue();
        assertThat(chore.isChore()).isTrue();
        assertThat(docs.isDocs()).isTrue();
        assertThat(refactor.isRefactor()).isTrue();
        assertThat(test.isTest()).isTrue();
        assertThat(style.isStyle()).isTrue();
        assertThat(perf.isPerf()).isTrue();
        assertThat(ci.isCi()).isTrue();
        assertThat(build.isBuild()).isTrue();
    }

    @Test
    void shouldFormat() {
        ConventionalCommit commit = ConventionalCommitParser.parse("feat(api): add endpoint");
        assertThat(commit.format()).isEqualTo("feat(api): add endpoint");

        ConventionalCommit breaking = ConventionalCommitParser.parse("feat!: breaking");
        assertThat(breaking.format()).isEqualTo("feat!: breaking");
    }

    @Test
    void shouldImplementEquals() {
        ConventionalCommit c1 = ConventionalCommitParser.parse("feat: x");
        ConventionalCommit c2 = ConventionalCommitParser.parse("feat: x");
        ConventionalCommit c3 = ConventionalCommitParser.parse("fix: x");

        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
        assertThat(c1).isNotEqualTo(c3);
    }

    @Test
    void shouldImplementToString() {
        ConventionalCommit commit = ConventionalCommitParser.parse("feat(api): add endpoint");
        assertThat(commit.toString()).isEqualTo("feat(api): add endpoint");
    }
}
