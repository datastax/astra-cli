package com.dtsx.astra.cli.core.upgrades;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.Version;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.Properties;

public record UpgradeStatus(
    Optional<Version> latestVersion,
    Instant lastChecked,
    Instant lastNotified,
    Instant currentTime
) {
    public static final String LATEST_VERSION_KEY = "LATEST_VERSION";
    public static final String LAST_CHECKED_KEY = "LAST_CHECKED";
    public static final String LAST_NOTIFIED_KEY = "LAST_NOTIFIED";

    public static Optional<UpgradeStatus> load(CliContext ctx, Path path) {
        val properties = new Properties();

        return (Files.exists(path))
            ? loadFromExistingPath(properties, ctx, path)
            : createNewStatusFile(properties, ctx, path);
    }

    private static Optional<UpgradeStatus> loadFromExistingPath(Properties properties, CliContext ctx, Path path) {
        try (val is = Files.newInputStream(path)) {
            properties.load(is);

            val rawLatestVersion = properties.getProperty(LATEST_VERSION_KEY);
            val rawLastChecked = properties.getProperty(LAST_CHECKED_KEY);
            val rawLastNotified = properties.getProperty(LAST_NOTIFIED_KEY);

            if (rawLatestVersion == null || rawLastChecked == null || rawLastNotified == null) {
                ctx.log().exception("Upgrade notifier properties file is missing required keys. Recreating it.");
                return createNewStatusFile(properties, ctx, path);
            }

            val version = Optional.of(rawLatestVersion).filter(s -> !s.isEmpty()).map(Version::parse);
            val lastChecked = Either.tryCatch(() -> Long.parseLong(rawLastChecked), Exception::getMessage);
            val lastNotified = Either.tryCatch(() -> Long.parseLong(rawLastNotified), Exception::getMessage);

            if (version.isPresent() && version.get().isLeft()) {
                ctx.log().exception("Upgrade notifier properties file has invalid latest version value '" + rawLatestVersion + "'. Recreating it.");
                return createNewStatusFile(properties, ctx, path);
            }

            if (lastChecked.isLeft()) {
                ctx.log().exception("Upgrade notifier properties file has invalid last checked value '" + rawLastChecked + "'. Recreating it.");
                return createNewStatusFile(properties, ctx, path);
            }

            if (lastNotified.isLeft()) {
                ctx.log().exception("Upgrade notifier properties file has invalid last notified value '" + rawLastNotified + "'. Recreating it.");
                return createNewStatusFile(properties, ctx, path);
            }

            return Optional.of(new UpgradeStatus(
                version.map(Either::getRight),
                Instant.ofEpochMilli(lastChecked.getRight()),
                Instant.ofEpochMilli(lastNotified.getRight()),
                Instant.now()
            ));
        } catch (IOException e) {
            ctx.log().exception("Failed to read upgrade notifier properties file");
            ctx.log().exception(e);
            return Optional.empty();
        }
    }

    private static Optional<UpgradeStatus> createNewStatusFile(Properties properties, CliContext ctx, Path path) {
        try {
            Files.deleteIfExists(path);
            Files.createDirectories(path.getParent());

            properties.setProperty(LATEST_VERSION_KEY, "");
            properties.setProperty(LAST_CHECKED_KEY, String.valueOf(Instant.EPOCH.toEpochMilli()));
            properties.setProperty(LAST_NOTIFIED_KEY, String.valueOf(Instant.EPOCH.toEpochMilli()));

            try (val os = Files.newOutputStream(path)) {
                properties.store(os, null);
            }

            return Optional.of(new UpgradeStatus(
                Optional.empty(),
                Instant.EPOCH,
                Instant.EPOCH,
                Instant.now()
            ));
        } catch (IOException e) {
            ctx.log().exception("Failed to create upgrade notifier properties file");
            ctx.log().exception(e);

            try {
                Files.deleteIfExists(path);
            } catch (IOException _) {}

            return Optional.empty();
        }
    }
}
