package com.dtsx.astra.cli.operations.db.cqlsh;

import com.dtsx.astra.cli.core.CliProperties;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.CoreCqlshOptions;
import com.dtsx.astra.cli.utils.DbUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.CqlshExecResult;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

@RequiredArgsConstructor
public abstract class AbstractCqlshExeOperation<Req extends CoreCqlshOptions> implements Operation<CqlshExecResult> {
    protected final DbGateway dbGateway;
    protected final DownloadsGateway downloadsGateway;
    protected final Req request;

    public sealed interface CqlshExecResult {}

    public record CqlshInstallFailed(String error) implements CqlshExecResult {}
    public record ScbDownloadFailed(String error) implements CqlshExecResult {}
    public record Executed(int exitCode) implements CqlshExecResult {}
    public record ExecutedWithOutput(int exitCode, List<String> stdout, List<String> stderr) implements CqlshExecResult {}

    public interface CoreCqlshOptions {
        boolean debug();
        Optional<String> encoding();
        int connectTimeout();
        boolean captureOutput();
    }

    abstract Either<CqlshExecResult, List<String>> buildCommandLine();

    @Override
    public CqlshExecResult execute() {
        return downloadCqlsh().flatMap((exe) -> buildCommandLine().map((flags) -> {
            val commandLine = new ArrayList<String>() {{
                add(exe.getAbsolutePath());
                addAll(buildCoreFlags());
                addAll(flags);
            }};

            val startedProcess = AstraLogger.loading("Starting cqlsh", (_) -> {
                try {
                    val res = startProcess(commandLine);
                    Thread.sleep(500); // cqlsh doesn't print anything immediately, so let spinner run a bit longer
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
        })).fold(l -> l, r -> r);
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

    private Either<CqlshExecResult, File> downloadCqlsh() {
        val downloadResult = downloadsGateway.downloadCqlsh(CliProperties.cqlsh());

        return downloadResult.bimap(
            CqlshInstallFailed::new,
            this::tryPatchCqlsh
        );
    }

    private File tryPatchCqlsh(File cqlshExe) {
        try {
            val content = Files.readAllLines(cqlshExe.toPath());

            val matcher = Pattern.compile("^for interpreter in (python.*\\s*)+; do\\s*$");

            val updatedLine = new Object() {
                int index = -1;
                String content = null;
            };

            val replaced = content.stream()
                .map(l -> matcher.matcher(l).replaceFirst((r) -> {
                    val existingInterpreters = r.group(1).split("\\s+");

                    val allInterpreters = new LinkedHashSet<String>() {{
                        addAll(List.of("python3.11", "python3.10", "python3.9"));
                        addAll(List.of(existingInterpreters));
                    }};

                    updatedLine.index = content.indexOf(l);
                    updatedLine.content = l;

                    return "for interpreter in " + String.join(" ", allInterpreters) + "; do";
                }))
                .collect(Collectors.toList());

            if (!content.equals(replaced)) {
                AstraLogger.info("Patched cqlsh script to try known supported Python versions first");

                replaced.add(updatedLine.index, "# Patched by `astra-cli` to try known supported Python versions first");
                replaced.add(updatedLine.index + 1, "# Previous line: `" + updatedLine.content + "`");

                Files.writeString(cqlshExe.toPath(), String.join(NL, replaced));
            }
        } catch (Exception e) {
            AstraLogger.exception("Error occurred attempting to patch '" + cqlshExe.getAbsolutePath() + "'");
            AstraLogger.exception(e);
        }

        return cqlshExe;
    }

    protected Either<CqlshExecResult, File> downloadSCB(DbRef dbRef, Optional<RegionName> regionName) {
        val db = dbGateway.findOne(dbRef);

        val scbPaths = downloadsGateway.downloadCloudSecureBundles(
            dbRef,
            db.getInfo().getName(),
            Collections.singleton(DbUtils.resolveDatacenter(db, regionName))
        );

        return scbPaths.bimap(
            ScbDownloadFailed::new,
            List::getFirst
        );
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
