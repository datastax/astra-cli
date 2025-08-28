package com.dtsx.astra.cli.operations.db.dsbulk;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkCountOperation.CountRequest;
import lombok.val;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DbDsbulkCountOperation extends AbstractDsbulkExeOperation<CountRequest> {
    public record CountRequest(
        DbRef dbRef,
        String keyspace,
        String table,
        String query,
        String encoding,
        String maxConcurrentQueries,
        String logDir,
        Either<Path, Map<String, String>> dsBulkConfig,
        AstraToken token,
        Optional<RegionName> region
    ) implements CoreDsbulkOptions {}

    public DbDsbulkCountOperation(CliContext ctx, DbGateway dbGateway, DownloadsGateway downloadsGateway, CountRequest request) {
        super(ctx, dbGateway, downloadsGateway, request);
    }

    @Override
    Either<DsbulkExecResult, List<String>> buildCommandLine() {
        return buildCoreFlags(request).map(coreFlags -> {
            val cmd = new ArrayList<>(coreFlags);

            cmd.add("count");

            if (request.query() != null && !request.query().isEmpty()) {
                cmd.add("--schema.query");
                cmd.add(request.query());
            }
            
            return cmd;
        });
    }
}
