package com.dtsx.astra.cli.core.output.serializers;

import lombok.NonNull;
import lombok.val;

enum CatchAllSerializer implements OutputSerializer<Object> {
    INSTANCE;

    @Override
    public boolean canSerialize(Object o) {
        return true;
    }

    @Override
    public String serializeAsHumanInternal(@NonNull Object o) { // nulls should've been caught by the NullSerializer
        return o.toString();
    }

    @Override
    public Object serializeAsJsonInternal(@NonNull Object o) {
        return o;
    }

    @Override
    public String serializeAsCsvInternal(@NonNull Object o) {
        val s = o.toString();

        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        } else {
            return s;
        }
    }
}
