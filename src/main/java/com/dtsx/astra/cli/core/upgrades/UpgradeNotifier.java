package com.dtsx.astra.cli.core.upgrades;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.output.AstraColors.AstraColor;
import com.dtsx.astra.cli.core.output.AstraLogger.Level;
import lombok.val;
import org.jetbrains.annotations.VisibleForTesting;

import static com.dtsx.astra.cli.core.output.AstraColors.stripAnsi;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

public class UpgradeNotifier {
    public static final int PADDING = 3;

    public static void run(CliContext ctx) {
        if (ctx.isWindows()) {
            return;
        }

        if (ctx.properties().noUpgradeNotifications()) {
            return;
        }

        val path = ctx.home().useDir().resolve("upgrade-notifier.properties");

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
        val commandMsg = "Run " + ctx.colors().BLUE_300.use("astra upgrade") + " to update";

        val versionMsgLength = stripAnsi(versionMsg).length();
        val commandMsgLength = stripAnsi(commandMsg).length();

        val maxTextWidth = Math.max(versionMsgLength, commandMsgLength);
        val boxWidth = Math.max(versionMsgLength, commandMsgLength) + PADDING * 2;

        val main = new StringBuilder();
        val blue = ctx.colors().BLUE_300;

        appendFillerLine(main, boxWidth, blue, '┌', '─', '┐');
        appendFillerLine(main, boxWidth, blue, '│', ' ', '│');
        appendTextualLine(main, versionMsg, maxTextWidth, versionMsgLength, blue);
        appendTextualLine(main, commandMsg, maxTextWidth, commandMsgLength, blue);
        appendFillerLine(main, boxWidth, blue, '│', ' ', '│');
        appendFillerLine(main, boxWidth, blue, '└', '─', '┘');

        return main.toString();
    }

    private static void appendFillerLine(StringBuilder main, int boxWidth, AstraColor blue, char l, char m, char r) {
        main.append(blue.on()).append(l).repeat(m, boxWidth).append(r).append(blue.off()).append(NL);
    }

    private static void appendTextualLine(StringBuilder main, String text, int maxTextWidth, int actualLength, AstraColor blue) {
        main.append(blue.on()).append("│").append(blue.off())
            .repeat(' ', PADDING + Math.floorDiv(maxTextWidth - actualLength, 2))
            .append(text)
            .repeat(' ', PADDING + Math.ceilDiv(maxTextWidth - actualLength, 2))
            .append(blue.on()).append("│").append(blue.off())
            .append(NL);
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
