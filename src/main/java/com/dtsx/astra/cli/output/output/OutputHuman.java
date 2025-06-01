package com.dtsx.astra.cli.output.output;

@FunctionalInterface
public interface OutputHuman {
    String renderAsHuman();

    static OutputHuman from(String str) {
        return () -> str;
    }
}
