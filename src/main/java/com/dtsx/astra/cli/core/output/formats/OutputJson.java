package com.dtsx.astra.cli.core.output.formats;

import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.output.Hint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.dtsx.astra.cli.core.output.ExitCode.OK;
import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

@FunctionalInterface
public interface OutputJson {
    String renderAsJson();

    static OutputJson response(CharSequence message, @Nullable Map<String, Object> data, @Nullable List<Hint> nextSteps, ExitCode code) {
        return () -> serializeValue(Map.of(
            "code", code,
            "message", trimIndent(message.toString()),
            "data", Optional.ofNullable(data),
            "nextSteps", Optional.ofNullable(nextSteps)
        ));
    }

    static OutputJson serializeValue(Object o) {
        return () -> serializeValue(Map.of(
            "code", OK,
            "data", o
        ));
    }

    @SneakyThrows
    private static String serializeValue(Map<String, ?> data) {
        return new ObjectMapper()
            .registerModule(new Jdk8Module())
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(data);
    }
}
