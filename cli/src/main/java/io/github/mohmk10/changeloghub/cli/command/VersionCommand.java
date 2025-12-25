package io.github.mohmk10.changeloghub.cli.command;

import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

@Command(
    name = "version",
    description = "Display version and system information",
    mixinStandardHelpOptions = true
)
public class VersionCommand implements Callable<Integer> {

    private static final String VERSION = "1.0.0-SNAPSHOT";
    private static final String BUILD_DATE = "2025-12-25";

    @Override
    public Integer call() {
        System.out.println();
        System.out.println("Changelog Hub");
        System.out.println("=============");
        System.out.println();
        System.out.println("Version:    " + VERSION);
        System.out.println("Build Date: " + BUILD_DATE);
        System.out.println();
        System.out.println("System Information");
        System.out.println("------------------");
        System.out.println("Java Version:    " + System.getProperty("java.version"));
        System.out.println("Java Vendor:     " + System.getProperty("java.vendor"));
        System.out.println("Java Home:       " + System.getProperty("java.home"));
        System.out.println("OS Name:         " + System.getProperty("os.name"));
        System.out.println("OS Version:      " + System.getProperty("os.version"));
        System.out.println("OS Architecture: " + System.getProperty("os.arch"));
        System.out.println("User Directory:  " + System.getProperty("user.dir"));
        System.out.println();
        System.out.println("Supported Formats");
        System.out.println("-----------------");
        System.out.println("  - OpenAPI 3.0.x");
        System.out.println("  - OpenAPI 3.1.x");
        System.out.println("  - Swagger 2.0");
        System.out.println("  - YAML / JSON");
        System.out.println();
        System.out.println("For more information: https://github.com/Mohmk10/changelog-hub");
        System.out.println();

        return 0;
    }
}
