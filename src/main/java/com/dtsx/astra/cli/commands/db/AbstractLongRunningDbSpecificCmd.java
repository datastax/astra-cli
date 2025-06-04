package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.exceptions.db.CongratsYouFoundABugException;
import com.dtsx.astra.cli.exceptions.db.OptionValidationException;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Option;

import java.util.Optional;

public abstract class AbstractLongRunningDbSpecificCmd extends AbstractDbSpecificCmd {
    protected static final String TIMEOUT_DESC = "How long the command should wait for the database to become active%n" + DEFAULT_VALUE;

    @Option(names = "--async", description = { "Do not wait for operation to complete", DEFAULT_VALUE })
    protected boolean async;

    protected Optional<Integer> timeout = Optional.empty();

    protected abstract void setTimeout(int timeout);

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();

        if (timeout.isEmpty()) {
            throw new CongratsYouFoundABugException("Forgot to set the default timeout for " + this.getClass().getSimpleName());
        }

        if (timeout.get() <= 0) {
            throw new OptionValidationException("timeout", "Timeout must be greater than 0 seconds (got " + timeout.get() + ")");
        }
    }

    @Override
    @MustBeInvokedByOverriders
    protected void postlude() {
        super.postlude();
    }
}
