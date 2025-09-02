package com.dtsx.astra.cli.core.output.serializers;

import java.util.function.Supplier;

enum SupplierSerializer implements OutputSerializer<Supplier<?>> {
    INSTANCE;

    @Override
    public boolean canSerialize(Object o) {
        return o instanceof Supplier<?>;
    }

    @Override
    public String serializeAsHumanInternal(Supplier<?> s) {
        return OutputSerializer.serializeAsHuman(s.get());
    }

    @Override
    public Object serializeAsJsonInternal(Supplier<?> s) {
        return OutputSerializer.serializeAsJson(s.get());
    }

    @Override
    public String serializeAsCsvInternal(Supplier<?> s) {
        return OutputSerializer.serializeAsCsv(s.get());
    }
}
