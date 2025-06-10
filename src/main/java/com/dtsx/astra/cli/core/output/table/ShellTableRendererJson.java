package com.dtsx.astra.cli.core.output.table;

import com.dtsx.astra.cli.core.output.output.OutputJson;
import com.dtsx.astra.cli.core.output.serializers.OutputSerializer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ShellTableRendererJson(RenderableShellTable table) implements OutputJson {
    @Override
    public String renderAsJson() {
        return OutputJson.serializeValue(serialize(table.raw())).renderAsJson();
    }

    private List<Map<String, Object>> serialize(List<? extends Map<String, ?>> raw) {
        return raw.stream()
            .map((row) ->
                row.entrySet().stream()
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> OutputSerializer.trySerializeAsJson(e.getValue())
                    )))
            .toList();
    }
}
