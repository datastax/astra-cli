package com.dtsx.astra.cli.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {
    public static final String NL = System.lineSeparator();

    public String removeQuotesIfAny(String str) {
        if (str != null && str.length() >= 2 && ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'")))) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }
}
