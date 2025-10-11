package com.dtsx.astra.cli.core.mixins;

import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

public class HelpMixin {
    @Option(
        names = { "-h", "--help" },
        description = "Show this help message and exit.",
        showDefaultValue = Visibility.NEVER,
        usageHelp = true
    )
    private boolean helpRequested;
}
