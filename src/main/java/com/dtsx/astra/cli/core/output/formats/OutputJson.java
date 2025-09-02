package com.dtsx.astra.cli.core.output.formats;

import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.output.Hint;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
    ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
        .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
        .addModule(new Jdk8Module())
        .build();

    String renderAsJson();

    abstract class Fields {
        public static final String CODE = "code";
        public static final String MESSAGE = "message";
        public static final String DATA = "data";
        public static final String NEXT_STEPS = "nextSteps";
    }

    static OutputJson response(CharSequence message, @Nullable SequencedMap<String, Object> data, @Nullable List<Hint> nextSteps, ExitCode code) {
        return () -> serializeValue(sequencedMapOf(
            Fields.CODE, code,
            Fields.MESSAGE, trimIndent(message.toString()),
            Fields.DATA, Optional.ofNullable(data),
            Fields.NEXT_STEPS, Optional.ofNullable(nextSteps)
        ));
    }

    static OutputJson serializeValue(Object o) {
        return () -> serializeValue(sequencedMapOf(
            Fields.CODE, OK,
            Fields.DATA, o
        ));
    }

    @SneakyThrows
    private static String serializeValue(SequencedMap<String, ?> data) {
        return OBJECT_MAPPER
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(data);
    }
}
