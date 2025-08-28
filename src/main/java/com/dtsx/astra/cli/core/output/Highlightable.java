package com.dtsx.astra.cli.core.output;

import com.dtsx.astra.cli.core.CliContext;

public interface Highlightable {
    String highlight(CliContext ctx);
}
