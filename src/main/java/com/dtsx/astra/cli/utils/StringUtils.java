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
}
