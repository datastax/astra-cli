package com.dtsx.astra.cli.operations.db.dsbulk;

import com.dtsx.astra.cli.CliProperties;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkPathOperation.DsbulkPathResponse;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.File;

@RequiredArgsConstructor
public class DbDsbulkPathOperation implements Operation<DsbulkPathResponse> {
    private final DownloadsGateway downloadsGateway;
    private final boolean shouldInstall;

    public sealed interface DsbulkPathResponse {}
    public record ExePathFound(File path) implements DsbulkPathResponse {}
    public record NoInstallationFound() implements DsbulkPathResponse {}
    public record InstallationFailed(String error) implements DsbulkPathResponse {}

    @Override
    public DsbulkPathResponse execute() {
        val existingPath = downloadsGateway.dsbulkPath(CliProperties.dsbulk());
        
        if (existingPath.isPresent()) {
            return new ExePathFound(existingPath.get());
        }
        
        if (shouldInstall) {
            val installResult = downloadsGateway.downloadDsbulk(CliProperties.dsbulk());

            if (installResult.isRight()) {
                return new ExePathFound(installResult.getRight());
            } else {
                return new InstallationFailed(installResult.getLeft());
            }
        }
        
        return new NoInstallationFound();
    }
}
