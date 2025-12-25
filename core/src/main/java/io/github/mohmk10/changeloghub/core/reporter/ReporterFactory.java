package io.github.mohmk10.changeloghub.core.reporter;

import io.github.mohmk10.changeloghub.core.reporter.impl.ConsoleReporter;
import io.github.mohmk10.changeloghub.core.reporter.impl.HtmlReporter;
import io.github.mohmk10.changeloghub.core.reporter.impl.JsonReporter;
import io.github.mohmk10.changeloghub.core.reporter.impl.MarkdownReporter;

public class ReporterFactory {

    private ReporterFactory() {
    }

    public static Reporter create(ReportFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("Report format cannot be null");
        }

        switch (format) {
            case MARKDOWN:
                return new MarkdownReporter();
            case JSON:
                return new JsonReporter();
            case HTML:
                return new HtmlReporter();
            case CONSOLE:
                return new ConsoleReporter();
            default:
                throw new IllegalArgumentException("Unsupported report format: " + format);
        }
    }
}
