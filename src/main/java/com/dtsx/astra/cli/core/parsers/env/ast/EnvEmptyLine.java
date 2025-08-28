package com.dtsx.astra.cli.core.parsers.env.ast;

import com.dtsx.astra.cli.core.output.AstraColors;

public record EnvEmptyLine(String blankLine) implements EnvNode {
    @Override
    public String render(AstraColors colors) {
        return blankLine;
    }
}
