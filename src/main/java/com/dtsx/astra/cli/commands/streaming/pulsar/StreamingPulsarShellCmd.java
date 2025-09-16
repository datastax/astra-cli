package com.dtsx.astra.cli.commands.streaming.pulsar;

import com.dtsx.astra.cli.core.completions.impls.TenantNamesCompletion;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.pulsar.AbstractPulsarExeOperation.PulsarExecResult;
import com.dtsx.astra.cli.operations.streaming.pulsar.StreamingPulsarOperation;
import com.dtsx.astra.cli.operations.streaming.pulsar.StreamingPulsarOperation.PulsarRequest;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.Optional;

@Command(
    name = "shell",
    description = "Launch Apache Pulsar shell for a streaming tenant"
)
@Example(
    comment = "Launch pulsar shell for a tenant",
    command = "${cli.name} streaming pulsar shell my_tenant"
)
public class StreamingPulsarShellCmd extends AbstractPulsarExecCmd {
    @Parameters(
        paramLabel = "TENANT",
        description = "The name of the tenant to connect to",
        completionCandidates = TenantNamesCompletion.class
    )
    public TenantName $tenantName;

    @ArgGroup
    public @Nullable Exec $exec;

    public static class Exec {
        @Option(
            names = { "-e", "--execute" },
            paramLabel = "COMMAND",
            description = "Execute the statement and quit"
        )
        public Optional<String> $execute;

        @Option(
            names = { "-f", "--filename" },
            description = "Input filename with a list of commands to be executed. Each command must be separated by a newline",
            paramLabel = "FILE"
        )
        public Optional<Path> $commandsFile;
    }

    @Option(
        names = { "-F", "--fail-on-error" },
        description = { "Interrupt the shell if a command throws an exception", DEFAULT_VALUE }
    )
    public boolean $failOnError;

    @Option(
        names = { "-np", "--no-progress" },
        description = "Display raw output of the commands without progress visualization"
    )
    public boolean $noProgress;

    @Override
    protected Operation<PulsarExecResult> mkOperation() {
        return new StreamingPulsarOperation(ctx, streamingGateway, downloadsGateway, new PulsarRequest(
            $tenantName,
            $failOnError,
            Optional.ofNullable($exec).map(e -> e.$execute.<Either<String, Path>>map(Either::left).orElseGet(() -> Either.pure(e.$commandsFile.orElseThrow()))),
            $noProgress
        ));
    }
}
