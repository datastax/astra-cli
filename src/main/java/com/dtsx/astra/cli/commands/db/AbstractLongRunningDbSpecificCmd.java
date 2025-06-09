package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.exceptions.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.exceptions.cli.OptionValidationException;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Option;

import java.util.Optional;

public abstract class AbstractLongRunningDbSpecificCmd extends AbstractDbSpecificCmd {
    protected static final String TIMEOUT_DESC = "How long the command should wait for the database to become active%n" + DEFAULT_VALUE;

    @Option(names = "--async", description = { "Do not wait for operation to complete", DEFAULT_VALUE })
    protected boolean dontWait;

    protected Integer timeout;

    protected abstract void setTimeout(int timeout);

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();

        if (timeout == null) {
            throw new CongratsYouFoundABugException("Forgot to set the default timeout for " + this.getClass().getSimpleName());
        }

        if (timeout <= 0) {
            throw new OptionValidationException("timeout", "Timeout must be greater than 0 seconds (got " + timeout + ")");
        }
    }

    @Override
    @MustBeInvokedByOverriders
    protected void postlude() {
        super.postlude();
    }
}
