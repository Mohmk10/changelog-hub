package io.github.mohmk10.changeloghub.cli.config;

import io.github.mohmk10.changeloghub.core.reporter.ReportFormat;

public class CliConfig {

    private boolean colorsEnabled;
    private boolean verboseMode;
    private boolean quietMode;
    private ReportFormat defaultFormat;

    public CliConfig() {
        this.colorsEnabled = detectColorSupport();
        this.verboseMode = false;
        this.quietMode = false;
        this.defaultFormat = ReportFormat.CONSOLE;
    }

    public boolean isColorsEnabled() {
        return colorsEnabled;
    }

    public void setColorsEnabled(boolean colorsEnabled) {
        this.colorsEnabled = colorsEnabled;
    }

    public boolean isVerboseMode() {
        return verboseMode;
    }

    public void setVerboseMode(boolean verboseMode) {
        this.verboseMode = verboseMode;
    }

    public boolean isQuietMode() {
        return quietMode;
    }

    public void setQuietMode(boolean quietMode) {
        this.quietMode = quietMode;
    }

    public ReportFormat getDefaultFormat() {
        return defaultFormat;
    }

    public void setDefaultFormat(ReportFormat defaultFormat) {
        this.defaultFormat = defaultFormat;
    }

    private static boolean detectColorSupport() {
        String term = System.getenv("TERM");
        String colorTerm = System.getenv("COLORTERM");

        if (System.console() == null) {
            return false;
        }

        if (colorTerm != null && !colorTerm.isEmpty()) {
            return true;
        }

        if (term != null) {
            return term.contains("color") ||
                   term.contains("xterm") ||
                   term.contains("ansi");
        }

        return false;
    }

    public static CliConfig defaultConfig() {
        return new CliConfig();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final CliConfig config = new CliConfig();

        public Builder colorsEnabled(boolean enabled) {
            config.setColorsEnabled(enabled);
            return this;
        }

        public Builder verboseMode(boolean verbose) {
            config.setVerboseMode(verbose);
            return this;
        }

        public Builder quietMode(boolean quiet) {
            config.setQuietMode(quiet);
            return this;
        }

        public Builder defaultFormat(ReportFormat format) {
            config.setDefaultFormat(format);
            return this;
        }

        public CliConfig build() {
            return config;
        }
    }
}
