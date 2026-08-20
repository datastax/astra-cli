package com.dtsx.astra.cli.core.output.table;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.*;

import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@RequiredArgsConstructor
public final class ShellTable {
    private final List<? extends Map<String, ?>> raw;
    private Optional<String> varLenColumn = Optional.empty();

    public static RenderableShellTable forAttributes(LinkedHashMap<String, Object> attributes) {
        return forAttributes(attributes, "Attribute", "Value");
    }

    public static RenderableShellTable forAttributes(LinkedHashMap<String, Object> attributes, String keyHeader, String valueHeader) {
        val rows = attributes.entrySet().stream()
            .map(entry -> sequencedMapOf(keyHeader, entry.getKey(), valueHeader, entry.getValue()))
            .toList();

        return new RenderableShellTable(rows, Arrays.asList(keyHeader, valueHeader), Optional.empty());
    }

    public static String highlight(CliContext ctx, String s) {
        return ctx.colors().PURPLE_300.use(s);
    }

    public ShellTable withVarLenColumn(String columnName) {
        this.varLenColumn = Optional.of(columnName);
        return this;
    }

    public RenderableShellTable withColumns(String... columnNamesArray) {
        val columnNames = List.of(columnNamesArray);

        val clonedRaw = raw.stream()
            .map(HashMap::new)
            .toList();

        clonedRaw.forEach((row) -> {
            row.keySet().retainAll(columnNames);

            if (row.size() < columnNames.size()) {
                throw new CongratsYouFoundABugException("Row is missing columns. Expected: " + columnNames + ", but got: " + row.keySet());
            }
        });

        if (varLenColumn.isPresent()) {
            if (!columnNames.contains(varLenColumn.get())) {
                throw new CongratsYouFoundABugException("Variable length column '" + varLenColumn.get() + "' is not in the list of columns: " + columnNames);
            }
        }

        return new RenderableShellTable(clonedRaw, columnNames, varLenColumn);
    }
}
