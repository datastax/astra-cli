package com.dtsx.astra.cli.output.serializers;

enum BoolSerializer implements OutputSerializer<Boolean> {
    INSTANCE;

    @Override
    public boolean canSerialize(Object o) {
        return o instanceof Boolean;
    }

    @Override
    public String serializeAsHumanInternal(Boolean s) {
        return s.toString();
    }

    @Override
    public Object serializeAsJsonInternal(Boolean s) {
        return s;
    }

    @Override
    public String serializeAsCsvInternal(Boolean s) {
        return s.toString();
    }
}
