package com.dtsx.astra.cli.core.output.table;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraColors.AstraColor;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.core.output.serializers.OutputSerializer;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

@RequiredArgsConstructor
public final class ShellTableRendererHuman implements OutputHuman {
    private final RenderableShellTable table;

    private AstraColor TABLE_COLOR;
    private AstraColor DATA_COLOR;

    @Override
    public String renderAsHuman(CliContext ctx) {
        this.TABLE_COLOR = ctx.colors().BLUE_300;
        this.DATA_COLOR = ctx.colors().NEUTRAL_300;

        val serialized = serialize(table.raw());
        val colSizes = computeColumnWidths(serialized, table.columns());

        val fullWidthLine = buildFullWidthTableLine(table.columns(), colSizes);

        val joiner = new StringJoiner(NL)
            .add(fullWidthLine)
            .add(buildTableHeader(table.columns(), colSizes))
            .add(fullWidthLine);

        val tableData = buildTableData(serialized, table.columns(), colSizes);

        if (!tableData.isEmpty()) {
            joiner.add(tableData);
        }

        return joiner.add(fullWidthLine) + NL;
    }

    private List<Map<String, List<String>>> serialize(List<? extends Map<String, ?>> raw) {
        return raw.stream()
            .map((map) -> {
                Map<String, List<String>> ret = new HashMap<>();

                for (val entry : map.entrySet()) {
                    val serialized = OutputSerializer.trySerializeAsHuman(entry.getValue())
                        .split(NL);

                    ret.put(entry.getKey(), List.of(serialized));
                }

                return ret;
            })
            .toList();
    }

    @SuppressWarnings("DataFlowIssue")
    private Map<String, Integer> computeColumnWidths(List<Map<String, List<String>>> data, List<String> columns) {
        val colSizes = columns.stream().collect(Collectors.toMap(key -> key, val -> val.length() + 1, Integer::max, HashMap::new));

        data.forEach((row) -> table.columns().forEach((col) ->
            colSizes.compute(col, (_, v) -> Math.max(v, maxStringWidth(row.get(col))))
        ));

        return colSizes;
    }

    private int maxStringWidth(List<String> text) {
        return text.stream().map(AstraColors::stripAnsi).mapToInt(String::length).max().orElse(0);
    }

    private String buildFullWidthTableLine(List<String> columns, Map<String, Integer> colSizes) {
        val lineJoiner = new StringJoiner("+", "+", "+");

        for (val col : columns) {
            lineJoiner.add("-".repeat(colSizes.get(col) + 2));
        }

        return TABLE_COLOR.use(lineJoiner.toString());
    }

    private String buildTableHeader(List<String> columns, Map<String, Integer> colSizes) {
        val headerJoiner = new StringJoiner(" | ", "| ", " |");

        for (val col : columns) {
            headerJoiner.add(col + " ".repeat(colSizes.get(col) - AstraColors.stripAnsi(col).length()));
        }

        return TABLE_COLOR.use(headerJoiner.toString());
    }

    private String buildTableData(List<Map<String, List<String>>> data, List<String> columns, Map<String, Integer> colSizes) {
        if (data.isEmpty()) {
            return "";
        }

        val dataJoiner = new StringJoiner(NL);

        for (val row : data) {
            val maxLinesInRow = row.values().stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);

            for (int i = 0; i < maxLinesInRow; i++) {
                val rowJoiner = new StringJoiner(TABLE_COLOR.use(" | "), TABLE_COLOR.use("| "), TABLE_COLOR.use(" |"));

                for (val column : columns) {
                    val lines = row.get(column);
                    val text = (i < lines.size()) ? lines.get(i) : "";

                    if (!text.isEmpty()) {
                        rowJoiner.add(DATA_COLOR.use(text) + " ".repeat(colSizes.get(column) - AstraColors.stripAnsi(text).length()));
                    } else {
                        rowJoiner.add(" ".repeat(colSizes.get(column)));
                    }
                }

                dataJoiner.add(rowJoiner.toString());
            }
        }

        return dataJoiner.toString();
    }
}
