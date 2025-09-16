package com.dtsx.astra.cli.operations.db.dsbulk;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;

import java.util.List;

public class DbDsbulkVersionOperation extends AbstractDsbulkExeOperation<Void> {
    public DbDsbulkVersionOperation(CliContext ctx, DbGateway dbGateway, DownloadsGateway downloadsGateway) {
        super(ctx, dbGateway, downloadsGateway, null);
    }

    @Override
    protected Either<DsbulkExecResult, List<String>> buildCommandLine() {
        return Either.pure(List.of("--version"));
    }
}
