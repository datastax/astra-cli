package com.dtsx.astra.cli.output.table;

import lombok.val;

import java.util.List;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

@SuppressWarnings("unchecked")
public interface ShellTableSerializer<T> {
    boolean canSerialize(Object o);

    default String serializeHuman(String col, Object t) {
        return serializeHumanInternal(col, (T) t);
    }

    default Object serializeJson(String col, Object t) {
        return serializeJsonInternal(col, (T) t);
    }

    default String serializeCsv(String col, Object t) {
        return serializeCsvInternal(col, (T) t);
    }

    String serializeHumanInternal(String col, T t);
    Object serializeJsonInternal(String col, T t);
    String serializeCsvInternal(String col, T t);

    static ShellTableSerializer<?> findSerializerForObj(String col, Object o, List<ShellTableSerializer<?>> serializers) {
        for (val serializer : serializers) {
            if (serializer.canSerialize(o)) {
                return serializer;
            }
        }
        throw new IllegalArgumentException("No serializer found for column " + col);
    }

    enum StringSerializer implements ShellTableSerializer<String> {
        INSTANCE;

        @Override
        public boolean canSerialize(Object o) {
            return o instanceof String;
        }

        @Override
        public String serializeHumanInternal(String col, String s) {
            return s;
        }

        @Override
        public Object serializeJsonInternal(String col, String s) {
            return s;
        }

        @Override
        public String serializeCsvInternal(String col, String s) {
            return s;
        }
    }

    enum ListSerializer implements ShellTableSerializer<List<String>> {
        INSTANCE;

        @Override
        public boolean canSerialize(Object o) {
            if (o instanceof List<?> l) {
                return l.stream().allMatch(e -> e instanceof String);
            }
            return false;
        }

        @Override
        public String serializeHumanInternal(String col, List<String> strings) {
            val counter = new int[1];

            return strings.stream()
                .map((s) -> (
                    "[" + counter[0]++ + "] " + s
                ))
                .collect(Collectors.joining(NL));
        }

        @Override
        public Object serializeJsonInternal(String col, List<String> strings) {
            return strings;
        }

        @Override
        public String serializeCsvInternal(String col, List<String> strings) {
            return String.join("|", strings);
        }
    }
}
