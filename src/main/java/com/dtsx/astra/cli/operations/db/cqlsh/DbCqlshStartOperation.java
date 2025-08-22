package com.dtsx.astra.cli.operations.db.cqlsh;

import com.dtsx.astra.cli.config.AstraConfig.Profile;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshStartOperation.CqlshRequest;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DbCqlshStartOperation extends AbstractCqlshExeOperation<CqlshRequest> {
    public DbCqlshStartOperation(DbGateway dbGateway, DownloadsGateway downloadsGateway, CqlshRequest request) {
        super(dbGateway, downloadsGateway, request);
    }

    public record CqlshRequest(
        DbRef dbRef,
        boolean debug,
        Optional<String> encoding,
        Optional<String> keyspace,
        int connectTimeout,
        int requestTimeout,
        Profile profile
    ) implements CoreCqlshOptions {}

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

        if (request.keyspace().isPresent()) {
            commands.add("-k");
            commands.add(request.keyspace().get());
        }

        commands.add("--request-timeout");
        commands.add(String.valueOf(request.requestTimeout()));

        return Either.right(commands);
    }
}
