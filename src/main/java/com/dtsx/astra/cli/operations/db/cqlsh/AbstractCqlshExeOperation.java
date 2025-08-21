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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.operations.db.cqlsh.AbstractCqlshExeOperation.CqlshExecResult;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

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
        val downloadResult = downloadsGateway.downloadCqlsh(CLIProperties.cqlsh());

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
