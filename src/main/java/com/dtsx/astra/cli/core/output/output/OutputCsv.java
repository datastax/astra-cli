package com.dtsx.astra.cli.core.output.output;

import com.dtsx.astra.cli.core.output.serializers.OutputSerializer;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

@FunctionalInterface
public interface OutputCsv {
    String renderAsCsv();

    static OutputCsv message(CharSequence s) {
        return () -> "code,message" + NL + "0," + OutputSerializer.trySerializeAsCsv(s);
    }

    static OutputCsv serializeValue(Object o) {
        return () -> "Value" + NL + OutputSerializer.trySerializeAsCsv(o);
    }
}
