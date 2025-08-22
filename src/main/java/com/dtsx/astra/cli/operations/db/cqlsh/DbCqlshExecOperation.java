package com.dtsx.astra.cli.operations.db.cqlsh;

import com.dtsx.astra.cli.core.CliEnvironment;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class DbCqlshExecOperation extends DbCqlshStartOperation {
    private final Optional<Either<String, File>> source;
    private final Supplier<String> readStdin;

    public DbCqlshExecOperation(DbGateway dbGateway, DownloadsGateway downloadsGateway, ExecRequest request) {
        super(dbGateway, downloadsGateway, new CqlshRequest(
            request.dbRef(),
            request.debug(),
            request.encoding(),
            request.keyspace(),
            request.connectTimeout(),
            request.requestTimeout(),
            request.profile()
        ));
        this.source = request.source();
        this.readStdin = request.readStdin();
    }

    public record ExecRequest(
        DbRef dbRef,
        boolean debug,
        Optional<String> encoding,
        Optional<Either<String, File>> source,
        Optional<String> keyspace,
        int connectTimeout,
        int requestTimeout,
        Profile profile,
        Supplier<String> readStdin
    ) implements CoreCqlshOptions {}

    @Override
    protected Either<CqlshExecResult, List<String>> buildCommandLine() {
        return super.buildCommandLine().map((flags) -> {
            if (source.isPresent()) {
                if (source.get().isLeft()) {
                    flags.add("-e");
                    flags.add(source.get().getLeft());
                } else {
                    flags.add("-f");
                    flags.add(source.get().getRight().getAbsolutePath());
                }
            } else {
                if (CliEnvironment.isTty()) {
                    AstraLogger.info("Reading CQL statements from stdin...");
                    AstraLogger.info("Use backslashes for multi-line statements");
                }
                flags.add("-e");
                flags.add(readStdin.get());
            }

            return flags;
        });
    }
}
