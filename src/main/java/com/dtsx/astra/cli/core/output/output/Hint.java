package com.dtsx.astra.cli.core.output.output;

public record Hint(String comment, String command) {
    public Hint(String comment, Iterable<? extends CharSequence> command, CharSequence extra) {
        this(comment, String.join(" ", command) + (extra.toString().isBlank() ? "" : " " + extra));
    }
}
