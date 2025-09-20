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
        private final int colorCode256;

        public AstraColor(int r, int g, int b) {
            this.red = r;
            this.green = g;
            this.blue = b;
            this.colorCode256 = rgbToAnsi256(r, g, b);
        }

        @Override
        public String on() {
            return ansi.enabled() ? (CSI + "38;5;" + colorCode256 + "m") : "";
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

        // NOTICE: THE FOLLOWING HAS BEEN TEMPORARILY AI GENERATED
        // TO ATTEMPT TO MAKE THE COLORING WORK PROPERLY ON MAC BY MANUALLY CONVERTING
        // RGB TO ANSI 256 COLOR CODES.
        //
        // THIS WILL BE REPLACED LATER WITH A MORE ROBUST SOLUTION.
        // (LIKELY HARDCODING THE 256 COLOR CODES ONCE THEY'VE BEEN VERIFIED)
        private static final int[][] ANSI_256_RGB_TABLE = new int[256][3];

        static {
            int idx = 0;

            int[] baseColors = {
                0x000000, 0x800000, 0x008000, 0x808000,
                0x000080, 0x800080, 0x008080, 0xc0c0c0,
                0x808080, 0xff0000, 0x00ff00, 0xffff00,
                0x0000ff, 0xff00ff, 0x00ffff, 0xffffff
            };

            for (int color : baseColors) {
                ANSI_256_RGB_TABLE[idx][0] = (color >> 16) & 0xFF;
                ANSI_256_RGB_TABLE[idx][1] = (color >> 8) & 0xFF;
                ANSI_256_RGB_TABLE[idx][2] = color & 0xFF;
                idx++;
            }

            int[] steps = { 0, 95, 135, 175, 215, 255 };
            for (int r : steps)
                for (int g : steps)
                    for (int b : steps) {
                        ANSI_256_RGB_TABLE[idx][0] = r;
                        ANSI_256_RGB_TABLE[idx][1] = g;
                        ANSI_256_RGB_TABLE[idx][2] = b;
                        idx++;
                    }

            for (int i = 0; i < 24; i++) {
                int gray = 8 + i * 10;
                ANSI_256_RGB_TABLE[idx][0] = gray;
                ANSI_256_RGB_TABLE[idx][1] = gray;
                ANSI_256_RGB_TABLE[idx][2] = gray;
                idx++;
            }
        }

        private static int rgbToAnsi256(int r, int g, int b) {
            int bestIndex = 0;
            double bestDistance = Double.MAX_VALUE;

            for (int i = 0; i < 256; i++) {
                int[] rgb = ANSI_256_RGB_TABLE[i];
                double distance = colorDistance(r, g, b, rgb[0], rgb[1], rgb[2]);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestIndex = i;
                }
            }

            return bestIndex;
        }

        private static double colorDistance(int r1, int g1, int b1, int r2, int g2, int b2) {
            int dr = r1 - r2;
            int dg = g1 - g2;
            int db = b1 - b2;
            return dr * dr + dg * dg + db * db;
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
