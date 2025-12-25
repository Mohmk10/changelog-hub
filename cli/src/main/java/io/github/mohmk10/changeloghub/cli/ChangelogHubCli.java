package io.github.mohmk10.changeloghub.cli;

import io.github.mohmk10.changeloghub.cli.command.AnalyzeCommand;
import io.github.mohmk10.changeloghub.cli.command.CompareCommand;
import io.github.mohmk10.changeloghub.cli.command.ValidateCommand;
import io.github.mohmk10.changeloghub.cli.command.VersionCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "changelog-hub",
    mixinStandardHelpOptions = true,
    version = "1.0.0-SNAPSHOT",
    description = "API Breaking Change Detector - Detect breaking changes in your APIs",
    subcommands = {
        CompareCommand.class,
        AnalyzeCommand.class,
        ValidateCommand.class,
        VersionCommand.class
    },
    footer = {
        "",
        "Examples:",
        "  changelog-hub compare old-api.yaml new-api.yaml",
        "  changelog-hub compare old.yaml new.yaml -f markdown -o CHANGELOG.md",
        "  changelog-hub analyze api.yaml",
        "  changelog-hub validate api.yaml --strict",
        "",
        "For more information, visit: https://github.com/Mohmk10/changelog-hub"
    }
)
public class ChangelogHubCli implements Runnable {

    private static final String BANNER = """

              _____ _                            _             _   _       _
             / ____| |                          | |           | | | |     | |
            | |    | |__   __ _ _ __   __ _  ___| | ___   __ _| |_| |_   _| |__
            | |    | '_ \\ / _` | '_ \\ / _` |/ _ \\ |/ _ \\ / _` |  _  | | | | '_ \\
            | |____| | | | (_| | | | | (_| |  __/ | (_) | (_| | | | | |_| | |_) |
             \\_____|_| |_|\\__,_|_| |_|\\__, |\\___|_|\\___/ \\__, |_| |_|\\__,_|_.__/
                                       __/ |              __/ |
                                      |___/              |___/
            """;

    @CommandLine.Option(names = {"--banner"}, description = "Show ASCII banner")
    private boolean showBanner;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new ChangelogHubCli())
            .setExecutionExceptionHandler(new ExceptionHandler())
            .execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        if (showBanner) {
            System.out.println(BANNER);
        }
        new CommandLine(this).usage(System.out);
    }

    private static class ExceptionHandler implements CommandLine.IExecutionExceptionHandler {
        @Override
        public int handleExecutionException(Exception ex, CommandLine cmd, CommandLine.ParseResult parseResult) {
            cmd.getErr().println(cmd.getColorScheme().errorText("Error: " + ex.getMessage()));
            if (parseResult.hasMatchedOption("--verbose") || parseResult.hasMatchedOption("-v")) {
                ex.printStackTrace(cmd.getErr());
            }
            return 1;
        }
    }
}
