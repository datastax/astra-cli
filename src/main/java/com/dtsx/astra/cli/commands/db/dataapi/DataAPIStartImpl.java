package com.dtsx.astra.cli.commands.db.dataapi;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.commands.db.keyspace.AbstractPromptForKeyspaceCmd;
import com.dtsx.astra.cli.core.CliConstants.$Collection;
import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.CliConstants.$Table;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.core.output.prompters.specific.CollectionNamePrompter;
import com.dtsx.astra.cli.core.output.prompters.specific.TableNamePrompter;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dataapi.DbDataAPIExecOperation;
import com.dtsx.astra.cli.operations.db.dataapi.DbDataAPIExecOperation.*;
import com.dtsx.astra.cli.utils.CollectionUtils;
import com.dtsx.astra.cli.utils.StringUtils;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class DataAPIStartImpl extends AbstractPromptForKeyspaceCmd<DataAPIExecResult> {
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
    public List<String> $packages = List.of();

    @Option(
        names = { $Regions.LONG, $Regions.SHORT },
        description = "The region to use",
        paramLabel = $Regions.LABEL
    )
    protected Optional<RegionName> $region;

    @Option(
        names = { $Collection.LONG, $Collection.SHORT },
        description = "The collection to use",
        paramLabel = $Collection.LABEL,
        fallbackValue = "__prompt__",
        arity = "0..1"
    )
    public @Nullable String $collectionName;

    @Option(
        names = { $Table.LONG, $Table.SHORT },
        description = "The table to use",
        paramLabel = $Table.LABEL,
        fallbackValue = "__prompt__",
        arity = "0..1"
    )
    public @Nullable String $tableName;

    @Parameters
    public List<String> $extraArgs = List.of();

    protected abstract String code();

    protected abstract boolean isRepl();

    protected abstract boolean captureOutputForNonHumanOutput();

    protected abstract String contextName();

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();

        if (!ctx.properties().disableBetaWarnings()) {
            ctx.log().warn("${cli.name} db data-api commands are still in beta and may change without notice.");
        }

        if (!$extraArgs.isEmpty()) {
            if ($extraArgs.getFirst().trim().startsWith("-")) {
                throw new AstraCliException(ExitCode.VALIDATION_ISSUE, """
                  @|bold,red Database must explicitly be passed as the first positional argument when using extra flags after '--'|@
            
                  @|italic Note: if you really have a database name that starts with a dash which triggered this check, pass the db's ID instead (which you can get with @!${cli.name} db list!@).|@
                """, List.of(
                    new Hint("Example usage", "${cli.name} db data-api repl my_db -- -p --trace-warnings")
                ));
            }

            $extraArgs.removeFirst();
        }

        if ("__prompt__".equals($collectionName)) {
            val collectionGateway = ctx.gateways().mkCollectionGateway(profile().token(), profile().env());
            $collectionName = CollectionNamePrompter.prompt(ctx, collectionGateway, $keyspaceRef, "Select the collection to use", originalArgs());
        }

        if ("__prompt__".equals($tableName)) {
            val tableGateway = ctx.gateways().mkTableGateway(profile().token(), profile().env());
            $tableName = TableNamePrompter.prompt(ctx, tableGateway, $keyspaceRef, "Select the table to use", originalArgs());
        }
    }

    @Override
    protected OutputHuman executeHuman(Supplier<DataAPIExecResult> resultSupplier) {
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
            $keyspaceRef,
            Optional.ofNullable($collectionName),
            Optional.ofNullable($tableName),
            profile(),
            $packages,
            code(),
            $extraArgs,
            isRepl(),
            captureOutputForNonHumanOutput() && ctx.outputIsNotHuman()
        ));
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the database to connect to:";
    }
}
