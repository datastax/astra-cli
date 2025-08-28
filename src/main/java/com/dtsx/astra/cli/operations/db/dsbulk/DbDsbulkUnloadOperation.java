package com.dtsx.astra.cli.operations.db.dsbulk;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkUnloadOperation.UnloadRequest;
import lombok.val;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DbDsbulkUnloadOperation extends AbstractDsbulkExeOperation<UnloadRequest> {
    public record UnloadRequest(
        DbRef dbRef,
        String keyspace,
        String table,
        String query,
        String encoding,
        String maxConcurrentQueries,
        String logDir,
        Either<Path, Map<String, String>> dsBulkConfig,
        AstraToken token,
        String url,
        String delimiter,
        String mapping,
        boolean header,
        int skipRecords,
        int maxErrors,
        Optional<RegionName> region
    ) implements CoreDsbulkOptions {}

    public DbDsbulkUnloadOperation(CliContext ctx, DbGateway dbGateway, DownloadsGateway downloadsGateway, UnloadRequest request) {
        super(ctx, dbGateway, downloadsGateway, request);
    }

    @Override
    Either<DsbulkExecResult, List<String>> buildCommandLine() {
        return buildCoreFlags(request).map(coreFlags -> {
            val cmd = new ArrayList<>(coreFlags);
            
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
            
            return cmd;
        });
    }
}
