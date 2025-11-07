package com.dtsx.astra.cli.utils;

import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.output.AstraColors;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@UtilityClass
public class StringUtils {
    public static final String NL = System.lineSeparator();

    public static String removeQuotesIfAny(@NonNull String str) {
        if (str.length() >= 2 && ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'")))) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    public static String trimIndent(String input) {
        return withIndent(input, "");
    }

    public static String withIndent(String input, int targetIndent) {
        return withIndent(input, " ".repeat(targetIndent));
    }

    public static String withIndent(String input, String indentStr) {
        input = (input == null) ? "" : input;

        val lines = input.split("\n", -1);

        val start = lines.length > 0 && stripAnsiCodes(lines[0]).isBlank()
            ? 1
            : 0;

        val end = lines.length > 1 && stripAnsiCodes(lines[lines.length - 1]).isBlank()
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

    public static String renderComment(AstraColors colors, CharSequence comment) {
        return colors.NEUTRAL_400.use("# " + comment);
    }

    public static String renderCommand(AstraColors colors, CharSequence command) {
        return colors.BLUE_300.use("$ ") + command;
    }

    public static String maskToken(AstraColors colors, @NonNull String token) {
        return AstraToken.parse(token).fold(
            _ -> colors.RED_500.use("<invalid_token('" + truncate(token, 4) + "')>"),
            AstraToken::toString
        );
    }

    public static boolean isPositiveInteger(@NonNull String str) {
        return str.matches("\\d+");
    }

    public static String truncate(@NonNull String str, int maxLength) {
        if (maxLength <= 0) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 1) + "…";
    }

    public static String capitalize(@NonNull String str) {
        if (str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String titleToCamelCase(@NonNull String title) {
        val parts = title.split(" ");
        val sb = new StringBuilder();

        for (var i = 0; i < parts.length; i++) {
            val part = parts[i];

            if (part.isEmpty()) {
                continue;
            }

            if (i == 0) {
                sb.append(Character.toLowerCase(part.charAt(0))).append(part.substring(1));
            } else {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }

        return sb.toString();
    }

    public static String titleToSnakeCase(@NonNull String input) {
        if (input.isBlank()) {
            return input;
        }

        return input.trim()
            .replaceAll("[\\s-]+", "_")
            .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
            .toLowerCase()
            .replaceAll("_+", "_")
            .replaceAll("^_+|_+$", "");
    }
}
