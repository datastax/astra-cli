package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.CLIProperties;
import com.dtsx.astra.cli.config.AstraConfig.Profile;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.dtsx.astra.cli.operations.db.DbCqlshOperation.CqlshResult;

@RequiredArgsConstructor
public class DbCqlshOperation implements Operation<CqlshResult> {
    private final DbGateway dbGateway;
    private final DownloadsGateway downloadsGateway;
    private final CqlshRequest request;

    public sealed interface CqlshResult {}

    public record CqlshInstallFailed(String error) implements CqlshResult {}
    public record ScbDownloadFailed(String error) implements CqlshResult {}
    public record Executed(int exitCode) implements CqlshResult {}

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
    public CqlshResult execute() {
        return downloadCqlsh().flatMap((exe) -> downloadSCB().map((scb) -> {
            val commandLine = buildCommandLine(request, exe, scb);

            val process = AstraLogger.loading("Starting cqlsh", (_) -> {
                try {
                    return new ProcessBuilder(commandLine)
                        .inheritIO()
                        .start();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            try {
                val res = new Executed(process.waitFor());
                Thread.sleep(500);
                return res;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        })).fold(l -> l, r -> r);
    }

    private Either<CqlshResult, File> downloadCqlsh() {
        val downloadResult = downloadsGateway.downloadCqlshArchive(CLIProperties.read("cqlsh.url"), CLIProperties.read("cqlsh.tarball"));

        return downloadResult.bimap(
            CqlshInstallFailed::new,
            Function.identity()
        );
    }

    private Either<CqlshResult, File> downloadSCB() {
        val db = dbGateway.findOneDb(request.dbRef());

        val scbPaths = downloadsGateway.downloadCloudSecureBundles(
            request.dbRef(),
            db.getInfo().getName(),
            db.getInfo().getDatacenters().stream().limit(1).toList()
        );

        return scbPaths.bimap(
            ScbDownloadFailed::new,
            List::getFirst
        );
    }

    private String[] buildCommandLine(CqlshRequest request, File exe, File scb) {
        val commands = new ArrayList<String>();
        
        commands.add(exe.getAbsolutePath());

        commands.add("-u");
        commands.add("token");
        commands.add("-p");
        commands.add(request.profile().token().unwrap());
        commands.add("-b");
        commands.add(scb.getAbsolutePath());

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

        return commands.toArray(new String[0]);
    }
}
