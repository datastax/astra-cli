package com.dtsx.astra.cli.core.output.output;

import com.dtsx.astra.cli.core.output.serializers.OutputSerializer;

import java.util.StringJoiner;

@FunctionalInterface
public interface OutputHuman {
    String renderAsHuman();

    static OutputHuman message(StringJoiner s) {
        return message(s.toString());
    }

    static OutputHuman message(CharSequence s) {
        return s::toString;
    }

    static OutputHuman serializeValue(Object o) {
        return () -> OutputSerializer.trySerializeAsHuman(o);
    }
}
