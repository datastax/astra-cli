package com.dtsx.astra.cli.core.parsers.env.ast;

import com.dtsx.astra.cli.core.output.AstraColors;
import lombok.val;

import java.util.Optional;

public record EnvKVPair(String key, String value, Optional<EnvComment> inlineComment) implements EnvNode {
    @Override
    public String render(AstraColors colors) {
        val base = colors.BLUE_300.use(key) + colors.NEUTRAL_400.use("=") + value;

        if (inlineComment.isPresent()) {
            return base + inlineComment().get().render(colors);
        }

        return base;
    }
}
