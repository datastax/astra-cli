package com.dtsx.astra.cli.core.mixins;

import picocli.CommandLine.Option;

public class HelpMixin {
    @Option(
        names = { "-h", "--help" },
        description = "Show this help message and exit.",
        usageHelp = true,
        hidden = true
    )
    private boolean helpRequested;
}
