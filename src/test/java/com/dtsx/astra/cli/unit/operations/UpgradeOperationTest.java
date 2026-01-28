package com.dtsx.astra.cli.unit.operations;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.Version;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.properties.CliProperties;
import com.dtsx.astra.cli.core.properties.CliProperties.AstraBinary;
import com.dtsx.astra.cli.core.properties.CliProperties.SupportedPackageManager;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.gateways.upgrade.UpgradeGateway;
import com.dtsx.astra.cli.operations.UpgradeOperation;
import com.dtsx.astra.cli.operations.UpgradeOperation.LatestVersion;
import com.dtsx.astra.cli.operations.UpgradeOperation.SpecificVersion;
import com.dtsx.astra.cli.operations.UpgradeOperation.UpgradeRequest;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import com.dtsx.astra.cli.testlib.extensions.context.UseTestCtx;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UpgradeOperationTest {
    @UseTestCtx(fs = "jimfs")
    private TestCliContext testCtx;

    @Test
    public void throws_when_trying_to_upgrade_to_same_version() throws Exception {
        val ctx = mkCtx("1.5.0", null);
        val upgradeGateway = mock(UpgradeGateway.class);
        val downloadsGateway = mock(DownloadsGateway.class);

        val request = new UpgradeRequest(
            new SpecificVersion(Version.mkUnsafe("1.5.0")),
            false,
            false,
            List.of(),
            (_, _) -> {}
        );

        val operation = new UpgradeOperation(ctx, downloadsGateway, upgradeGateway, request);

        assertThatThrownBy(operation::execute)
            .isInstanceOf(AstraCliException.class)
            .hasMessageContaining("already using")
            .extracting(e -> ((AstraCliException) e).code())
            .isEqualTo(ExitCode.RELEASE_NOT_FOUND);
    }

    @Test
    public void throws_when_trying_to_downgrade_to_pre_1_0_version() throws Exception {
        val ctx = mkCtx("1.5.0", null);
        val upgradeGateway = mock(UpgradeGateway.class);
        val downloadsGateway = mock(DownloadsGateway.class);

        val request = new UpgradeRequest(
            new SpecificVersion(Version.mkUnsafe("0.9.5")),
            false,
            false,
            List.of(),
            (_, _) -> {}
        );

        val operation = new UpgradeOperation(ctx, downloadsGateway, upgradeGateway, request);

        assertThatThrownBy(operation::execute)
            .isInstanceOf(AstraCliException.class)
            .hasMessageContaining("Can not downgrade to a version prior to 1.0.0")
            .extracting(e -> ((AstraCliException) e).code())
            .isEqualTo(ExitCode.RELEASE_NOT_FOUND);
    }

    @Test
    public void throws_when_latest_version_is_not_newer() throws Exception {
        val ctx = mkCtx("1.5.0", null);
        val upgradeGateway = mock(UpgradeGateway.class);
        val downloadsGateway = mock(DownloadsGateway.class);

        when(upgradeGateway.latestVersion(false)).thenReturn(Version.mkUnsafe("1.4.0"));

        val request = new UpgradeRequest(
            new LatestVersion(false),
            false,
            false,
            List.of(),
            (_, _) -> {}
        );

        val operation = new UpgradeOperation(ctx, downloadsGateway, upgradeGateway, request);

        assertThatThrownBy(operation::execute)
            .isInstanceOf(AstraCliException.class)
            .hasMessageContaining("No newer version available")
            .extracting(e -> ((AstraCliException) e).code())
            .isEqualTo(ExitCode.RELEASE_NOT_FOUND);
    }

    @Test
    public void throws_when_latest_equals_current() throws Exception {
        val ctx = mkCtx("1.5.0", null);
        val upgradeGateway = mock(UpgradeGateway.class);
        val downloadsGateway = mock(DownloadsGateway.class);

        when(upgradeGateway.latestVersion(false)).thenReturn(Version.mkUnsafe("1.5.0"));

        val request = new UpgradeRequest(
            new LatestVersion(false),
            false,
            false,
            List.of(),
            (_, _) -> {}
        );

        val operation = new UpgradeOperation(ctx, downloadsGateway, upgradeGateway, request);

        assertThatThrownBy(operation::execute)
            .isInstanceOf(AstraCliException.class)
            .hasMessageContaining("You are already using the latest Astra CLI version");
    }

    @Test
    public void throws_when_managed_by_package_manager() throws Exception {
        val ctx = mkCtx("1.5.0", SupportedPackageManager.BREW);
        val upgradeGateway = mock(UpgradeGateway.class);
        val downloadsGateway = mock(DownloadsGateway.class);

        val request = new UpgradeRequest(
            new SpecificVersion(Version.mkUnsafe("1.6.0")),
            false,
            false,
            List.of(),
            (_, _) -> {}
        );

        val operation = new UpgradeOperation(ctx, downloadsGateway, upgradeGateway, request);

        assertThatThrownBy(operation::execute)
            .isInstanceOf(AstraCliException.class)
            .hasMessageContaining("managed by a package manager")
            .extracting(e -> ((AstraCliException) e).code())
            .isEqualTo(ExitCode.UNSUPPORTED_EXECUTION);
    }

    @SuppressWarnings("SameParameterValue")
    private CliContext mkCtx(String version, SupportedPackageManager pm) throws Exception {
        val binaryPath = testCtx.get().path("/usr/local/bin/astra");
        Files.createDirectories(binaryPath.getParent());
        Files.createFile(binaryPath);

        val originalCtx = testCtx.get();
        val mockProperties = mock(CliProperties.class);

        when(mockProperties.version()).thenReturn(Version.mkUnsafe(version));
        when(mockProperties.cliPath(any())).thenReturn(new AstraBinary(binaryPath));
        when(mockProperties.owningPackageManager()).thenReturn(Optional.ofNullable(pm));

        return originalCtx.withProperties(mockProperties);
    }
}
