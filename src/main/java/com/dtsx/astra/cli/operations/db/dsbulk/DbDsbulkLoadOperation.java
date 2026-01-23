package com.dtsx.astra.cli.operations.db.dsbulk;

import com.dtsx.astra.cli.core.CliContext;
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
        Optional<String> keyspace,
        Optional<String> table,
        String encoding,
        String maxConcurrentQueries,
        String logDir,
        Optional<Path> dsBulkConfigPath,
        Map<String, String> dsBulkConfigMap,
        AstraToken token,
        String url,
        String delimiter,
        Optional<String> mapping,
        boolean header,
        int skipRecords,
        int maxErrors,
        boolean dryRun,
        boolean allowMissingFields,
        Optional<RegionName> region
    ) implements CoreDsbulkOptions {}

    public DbDsbulkLoadOperation(CliContext ctx, DbGateway dbGateway, DownloadsGateway downloadsGateway, LoadRequest request) {
        super(ctx, dbGateway, downloadsGateway, request);
    }

    @Override
    protected Either<DsbulkExecResult, List<String>> buildCommandLine() {
        return buildCoreFlags(request).map((coreFlags) -> {
            val cmd = new ArrayList<String>() {{
                add("load");
                addAll(coreFlags);
            }};

            addLoadUnloadOptions(cmd, request.delimiter(), request.url(), request.header(), request.skipRecords(), request.maxErrors(), request.mapping());

            if (request.dryRun()) {
                cmd.add("-dryRun");
                cmd.add("true");
            }
            
            if (request.allowMissingFields()) {
                cmd.add("--schema.allowMissingFields");
                cmd.add("true");
            }
            
            return cmd;
        });
    }
}
