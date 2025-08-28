package com.dtsx.astra.cli.operations.db.cqlsh;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshVersionOperation.CqlshVersionRequest;

import java.util.List;
import java.util.Optional;

public class DbCqlshVersionOperation extends AbstractCqlshExeOperation<CqlshVersionRequest> {
    public DbCqlshVersionOperation(CliContext ctx, DbGateway dbGateway, DownloadsGateway downloadsGateway, CqlshVersionRequest request) {
        super(ctx, dbGateway, downloadsGateway, request);
    }

    public record CqlshVersionRequest(
        boolean debug,
        Optional<String> encoding,
        int connectTimeout,
        boolean captureOutput
    ) implements CoreCqlshOptions {}

    @Override
    protected Either<CqlshExecResult, List<String>> buildCommandLine() {
        return Either.right(List.of("--version"));
    }
}
