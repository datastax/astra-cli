package com.dtsx.astra.cli.operations.db.cqlsh;

import com.dtsx.astra.cli.CLIProperties;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshPathOperation.CqlPathResponse;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.File;

@RequiredArgsConstructor
public class DbCqlshPathOperation implements Operation<CqlPathResponse> {
    private final DownloadsGateway downloadsGateway;
    private final boolean shouldInstall;

    public sealed interface CqlPathResponse {}
    public record ExePathFound(File path) implements CqlPathResponse {}
    public record NoInstallationFound() implements CqlPathResponse {}
    public record InstallationFailed(String error) implements CqlPathResponse {}

    @Override
    public CqlPathResponse execute() {
        val existingPath = downloadsGateway.cqlshPath(CLIProperties.cqlsh());
        
        if (existingPath.isPresent()) {
            return new ExePathFound(existingPath.get());
        }
        
        if (shouldInstall) {
            val installResult = downloadsGateway.downloadCqlsh(CLIProperties.cqlsh());

            if (installResult.isRight()) {
                return new ExePathFound(installResult.getRight());
            } else {
                return new InstallationFailed(installResult.getLeft());
            }
        }
        
        return new NoInstallationFound();
    }
}
