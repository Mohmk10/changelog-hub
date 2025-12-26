package io.github.mohmk10.changeloghub.analytics.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class StabilityGradeTest {

    @ParameterizedTest
    @CsvSource({
            "100, A",
            "95, A",
            "90, A",
            "89, B",
            "85, B",
            "80, B",
            "79, C",
            "75, C",
            "70, C",
            "69, D",
            "65, D",
            "60, D",
            "59, F",
            "30, F",
            "0, F"
    })
    void fromScore_shouldReturnCorrectGrade(int score, String expectedGrade) {
        StabilityGrade grade = StabilityGrade.fromScore(score);
        assertThat(grade.name()).isEqualTo(expectedGrade);
    }

    @Test
    void gradeA_shouldHaveCorrectProperties() {
        StabilityGrade grade = StabilityGrade.A;
        assertThat(grade.getLabel()).isEqualTo("Excellent");
        assertThat(grade.getMinScore()).isEqualTo(90);
        assertThat(grade.getMaxScore()).isEqualTo(100);
        assertThat(grade.getDescription()).contains("Highly stable");
    }

    @Test
    void gradeB_shouldHaveCorrectProperties() {
        StabilityGrade grade = StabilityGrade.B;
        assertThat(grade.getLabel()).isEqualTo("Good");
        assertThat(grade.getMinScore()).isEqualTo(80);
        assertThat(grade.getMaxScore()).isEqualTo(89);
    }

    @Test
    void gradeC_shouldHaveCorrectProperties() {
        StabilityGrade grade = StabilityGrade.C;
        assertThat(grade.getLabel()).isEqualTo("Fair");
        assertThat(grade.getMinScore()).isEqualTo(70);
        assertThat(grade.getMaxScore()).isEqualTo(79);
    }

    @Test
    void gradeD_shouldHaveCorrectProperties() {
        StabilityGrade grade = StabilityGrade.D;
        assertThat(grade.getLabel()).isEqualTo("Poor");
        assertThat(grade.getMinScore()).isEqualTo(60);
        assertThat(grade.getMaxScore()).isEqualTo(69);
    }

    @Test
    void gradeF_shouldHaveCorrectProperties() {
        StabilityGrade grade = StabilityGrade.F;
        assertThat(grade.getLabel()).isEqualTo("Failing");
        assertThat(grade.getMinScore()).isEqualTo(0);
        assertThat(grade.getMaxScore()).isEqualTo(59);
    }

    @Test
    void fromScore_withNegativeScore_shouldReturnF() {
        StabilityGrade grade = StabilityGrade.fromScore(-10);
        assertThat(grade).isEqualTo(StabilityGrade.F);
    }

    @Test
    void fromScore_withScoreAbove100_shouldReturnA() {
        StabilityGrade grade = StabilityGrade.fromScore(150);
        assertThat(grade).isEqualTo(StabilityGrade.A);
    }
}
