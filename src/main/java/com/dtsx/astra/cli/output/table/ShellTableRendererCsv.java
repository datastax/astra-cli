package com.dtsx.astra.cli.output.table;

import com.dtsx.astra.cli.output.output.OutputCsv;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

public record ShellTableRendererCsv(RenderableShellTable table) implements OutputCsv {
    @Override
    public String renderAsCsv() {
        return buildHeaders(table.columns()) + NL + buildValues(table.raw(), table.columns(), table.serializers());
    }

    private String buildHeaders(List<String> columns) {
        return String.join(",", columns);
    }

    private String buildValues(List<? extends Map<String, ?>> raw, List<String> columns, List<ShellTableSerializer<?>> serializers) {
        return raw.stream()
            .map((row) -> {
                val ret = new StringJoiner(",");

                for (val col : columns) {
                    val serializer = ShellTableSerializer.findSerializerForObj(col, row.get(col), serializers);
                    val serialized = serializer.serializeCsv(col, row.get(col));
                    ret.add(serialized);
                }

                return ret.toString();
            })
            .collect(Collectors.joining(NL));
    }
}
