package com.dtsx.astra.cli.core.output;

import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.Setter;
import lombok.val;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Option;

import java.util.UUID;

public enum AstraColors implements Ansi.IStyle {
    PURPLE_300(175, 110, 195),
    PURPLE_500(110, 46, 164),

    YELLOW_300(223, 161, 67),
    YELLOW_500(162, 91, 39),

    GREEN_300(128, 189, 244),
    GREEN_500(61, 126, 64),

    BLUE_300(129, 163, 231),
    BLUE_500(46, 101, 211),

    RED_300(221, 127, 135),
    RED_500(199, 49, 44),

    MAGENTA_400(239, 134, 180),
    MAGENTA_600(191, 57, 111),

    CYAN_400(91, 176, 248),
    CYAN_600(48, 113, 189),

    ORANGE_400(239, 137, 67),
    ORANGE_600(173, 84, 31),

    NEUTRAL_300(167, 170, 173),
    NEUTRAL_500(108, 111, 115),

    TEAL_400(85, 186, 185),
    TEAL_600(53, 123, 120);

    private static final String DISABLE_STRING = CSI + "39m";

    public static final Help.ColorScheme DEFAULT_COLOR_SCHEME = new Help.ColorScheme.Builder(Help.defaultColorScheme(Ansi.AUTO))
        .options(AstraColors.BLUE_300)
        .parameters(AstraColors.BLUE_300)
        .build();

    @Setter
    private static Ansi ansi = Ansi.AUTO;

    public static Ansi ansi() {
        return ansi;
    }

    public static boolean enabled() {
        return ansi.enabled();
    }

    public static String reset() {
        return ansi().enabled() ? DISABLE_STRING : "";
    }

    public static String stripAnsi(String str) {
        return str.replaceAll("\\u001B\\[[;\\d]*m", "");
    }

    public static class Mixin {
        @Option(names = "--color", negatable = true, description = "Force colored output")
        public void setAnsi(boolean ansi) {
            AstraColors.ansi = (ansi) ? Ansi.ON : Ansi.OFF;
        }

        public ColorScheme getColorScheme() {
            val ansi = (AstraColors.ansi == null) ? Ansi.AUTO : AstraColors.ansi;

            return new Help.ColorScheme.Builder(DEFAULT_COLOR_SCHEME)
                .ansi(ansi)
                .build();
        }
    }

    private final String enableString;

    AstraColors(int red, int green, int blue) {
        this.enableString = CSI + "38;2;" + red + ";" + green + ";" + blue + "m";
    }

    @Override
    public String on() {
        return enabled() ? enableString : "";
    }

    @Override
    public String off() {
        return reset();
    }

    public String use(String string) {
        return enabled() ? (enableString + string + DISABLE_STRING) : string;
    }

    public String useOrQuote(String string) {
        return enabled() ? (enableString + string + DISABLE_STRING) : "'" + string + "'";
    }

    public static String highlight(String s) {
        return AstraColors.BLUE_300.useOrQuote(s);
    }

    public static String highlight(UUID u) {
        return highlight(u.toString());
    }

    public static String highlight(long l) {
        return enabled() ? AstraColors.BLUE_300.use(String.valueOf(l)) : String.valueOf(l);
    }

    public static String highlight(Highlightable h) {
        return h.highlight();
    }

    public static String highlight(DatabaseStatusType status) {
        if (!enabled()) {
            return "'" + status.name() + "'";
        }

        val color = switch (status) {
            case ACTIVE -> AstraColors.GREEN_500;
            case ERROR, TERMINATED, UNKNOWN -> AstraColors.RED_500;
            case DECOMMISSIONING, TERMINATING, DEGRADED -> AstraColors.YELLOW_500;
            case HIBERNATED, PARKED, PREPARED -> AstraColors.BLUE_500;
            case INITIALIZING, PENDING, HIBERNATING, PARKING, MAINTENANCE, PREPARING, RESIZING, RESUMING, UNPARKING -> AstraColors.YELLOW_300;
            default -> AstraColors.NEUTRAL_500;
        };

        return color.use(status.name());
    }

    public interface Highlightable {
        String highlight();
    }
}
