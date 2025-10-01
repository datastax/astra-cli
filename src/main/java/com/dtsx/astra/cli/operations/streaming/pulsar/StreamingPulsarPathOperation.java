package com.dtsx.astra.cli.operations.streaming.pulsar;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.nio.file.Path;

@RequiredArgsConstructor
public class StreamingPulsarPathOperation implements Operation<StreamingPulsarPathOperation.PulsarPathResponse> {
    private final CliContext ctx;
    private final DownloadsGateway downloadsGateway;
    private final boolean shouldInstall;

    public sealed interface PulsarPathResponse {}
    public record ExePathFound(Path path) implements PulsarPathResponse {}
    public record NoInstallationFound() implements PulsarPathResponse {}
    public record InstallationFailed(String error) implements PulsarPathResponse {}

    @Override
    public PulsarPathResponse execute() {
        val existingPath = downloadsGateway.pulsarShellPath(ctx.properties().pulsar());
        
        if (existingPath.isPresent()) {
            return new ExePathFound(existingPath.get());
        }
        
        if (shouldInstall) {
            val installResult = downloadsGateway.downloadPulsarShell(ctx.properties().pulsar());

            if (installResult.isRight()) {
                return new ExePathFound(installResult.getRight());
            } else {
                return new InstallationFailed(installResult.getLeft());
            }
        }
        
        return new NoInstallationFound();
    }
}
