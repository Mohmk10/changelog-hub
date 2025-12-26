package io.github.mohmk10.changeloghub.notification.model;

import io.github.mohmk10.changeloghub.core.model.*;
import io.github.mohmk10.changeloghub.notification.util.ChannelType;
import io.github.mohmk10.changeloghub.notification.util.EventType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    @Test
    void shouldBuildNotification() {
        Notification notification = Notification.builder()
            .id("test-id")
            .title("Test Title")
            .message("Test Message")
            .severity(Severity.BREAKING)
            .build();

        assertThat(notification.getId()).isEqualTo("test-id");
        assertThat(notification.getTitle()).isEqualTo("Test Title");
        assertThat(notification.getMessage()).isEqualTo("Test Message");
        assertThat(notification.getSeverity()).isEqualTo(Severity.BREAKING);
    }

    @Test
    void shouldGenerateIdIfNotProvided() {
        Notification notification = Notification.builder()
            .title("Test")
            .build();

        assertThat(notification.getId()).isNotNull().isNotEmpty();
    }

    @Test
    void shouldDefaultToInfoSeverity() {
        Notification notification = Notification.builder()
            .title("Test")
            .build();

        assertThat(notification.getSeverity()).isEqualTo(Severity.INFO);
    }

    @Test
    void shouldSetCreatedAt() {
        Instant now = Instant.now();
        Notification notification = Notification.builder()
            .title("Test")
            .createdAt(now)
            .build();

        assertThat(notification.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldAddTargetChannels() {
        Notification notification = Notification.builder()
            .title("Test")
            .targetChannel(ChannelType.SLACK)
            .targetChannel(ChannelType.DISCORD)
            .build();

        assertThat(notification.getTargetChannels())
            .containsExactlyInAnyOrder(ChannelType.SLACK, ChannelType.DISCORD);
    }

    @Test
    void shouldAddMetadata() {
        Notification notification = Notification.builder()
            .title("Test")
            .addMetadata("key1", "value1")
            .addMetadata("key2", 42)
            .build();

        assertThat(notification.getMetadata("key1", String.class)).isEqualTo("value1");
        assertThat(notification.getMetadata("key2", Integer.class)).isEqualTo(42);
        assertThat(notification.getMetadata("missing", String.class)).isNull();
    }

    @Test
    void shouldAddMentions() {
        Notification notification = Notification.builder()
            .title("Test")
            .addMention("@channel")
            .addMention("@user")
            .build();

        assertThat(notification.getMentions()).containsExactly("@channel", "@user");
    }

    @Test
    void shouldCreateFromChangelog() {
        Changelog changelog = Changelog.builder()
            .apiName("Test API")
            .fromVersion("1.0.0")
            .toVersion("2.0.0")
            .addChange(Change.builder()
                .description("Test change")
                .type(ChangeType.REMOVED)
                .severity(Severity.BREAKING)
                .build())
            .build();

        Notification notification = Notification.fromChangelog(changelog);

        assertThat(notification.getTitle()).contains("Test API");
        assertThat(notification.hasChangelog()).isTrue();
        assertThat(notification.getChangelog()).isEqualTo(changelog);
    }

    @Test
    void shouldIdentifyCriticalNotification() {
        Notification breaking = Notification.builder()
            .title("Test")
            .severity(Severity.BREAKING)
            .build();

        Notification dangerous = Notification.builder()
            .title("Test")
            .severity(Severity.DANGEROUS)
            .build();

        Notification info = Notification.builder()
            .title("Test")
            .severity(Severity.INFO)
            .build();

        assertThat(breaking.isCritical()).isTrue();
        assertThat(dangerous.isCritical()).isTrue();
        assertThat(info.isCritical()).isFalse();
    }

    @Test
    void shouldCheckTargetedChannel() {
        Notification targeted = Notification.builder()
            .title("Test")
            .targetChannels(EnumSet.of(ChannelType.SLACK))
            .build();

        Notification untargeted = Notification.builder()
            .title("Test")
            .build();

        assertThat(targeted.isTargetedTo(ChannelType.SLACK)).isTrue();
        assertThat(targeted.isTargetedTo(ChannelType.DISCORD)).isFalse();
        assertThat(untargeted.isTargetedTo(ChannelType.DISCORD)).isTrue(); 
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        Notification n1 = Notification.builder().id("same-id").title("Test1").build();
        Notification n2 = Notification.builder().id("same-id").title("Test2").build();
        Notification n3 = Notification.builder().id("different-id").title("Test1").build();

        assertThat(n1).isEqualTo(n2);
        assertThat(n1.hashCode()).isEqualTo(n2.hashCode());
        assertThat(n1).isNotEqualTo(n3);
    }
}
