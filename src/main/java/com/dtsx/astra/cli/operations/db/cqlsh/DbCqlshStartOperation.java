package com.dtsx.astra.cli.operations.db.cqlsh;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshStartOperation.CqlshRequest;
import lombok.val;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class DbCqlshStartOperation extends AbstractCqlshExeOperation<CqlshRequest> {
    public DbCqlshStartOperation(CliContext ctx, DbGateway dbGateway, DownloadsGateway downloadsGateway, CqlshRequest request) {
        super(ctx, dbGateway, downloadsGateway, request);
    }

    public record CqlshRequest(
        Either<DbRef, Path> dbOrScb,
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
        record CqlFile(Path file) implements ExecSource {}
        record Stdin() implements ExecSource {}
    }

    @Override
    protected Either<CqlshExecResult, List<String>> buildCommandLine() {
        val commands = new ArrayList<String>();

        val scbFile = getScb(request.dbOrScb, request.region);

        if (scbFile.isLeft()) {
            return Either.left(scbFile.getLeft());
        }

        commands.add("-u");
        commands.add("token");
        commands.add("-p");
        commands.add(request.profile().token().unsafeUnwrap());
        commands.add("-b");
        commands.add(scbFile.getRight().toString());

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
                    commands.add(file.toString());
                }
                case ExecSource.Stdin _ -> {
                    if (ctx.isTty()) {
                        ctx.log().info("Reading CQL statements from stdin...");
                        ctx.log().info("Use backslashes for multi-line statements");
                    }
                    commands.add("-e");
                    commands.add(request.readStdin.get());
                }
            }
        }

        return Either.pure(commands);
    }
}
