package com.dtsx.astra.cli.utils;

import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.AstraColors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.UUID;

@Slf4j
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
        return withIndent(input, " ".repeat(targetIndent));
    }

    public static String withIndent(String input, String indentStr) {
        val lines = input.split("\n", -1);

        val start = lines.length > 0 && stripAnsiCodes(lines[0]).trim().isEmpty()
            ? 1
            : 0;

        val end = lines.length > 1 && stripAnsiCodes(lines[lines.length - 1]).trim().isEmpty()
            ? lines.length - 1
            : lines.length;

        val trimmedLines = new String[end - start];
        System.arraycopy(lines, start, trimmedLines, 0, end - start);

        var minIndent = Integer.MAX_VALUE;

        for (val line : trimmedLines) {
            val strippedLine = stripAnsiCodes(line);

            if (!strippedLine.trim().isEmpty()) {
                minIndent = Math.min(minIndent, findVisibleTextStart(strippedLine));
            }
        }

        for (int i = 0; i < trimmedLines.length; i++) {
            trimmedLines[i] = indentStr + trimLeadingSpaces(trimmedLines[i], minIndent);
        }

        return String.join(NL, trimmedLines);
    }

    private static String stripAnsiCodes(String input) {
        return input.replaceAll("\u001B\\[[0-9;]*m", "");
    }

    private static String trimLeadingSpaces(String line, int max) {
        int start = 0;
        while (start < line.length() && start < max) {
            char c = line.charAt(start);
            if (c != ' ') {
                break;
            }
            start++;
        }
        return line.substring(start);
    }

    private static int findVisibleTextStart(String strippedLine) {
        val trimmed = strippedLine.trim();

        if (trimmed.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        return strippedLine.indexOf(trimmed);
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

    public static String maskToken(String token) {
        return AstraToken.parse(token).fold(
            _ -> AstraColors.RED_500.use("<invalid_token('" + truncate(token, 4) + "')>"),
            AstraToken::toString
        );
    }

    public static boolean isWhitespaceOnly(String str) {
        if (str == null || str.isEmpty()) {
            return true;
        }

        for (char c : str.toCharArray()) {
            if (c != ' ' && c != '\t') {
                return false;
            }
        }

        return true;
    }

    public static boolean isInteger(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 1) + "…";
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
