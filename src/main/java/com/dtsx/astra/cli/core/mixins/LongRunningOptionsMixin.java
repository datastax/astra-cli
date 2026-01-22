package com.dtsx.astra.cli.core.mixins;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import lombok.val;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.AstraColors.stripAnsi;
import static com.dtsx.astra.cli.core.output.ExitCode.TIMED_OUT;
import static picocli.CommandLine.Spec.Target.MIXEE;

public final class LongRunningOptionsMixin {
    public record LongRunningOptions(boolean dontWait, Duration timeout) {}

    public interface WithSetTimeout {
        void setTimeout(Duration timeout);
    }

    public static final String LR_OPTS_TIMEOUT_NAME = "--timeout";
    public static final String LR_OPTS_TIMEOUT_DB_ACTIVE_DESC = "How long the command should wait for the database to become active";

    @Spec(MIXEE)
    private CommandSpec mixeeSpec;

    @Option(
        names = "--async",
        description = "Do not wait for the database to become active",
        negatable = true
    )
    private boolean dontWait;

    private Optional<Duration> timeout;

    public LongRunningOptions options(CliContext ctx) {
        val requireTimeout = timeout.orElseThrow(() -> new CongratsYouFoundABugException(mixeeSpec.root().userObject().getClass().getSimpleName() + " does not set LongRunningOptionsMixin's timeout"));

        if (requireTimeout.isZero()) {
            ctx.log().warn("Command is using a timeout of 0, which means there will be no timeout for the operation.");
            ctx.log().warn("Use --async if you meant to not wait for the operation to complete at all.");
        }

        return new LongRunningOptions(
            dontWait,
            requireTimeout
        );
    }

    public void setTimeout(Duration timeout) {
        if (timeout.isNegative()) {
            throw new OptionValidationException("timeout", "Timeout must be >= 0 (got " + timeout.toMillis() + "ms)");
        }
        this.timeout = Optional.of(timeout);
    }

    public static <S> Duration awaitGenericStatus(CliContext ctx, String thing, S target, Supplier<S> fetchStatus, Function<S, String> highlightStatus, Duration timeout) {
        val startTime = System.currentTimeMillis();

        var status = new AtomicReference<>(
            ctx.log().loading("Fetching initial status of %s".formatted(thing), (_) -> fetchStatus.get())
        );

        if (status.get().equals(target)) {
            return Duration.ZERO;
        }

        val initialMessage = "Waiting for %s to become %s (currently %s)"
            .formatted(thing, highlightStatus.apply(target), highlightStatus.apply(status.get()));

        return ctx.log().loading(initialMessage, (updateMsg) -> {
            var cycles = 0;

            while (!status.get().equals(target)) {
                val elapsed = Duration.ofMillis(System.currentTimeMillis() - startTime);

                if (timeout.isPositive() && elapsed.compareTo(timeout) >= 0) {
                    throw new AstraCliException(TIMED_OUT, """
                      @|bold,red Operation timed out after %d seconds while waiting for %s to become %s (currently %s)|@
                    
                      You can retry the operation or increase the timeout using the @!%s!@ option. @!0!@ means no timeout.
                    
                      Alternatively, you can run the command without waiting with the @!--async!@ option.
                    """.formatted(
                        timeout.toSeconds(),
                        stripAnsi(thing),
                        target,
                        status.get(),
                        LR_OPTS_TIMEOUT_NAME
                    ));
                }

                try {
                    updateMsg.accept(
                        "Waiting for %s to become %s (currently %s, elapsed: %ds)"
                            .formatted(thing, highlightStatus.apply(target), highlightStatus.apply(status.get()), elapsed.toSeconds())
                    );

                    if (cycles % 5 == 0) {
                        updateMsg.accept(
                            "Checking if %s is status %s (currently %s, elapsed: %ds)"
                                .formatted(thing, highlightStatus.apply(target), highlightStatus.apply(status.get()), elapsed.toSeconds())
                        );

                        status.set(fetchStatus.get());
                    }

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                cycles++;
            }

            return Duration.ofMillis(System.currentTimeMillis() - startTime);
        });
    }
}
