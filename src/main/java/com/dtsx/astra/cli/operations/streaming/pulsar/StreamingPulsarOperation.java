package com.dtsx.astra.cli.operations.streaming.pulsar;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.operations.streaming.pulsar.StreamingPulsarOperation.PulsarRequest;
import lombok.val;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.utils.MiscUtils.toFn;

public class StreamingPulsarOperation extends AbstractPulsarExeOperation<PulsarRequest> {
    public StreamingPulsarOperation(CliContext ctx, StreamingGateway streamingGateway, DownloadsGateway downloadsGateway, PulsarRequest request) {
        super(ctx, streamingGateway, downloadsGateway, request);
    }

    public record PulsarRequest(
        TenantName tenantName,
        boolean failOnError,
        Optional<Either<String, Path>> execute,
        boolean noProgress
    ) {}

    @Override
    protected Either<PulsarExecResult, List<String>> buildCommandLine() {
        val commands = new ArrayList<String>();

        val pulsarConfFile = mkPulsarConfFile(request.tenantName());

        if (pulsarConfFile.isLeft()) {
            return Either.left(pulsarConfFile.getLeft());
        }

        commands.add("--config");
        commands.add(pulsarConfFile.getRight().toString());

        if (request.noProgress) {
            commands.add("--no-progress");
        }

        if (request.failOnError) {
            commands.add("--fail-on-error");
        }

        request.execute.ifPresent(either -> either.fold(
            toFn((command) -> {
                commands.add("--execute-command");
                commands.add(command);
            }),
            toFn((file) -> {
                commands.add("--filename");
                commands.add(file.toString());
            })
        ));

        return Either.right(commands);
    }
}
