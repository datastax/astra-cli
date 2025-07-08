package com.dtsx.astra.cli.core.output.table;

import com.dtsx.astra.cli.core.output.AstraColors;
import lombok.val;

import java.util.*;

public record ShellTable(List<? extends Map<String, ?>> raw) {
    public static RenderableShellTable forAttributes(LinkedHashMap<String, Object> attributes) {
        val rows = attributes.entrySet().stream()
            .map(entry -> Map.of("Attribute", entry.getKey(), "Value", entry.getValue()))
            .toList();

        return new RenderableShellTable(rows, Arrays.asList("Attribute", "Value"));
    }

    public static String highlight(String s) {
        return AstraColors.PURPLE_300.use(s);
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
