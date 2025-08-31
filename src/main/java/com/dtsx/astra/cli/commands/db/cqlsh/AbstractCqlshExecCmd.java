package com.dtsx.astra.cli.commands.db.cqlsh;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.commands.db.AbstractDbCmd;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.exceptions.internal.misc.WindowsUnsupportedException;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.*;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.FILE_ISSUE;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

public abstract class AbstractCqlshExecCmd extends AbstractDbCmd<CqlshExecResult> {
    @Option(
        names = { "--debug" },
        description = "Show additional debugging information"
    )
    public boolean $debug;

    @Option(
        names = { "--encoding" },
        description = "Output encoding (default: utf8)",
        paramLabel = "ENCODING"
    )
    public Optional<String> $encoding;

    @Option(
        names = { "--connect-timeout" },
        description = { "Connection timeout in seconds", DEFAULT_VALUE },
        paramLabel = "TIMEOUT",
        defaultValue = "10"
    )
    public int $connectTimeout;

    protected DownloadsGateway downloadsGateway;

    protected abstract boolean captureOutputForNonHumanOutput();

    protected abstract Operation<CqlshExecResult> mkOperation(boolean captureOutput);

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        WindowsUnsupportedException.throwIfWindows(ctx);
        super.prelude();
        downloadsGateway = ctx.gateways().mkDownloadsGateway(profile().token(), profile().env(), ctx);
    }

    @Override
    protected Operation<CqlshExecResult> mkOperation() {
        return mkOperation(captureOutputForNonHumanOutput() && ctx.outputIsNotHuman());
    }

    @Override
    protected final OutputHuman executeHuman(Supplier<CqlshExecResult> result) {
        return switch (result.get()) {
            case CqlshInstallFailed(var msg) -> throwCqlshInstallationFailed(msg);
            case ScbDownloadFailed(var msg) -> throwCqlshInstallationFailed(msg);
            case Executed(var exitCode) -> AstraCli.exit(exitCode);
            case ExecutedWithOutput _ -> throw new CongratsYouFoundABugException("Should not be able to get to `executeHuman` with `ExecutedWithOutput` when output is `HUMAN`");
        };
    }

    @Override
    protected final OutputAll execute(Supplier<CqlshExecResult> result) {
        if (!captureOutputForNonHumanOutput()) {
            return super.execute(result);
        }

        return switch (result.get()) {
            case CqlshInstallFailed(var msg) -> throwCqlshInstallationFailed(msg);
            case ScbDownloadFailed(var msg) -> throwCqlshInstallationFailed(msg);
            case Executed _ -> throw new CongratsYouFoundABugException("Should not be able to get to `execute` with `Executed` when output is `" + ctx.outputType() + "`");
            case ExecutedWithOutput res -> handleExecutedWithOutput(res);
        };
    }

    public static <T> T throwCqlshInstallationFailed(String error) {
        throw new AstraCliException(FILE_ISSUE, """
          @|bold,red Failed to install cqlsh: %s|@
        
          Please ensure you have a stable network connection and sufficient permissions, then try again.
        """.formatted(error), List.of(
            new Hint("Retry installation:", "${cli.name} db cqlsh version")
        ));
    }

    private OutputAll handleExecutedWithOutput(ExecutedWithOutput res) {
        val msg = "Cqlsh executed with exit code %d, with %d lines in stdout and %d lines in stderr."
            .formatted(res.exitCode(), res.stdout().size(), res.stderr().size());

        return OutputAll.response(msg, sequencedMapOf(
            "exitCode", res.exitCode(),
            "stdout", String.join(NL, res.stdout()),
            "stderr", String.join(NL, res.stderr())
        ));
    }
}
