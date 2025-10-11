package com.dtsx.astra.cli.commands.streaming.pulsar;

import com.dtsx.astra.cli.commands.streaming.AbstractStreamingCmd;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.pulsar.StreamingPulsarPathOperation;
import com.dtsx.astra.cli.operations.streaming.pulsar.StreamingPulsarPathOperation.ExePathFound;
import com.dtsx.astra.cli.operations.streaming.pulsar.StreamingPulsarPathOperation.InstallationFailed;
import com.dtsx.astra.cli.operations.streaming.pulsar.StreamingPulsarPathOperation.NoInstallationFound;
import com.dtsx.astra.cli.operations.streaming.pulsar.StreamingPulsarPathOperation.PulsarPathResponse;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.commands.streaming.pulsar.AbstractPulsarExecCmd.throwPulsarInstallationFailed;
import static com.dtsx.astra.cli.core.output.ExitCode.FILE_ISSUE;

@Command(
    name = "path",
    description = "Get the path to the currently installed pulsar executable"
)
@Example(
    comment = "Get the path to the pulsar executable",
    command = "${cli.name} streaming pulsar path"
)
@Example(
    comment = "Use the pulsar exe to run a custom command",
    command = "$(${cli.name} streaming pulsar path) --help"
)
@Example(
    comment = "Get path only if pulsar exists, don't install",
    command = "${cli.name} streaming pulsar path --if-exists"
)
public class StreamingPulsarPathCmd extends AbstractStreamingCmd<PulsarPathResponse> {
    @Option(
        names = { "--if-exists" },
        description = "Only return path if pulsar exists, don't install automatically",
        defaultValue = "false"
    )
    public boolean $ifExists;

    @Override
    protected Operation<PulsarPathResponse> mkOperation() {
        return new StreamingPulsarPathOperation(ctx, ctx.gateways().mkDownloadsGateway(ctx), !$ifExists);
    }

    @Override
    protected final OutputAll execute(Supplier<PulsarPathResponse> result) {
        return switch (result.get()) {
            case ExePathFound(var file) -> handleExePathFound(file);
            case NoInstallationFound _ -> throwNoInstallationFound();
            case InstallationFailed(var error) -> throwPulsarInstallationFailed(error);
        };
    }

    public OutputAll handleExePathFound(Path file) {
        return OutputAll.response(file.toString());
    }

    public <T> T throwNoInstallationFound() {
        throw new AstraCliException(FILE_ISSUE, """
          @|bold,red No pulsar installation was found in '%s'.|@
        
          Please install @!pulsar!@ by running any pulsar command through @!${cli.name}!@.
        
          @|faint,italic Note that the CLI does not recognize pulsar installations done outside of astra.|@
        """.formatted(ctx.home().getDir()), List.of(
            new Hint("Example command to install pulsar:", "${cli.name} streaming pulsar version")
        ));
    }
}
