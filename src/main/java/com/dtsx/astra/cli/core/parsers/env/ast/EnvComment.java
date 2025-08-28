package com.dtsx.astra.cli.core.parsers.env.ast;

import com.dtsx.astra.cli.core.output.AstraColors;

public record EnvComment(String comment) implements EnvNode {
    @Override
    public String render(AstraColors colors) {
        return colors.NEUTRAL_400.use(comment);
    }
}
