package io.github.mohmk10.changeloghub.analytics.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TechnicalDebtTest {

    @Test
    void builder_shouldCreateTechnicalDebt() {
        TechnicalDebt debt = TechnicalDebt.builder()
                .debtScore(35)
                .deprecatedEndpointsCount(5)
                .missingDocumentationCount(10)
                .inconsistentNamingCount(3)
                .build();

        assertThat(debt.getDebtScore()).isEqualTo(35);
        assertThat(debt.getDeprecatedEndpointsCount()).isEqualTo(5);
        assertThat(debt.getMissingDocumentationCount()).isEqualTo(10);
        assertThat(debt.getInconsistentNamingCount()).isEqualTo(3);
    }

    @Test
    void settersAndGetters_shouldWork() {
        TechnicalDebt debt = new TechnicalDebt();
        debt.setDebtScore(40);
        debt.setDeprecatedEndpointsCount(3);
        debt.setMissingDocumentationCount(8);
        debt.setInconsistentNamingCount(2);

        assertThat(debt.getDebtScore()).isEqualTo(40);
        assertThat(debt.getDeprecatedEndpointsCount()).isEqualTo(3);
        assertThat(debt.getMissingDocumentationCount()).isEqualTo(8);
        assertThat(debt.getInconsistentNamingCount()).isEqualTo(2);
    }

    @Test
    void getTotalIssues_shouldSumAllCounts() {
        TechnicalDebt debt = TechnicalDebt.builder()
                .deprecatedEndpointsCount(5)
                .missingDocumentationCount(10)
                .inconsistentNamingCount(3)
                .build();

        assertThat(debt.getTotalIssues()).isEqualTo(18);
    }

    @Test
    void debtItem_shouldBeCreatedCorrectly() {
        TechnicalDebt.DebtItem item = new TechnicalDebt.DebtItem(
                TechnicalDebt.DebtItem.DebtType.DEPRECATED_ENDPOINT,
                "/api/v1/oldEndpoint",
                "Endpoint deprecated since v2.0"
        );

        assertThat(item.getType()).isEqualTo(TechnicalDebt.DebtItem.DebtType.DEPRECATED_ENDPOINT);
        assertThat(item.getPath()).isEqualTo("/api/v1/oldEndpoint");
        assertThat(item.getDescription()).isEqualTo("Endpoint deprecated since v2.0");
    }

    @Test
    void items_shouldBeSetAndRetrieved() {
        List<TechnicalDebt.DebtItem> items = Arrays.asList(
                new TechnicalDebt.DebtItem(TechnicalDebt.DebtItem.DebtType.MISSING_DOCUMENTATION, "/api/users", "No description"),
                new TechnicalDebt.DebtItem(TechnicalDebt.DebtItem.DebtType.INCONSISTENT_NAMING, "/api/GetUsers", "Should be /api/users")
        );

        TechnicalDebt debt = TechnicalDebt.builder()
                .items(items)
                .build();

        assertThat(debt.getItems()).hasSize(2);
        assertThat(debt.getItems().get(0).getType()).isEqualTo(TechnicalDebt.DebtItem.DebtType.MISSING_DOCUMENTATION);
    }

    @Test
    void debtType_shouldHaveAllTypes() {
        assertThat(TechnicalDebt.DebtItem.DebtType.values()).contains(
                TechnicalDebt.DebtItem.DebtType.DEPRECATED_ENDPOINT,
                TechnicalDebt.DebtItem.DebtType.MISSING_DOCUMENTATION,
                TechnicalDebt.DebtItem.DebtType.INCONSISTENT_NAMING,
                TechnicalDebt.DebtItem.DebtType.MISSING_EXAMPLES,
                TechnicalDebt.DebtItem.DebtType.OUTDATED_SCHEMA
        );
    }

    @Test
    void hasHighDebt_shouldReturnTrueForHighScores() {
        TechnicalDebt highDebt = TechnicalDebt.builder()
                .debtScore(60)
                .build();

        TechnicalDebt lowDebt = TechnicalDebt.builder()
                .debtScore(20)
                .build();

        assertThat(highDebt.hasHighDebt()).isTrue();
        assertThat(lowDebt.hasHighDebt()).isFalse();
    }
}
