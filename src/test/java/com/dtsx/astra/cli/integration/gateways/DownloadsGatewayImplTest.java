package com.dtsx.astra.cli.integration.gateways;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.properties.CliProperties.ExternalSoftware;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGatewayImpl;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import com.dtsx.astra.cli.testlib.extensions.context.UseTestCtx;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class DownloadsGatewayImplTest {
    @UseTestCtx(fs = "jimfs")
    private TestCliContext ctx;

    private DownloadsGatewayImpl gateway;

    @BeforeEach
    void setup() {
        this.gateway = new DownloadsGatewayImpl(ctx.get());
    }

    @Nested
    class downloadAstra {
        private static final String version = "1.0.1";

        @Test
        @SneakyThrows
        public void downloads_astra_tar() {
            val binPath = test_downloads_generic_astra_archive(ctx.get().properties().cliGithubRepoUrl() + "/releases/download/v" + version + "/" + ctx.get().properties().cliName() + "-" + "linux-arm64.tar.gz", "");

            assertThat(Files.walk(binPath, 1)).containsExactly(
                binPath,
                binPath.resolve("astra")
            );
        }

        @Test
        @SneakyThrows
        public void downloads_astra_zip() {
            val binPath = test_downloads_generic_astra_archive(ctx.get().properties().cliGithubRepoUrl() + "/releases/download/v" + version + "/" + ctx.get().properties().cliName() + "-" + "windows-x86_64.zip", ".exe");

            assertThat(Files.walk(binPath, 1)).containsExactly(
                binPath,
                binPath.resolve("astra.exe"),
                binPath.resolve("astra.ico"),
                binPath.resolve("uninstall.ps1")
            );
        }

        @SneakyThrows
        private Path test_downloads_generic_astra_archive(String downloadUrl, String exeExtension) {
            val maybeExePath = gateway.downloadAstra(new ExternalSoftware(downloadUrl, version));

            assertThat(maybeExePath).isInstanceOf(Either.Right.class);
            val exePath = maybeExePath.getRight();

            assertThat(exePath)
                .startsWith(ctx.get().path(System.getProperty("java.io.tmpdir")))
                .satisfies(p -> assertThat(p.toString()).contains("astra-cli-upgrade-"))
                .endsWith(ctx.get().path("astra").resolve("bin").resolve("astra" + exeExtension))
                .isExecutable();

            val rootPath = exePath.getParent().getParent();
            assertThat(Files.walk(rootPath, 1)).containsExactly(rootPath, rootPath.resolve("bin"));

            return exePath.getParent();
        }
    }

    @Nested
    class downloadExternalSoftware {
        @Test
        public void downloads_cqlsh() {
            test_downloads_generic_ext_software(ctx.get().properties().cqlsh(), "cqlsh-astra@v1", "cqlsh", gateway::cqlshPath, gateway::downloadCqlsh);
        }

        @Test
        public void downloads_dsbulk() {
            test_downloads_generic_ext_software(ctx.get().properties().dsbulk(), "dsbulk@v1", "dsbulk", gateway::dsbulkPath, gateway::downloadDsbulk);
        }

        @Test
        public void downloads_pulsar_shell() {
            test_downloads_generic_ext_software(ctx.get().properties().pulsar(), "pulsar-shell@v1", "pulsar-shell", gateway::pulsarShellPath, gateway::downloadPulsarShell);
        }

        @SneakyThrows
        private void test_downloads_generic_ext_software(ExternalSoftware ex, String dirName, String exeName, Function<ExternalSoftware, Optional<Path>> getPath, Function<ExternalSoftware, Either<String, Path>> download) {
            val expectBasePath = ctx.get().path(ctx.get().home().root()).resolve(dirName);
            val expectVersionPath = expectBasePath.resolve(ex.version());

            assertThat(getPath.apply(ex)).isEmpty();
            assertThat(expectBasePath).doesNotExist();

            val maybeExePath = download.apply(ex);

            assertThat(maybeExePath).isInstanceOf(Either.Right.class);
            val exePath = expectVersionPath.resolve("bin").resolve(exeName);

            assertThat(Files.walk(expectBasePath, 1)).containsExactly(expectBasePath, expectVersionPath);

            assertThat(maybeExePath.getRight()).isEqualTo(exePath);
            assertThat(getPath.apply(ex)).hasValue(exePath);
            assertThat(exePath).isExecutable();
        }
    }
}
