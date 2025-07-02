package com.dtsx.astra.cli.core.output.serializers;

import lombok.val;

enum CatchAllSerializer implements OutputSerializer<Object> {
    INSTANCE;

    @Override
    public boolean canSerialize(Object o) {
        return true;
    }

    @Override
    public String serializeAsHumanInternal(Object o) {
        return o.toString();
    }

    @Override
    public Object serializeAsJsonInternal(Object o) {
        return o;
    }

    @Override
    public String serializeAsCsvInternal(Object o) {
        val s = o.toString();

        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        } else {
            return s;
        }
    }
}
