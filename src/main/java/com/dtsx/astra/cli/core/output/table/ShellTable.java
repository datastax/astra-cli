package com.dtsx.astra.cli.core.output.table;

import com.dtsx.astra.cli.core.CliContext;
import lombok.val;

import java.util.*;

import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

public record ShellTable(List<? extends Map<String, ?>> raw) {
    public static RenderableShellTable forAttributes(LinkedHashMap<String, Object> attributes) {
        return forAttributes(attributes, "Attribute", "Value");
    }

    public static RenderableShellTable forAttributes(LinkedHashMap<String, Object> attributes, String attr, String value) {
        val rows = attributes.entrySet().stream()
            .map(entry -> sequencedMapOf(attr, entry.getKey(), value, entry.getValue()))
            .toList();

        return new RenderableShellTable(rows, Arrays.asList(attr, value));
    }

    public static String highlight(CliContext ctx, String s) {
        return ctx.colors().PURPLE_300.use(s);
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
