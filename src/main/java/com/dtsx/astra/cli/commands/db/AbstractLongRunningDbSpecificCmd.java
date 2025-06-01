package com.dtsx.astra.cli.commands.db;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Option;

public abstract class AbstractLongRunningDbSpecificCmd extends AbstractDbSpecificCmd {
    @Option(names = "--async")
    protected boolean async;

    @Option(names = "--timeout", defaultValue = "600")
    protected int timeout;

    @Override
    @MustBeInvokedByOverriders
    protected void postlude() {
        super.postlude();

        if (!async) {
            dbService.waitUntilDbActive(dbName, timeout);
        }
    }
}
