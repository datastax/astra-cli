package com.dtsx.astra.cli.operations.db.dsbulk;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.db.dsbulk.DbDsbulkLoadOperation.LoadRequest;
import lombok.val;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DbDsbulkLoadOperation extends AbstractDsbulkExeOperation<LoadRequest> {
    public record LoadRequest(
        DbRef dbRef,
        String keyspace,
        String table,
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
        boolean dryRun,
        boolean allowMissingFields,
        Optional<RegionName> region
    ) implements CoreDsbulkOptions {}

    public DbDsbulkLoadOperation(DbGateway dbGateway, DownloadsGateway downloadsGateway, LoadRequest request) {
        super(dbGateway, downloadsGateway, request);
    }

    @Override
    protected Either<DsbulkExecResult, List<String>> buildCommandLine() {
        return buildCoreFlags(request).map(coreFlags -> {
            val cmd = new ArrayList<>(coreFlags);
            
            cmd.add("load");

            addLoadUnloadOptions(cmd, request.delimiter(), request.url(), request.header(), request.encoding(), request.skipRecords(), request.maxErrors());

            if (request.mapping() != null && !request.mapping().isEmpty()) {
                cmd.add("-m");
                cmd.add(request.mapping());
            }
            
            if (request.dryRun()) {
                cmd.add("-dryRun");
            }
            
            if (request.allowMissingFields()) {
                cmd.add("--schema.allowMissingFields");
                cmd.add("true");
            }
            
            return cmd;
        });
    }
}
