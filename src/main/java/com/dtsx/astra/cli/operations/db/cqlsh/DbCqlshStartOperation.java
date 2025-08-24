package com.dtsx.astra.cli.operations.db.cqlsh;

import com.dtsx.astra.cli.core.CliEnvironment;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshStartOperation.CqlshRequest;
import lombok.val;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class DbCqlshStartOperation extends AbstractCqlshExeOperation<CqlshRequest> {
    public DbCqlshStartOperation(DbGateway dbGateway, DownloadsGateway downloadsGateway, CqlshRequest request) {
        super(dbGateway, downloadsGateway, request);
    }

    public record CqlshRequest(
        DbRef dbRef,
        boolean debug,
        Optional<String> encoding,
        Optional<String> keyspace,
        Optional<ExecSource> execSource,
        int connectTimeout,
        int requestTimeout,
        Optional<RegionName> region,
        Profile profile,
        Supplier<String> readStdin,
        boolean captureOutput
    ) implements CoreCqlshOptions {}

    public sealed interface ExecSource {
        record Statement(String cql) implements ExecSource {}
        record CqlFile(File file) implements ExecSource {}
        record Stdin() implements ExecSource {}
    }

    @Override
    protected Either<CqlshExecResult, List<String>> buildCommandLine() {
        val commands = new ArrayList<String>();

        val scbFile = downloadSCB(request.dbRef, request.region);

        if (scbFile.isLeft()) {
            return Either.left(scbFile.getLeft());
        }

        commands.add("-u");
        commands.add("token");
        commands.add("-p");
        commands.add(request.profile().token().unwrap());
        commands.add("-b");
        commands.add(scbFile.getRight().getAbsolutePath());

        if (request.keyspace().isPresent()) {
            commands.add("-k");
            commands.add(request.keyspace().get());
        }

        commands.add("--request-timeout");
        commands.add(String.valueOf(request.requestTimeout()));

        if (request.execSource().isPresent()) {
            switch (request.execSource().get()) {
                case ExecSource.Statement(var stmt) -> {
                    commands.add("-e");
                    commands.add(stmt);
                }
                case ExecSource.CqlFile(var file) -> {
                    commands.add("-f");
                    commands.add(file.getAbsolutePath());
                }
                case ExecSource.Stdin _ -> {
                    if (CliEnvironment.isTty()) {
                        AstraLogger.info("Reading CQL statements from stdin...");
                        AstraLogger.info("Use backslashes for multi-line statements");
                    }
                    commands.add("-e");
                    commands.add(request.readStdin.get());
                }
            }
        }

        return Either.right(commands);
    }
}
