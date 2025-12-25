package io.github.mohmk10.changeloghub.core.reporter;

import io.github.mohmk10.changeloghub.core.reporter.impl.ConsoleReporter;
import io.github.mohmk10.changeloghub.core.reporter.impl.HtmlReporter;
import io.github.mohmk10.changeloghub.core.reporter.impl.JsonReporter;
import io.github.mohmk10.changeloghub.core.reporter.impl.MarkdownReporter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReporterFactoryTest {

    @Test
    void testCreateMarkdownReporter() {
        Reporter reporter = ReporterFactory.create(ReportFormat.MARKDOWN);

        assertThat(reporter).isNotNull();
        assertThat(reporter).isInstanceOf(MarkdownReporter.class);
    }

    @Test
    void testCreateJsonReporter() {
        Reporter reporter = ReporterFactory.create(ReportFormat.JSON);

        assertThat(reporter).isNotNull();
        assertThat(reporter).isInstanceOf(JsonReporter.class);
    }

    @Test
    void testCreateHtmlReporter() {
        Reporter reporter = ReporterFactory.create(ReportFormat.HTML);

        assertThat(reporter).isNotNull();
        assertThat(reporter).isInstanceOf(HtmlReporter.class);
    }

    @Test
    void testCreateConsoleReporter() {
        Reporter reporter = ReporterFactory.create(ReportFormat.CONSOLE);

        assertThat(reporter).isNotNull();
        assertThat(reporter).isInstanceOf(ConsoleReporter.class);
    }

    @Test
    void testCreateWithNullThrowsException() {
        assertThatThrownBy(() -> ReporterFactory.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }

    @Test
    void testAllFormatsAreSupported() {
        for (ReportFormat format : ReportFormat.values()) {
            Reporter reporter = ReporterFactory.create(format);
            assertThat(reporter).isNotNull();
        }
    }
}
