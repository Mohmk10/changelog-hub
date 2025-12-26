package io.github.mohmk10.changeloghub.cli.output;

import java.io.IOException;
import java.io.PrintStream;

public class ConsoleOutputHandler implements OutputHandler {

    private final PrintStream out;
    private final PrintStream err;
    private final boolean colorsEnabled;

    public ConsoleOutputHandler() {
        this(System.out, System.err, detectColorSupport());
    }

    public ConsoleOutputHandler(PrintStream out, PrintStream err, boolean colorsEnabled) {
        this.out = out;
        this.err = err;
        this.colorsEnabled = colorsEnabled;
    }

    @Override
    public void write(String content) throws IOException {
        out.print(content);
    }

    @Override
    public void writeLine(String line) throws IOException {
        out.println(line);
    }

    @Override
    public void writeError(String error) throws IOException {
        if (colorsEnabled) {
            err.println(AnsiColors.RED + error + AnsiColors.RESET);
        } else {
            err.println(error);
        }
    }

    @Override
    public void close() throws IOException {
        out.flush();
        err.flush();
    }

    public boolean isColorsEnabled() {
        return colorsEnabled;
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
                   term.contains("ansi") ||
                   term.contains("256");
        }

        String os = System.getProperty("os.name").toLowerCase();
        return !os.contains("win");
    }

    public static final class AnsiColors {
        public static final String RESET = "\u001B[0m";
        public static final String RED = "\u001B[31m";
        public static final String GREEN = "\u001B[32m";
        public static final String YELLOW = "\u001B[33m";
        public static final String BLUE = "\u001B[34m";
        public static final String MAGENTA = "\u001B[35m";
        public static final String CYAN = "\u001B[36m";
        public static final String WHITE = "\u001B[37m";
        public static final String BOLD = "\u001B[1m";

        private AnsiColors() {
        }
    }
}
