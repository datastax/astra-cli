package com.dtsx.astra.cli.core.output.serializers;

import lombok.val;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.utils.StringUtils.*;

enum CollectionSerializer implements OutputSerializer<Collection<?>> {
    INSTANCE;

    @Override
    public boolean canSerialize(Object o) {
        return o instanceof Collection<?>;
    }

    @Override
    public String serializeAsHumanInternal(Collection<?> values) {
        if (values.isEmpty()) {
            return "<none>";
        }

        val counter = new int[1];

        return values.stream()
            .map((s) -> (
                "[" + counter[0]++ + "] " + withIndent(OutputSerializer.serializeAsHuman(s), 4).substring(4)
            ))
            .collect(Collectors.joining(NL));
    }

    @Override
    public Object serializeAsJsonInternal(Collection<?> values) {
        return values;
    }

    @Override
    public String serializeAsCsvInternal(Collection<?> values) {
        if (values.isEmpty()) {
            return "";
        }

        if (values.size() == 1) {
            return OutputSerializer.serializeAsCsv(values.iterator().next());
        }

        val content = values.stream()
            .map(OutputSerializer::serializeAsCsv)
            .map((s) -> (
                (s.startsWith("\""))
                    ? s.substring(1, s.length() - 1)
                    : s
            ))
            .collect(Collectors.joining(","));

        return '"' + content + '"';
    }
}
