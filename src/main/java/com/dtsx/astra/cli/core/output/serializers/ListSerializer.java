package com.dtsx.astra.cli.core.output.serializers;

import lombok.val;

import java.util.List;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

enum ListSerializer implements OutputSerializer<List<String>> {
    INSTANCE;

    @Override
    public boolean canSerialize(Object o) {
        if (o instanceof List<?> l) {
            return l.stream().allMatch(e -> e instanceof String);
        }
        return false;
    }

    @Override
    public String serializeAsHumanInternal(List<String> strings) {
        val counter = new int[1];

        return strings.stream()
            .map((s) -> (
                "[" + counter[0]++ + "] " + s
            ))
            .collect(Collectors.joining(NL));
    }

    @Override
    public Object serializeAsJsonInternal(List<String> strings) {
        return strings;
    }

    @Override
    public String serializeAsCsvInternal(List<String> strings) {
        if (strings.isEmpty()) {
            return "";
        }

        if (strings.size() == 1) {
            return OutputSerializer.trySerializeAsCsv(strings.getFirst());
        }

        val content = strings.stream()
            .map(OutputSerializer::trySerializeAsCsv)
            .map((s) -> (
                (s.startsWith("\""))
                    ? s.substring(1, s.length() - 1)
                    : s
            ))
            .collect(Collectors.joining(","));

        return '"' + content + '"';
    }
}
