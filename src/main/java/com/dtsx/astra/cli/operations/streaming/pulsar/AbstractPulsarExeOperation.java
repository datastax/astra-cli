package com.dtsx.astra.cli.operations.streaming.pulsar;

import com.dtsx.astra.cli.CLIProperties;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.pulsar.AbstractPulsarExeOperation.PulsarResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
public abstract class AbstractPulsarExeOperation<Req> implements Operation<PulsarResult> {
    protected final DbGateway dbGateway;
    protected final DownloadsGateway downloadsGateway;
    protected final Req request;

    public sealed interface PulsarResult {}

    public record PulsarInstallFailed(String error) implements PulsarResult {}
    public record ScbDownloadFailed(String error) implements PulsarResult {}
    public record Executed(int exitCode) implements PulsarResult {}

    abstract Either<PulsarResult, List<String>> buildCommandLine();

    @Override
    public PulsarResult execute() {
        return downloadPulsar().flatMap((exe) -> buildCommandLine().map((flags) -> {
            val commandLine = new ArrayList<String>() {{
                add(exe.getAbsolutePath());
                addAll(flags);
            }};

            val process = AstraLogger.loading("Starting pulsar", (_) -> {
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

    private Either<PulsarResult, File> downloadPulsar() {
        val downloadResult = downloadsGateway.downloadPulsarShell(CLIProperties.read("pulsar.shell.url"), CLIProperties.read("pulsar.shell.version"));

        return downloadResult.bimap(
            PulsarInstallFailed::new,
            Function.identity()
        );
    }

    protected Either<PulsarResult, File> downloadSCB(DbRef dbRef) {
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
