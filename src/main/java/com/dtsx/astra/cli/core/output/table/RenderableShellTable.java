package com.dtsx.astra.cli.core.output.table;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.output.formats.OutputAll;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record RenderableShellTable(
    List<? extends Map<String, ?>> raw,
    List<String> columns,
    Optional<String> varLenColumn
) implements OutputAll {
    @Override
    public String renderAsCsv() {
        return new ShellTableRendererCsv(this).renderAsCsv();
    }

    @Override
    public String renderAsHuman(CliContext ctx) {
        return new ShellTableRendererHuman(this).renderAsHuman(ctx);
    }

    @Override
    public String renderAsJson() {
        return new ShellTableRendererJson(this).renderAsJson();
    }
}
