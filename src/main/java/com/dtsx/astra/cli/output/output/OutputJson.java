package com.dtsx.astra.cli.output.output;

import com.dtsx.astra.cli.output.JsonResponse;
import com.fasterxml.jackson.jr.ob.JSON;
import lombok.SneakyThrows;
import lombok.val;

@FunctionalInterface
public interface OutputJson {
    String renderAsJson();

    static OutputJson message(CharSequence s) {
        return serializeValue(s.toString());
    }

    @SuppressWarnings("Convert2Lambda")
    static OutputJson serializeValue(Object o) {
        return new OutputJson() {
            @SneakyThrows
            public String renderAsJson() {
                val resp = JsonResponse.ok(o);

                return JSON.std
                    .with(JSON.Feature.PRETTY_PRINT_OUTPUT)
                    .asString(resp);
            }
        };
    }
}
