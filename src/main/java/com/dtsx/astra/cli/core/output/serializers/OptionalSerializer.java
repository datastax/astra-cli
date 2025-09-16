package com.dtsx.astra.cli.core.output.serializers;

import java.util.Optional;

enum OptionalSerializer implements OutputSerializer<Optional<?>> {
    INSTANCE;

    @Override
    public boolean canSerialize(Object o) {
        return o instanceof Optional<?>;
    }

    @Override
    public String serializeAsHumanInternal(Optional<?> s) {
        return s.map(OutputSerializer::serializeAsHuman).orElse(OutputSerializer.serializeAsHuman(null));
    }

    @Override
    public Object serializeAsJsonInternal(Optional<?> s) {
        return s.map(OutputSerializer::serializeAsJson).orElse(OutputSerializer.serializeAsJson(null));
    }

    @Override
    public String serializeAsCsvInternal(Optional<?> s) {
        return s.map(OutputSerializer::serializeAsCsv).orElse(OutputSerializer.serializeAsCsv(null));
    }
}
