package com.dtsx.astra.cli.operations.db.dsbulk;

import com.dtsx.astra.cli.CLIProperties;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.CoreDsbulkOptions;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkExecResult;

@RequiredArgsConstructor
public abstract class AbstractDsbulkExeOperation<Req extends CoreDsbulkOptions> implements Operation<DsbulkExecResult> {
    protected final DbGateway dbGateway;
    protected final DownloadsGateway downloadsGateway;
    protected final Req request;

    public sealed interface DsbulkExecResult {}

    public record DsbulkInstallFailed(String error) implements DsbulkExecResult {}
    public record ScbDownloadFailed(String error) implements DsbulkExecResult {}
    public record Executed(int exitCode) implements DsbulkExecResult {}

    public interface CoreDsbulkOptions {
        DbRef dbRef();
        String keyspace();
        String table();
        String encoding();
        String maxConcurrentQueries();
        String logDir();
        Either<File, Map<String, String>> dsBulkConfig();
        AstraToken token();
    }

    abstract Either<DsbulkExecResult, List<String>> buildCommandLine();

    @Override
    public DsbulkExecResult execute() {
        return downloadDsbulk().flatMap((exe) -> buildCoreFlags().flatMap((coreFlags) -> buildCommandLine().map((flags) -> {
            val commandLine = new ArrayList<String>() {{
                add(exe.getAbsolutePath());
                addAll(coreFlags);
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
        }))).fold(l -> l, r -> r);
    }

    private Either<DsbulkExecResult, File> downloadDsbulk() {
        val downloadResult = downloadsGateway.downloadDsbulk(CLIProperties.dsbulk());

        return downloadResult.bimap(
            DsbulkInstallFailed::new,
            Function.identity()
        );
    }

    protected Either<DsbulkExecResult, File> downloadSCB(DbRef dbRef) {
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

    private Either<DsbulkExecResult, List<String>> buildCoreFlags() {
        return downloadSCB(request.dbRef()).map(scbFile -> {
            val flags = new ArrayList<String>();
            
            flags.add("-u");
            flags.add("token");
            
            flags.add("-p");
            flags.add(request.token().unwrap());
            
            flags.add("-b");
            flags.add(scbFile.getAbsolutePath());

            if (request.keyspace() != null && !request.keyspace().isEmpty()) {
                flags.add("-k");
                flags.add(request.keyspace());
            }

            if (request.table() != null && !request.table().isEmpty()) {
                flags.add("-t");
                flags.add(request.table());
            }

            if (request.encoding() != null && !request.encoding().isEmpty()) {
                flags.add("-encoding");
                flags.add(request.encoding());
            }

            if (request.logDir() != null && !request.logDir().isEmpty()) {
                flags.add("-logDir");
                flags.add(request.logDir());
            }

            flags.add("--log.verbosity");
            flags.add("normal");

            flags.add("--schema.allowMissingFields");
            flags.add("true");

            if (request.maxConcurrentQueries() != null && !request.maxConcurrentQueries().isEmpty()) {
                flags.add("-maxConcurrentQueries");
                flags.add(request.maxConcurrentQueries());
            }

            request.dsBulkConfig().fold(
                configFile -> {
                    flags.add("-f");
                    flags.add(configFile.getAbsolutePath());
                    return null;
                },
                configMap -> {
                    configMap.forEach((key, value) -> {
                        flags.add(key);
                        if (value != null && !value.isEmpty()) {
                            flags.add(value);
                        }
                    });
                    return null;
                }
            );
            
            return flags;
        });
    }

    protected static void addLoadUnloadOptions(ArrayList<String> cmd, String delimiter, String url, boolean header, String encoding, int i, int i2) {
        cmd.add("-delim");
        cmd.add(delimiter);

        cmd.add("-url");
        cmd.add(url);

        cmd.add("-header");
        cmd.add(String.valueOf(header));

        cmd.add("-encoding");
        cmd.add(encoding);

        cmd.add("-skipRecords");
        cmd.add(String.valueOf(i));

        cmd.add("-maxErrors");
        cmd.add(String.valueOf(i2));
    }
}
