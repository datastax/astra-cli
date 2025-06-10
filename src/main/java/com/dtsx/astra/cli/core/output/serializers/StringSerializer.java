package com.dtsx.astra.cli.core.output.serializers;

enum StringSerializer implements OutputSerializer<String> {
    INSTANCE;

    @Override
    public boolean canSerialize(Object o) {
        return o instanceof String;
    }

    @Override
    public String serializeAsHumanInternal(String s) {
        return s;
    }

    @Override
    public Object serializeAsJsonInternal(String s) {
        return s;
    }

    @Override
    public String serializeAsCsvInternal(String s) {
        return s;
    }
}
