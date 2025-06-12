package com.dtsx.astra.cli.core.output.serializers;

enum CatchAllSerializer implements OutputSerializer<Object> {
    INSTANCE;

    @Override
    public boolean canSerialize(Object o) {
        return true;
    }

    @Override
    public String serializeAsHumanInternal(Object s) {
        return s.toString();
    }

    @Override
    public Object serializeAsJsonInternal(Object s) {
        return s;
    }

    @Override
    public String serializeAsCsvInternal(Object s) {
        return s.toString();
    }
}
