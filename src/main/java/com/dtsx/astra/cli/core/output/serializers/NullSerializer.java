package com.dtsx.astra.cli.core.output.serializers;

enum NullSerializer implements OutputSerializer<Object> {
    INSTANCE;

    @Override
    public boolean canSerialize(Object o) {
        return o == null;
    }

    @Override
    public String serializeAsHumanInternal(Object value) {
        return "null";
    }

    @Override
    public Object serializeAsJsonInternal(Object value) {
        return null;
    }

    @Override
    public String serializeAsCsvInternal(Object value) {
        return "";
    }
}
