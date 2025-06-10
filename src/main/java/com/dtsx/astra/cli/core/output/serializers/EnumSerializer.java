package com.dtsx.astra.cli.core.output.serializers;

enum EnumSerializer implements OutputSerializer<Enum<?>> {
    INSTANCE;

    @Override
    public boolean canSerialize(Object o) {
        return o instanceof Enum<?>;
    }

    @Override
    public String serializeAsHumanInternal(Enum<?> s) {
        return s.name();
    }

    @Override
    public Object serializeAsJsonInternal(Enum<?> s) {
        return s;
    }

    @Override
    public String serializeAsCsvInternal(Enum<?> s) {
        return s.name();
    }
}
