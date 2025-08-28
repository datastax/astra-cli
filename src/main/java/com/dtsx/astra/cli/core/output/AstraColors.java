package com.dtsx.astra.cli.core.output;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.Ansi.IStyle;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Option;

import java.util.Optional;

@Accessors(fluent = true)
@RequiredArgsConstructor
public class AstraColors {
    private static final String CSI = "\u001B[";
    private static final String DISABLE_STRING = CSI + "39m";

    @Getter
    private final Ansi ansi;

    @RequiredArgsConstructor
    public class AstraColor implements IStyle {
        private final int red;
        private final int green;
        private final int blue;

        @Override
        public String on() {
            return ansi.enabled() ? (CSI + "38;2;" + (red & 255) + ";" + (green & 255) + ";" + (blue & 255) + "m") : "";
        }

        @Override
        public String off() {
            return ansi.enabled() ? DISABLE_STRING : "";
        }

        public String use(String string) {
            return on() + string + off();
        }

        public String useOrQuote(String string) {
            return ansi.enabled() ? (on() + string + off()) : "'" + string + "'";
        }
    }

    public final AstraColor PURPLE_300 = new AstraColor(175, 110, 195);
    public final AstraColor PURPLE_500 = new AstraColor(110, 46, 164);

    public final AstraColor YELLOW_300 = new AstraColor(223, 161, 67);
    public final AstraColor YELLOW_500 = new AstraColor(162, 91, 39);

    public final AstraColor GREEN_300 = new AstraColor(128, 189, 244);
    public final AstraColor GREEN_500 = new AstraColor(61, 126, 64);

    public final AstraColor BLUE_300 = new AstraColor(129, 163, 231);
    public final AstraColor BLUE_500 = new AstraColor(46, 101, 211);

    public final AstraColor RED_300 = new AstraColor(221, 127, 135);
    public final AstraColor RED_500 = new AstraColor(199, 49, 44);

    public final AstraColor MAGENTA_400 = new AstraColor(239, 134, 180);
    public final AstraColor MAGENTA_600 = new AstraColor(191, 57, 111);

    public final AstraColor CYAN_400 = new AstraColor(91, 176, 248);
    public final AstraColor CYAN_600 = new AstraColor(48, 113, 189);

    public final AstraColor ORANGE_400 = new AstraColor(239, 137, 67);
    public final AstraColor ORANGE_600 = new AstraColor(173, 84, 31);

    public final AstraColor NEUTRAL_300 = new AstraColor(167, 170, 173);
    public final AstraColor NEUTRAL_400 = new AstraColor(138, 141, 144);
    public final AstraColor NEUTRAL_500 = new AstraColor(108, 111, 115);

    public final AstraColor TEAL_400 = new AstraColor(85, 186, 185);
    public final AstraColor TEAL_600 = new AstraColor(53, 123, 120);

    public static final ColorScheme DEFAULT_COLOR_SCHEME;

    static {
        val colors = new AstraColors(Ansi.AUTO);

        DEFAULT_COLOR_SCHEME = new ColorScheme.Builder(Help.defaultColorScheme(Ansi.AUTO))
            .options(colors.BLUE_300)
            .parameters(colors.BLUE_300)
            .build();
    }

    public String reset() {
        return ansi.enabled() ? DISABLE_STRING : "";
    }

    public static String stripAnsi(String str) {
        return str.replaceAll("\\u001B\\[[;\\d]*m", "");
    }

    @Accessors(fluent = true)
    public static class Mixin {
        @Getter
        private Optional<Ansi> ansi = Optional.empty();

        @Option(names = "--color", negatable = true, description = "Force colored output")
        private void setAnsi(boolean ansi) {
            if (ansi) {
                this.ansi = Optional.of(Ansi.ON);
            } else {
                this.ansi = Optional.of(Ansi.OFF);
            }
        }
    }
}
