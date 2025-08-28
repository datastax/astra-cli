package com.dtsx.astra.cli.operations.db.dsbulk;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.CliProperties;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.utils.DbUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkExecResult;

@RequiredArgsConstructor
public abstract class AbstractDsbulkExeOperation<Req> implements Operation<DsbulkExecResult> {
    protected final CliContext ctx;
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
        Either<Path, Map<String, String>> dsBulkConfig();
        AstraToken token();
        Optional<RegionName> region();
    }

    abstract Either<DsbulkExecResult, List<String>> buildCommandLine();

    @Override
    public DsbulkExecResult execute() {
        return downloadDsbulk().flatMap((exe) -> buildCommandLine().map((flags) -> {
            val commandLine = new ArrayList<String>() {{
                add(exe.toString());
                addAll(flags);
            }};

            val process = ctx.log().loading("Starting dsbulk", (_) -> {
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

    private Either<DsbulkExecResult, Path> downloadDsbulk() {
        val downloadResult = downloadsGateway.downloadDsbulk(CliProperties.dsbulk());

        return downloadResult.bimap(
            DsbulkInstallFailed::new,
            Function.identity()
        );
    }

    protected Either<DsbulkExecResult, Path> downloadSCB(DbRef dbRef, Optional<RegionName> regionName) {
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

    protected Either<DsbulkExecResult, List<String>> buildCoreFlags(CoreDsbulkOptions options) {
        return downloadSCB(options.dbRef(), options.region()).map(scbFile -> {
            val flags = new ArrayList<String>();
            
            flags.add("-u");
            flags.add("token");
            
            flags.add("-p");
            flags.add(options.token().unwrap());
            
            flags.add("-b");
            flags.add(scbFile.toString());

            if (options.keyspace() != null && !options.keyspace().isEmpty()) {
                flags.add("-k");
                flags.add(options.keyspace());
            }

            if (options.table() != null && !options.table().isEmpty()) {
                flags.add("-t");
                flags.add(options.table());
            }

            if (options.encoding() != null && !options.encoding().isEmpty()) {
                flags.add("-encoding");
                flags.add(options.encoding());
            }

            if (options.logDir() != null && !options.logDir().isEmpty()) {
                flags.add("-logDir");
                flags.add(options.logDir());
            }

            flags.add("--log.verbosity");
            flags.add("normal");

            flags.add("--schema.allowMissingFields");
            flags.add("true");

            if (options.maxConcurrentQueries() != null && !options.maxConcurrentQueries().isEmpty()) {
                flags.add("-maxConcurrentQueries");
                flags.add(options.maxConcurrentQueries());
            }

            options.dsBulkConfig().fold(
                configFile -> {
                    flags.add("-f");
                    flags.add(configFile.toString());
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
