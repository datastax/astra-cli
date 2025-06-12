package com.dtsx.astra.cli.core.output.table;

import com.dtsx.astra.cli.core.output.AstraColors;
import lombok.val;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ShellTable(List<? extends Map<String, ?>> raw) {
    public static Map<String, Object> attr(String key, Object value) {
        return Map.of("Attribute", key, "Value", value);
    }

    public static String highlight(String s) {
        return AstraColors.PURPLE_300.use(s);
    }

    public RenderableShellTable withAttributeColumns() {
        return withColumns("Attribute", "Value");
    }

    public RenderableShellTable withColumns(String... columnNames) {
        val columnNamesList = List.of(columnNames);

        val clonedRaw = raw.stream()
            .map(HashMap::new)
            .toList();

        clonedRaw.forEach((row) ->
            row.keySet().retainAll(columnNamesList)
        );

        return new RenderableShellTable(clonedRaw, columnNamesList);
    }
}
