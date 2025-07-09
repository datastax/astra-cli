package com.dtsx.astra.cli.operations.db.dsbulk;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DbCountOperation extends AbstractDsbulkExeOperation<DbCountOperation.CountRequest> {
    public record CountRequest(
        DbRef dbRef,
        String keyspace,
        String table,
        String query,
        String encoding,
        String maxConcurrentQueries,
        String logDir,
        Either<File, Map<String, String>> dsBulkConfig,
        AstraToken token
    ) implements CoreDsbulkOptions {}

    public DbCountOperation(DbGateway dbGateway, DownloadsGateway downloadsGateway, CountRequest request) {
        super(dbGateway, downloadsGateway, request);
    }

    @Override
    Either<DsbulkExecResult, List<String>> buildCommandLine() {
        var cmd = new ArrayList<String>();

        cmd.add("count");

        if (request.query() != null && !request.query().isEmpty()) {
            cmd.add("--schema.query");
            cmd.add(request.query());
        }
        
        return Either.right(cmd);
    }
}
