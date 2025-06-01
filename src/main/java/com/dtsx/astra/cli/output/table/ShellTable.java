package com.dtsx.astra.cli.output.table;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ShellTable {
    @Getter
    private final List<? extends Map<String, ?>> raw;

    @Getter
    private final List<ShellTableSerializer<?>> serializers = List.of(
        ShellTableSerializer.StringSerializer.INSTANCE,
        ShellTableSerializer.ListSerializer.INSTANCE
    );

    public static Map<String, Object> attr(String key, Object value) {
        return Map.of("
    }

    public RenderableShellTable withAttributeColumns() {
        return withColumns("Attribute", "Value");
    }

    public RenderableShellTable withColumns(String... columnNames) {
        val columnNamesList = List.of(columnNames);

        raw.forEach((row) -> {
            row.keySet().retainAll(columnNamesList);
        });

        return new RenderableShellTable(raw, serializers, columnNamesList);
    }
}
