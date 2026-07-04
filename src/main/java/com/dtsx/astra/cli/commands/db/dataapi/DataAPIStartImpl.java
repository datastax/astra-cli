package com.dtsx.astra.cli.commands.db.dataapi;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.commands.db.AbstractPromptForDbCmd;
import com.dtsx.astra.cli.core.CliConstants.$Collection;
import com.dtsx.astra.cli.core.CliConstants.$Keyspace;
import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.CliConstants.$Table;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dataapi.DbDataAPIExecOperation;
import com.dtsx.astra.cli.operations.db.dataapi.DbDataAPIExecOperation.*;
import com.dtsx.astra.cli.utils.CollectionUtils;
import com.dtsx.astra.cli.utils.StringUtils;
import lombok.val;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class DataAPIStartImpl extends AbstractPromptForDbCmd<DataAPIExecResult> {
    @Option(
        names = { "-l", "--lang" },
        description = "The client language to use (default: ${DEFAULT-VALUE}). Valid values: ${COMPLETION-CANDIDATES}",
        defaultValue = "js"
    )
    public Language $language;

    @Option(
        names = { "-a", "--artifacts" },
        split = ",",
        description = "Additional packages to include (e.g., pandas for Python, lodash for JS)"
    )
    public List<String> $packages = new ArrayList<>();

    @Option(
        names = { $Regions.LONG, $Regions.SHORT },
        description = "The region to use",
        paramLabel = $Regions.LABEL
    )
    protected Optional<RegionName> $region;

    @Option(
        names = { $Keyspace.LONG, $Keyspace.SHORT },
        description = "The keyspace to use",
        paramLabel = $Keyspace.LABEL
    )
    public Optional<String> $keyspaceName;

    @Option(
        names = { $Collection.LONG, $Collection.SHORT },
        description = "The collection to use",
        paramLabel = $Collection.LABEL
    )
    public Optional<String> $collectionName;

    @Option(
        names = { $Table.LONG, $Table.SHORT },
        description = "The table to use",
        paramLabel = $Table.LABEL
    )
    public Optional<String> $tableName;

    protected abstract String code();
    
    protected abstract boolean isRepl();

    protected abstract boolean captureOutputForNonHumanOutput();

    protected abstract String contextName();

    @Override
    protected OutputHuman executeHuman(Supplier<DataAPIExecResult> resultSupplier) {
        if (!ctx.properties().disableBetaWarnings()) {
            ctx.log().warn("${cli.name} db data-api commands are still in beta and may change without notice.");
        }

        return switch (resultSupplier.get()) {
            case Executed e -> AstraCli.exit(e.exitCode());
            case InvalidDbStatus e -> throwInvalidDbStatus(e);
            case OperationFailed e -> throwOperationFailed(e);
            case ExecutedWithOutput _ -> throw new CongratsYouFoundABugException("Should not be able to get to `executeHuman` with `ExecutedWithOutput` when output is `HUMAN`");
        };
    }

    @Override
    protected final OutputAll execute(Supplier<DataAPIExecResult> resultSupplier) {
        if (!captureOutputForNonHumanOutput()) {
            return super.execute(resultSupplier);
        }

        if (!ctx.properties().disableBetaWarnings()) {
            ctx.log().warn("${cli.name} db data-api commands are still in beta and may change without notice.");
        }

        return switch (resultSupplier.get()) {
            case Executed _ -> throw new CongratsYouFoundABugException("Should not be able to get to `execute` with `Executed` when output is `" + ctx.outputType() + "`");
            case ExecutedWithOutput res -> {
                val msg = "Data API executed with exit code " + res.exitCode() + ", with " + res.stdout().size() + " lines in stdout and " + res.stderr().size() + " lines in stderr.";

                yield OutputAll.response(msg, CollectionUtils.sequencedMapOf(
                    "exitCode", res.exitCode(),
                    "stdout", String.join(StringUtils.NL, res.stdout()),
                    "stderr", String.join(StringUtils.NL, res.stderr())
                ));
            }
            case InvalidDbStatus e -> throwInvalidDbStatus(e);
            case OperationFailed e -> throwOperationFailed(e);
        };
    }

    private <T> T throwInvalidDbStatus(InvalidDbStatus e) {
        throw new AstraCliException(ExitCode.STATUS_ISSUE, "Cannot start " + contextName() + ": database is in status " + e.status());
    }

    private <T> T throwOperationFailed(OperationFailed e) {
        throw new AstraCliException(ExitCode.IO_ISSUE, "Failed to start " + contextName() + ": " + e.error());
    }

    @Override
    protected Operation<DataAPIExecResult> mkOperation() {
        return new DbDataAPIExecOperation(ctx, dbGateway, new DbDataAPIExecRequest(
            $language,
            $dbRef,
            $region,
            $keyspaceName,
            $collectionName,
            $tableName,
            profile(),
            $packages,
            code(),
            isRepl(),
            captureOutputForNonHumanOutput() && ctx.outputIsNotHuman()
        ));
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the database to connect to:";
    }
}
