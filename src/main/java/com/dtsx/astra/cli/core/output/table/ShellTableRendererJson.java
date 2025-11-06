package com.dtsx.astra.cli.core.output.table;

import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.serializers.OutputSerializer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.utils.StringUtils.titleToCamelCase;

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
                        e -> titleToCamelCase(e.getKey()),
                        e -> OutputSerializer.serializeAsJson(e.getValue())
                    )))
            .toList();
    }
}
