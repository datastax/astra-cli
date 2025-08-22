package com.dtsx.astra.cli.commands.db.cqlsh;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.commands.db.AbstractDbCmd;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.misc.WindowsUnsupportedException;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.CqlshExecResult;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.CqlshInstallFailed;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.Executed;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.ScbDownloadFailed;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.FILE_ISSUE;

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

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        WindowsUnsupportedException.throwIfWindows();
        super.prelude();
    }

    @Override
    protected final OutputHuman executeHuman(Supplier<CqlshExecResult> result) {
        return switch (result.get()) {
            case CqlshInstallFailed(var msg) -> throwCqlshInstallationFailed(msg);
            case ScbDownloadFailed(var msg) -> throwCqlshInstallationFailed(msg);
            case Executed(var exitCode) -> AstraCli.exit(exitCode);
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
}
