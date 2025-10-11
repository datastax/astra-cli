package com.dtsx.astra.cli.core.output.formats;

public enum OutputType {
    HUMAN,
    JSON,
    CSV;

    public boolean isHuman() {
        return this == HUMAN;
    }

    public boolean isNotHuman() {
        return this != HUMAN;
    }
}
