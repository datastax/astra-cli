package com.dtsx.astra.cli.core.output;

import com.dtsx.astra.cli.core.output.AstraColors.AstraColor;
import lombok.val;

import java.util.List;

import static com.dtsx.astra.cli.core.output.AstraColors.stripAnsi;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

public class BoxDrawer {
    public enum Alignment {
        LEFT, CENTER
    }

    public static String drawBox(int padding, AstraColor borderColor, List<String> messages, Alignment alignment) {
        var maxTextWidth = 0;
        for (val msg : messages) {
            maxTextWidth = Math.max(maxTextWidth, stripAnsi(msg).length());
        }

        val boxWidth = maxTextWidth + padding * 2 + 2;
        val main = new StringBuilder();

        appendFillerLine(main, boxWidth, borderColor, '┌', '─', '┐');
        appendFillerLine(main, boxWidth, borderColor, '│', ' ', '│');
        for (val msg : messages) {
            appendTextualLine(main, padding, msg, maxTextWidth, stripAnsi(msg).length(), borderColor, alignment);
        }
        appendFillerLine(main, boxWidth, borderColor, '│', ' ', '│');
        appendFillerLine(main, boxWidth, borderColor, '└', '─', '┘');

        return main.toString();
    }

    private static void appendFillerLine(StringBuilder main, int boxWidth, AstraColor color, char l, char m, char r) {
        main.append(color.on()).append(l).repeat(m, boxWidth - 2).append(r).append(color.off()).append(NL);
    }

    private static void appendTextualLine(StringBuilder main, int padding, String text, int maxTextWidth, int actualLength, AstraColor color, Alignment alignment) {
        val leftPadding = (alignment == Alignment.CENTER)
            ? padding + Math.floorDiv(maxTextWidth - actualLength, 2) 
            : padding;

        val contentWidth = maxTextWidth + 2 * padding;
        val rightPadding = contentWidth - actualLength - leftPadding;

        main.append(color.on()).append("│").append(color.off())
            .repeat(' ', leftPadding)
            .append(text)
            .repeat(' ', rightPadding)
            .append(color.on()).append("│").append(color.off())
            .append(NL);
    }
}
