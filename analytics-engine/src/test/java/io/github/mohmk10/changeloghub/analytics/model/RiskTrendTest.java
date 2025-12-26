package io.github.mohmk10.changeloghub.analytics.model;

import io.github.mohmk10.changeloghub.analytics.util.TrendDirection;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiskTrendTest {

    @Test
    void builder_shouldCreateRiskTrend() {
        RiskTrend trend = RiskTrend.builder()
                .direction(TrendDirection.IMPROVING)
                .currentRiskScore(30)
                .previousRiskScore(50)
                .slope(-0.5)
                .build();

        assertThat(trend.getDirection()).isEqualTo(TrendDirection.IMPROVING);
        assertThat(trend.getCurrentRiskScore()).isEqualTo(30);
        assertThat(trend.getPreviousRiskScore()).isEqualTo(50);
        assertThat(trend.getSlope()).isEqualTo(-0.5);
    }

    @Test
    void getRiskChange_shouldCalculateCorrectly() {
        RiskTrend trend = RiskTrend.builder()
                .currentRiskScore(30)
                .previousRiskScore(50)
                .build();

        assertThat(trend.getRiskChange()).isEqualTo(-20);
    }

    @Test
    void settersAndGetters_shouldWork() {
        RiskTrend trend = new RiskTrend();
        trend.setDirection(TrendDirection.DEGRADING);
        trend.setCurrentRiskScore(70);
        trend.setPreviousRiskScore(40);
        trend.setSlope(0.8);

        assertThat(trend.getDirection()).isEqualTo(TrendDirection.DEGRADING);
        assertThat(trend.getCurrentRiskScore()).isEqualTo(70);
        assertThat(trend.getPreviousRiskScore()).isEqualTo(40);
        assertThat(trend.getSlope()).isEqualTo(0.8);
    }

    @Test
    void periodRisk_shouldStoreData() {
        RiskTrend.PeriodRisk periodRisk = new RiskTrend.PeriodRisk();
        periodRisk.setPeriodLabel("Q1 2024");
        periodRisk.setStartDate(LocalDate.of(2024, 1, 1));
        periodRisk.setEndDate(LocalDate.of(2024, 3, 31));
        periodRisk.setRiskScore(45);
        periodRisk.setBreakingChangeCount(3);

        assertThat(periodRisk.getPeriodLabel()).isEqualTo("Q1 2024");
        assertThat(periodRisk.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(periodRisk.getEndDate()).isEqualTo(LocalDate.of(2024, 3, 31));
        assertThat(periodRisk.getRiskScore()).isEqualTo(45);
        assertThat(periodRisk.getBreakingChangeCount()).isEqualTo(3);
    }

    @Test
    void periodRisks_shouldBeSetAndRetrieved() {
        List<RiskTrend.PeriodRisk> periodRisks = Arrays.asList(
                createPeriodRisk("Q1", 30),
                createPeriodRisk("Q2", 40)
        );

        RiskTrend trend = RiskTrend.builder()
                .direction(TrendDirection.STABLE)
                .periodRisks(periodRisks)
                .build();

        assertThat(trend.getPeriodRisks()).hasSize(2);
        assertThat(trend.getPeriodRisks().get(0).getPeriodLabel()).isEqualTo("Q1");
    }

    @Test
    void isImproving_shouldReturnCorrectly() {
        RiskTrend improving = RiskTrend.builder()
                .direction(TrendDirection.IMPROVING)
                .build();

        RiskTrend degrading = RiskTrend.builder()
                .direction(TrendDirection.DEGRADING)
                .build();

        assertThat(improving.isImproving()).isTrue();
        assertThat(degrading.isImproving()).isFalse();
    }

    @Test
    void isDegrading_shouldReturnCorrectly() {
        RiskTrend improving = RiskTrend.builder()
                .direction(TrendDirection.IMPROVING)
                .build();

        RiskTrend degrading = RiskTrend.builder()
                .direction(TrendDirection.DEGRADING)
                .build();

        assertThat(improving.isDegrading()).isFalse();
        assertThat(degrading.isDegrading()).isTrue();
    }

    private RiskTrend.PeriodRisk createPeriodRisk(String label, int score) {
        RiskTrend.PeriodRisk risk = new RiskTrend.PeriodRisk();
        risk.setPeriodLabel(label);
        risk.setRiskScore(score);
        return risk;
    }
}
