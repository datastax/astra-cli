package com.dtsx.astra.cli.core.parsers.env.ast;

import com.dtsx.astra.cli.core.output.AstraColors;

public sealed interface EnvNode permits EnvComment, EnvKVPair, EnvEmptyLine {
    String render(AstraColors colors);
}
