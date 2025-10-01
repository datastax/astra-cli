package com.dtsx.astra.cli.operations.db.cqlsh;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshPathOperation.CqlPathResponse;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.nio.file.Path;

@RequiredArgsConstructor
public class DbCqlshPathOperation implements Operation<CqlPathResponse> {
    private final CliContext ctx;
    private final DownloadsGateway downloadsGateway;
    private final boolean shouldInstall;

    public sealed interface CqlPathResponse {}
    public record ExePathFound(Path path) implements CqlPathResponse {}
    public record NoInstallationFound() implements CqlPathResponse {}
    public record InstallationFailed(String error) implements CqlPathResponse {}

    @Override
    public CqlPathResponse execute() {
        val existingPath = downloadsGateway.cqlshPath(ctx.properties().cqlsh());
        
        if (existingPath.isPresent()) {
            return new ExePathFound(existingPath.get());
        }
        
        if (shouldInstall) {
            val installResult = downloadsGateway.downloadCqlsh(ctx.properties().cqlsh());

            if (installResult.isRight()) {
                return new ExePathFound(installResult.getRight());
            } else {
                return new InstallationFailed(installResult.getLeft());
            }
        }
        
        return new NoInstallationFound();
    }
}
