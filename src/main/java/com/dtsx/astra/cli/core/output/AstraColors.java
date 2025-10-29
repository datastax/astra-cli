package com.dtsx.astra.cli.core.output;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.Ansi.IStyle;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Help.ColorScheme.Builder;

import java.util.HashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

@Accessors(fluent = true)
@RequiredArgsConstructor
public class AstraColors {
    private static final String CSI = "\u001B[";
    private static final String DISABLE_STRING = CSI + "39m";

    @Getter
    private final Ansi ansi;

    private final Function<Builder, Builder> colorSchemeCustomizer;

    public AstraColors(Ansi ansi) {
        this(ansi, Function.identity());
    }

    @RequiredArgsConstructor
    public class AstraColor implements IStyle {
        private final int colorCode256;

        public AstraColor(int r, int g, int b) {
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

    public final AstraColor GREEN_300 = new AstraColor(31, 201, 28);
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
        val colors = new AstraColors(Ansi.ON);

        // TODO add AstraColors to this
        val markupMap = new HashMap<String, IStyle>();

        for (val style : Style.values()) {
            markupMap.put(style.name(), style);

            if (style.name().startsWith("fg_")) {
                markupMap.put(style.name().replace("fg_", ""), style);
            }
        }

        // overridden in `docgen` to render backticks
        markupMap.put("code", Style.italic);

        DEFAULT_COLOR_SCHEME = new ColorScheme.Builder(Help.defaultColorScheme(Ansi.AUTO))
            .options(colors.BLUE_300)
            .parameters(colors.BLUE_300)
            .customMarkupMap(markupMap)
            .build();
    }

    private @Nullable ColorScheme cachedColorScheme = null;

    private static final Pattern HIGHLIGHT_PATTERN = Pattern.compile("@!(.*?)!@");
    private static final Pattern HIGHLIGHT_OR_QUOTE_PATTERN = Pattern.compile("@'!(.*?)!@");

    public ColorScheme colorScheme() {
        if (cachedColorScheme != null) {
            return cachedColorScheme;
        }

        return cachedColorScheme = colorSchemeCustomizer.apply(
            new Help.ColorScheme.Builder(AstraColors.DEFAULT_COLOR_SCHEME)
                .ansi(ansi)
        ).build();
    }

    public String format(Object... args) {
        val sb = new StringBuilder();
        var colorUsed = false;

        for (val item : args) {
            if (item instanceof AstraColor color) {
                sb.append(color.on());
                colorUsed = true;
            } else if (item instanceof String str) {
                var processedStr = str.replace("${cli.name}", System.getProperty("cli.name"));

                processedStr = HIGHLIGHT_PATTERN.matcher(processedStr)
                    .replaceAll((match) -> highlight(match.group(1), false));

                processedStr = HIGHLIGHT_OR_QUOTE_PATTERN.matcher(processedStr)
                    .replaceAll((match) -> highlight(match.group(1), true));

                sb.append(ansi.new Text(processedStr, colorScheme()));
            } else {
                sb.append(item);
            }
        }

        if (colorUsed) {
            sb.append(reset());
        }

        return sb.toString();
    }

    public String highlight(String s, boolean orQuote) {
        return (orQuote) ? BLUE_300.useOrQuote(s) : BLUE_300.use(s);
    }

    public String reset() {
        return ansi.enabled() ? DISABLE_STRING : "";
    }

    public static String stripAnsi(String str) {
        return str.replaceAll("\\u001B\\[[;\\d]*m", "");
    }
}
