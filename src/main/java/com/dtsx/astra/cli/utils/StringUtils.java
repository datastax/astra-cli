package com.dtsx.astra.cli.utils;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class StringUtils {
    public static final String NL = System.lineSeparator();

    public String removeQuotesIfAny(String str) {
        if (str != null && str.length() >= 2 && ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'")))) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    public boolean isUUID(String str) {
        try {
            UUID.fromString(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String trimIndent(String input) {
        String[] lines = input.split("\n", -1);

        int start = 0;
        while (start < lines.length && lines[start].trim().isEmpty()) {
            start++;
        }
        int end = lines.length;
        while (end > start && lines[end - 1].trim().isEmpty()) {
            end--;
        }

        String[] trimmedLines = new String[end - start];
        System.arraycopy(lines, start, trimmedLines, 0, end - start);

        int minIndent = Integer.MAX_VALUE;
        for (String line : trimmedLines) {
            if (!line.trim().isEmpty()) {
                int indent = line.indexOf(line.trim());
                if (indent < minIndent) {
                    minIndent = indent;
                }
            }
        }

        for (int i = 0; i < trimmedLines.length; i++) {
            if (trimmedLines[i].length() >= minIndent) {
                trimmedLines[i] = trimmedLines[i].substring(minIndent);
            }
        }

        return String.join("\n", trimmedLines);
    }
}
