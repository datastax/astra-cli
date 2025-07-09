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

public class DbUnloadOperation extends AbstractDsbulkExeOperation<DbUnloadOperation.UnloadRequest> {
    public record UnloadRequest(
        DbRef dbRef,
        String keyspace,
        String table,
        String query,
        String encoding,
        String maxConcurrentQueries,
        String logDir,
        Either<File, Map<String, String>> dsBulkConfig,
        AstraToken token,
        String url,
        String delimiter,
        String mapping,
        boolean header,
        int skipRecords,
        int maxErrors
    ) implements CoreDsbulkOptions {}

    public DbUnloadOperation(DbGateway dbGateway, DownloadsGateway downloadsGateway, UnloadRequest request) {
        super(dbGateway, downloadsGateway, request);
    }

    @Override
    Either<DsbulkExecResult, List<String>> buildCommandLine() {
        var cmd = new ArrayList<String>();

        cmd.add("unload");

        addLoadUnloadOptions(cmd, request.delimiter(), request.url(), request.header(), request.encoding(), request.skipRecords(), request.maxErrors());

        if (request.query() != null && !request.query().isEmpty()) {
            cmd.add("-query");
            cmd.add(request.query());
        }
        
        if (request.mapping() != null && !request.mapping().isEmpty()) {
            cmd.add("-m");
            cmd.add(request.mapping());
        }
        
        return Either.right(cmd);
    }
}
