package com.dtsx.astra.cli.core.parsers.env.ast;

import com.dtsx.astra.cli.core.output.AstraColors;

public record EnvKVPair(String key, String value) implements EnvNode {
    @Override
    @SuppressWarnings("DuplicatedCode")
    public String render(AstraColors colors) {
        return colors.BLUE_300.use(key) + colors.NEUTRAL_400.use("=") + value;
    }
}
