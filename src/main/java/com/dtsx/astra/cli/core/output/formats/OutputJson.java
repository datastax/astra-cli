package com.dtsx.astra.cli.core.output.formats;

import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.output.Hint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.SequencedMap;

import static com.dtsx.astra.cli.core.output.ExitCode.OK;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;
import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

@FunctionalInterface
public interface OutputJson {
    String renderAsJson();

    static OutputJson response(CharSequence message, @Nullable SequencedMap<String, Object> data, @Nullable List<Hint> nextSteps, ExitCode code) {
        return () -> serializeValue(sequencedMapOf(
            "code", code,
            "message", trimIndent(message.toString()),
            "data", Optional.ofNullable(data),
            "nextSteps", Optional.ofNullable(nextSteps)
        ));
    }

    static OutputJson serializeValue(Object o) {
        return () -> serializeValue(sequencedMapOf(
            "code", OK,
            "data", o
        ));
    }

    @SneakyThrows
    private static String serializeValue(SequencedMap<String, ?> data) {
        return new ObjectMapper()
            .registerModule(new Jdk8Module())
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(data);
    }
}
