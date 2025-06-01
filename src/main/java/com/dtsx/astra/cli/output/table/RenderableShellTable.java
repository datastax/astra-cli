package com.dtsx.astra.cli.output.table;

import com.dtsx.astra.cli.output.output.OutputAll;

import java.util.List;
import java.util.Map;

public record RenderableShellTable(
    List<? extends Map<String, ?>> raw,
    List<ShellTableSerializer<?>> serializers,
    List<String> columns
) implements OutputAll {
    @Override
    public String renderAsCsv() {
        return new ShellTableRendererCsv(this).renderAsCsv();
    }

    @Override
    public String renderAsHuman() {
        return new ShellTableRendererHuman(this).renderAsHuman();
    }

    @Override
    public String renderAsJson() {
        return new ShellTableRendererJson(this).renderAsJson();
    }
}
