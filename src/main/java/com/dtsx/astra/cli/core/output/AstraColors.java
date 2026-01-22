package com.dtsx.astra.cli.core.output;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
    }

    public final AstraColor PURPLE_300 = new AstraColor(133);
    public final AstraColor PURPLE_500 = new AstraColor(55);

    public final AstraColor YELLOW_300 = new AstraColor(179);
    public final AstraColor YELLOW_500 = new AstraColor(130);

    public final AstraColor GREEN_300 = new AstraColor(40);
    public final AstraColor GREEN_500 = new AstraColor(65);

    public final AstraColor BLUE_300 = new AstraColor(110);
    public final AstraColor BLUE_500 = new AstraColor(26);

    public final AstraColor RED_300 = new AstraColor(174);
    public final AstraColor RED_500 = new AstraColor(160);

    public final AstraColor MAGENTA_400 = new AstraColor(211);
    public final AstraColor MAGENTA_600 = new AstraColor(131);

    public final AstraColor CYAN_400 = new AstraColor(75);
    public final AstraColor CYAN_600 = new AstraColor(61);

    public final AstraColor ORANGE_400 = new AstraColor(209);
    public final AstraColor ORANGE_600 = new AstraColor(130);

    public final AstraColor NEUTRAL_300 = new AstraColor(248);
    public final AstraColor NEUTRAL_400 = new AstraColor(245);
    public final AstraColor NEUTRAL_500 = new AstraColor(242);

    public final AstraColor TEAL_400 = new AstraColor(73);
    public final AstraColor TEAL_600 = new AstraColor(66);

    public static final ColorScheme DEFAULT_COLOR_SCHEME;

    static {
        val colors = new AstraColors(Ansi.ON);

        val markupMap = new HashMap<String, IStyle>();

        for (val style : Style.values()) {
            markupMap.put(style.name(), style);

            if (style.name().startsWith("fg_")) {
                markupMap.put(style.name().replace("fg_", ""), style);
            }
        }

        // overridden in `docgen` to render backticks
        markupMap.put("code", Style.italic);

        for (val fields : AstraColors.class.getDeclaredFields()) {
            if (fields.getType().equals(AstraColor.class)) {
                try {
                    val color = (AstraColor) fields.get(colors);
                    markupMap.put(fields.getName().toLowerCase().replace('_', ':'), color);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

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

    public String format(String... args) {
        val sb = new StringBuilder();

        for (val str : args) {
             var processedStr = str
                 .replace("${cli.name}", System.getProperty("cli.name"))
                 .replace("${cli.path}", System.getProperty("cli.path", "/path/to/astra"));

             processedStr = HIGHLIGHT_PATTERN.matcher(processedStr)
                 .replaceAll((match) -> highlight(match.group(1), false));

             processedStr = HIGHLIGHT_OR_QUOTE_PATTERN.matcher(processedStr)
                 .replaceAll((match) -> highlight(match.group(1), true));

             sb.append(ansi.new Text(processedStr, colorScheme()));
        }

        return sb.toString();
    }

    public String highlight(String s, boolean orQuote) {
        return (orQuote) ? BLUE_300.useOrQuote(s) : BLUE_300.use(s);
    }

    public static String stripAnsi(String str) {
        return str.replaceAll("\\u001B\\[[;\\d]*m", "");
    }
}
