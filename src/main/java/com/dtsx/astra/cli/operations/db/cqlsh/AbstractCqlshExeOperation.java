package com.dtsx.astra.cli.operations.db.cqlsh;

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

import static com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.CqlshExecResult;

@RequiredArgsConstructor
public abstract class AbstractCqlshExeOperation<Req> implements Operation<CqlshExecResult> {
    protected final DbGateway dbGateway;
    protected final DownloadsGateway downloadsGateway;
    protected final Req request;

    public sealed interface CqlshExecResult {}

    public record CqlshInstallFailed(String error) implements CqlshExecResult {}
    public record ScbDownloadFailed(String error) implements CqlshExecResult {}
    public record Executed(int exitCode) implements CqlshExecResult {}

    abstract Either<CqlshExecResult, List<String>> buildCommandLine();

    @Override
    public CqlshExecResult execute() {
        return downloadCqlsh().flatMap((exe) -> buildCommandLine().map((flags) -> {
            val commandLine = new ArrayList<String>() {{
                add(exe.getAbsolutePath());
                addAll(flags);
            }};

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

    private Either<CqlshExecResult, File> downloadCqlsh() {
        val downloadResult = downloadsGateway.downloadCqlsh(CLIProperties.read("cqlsh.url"));

        return downloadResult.bimap(
            CqlshInstallFailed::new,
            Function.identity()
        );
    }

    protected Either<CqlshExecResult, File> downloadSCB(DbRef dbRef) {
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
