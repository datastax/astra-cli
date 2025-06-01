package com.dtsx.astra.cli.output.table;

import com.dtsx.astra.cli.output.output.OutputJson;

public record ShellTableRendererJson(RenderableShellTable table) implements OutputJson {
    @Override
    public String renderAsJson() {
        return "";
    }
}
