package com.dtsx.astra.cli.utils;

import com.dtsx.astra.cli.core.output.AstraColors;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.UUID;

@UtilityClass
public class StringUtils {
    public static final String NL = System.lineSeparator();

    public static String removeQuotesIfAny(String str) {
        if (str != null && str.length() >= 2 && ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'")))) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    public static boolean isUUID(String str) {
        try {
            UUID.fromString(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String trimIndent(String input) {
        return withIndent(input, 0);
    }

    public static String withIndent(String input, int targetIndent) {
        val lines = input.split("\n", -1);

        var start = 0;
        while (start < lines.length && stripAnsiCodes(lines[start]).trim().isEmpty()) {
            start++;
        }

        var end = lines.length;
        while (end > start && stripAnsiCodes(lines[end - 1]).trim().isEmpty()) {
            end--;
        }

        val trimmedLines = new String[end - start];
        System.arraycopy(lines, start, trimmedLines, 0, end - start);

        var minIndent = Integer.MAX_VALUE;

        for (val line : trimmedLines) {
            val strippedLine = stripAnsiCodes(line);
            if (!strippedLine.trim().isEmpty()) {
                int indent = findVisibleTextStart(line);
                if (indent < minIndent) {
                    minIndent = indent;
                }
            }
        }

        val indentStr = " ".repeat(targetIndent);

        for (int i = 0; i < trimmedLines.length; i++) {
            if (trimmedLines[i].length() >= minIndent) {
                trimmedLines[i] = trimmedLines[i].substring(minIndent);
            }
            trimmedLines[i] = indentStr + trimmedLines[i];
        }

        return String.join(NL, trimmedLines);
    }

    private static String stripAnsiCodes(String input) {
        return input.replaceAll("\u001B\\[[0-9;]*m", "");
    }

    private static int findVisibleTextStart(String line) {
        val stripped = stripAnsiCodes(line);
        val trimmed = stripped.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        return stripped.indexOf(trimmed);
    }

    public static String renderComment(CharSequence comment) {
        return AstraColors.NEUTRAL_400.use("# " + comment);
    }

    public static String renderCommand(Iterable<? extends CharSequence> command, CharSequence extra) {
        return renderCommand(String.join(" ", command) + " " + extra);
    }

    public static String renderCommand(CharSequence command) {
        return AstraColors.BLUE_300.use("$ ") + command;
    }
}
