package com.dtsx.astra.cli.core.output.table;

import com.dtsx.astra.cli.core.output.formats.OutputCsv;
import com.dtsx.astra.cli.core.output.serializers.OutputSerializer;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

public record ShellTableRendererCsv(RenderableShellTable table) implements OutputCsv {
    @Override
    public String renderAsCsv() {
        return buildHeaders(table.columns()) + NL + buildValues(table.raw(), table.columns());
    }

    private String buildHeaders(List<String> columns) {
        return String.join(",", columns);
    }

    // TODO how to format csv in the shell table case?
    private String buildValues(List<? extends Map<String, ?>> raw, List<String> columns) {
        return raw.stream()
            .map((row) -> {
                val ret = new StringJoiner(",");

                for (val col : columns) {
                    ret.add(OutputSerializer.serializeAsCsv(row.get(col)));
                }

                return ret.toString();
            })
            .collect(Collectors.joining(NL));
    }
}
