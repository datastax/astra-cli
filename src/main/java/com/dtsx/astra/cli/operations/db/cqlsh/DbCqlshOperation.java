package com.dtsx.astra.cli.operations.db.cqlsh;

import com.dtsx.astra.cli.config.AstraConfig.Profile;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshOperation.CqlshRequest;
import lombok.val;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DbCqlshOperation extends AbstractCqlshExeOperation<CqlshRequest> {
    public DbCqlshOperation(DbGateway dbGateway, DownloadsGateway downloadsGateway, CqlshRequest request) {
        super(dbGateway, downloadsGateway, request);
    }

    public record CqlshRequest(
        DbRef dbRef,
        boolean version,
        boolean debug,
        Optional<String> encoding,
        Optional<String> execute,
        Optional<File> file,
        Optional<String> keyspace,
        int connectTimeout,
        int requestTimeout,
        Profile profile
    ) {}

    @Override
    protected Either<CqlshExecResult, List<String>> buildCommandLine() {
        val commands = new ArrayList<String>();

        val scbFile = downloadSCB(request.dbRef);

        if (scbFile.isLeft()) {
            return Either.left(scbFile.getLeft());
        }

        commands.add("-u");
        commands.add("token");
        commands.add("-p");
        commands.add(request.profile().token().unwrap());
        commands.add("-b");
        commands.add(scbFile.getRight().getAbsolutePath());

        if (request.debug()) {
            commands.add("--debug");
        }
        if (request.version()) {
            commands.add("--version");
        }
        if (request.file().isPresent()) {
            commands.add("-f");
            commands.add(request.file().get().getAbsolutePath());
        }
        if (request.keyspace().isPresent()) {
            commands.add("-k");
            commands.add(request.keyspace().get());
        }
        if (request.encoding().isPresent()) {
            commands.add("--encoding");
            commands.add(request.encoding().get());
        }

        commands.add("--connect-timeout");
        commands.add(String.valueOf(request.connectTimeout()));
        commands.add("--request-timeout");
        commands.add(String.valueOf(request.requestTimeout()));
        
        if (request.execute().isPresent()) {
            commands.add("-e");
            commands.add(request.execute().get());
        }

        return Either.right(commands);
    }
}
