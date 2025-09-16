package com.dtsx.astra.cli.core.output;

public record Hint(String comment, String command) {
    public Hint(String comment, Iterable<? extends CharSequence> command, CharSequence extra) {
        this(comment, mkExtra(command, extra.toString()));
    }

    private static String mkExtra(Iterable<? extends CharSequence> command, String extra) {
        return String.join(" ", command) + (extra.isBlank() ? "" : " " + extra.trim());
    }
}
