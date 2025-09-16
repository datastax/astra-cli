package com.dtsx.astra.cli.commands.streaming.pulsar;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.commands.streaming.AbstractStreamingCmd;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.misc.WindowsUnsupportedException;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.streaming.pulsar.AbstractPulsarExeOperation.ConfFileCreationFailed;
import com.dtsx.astra.cli.operations.streaming.pulsar.AbstractPulsarExeOperation.Executed;
import com.dtsx.astra.cli.operations.streaming.pulsar.AbstractPulsarExeOperation.PulsarExecResult;
import com.dtsx.astra.cli.operations.streaming.pulsar.AbstractPulsarExeOperation.PulsarInstallFailed;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.FILE_ISSUE;

public abstract class AbstractPulsarExecCmd extends AbstractStreamingCmd<PulsarExecResult> {
    protected DownloadsGateway downloadsGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        WindowsUnsupportedException.throwIfWindows(ctx);
        super.prelude();
        downloadsGateway = ctx.gateways().mkDownloadsGateway(ctx);
    }

    @Override
    protected final OutputHuman executeHuman(Supplier<PulsarExecResult> result) {
        return switch (result.get()) {
            case PulsarInstallFailed(var msg) -> throwPulsarInstallationFailed(msg);
            case ConfFileCreationFailed(var msg) -> throwPulsarInstallationFailed(msg);
            case Executed(var exitCode) -> AstraCli.exit(exitCode);
        };
    }

    public static <T> T throwPulsarInstallationFailed(String error) {
        throw new AstraCliException(FILE_ISSUE, """
          @|bold,red Failed to install pulsar: %s|@
        
          Please ensure you have a stable network connection and sufficient permissions, then try again.
        """.formatted(error), List.of(
            new Hint("Retry installation:", "${cli.name} streaming pulsar version")
        ));
    }
}
