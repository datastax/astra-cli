package com.dtsx.astra.cli.output.serializers;

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
        return String.join("|", strings);
    }
}
