package com.dtsx.astra.cli.core.mixins;

import com.dtsx.astra.cli.core.exceptions.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.util.Optional;

import static com.dtsx.astra.cli.commands.AbstractCmd.DEFAULT_VALUE;
import static picocli.CommandLine.Spec.Target.MIXEE;

public final class LongRunningOptionsMixin {
    public record LongRunningOptions(boolean dontWait, int timeout) {}

    public interface WithSetTimeout {
        void setTimeout(int timeout);
    }

    public static final String LR_OPTS_TIMEOUT_NAME = "--timeout";
    public static final String LR_OPTS_TIMEOUT_DESC = "How long the command should wait for the database to become active%n" + DEFAULT_VALUE;

    @Spec(MIXEE)
    private CommandSpec mixeeSpec;

    @Option(
        names = "--async",
        description = { "Do not wait for the database to become active", DEFAULT_VALUE },
        negatable = true
    )
    private boolean dontWait;

    private Optional<Integer> timeout;

    public LongRunningOptions options() {
        return new LongRunningOptions(
            dontWait,
            timeout.orElseThrow(() -> new CongratsYouFoundABugException(mixeeSpec.root().userObject().getClass().getSimpleName() + " does not set LongRunningOptionsMixin's timeout"))
        );
    }

    public void setTimeout(int timeout) {
        if (timeout <= 0) {
            throw new OptionValidationException("timeout", "Timeout must be greater than 0 seconds (got " + timeout + ")");
        }
        this.timeout = Optional.of(timeout);
    }
}
