package com.dtsx.astra.cli.unit.core.upgrades;

import com.dtsx.astra.cli.core.models.Version;
import com.dtsx.astra.cli.core.upgrades.UpgradeStatus;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import com.dtsx.astra.cli.testlib.extensions.context.UseTestCtx;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class UpgradeStatusTest {
    @UseTestCtx(fs = "jimfs")
    private TestCliContext ctx;

    @Test
    public void load_creates_new_file_when_none_exists() {
        var path = ctx.get().path("/test/upgrade-notifier.properties");

        var status = UpgradeStatus.load(ctx.get(), path);

        assertThat(status).isPresent();
        assertThat(status.get().latestVersion()).isEmpty();
        assertThat(status.get().lastChecked()).isEqualTo(Instant.EPOCH);
        assertThat(status.get().lastNotified()).isEqualTo(Instant.EPOCH);
        assertThat(Files.exists(path)).isTrue();
    }

    @Test
    @SneakyThrows
    public void load_reads_existing_file_with_valid_data() {
        var path = ctx.get().path("/test/upgrade-notifier.properties");

        Files.createDirectories(path.getParent());

        Files.writeString(path, """
            LATEST_VERSION=1.2.3
            LAST_CHECKED=1000000000000
            LAST_NOTIFIED=2000000000000
        """);

        var status = UpgradeStatus.load(ctx.get(), path);

        assertThat(status).isPresent();
        assertThat(status.get().latestVersion())
            .isPresent()
            .get()
            .isEqualTo(Version.mkUnsafe("1.2.3"));
        assertThat(status.get().lastChecked()).isEqualTo(Instant.ofEpochMilli(1000000000000L));
        assertThat(status.get().lastNotified()).isEqualTo(Instant.ofEpochMilli(2000000000000L));
    }

    @Test
    @SneakyThrows
    public void load_handles_empty_version_string() {
        var path = ctx.get().path("/test/upgrade-notifier.properties");

        Files.createDirectories(path.getParent());

        Files.writeString(path, """
            LATEST_VERSION=
            LAST_CHECKED=1000000000000
            LAST_NOTIFIED=2000000000000
        """);

        var status = UpgradeStatus.load(ctx.get(), path);

        assertThat(status).isPresent();
        assertThat(status.get().latestVersion()).isEmpty();
    }

    @Test
    @SneakyThrows
    public void load_recreates_file_when_missing_required_keys() {
        var path = ctx.get().path("/test/upgrade-notifier.properties");

        Files.createDirectories(path.getParent());

        // Missing LAST_NOTIFIED
        Files.writeString(path, """
            LATEST_VERSION=1.2.3
            LAST_CHECKED=1000000000000
        """);

        var status = UpgradeStatus.load(ctx.get(), path);

        assertThat(status).isPresent();
        // Should have recreated with defaults
        assertThat(status.get().latestVersion()).isEmpty();
        assertThat(status.get().lastChecked()).isEqualTo(Instant.EPOCH);
        assertThat(status.get().lastNotified()).isEqualTo(Instant.EPOCH);
    }

    @Test
    @SneakyThrows
    public void load_recreates_file_when_invalid_version() {
        var path = ctx.get().path("/test/upgrade-notifier.properties");

        Files.createDirectories(path.getParent());

        Files.writeString(path, """
            LATEST_VERSION=not-a-version
            LAST_CHECKED=1000000000000
            LAST_NOTIFIED=2000000000000
        """);

        var status = UpgradeStatus.load(ctx.get(), path);

        assertThat(status).isPresent();
        assertThat(status.get().latestVersion()).isEmpty();
        assertThat(status.get().lastChecked()).isEqualTo(Instant.EPOCH);
    }

    @Test
    @SneakyThrows
    public void load_recreates_file_when_invalid_timestamp() {
        var path = ctx.get().path("/test/upgrade-notifier.properties");

        Files.createDirectories(path.getParent());

        Files.writeString(path, """
            LATEST_VERSION=1.2.3
            LAST_CHECKED=not-a-number
            LAST_NOTIFIED=2000000000000
        """);

        var status = UpgradeStatus.load(ctx.get(), path);

        assertThat(status).isPresent();
        assertThat(status.get().latestVersion()).isEmpty();
        assertThat(status.get().lastChecked()).isEqualTo(Instant.EPOCH);
    }

    @Test
    @SneakyThrows
    public void load_handles_prerelease_versions() {
        var path = ctx.get().path("/test/upgrade-notifier.properties");

        Files.createDirectories(path.getParent());

        Files.writeString(path, """
            LATEST_VERSION=1.2.3-rc.1
            LAST_CHECKED=1000000000000
            LAST_NOTIFIED=2000000000000
        """);

        var status = UpgradeStatus.load(ctx.get(), path);

        assertThat(status).isPresent();
        assertThat(status.get().latestVersion())
            .isPresent()
            .get()
            .isEqualTo(Version.mkUnsafe("1.2.3-rc.1"));
    }
}
