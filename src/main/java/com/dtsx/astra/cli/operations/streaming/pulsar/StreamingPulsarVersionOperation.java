package com.dtsx.astra.cli.operations.streaming.pulsar;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;

import java.util.List;

public class StreamingPulsarVersionOperation extends AbstractPulsarExeOperation<StreamingPulsarVersionOperation.PulsarVersionRequest> {
    public StreamingPulsarVersionOperation(StreamingGateway streamingGateway, DownloadsGateway downloadsGateway, PulsarVersionRequest request) {
        super(streamingGateway, downloadsGateway, request);
    }

    public record PulsarVersionRequest() {}

    @Override
    protected Either<PulsarExecResult, List<String>> buildCommandLine() {
        return Either.right(List.of("version"));
    }
}
