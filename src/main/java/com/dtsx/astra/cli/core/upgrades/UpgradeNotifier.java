package com.dtsx.astra.cli.core.upgrades;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.output.AstraLogger.Level;
import com.dtsx.astra.cli.core.output.BoxDrawer;
import com.dtsx.astra.cli.core.output.BoxDrawer.Alignment;
import lombok.val;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;

public class UpgradeNotifier {
    private static final int PADDING = 3;

    public static void run(CliContext ctx) {
        if (ctx.isWindows()) {
            return;
        }

        if (ctx.properties().noUpgradeNotifications()) {
            return;
        }

        val path = ctx.home().updateNotifierProperties.use();

        // every 10 minutes for pre-releases, every 48 hours for actual releases
        val INTERVAL_MS = (ctx.properties().version().isPreRelease())
            ? 1000 * 60 * 10
            : 24 * 60 * 60 * 1000 * 2;

        UpgradeStatus.load(ctx, path).ifPresent((status) -> {
            val shouldAnnoyUser = updateAvailable(status, ctx) && haventAnnoyedUserInAWhile(status, INTERVAL_MS) && isAppropriateEnvToAnnoyUser(ctx);

            if (shouldAnnoyUser) {
                annoyUser(ctx, status);
            }

            UpgradeStatusKeeper.runIfNecessary(ctx, path, status, timeToCheckForUpdate(status, INTERVAL_MS), shouldAnnoyUser);
        });
    }

    private static void annoyUser(CliContext ctx, UpgradeStatus status) {
        ctx.console().error(buildAnnoyingText(ctx, status));
    }

    @VisibleForTesting
    public static String buildAnnoyingText(CliContext ctx, UpgradeStatus status) {
        val currentVersion = ctx.properties().version().toString();
        val latestVersion = status.latestVersion().orElseThrow().toString();

        val versionMsg = "Update available! " + ctx.colors().NEUTRAL_400.use(currentVersion) + " -> " + ctx.colors().YELLOW_300.use(latestVersion);
        val commandMsg = ctx.colors().format(mkUpgradeCommandMsg(ctx));

        return BoxDrawer.drawBox(PADDING, ctx.colors().BLUE_300, List.of(versionMsg, commandMsg), Alignment.CENTER);
    }

    private static String mkUpgradeCommandMsg(CliContext ctx) {
        val pm = ctx.properties().owningPackageManager();

        if (pm.isEmpty()) {
            return "Run @!astra upgrade!@ to update";
        }

        return switch (pm.get()) {
            case BREW -> "Run @!brew upgrade astra!@ to update";
            case NIX -> "Upgrade @!astra!@ through @!Nix!@";
        };
    }

    private static boolean updateAvailable(UpgradeStatus status, CliContext ctx) {
        return status.latestVersion()
            .map(v -> ctx.properties().version().compareTo(v) < 0)
            .orElse(false);
    }

    private static boolean haventAnnoyedUserInAWhile(UpgradeStatus status, long interval) {
        return (status.currentTime().toEpochMilli() - status.lastNotified().toEpochMilli()) > interval;
    }

    private static boolean isAppropriateEnvToAnnoyUser(CliContext ctx) {
        return ctx.isTty() && !ctx.log().level().equals(Level.QUIET);
    }

    private static boolean timeToCheckForUpdate(UpgradeStatus status, long interval) {
        return (status.currentTime().toEpochMilli() - status.lastChecked().toEpochMilli()) > interval;
    }
}
