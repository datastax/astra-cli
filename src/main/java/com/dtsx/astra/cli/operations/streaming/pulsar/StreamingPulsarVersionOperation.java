package com.dtsx.astra.cli.operations.streaming.pulsar;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;

import java.util.List;

public class StreamingPulsarVersionOperation extends AbstractPulsarExeOperation<StreamingPulsarVersionOperation.PulsarVersionRequest> {
    public StreamingPulsarVersionOperation(CliContext ctx, StreamingGateway streamingGateway, DownloadsGateway downloadsGateway, PulsarVersionRequest request) {
        super(ctx, streamingGateway, downloadsGateway, request);
    }

    public record PulsarVersionRequest() {}

    @Override
    protected Either<PulsarExecResult, List<String>> buildCommandLine() {
        return Either.pure(List.of("version"));
    }
}
