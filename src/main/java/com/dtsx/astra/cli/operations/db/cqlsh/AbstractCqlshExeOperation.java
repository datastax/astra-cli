package com.dtsx.astra.cli.operations.db.cqlsh;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.exceptions.internal.db.ScbDownloadException;
import com.dtsx.astra.cli.core.exceptions.internal.db.UnexpectedDbStatusException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.CoreCqlshOptions;
import com.dtsx.astra.cli.utils.DbUtils;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.CqlshExecResult;

@RequiredArgsConstructor
public abstract class AbstractCqlshExeOperation<Req extends CoreCqlshOptions> implements Operation<CqlshExecResult> {
    protected final CliContext ctx;
    protected final DbGateway dbGateway;
    protected final DownloadsGateway downloadsGateway;
    protected final Req request;

    public sealed interface CqlshExecResult {}
    public record InvalidDbStatus(DatabaseStatusType status) implements CqlshExecResult {}

    public record CqlshInstallFailed(String error) implements CqlshExecResult {}
    public record Executed(int exitCode) implements CqlshExecResult {}
    public record ExecutedWithOutput(int exitCode, List<String> stdout, List<String> stderr) implements CqlshExecResult {}

    public static class CqlshInvalidDbStatusException extends RuntimeException {
        public final DatabaseStatusType status;
        public CqlshInvalidDbStatusException(DatabaseStatusType status) { this.status = status; }
    }

    public interface CoreCqlshOptions {
        boolean debug();
        Optional<String> encoding();
        int connectTimeout();
        boolean captureOutput();
    }

    abstract List<String> buildCommandLine();

    @Override
    public CqlshExecResult execute() {
        try {
            return downloadCqlsh().map((exe) -> {
                val flags = buildCommandLine();
                val commandLine = new ArrayList<String>() {{
                    add(exe.toString());
                    addAll(buildCoreFlags());
                    addAll(flags);
                }};

                val startedProcess = ctx.log().loading("Starting cqlsh", (_) -> {
                    try {
                        val res = startProcess(commandLine);
                        Thread.sleep(100); // cqlsh doesn't print anything immediately, so let spinner run a bit longer
                        return res;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                try {
                    val result = startedProcess.waitFor();
                    Thread.sleep(500);
                    return result;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }).fold(l -> l, r -> r);
        } catch (CqlshInvalidDbStatusException e) {
            return new InvalidDbStatus(e.status);
        }
    }

    @FunctionalInterface
    interface RunningProcess {
        CqlshExecResult waitFor() throws InterruptedException;
    }

    private RunningProcess startProcess(List<String> commandLine) throws Exception {
        val pb = new ProcessBuilder(commandLine);

        if (request.captureOutput()) {
            val process = pb.start();

            val stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            val stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            val output = stdOut.lines().toList();
            val error = stdErr.lines().toList();

            return () -> new ExecutedWithOutput(process.waitFor(), output, error);
        } else {
            val process = pb.inheritIO().start();
            return () -> new Executed(process.waitFor());
        }
    }

    private Either<CqlshExecResult, Path> downloadCqlsh() {
        return downloadsGateway
            .downloadCqlsh(ctx.properties().cqlsh())
            .mapLeft(CqlshInstallFailed::new);
    }

    protected Path getScb(Either<DbRef, Path> dbOrScb, Optional<RegionName> regionName) {
        if (dbOrScb.isRight()) {
            return validateScbPath(dbOrScb);
        }
        return downloadScb(dbOrScb, regionName);
    }

    private static @NotNull Path validateScbPath(Either<DbRef, Path> dbRef) {
        val path = dbRef.getRight();

        if (!path.toString().endsWith(".zip")) {
            throw new ScbDownloadException("Invalid SCB path (" + path + "): bundle must be a .zip file");
        }

        if (!Files.exists(path)) {
            throw new ScbDownloadException("Invalid SCB path (" + path + "): file does not exist");
        }

        return path;
    }

    private Path downloadScb(Either<DbRef, Path> dbRef, Optional<RegionName> regionName) {
        val db = dbGateway.findOne(dbRef.getLeft());

        if (db.getStatus() != DatabaseStatusType.ACTIVE) {
            throw new CqlshInvalidDbStatusException(db.getStatus());
        }

        val scbPaths = downloadsGateway.downloadCloudSecureBundles(
            dbRef.getLeft(),
            Collections.singleton(DbUtils.resolveDatacenter(db, regionName))
        );

        return scbPaths.getFirst();
    }

    private List<String> buildCoreFlags() {
        val flags = new ArrayList<String>();

         if (request.debug()) {
            flags.add("--debug");
        }

        if (request.encoding().isPresent()) {
            flags.add("--encoding");
            flags.add(request.encoding().get());
        }

        flags.add("--connect-timeout");
        flags.add(String.valueOf(request.connectTimeout()));

        return flags;
    }
}
