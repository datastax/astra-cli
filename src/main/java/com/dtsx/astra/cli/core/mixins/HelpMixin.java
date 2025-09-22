package com.dtsx.astra.cli.core.mixins;

import picocli.CommandLine.Option;

public final class HelpMixin {
    @Option(
        names = { "-h", "--help" },
        description = "Show this help message and exit.",
        usageHelp = true
    )
    private boolean helpRequested;
}
