package com.dtsx.astra.cli.operations.db.dsbulk;

import com.dtsx.astra.cli.CLIProperties;
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
import java.util.function.Function;

import static com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkResult;

@RequiredArgsConstructor
public abstract class AbstractDsbulkExeOperation<Req> implements Operation<DsbulkResult> {
    protected final DbGateway dbGateway;
    protected final DownloadsGateway downloadsGateway;
    protected final Req request;

    public sealed interface DsbulkResult {}

    public record DsbulkInstallFailed(String error) implements DsbulkResult {}
    public record ScbDownloadFailed(String error) implements DsbulkResult {}
    public record Executed(int exitCode) implements DsbulkResult {}

    public record CoreDsbulkOptions(
        String keyspace,
        String table,
        String encoding,
        String maxConcurrentQueries,
        String logDir,
        File dsBulkConfig
    ) {}

    abstract Either<DsbulkResult, List<String>> buildCommandLine();

    @Override
    public DsbulkResult execute() {
        return downloadDsbulk().flatMap((exe) -> buildCommandLine().map((flags) -> {
            val commandLine = new ArrayList<String>() {{
                add(exe.getAbsolutePath());
                addAll(flags);
            }};

            val process = AstraLogger.loading("Starting dsbulk", (_) -> {
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

    private Either<DsbulkResult, File> downloadDsbulk() {
        val downloadResult = downloadsGateway.downloadDsbulk(CLIProperties.read("dsbulk.url"), CLIProperties.read("dsbulk.version"));

        return downloadResult.bimap(
            DsbulkInstallFailed::new,
            Function.identity()
        );
    }

    protected Either<DsbulkResult, File> downloadSCB(DbRef dbRef) {
        val db = dbGateway.findOne(dbRef);

        val scbPaths = downloadsGateway.downloadCloudSecureBundles(
            dbRef,
            db.getInfo().getName(),
            db.getInfo().getDatacenters().stream().limit(1).toList()
        );

        return scbPaths.bimap(
            ScbDownloadFailed::new,
            List::getFirst
        );
    }
}
