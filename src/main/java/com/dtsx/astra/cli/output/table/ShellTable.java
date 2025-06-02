package com.dtsx.astra.cli.output.table;

import lombok.val;

import java.util.List;
import java.util.Map;

public record ShellTable(List<? extends Map<String, ?>> raw) {
    public static Map<String, Object> attr(String key, Object value) {
        return Map.of("Attribute", key, "Value", value);
    }

    public RenderableShellTable withAttributeColumns() {
        return withColumns("Attribute", "Value");
    }

    public RenderableShellTable withColumns(String... columnNames) {
        val columnNamesList = List.of(columnNames);

        raw.forEach((row) ->
            row.keySet().retainAll(columnNamesList)
        );

        return new RenderableShellTable(raw, columnNamesList);
    }
}
