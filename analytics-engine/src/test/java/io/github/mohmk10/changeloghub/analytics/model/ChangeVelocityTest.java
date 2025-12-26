package io.github.mohmk10.changeloghub.analytics.model;

import org.junit.jupiter.api.Test;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ChangeVelocityTest {

    @Test
    void builder_shouldCreateChangeVelocity() {
        ChangeVelocity velocity = ChangeVelocity.builder()
                .changesPerDay(2.5)
                .changesPerWeek(17.5)
                .changesPerMonth(75.0)
                .accelerationRate(0.1)
                .accelerating(true)
                .build();

        assertThat(velocity.getChangesPerDay()).isEqualTo(2.5);
        assertThat(velocity.getChangesPerWeek()).isEqualTo(17.5);
        assertThat(velocity.getChangesPerMonth()).isEqualTo(75.0);
        assertThat(velocity.getAccelerationRate()).isEqualTo(0.1);
        assertThat(velocity.isAccelerating()).isTrue();
    }

    @Test
    void settersAndGetters_shouldWork() {
        ChangeVelocity velocity = new ChangeVelocity();
        velocity.setChangesPerDay(1.0);
        velocity.setChangesPerWeek(7.0);
        velocity.setChangesPerMonth(30.0);
        velocity.setAccelerationRate(-0.05);
        velocity.setAccelerating(false);

        assertThat(velocity.getChangesPerDay()).isEqualTo(1.0);
        assertThat(velocity.getChangesPerWeek()).isEqualTo(7.0);
        assertThat(velocity.getChangesPerMonth()).isEqualTo(30.0);
        assertThat(velocity.getAccelerationRate()).isEqualTo(-0.05);
        assertThat(velocity.isAccelerating()).isFalse();
    }

    @Test
    void defaultValues_shouldBeZero() {
        ChangeVelocity velocity = new ChangeVelocity();
        assertThat(velocity.getChangesPerDay()).isEqualTo(0.0);
        assertThat(velocity.getChangesPerWeek()).isEqualTo(0.0);
        assertThat(velocity.getChangesPerMonth()).isEqualTo(0.0);
        assertThat(velocity.getAccelerationRate()).isEqualTo(0.0);
    }

    @Test
    void isDecelerating_shouldReturnTrueForNegativeAcceleration() {
        ChangeVelocity decelerating = ChangeVelocity.builder()
                .accelerationRate(-0.1)
                .accelerating(false)
                .build();

        ChangeVelocity accelerating = ChangeVelocity.builder()
                .accelerationRate(0.1)
                .accelerating(true)
                .build();

        assertThat(decelerating.isDecelerating()).isTrue();
        assertThat(accelerating.isDecelerating()).isFalse();
    }

    @Test
    void isStable_shouldReturnTrueWhenAccelerationNearZero() {
        ChangeVelocity stable = ChangeVelocity.builder()
                .accelerationRate(0.05)
                .build();

        ChangeVelocity notStable = ChangeVelocity.builder()
                .accelerationRate(0.5)
                .build();

        assertThat(stable.isStable()).isTrue();
        assertThat(notStable.isStable()).isFalse();
    }

    @Test
    void averageTimeBetweenReleases_shouldWork() {
        ChangeVelocity velocity = ChangeVelocity.builder()
                .averageTimeBetweenReleases(Duration.ofDays(14))
                .build();

        assertThat(velocity.getAverageTimeBetweenReleasesDays()).isEqualTo(14);
    }

    @Test
    void analyzedAt_shouldBeSetAutomatically() {
        ChangeVelocity velocity = new ChangeVelocity();
        assertThat(velocity.getAnalyzedAt()).isNotNull();
    }
}
