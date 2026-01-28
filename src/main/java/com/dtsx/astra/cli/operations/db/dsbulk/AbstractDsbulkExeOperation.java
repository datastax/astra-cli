package com.dtsx.astra.cli.operations.db.dsbulk;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
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
        Optional<String> keyspace();
        Optional<String> table();
        String encoding();
        String maxConcurrentQueries();
        String logDir();
        Optional<Path> dsBulkConfigPath();
        Map<String, String> dsBulkConfigMap();
        AstraToken token();
        Optional<RegionName> region();
    }

    protected abstract Either<DsbulkExecResult, List<String>> buildCommandLine();

    @Override
    public DsbulkExecResult execute() {
        return downloadDsbulk().flatMap((exe) -> buildCommandLine().map((flags) -> {
            val commandLine = new ArrayList<String>() {{
                add(exe.toString());
                addAll(flags);
            }};

            val process = ctx.log().loading("Starting dsbulk", (_) -> {
                try {
                    return new ProcessBuilder(commandLine) // TODO log what's being run for all exe commands
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
        val downloadResult = downloadsGateway.downloadDsbulk(ctx.properties().dsbulk());

        return downloadResult.bimap(
            DsbulkInstallFailed::new,
            Function.identity()
        );
    }

    protected Either<DsbulkExecResult, Path> downloadSCB(DbRef dbRef, Optional<RegionName> regionName) {
        val db = dbGateway.findOne(dbRef);

        val scbPaths = downloadsGateway.downloadCloudSecureBundles(
            dbRef,
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
            flags.add(options.token().unsafeUnwrap());
            
            flags.add("-b");
            flags.add(scbFile.toString());

            options.keyspace().ifPresent((ks) -> {
                flags.add("-k");
                flags.add(ks);
            });

            options.table().ifPresent((tb) -> {
                flags.add("-t");
                flags.add(tb);
            });

            flags.add("-encoding");
            flags.add(options.encoding());

            flags.add("-logDir");
            flags.add(options.logDir());

            flags.add("--log.verbosity");
            flags.add("normal");

            flags.add("--schema.allowMissingFields");
            flags.add("true");

            flags.add("-maxConcurrentQueries");
            flags.add(options.maxConcurrentQueries());

            options.dsBulkConfigPath().ifPresent((configPath) -> {
                flags.add("-f");
                flags.add(configPath.toString());
            });

            if (options.dsBulkConfigMap() != null) {
                options.dsBulkConfigMap().forEach((key, value) -> {
                    if (!key.startsWith("-")) {
                        throw new OptionValidationException("custom dsbulk flag '" + key + "'", "flag must start with '-' or '--'");
                    }

                    flags.add(key);

                    if (value != null) {
                        flags.add(value);
                    }
                });
            }

            return flags;
        });
    }

    protected static void addLoadUnloadOptions(ArrayList<String> cmd, String delimiter, String url, boolean header, int i, int i2, Optional<String> mapping) {
        cmd.add("-delim");
        cmd.add(delimiter);

        cmd.add("-url");
        cmd.add(url);

        cmd.add("-header");
        cmd.add(String.valueOf(header));

        cmd.add("-skipRecords");
        cmd.add(String.valueOf(i));

        cmd.add("-maxErrors");
        cmd.add(String.valueOf(i2));

        mapping.ifPresent((m) -> {
            cmd.add("-m");
            cmd.add(m);
        });
    }
}
