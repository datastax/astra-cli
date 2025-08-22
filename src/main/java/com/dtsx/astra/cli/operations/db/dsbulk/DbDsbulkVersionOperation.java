package com.dtsx.astra.cli.operations.db.dsbulk;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;

import java.util.List;

public class DbDsbulkVersionOperation extends AbstractDsbulkExeOperation<Void> {
    public DbDsbulkVersionOperation(DbGateway dbGateway, DownloadsGateway downloadsGateway) {
        super(dbGateway, downloadsGateway, null);
    }

    @Override
    protected Either<DsbulkExecResult, List<String>> buildCommandLine() {
        return Either.right(List.of("--version"));
    }
}
