package com.dtsx.astra.cli.operations.streaming.pulsar;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.pulsar.AbstractPulsarExeOperation.PulsarExecResult;
import com.dtsx.astra.cli.utils.FileUtils;
import com.dtsx.astra.sdk.streaming.domain.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
public abstract class AbstractPulsarExeOperation<Req> implements Operation<PulsarExecResult> {
    protected final CliContext ctx;
    protected final StreamingGateway streamingGateway;
    protected final DownloadsGateway downloadsGateway;
    protected final Req request;

    public sealed interface PulsarExecResult {}

    public record PulsarInstallFailed(String error) implements PulsarExecResult {}
    public record ConfFileCreationFailed(String error) implements PulsarExecResult {}
    public record Executed(int exitCode) implements PulsarExecResult {}

    abstract Either<PulsarExecResult, List<String>> buildCommandLine();

    @Override
    public PulsarExecResult execute() {
        return downloadPulsar().flatMap((exe) -> buildCommandLine().map((flags) -> {
            val commandLine = new ArrayList<String>() {{
                add(exe.toString());
                addAll(flags);
            }};

            val process = ctx.log().loading("Starting pulsar", (_) -> {
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

    private Either<PulsarExecResult, Path> downloadPulsar() {
        val downloadResult = downloadsGateway.downloadPulsarShell(ctx.properties().pulsar());

        return downloadResult.bimap(
            PulsarInstallFailed::new,
            Function.identity()
        );
    }

    protected Either<PulsarExecResult, Path> mkPulsarConfFile(TenantName tenantName) {
        val tenant = streamingGateway.findOne(tenantName);

        val confFile = ctx.home().dirs.pulsar(ctx.properties().pulsar().version()).use().resolve(
            "client-" + tenant.getCloudProvider() + "-" + tenant.getCloudRegion() + "-" + tenant.getTenantName() + ".conf"
        );

        if (Files.exists(confFile) && Files.isDirectory(confFile)) {
            return Either.left(new ConfFileCreationFailed("A file with the same name already exists and is a directory: " + confFile));
        }

        FileUtils.createFileIfNotExists(confFile, null);

        try (val writer = Files.newBufferedWriter(confFile)) {
            genConfFileContents(writer, tenant);
            writer.flush();
            writer.close();
            return Either.pure(confFile);
        } catch (IOException e1) {
            return Either.left(new ConfFileCreationFailed("Failed to write to Pulsar configuration file %s: %s".formatted(confFile, e1.getMessage())));
        }
    }

    protected void genConfFileContents(Writer writer, Tenant tenant) throws IOException {
        writer.write("brokerServiceUrl=pulsar+ssl://pulsar-%s-%s.streaming.datastax.com:6651%n".formatted(tenant.getCloudProvider(), tenant.getCloudRegion()));
        writer.write("webServiceUrl=https://pulsar-%s-%s.api.streaming.datastax.com%n".formatted(tenant.getCloudProvider(), tenant.getCloudRegion()));
        writer.write("authPlugin=org.apache.pulsar.client.impl.auth.AuthenticationToken%n".formatted());
        writer.write("authParams=token:%s%n".formatted(tenant.getPulsarToken()));
        writer.write("tlsAllowInsecureConnection=false%n".formatted());
        writer.write("tlsEnableHostnameVerification=true%n".formatted());
        writer.write("useKeyStoreTls=false%n".formatted());
        writer.write("tlsTrustStoreType=JKS%n".formatted());
        writer.write("tlsTrustStorePath=%s%n".formatted(""));
        writer.write("tlsTrustStorePassword=%s%n".formatted(""));
    }
}
