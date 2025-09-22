package com.dtsx.astra.cli.core.output.formats;

import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.serializers.OutputSerializer;
import com.dtsx.astra.cli.utils.JsonUtils;
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
            Fields.DATA, Optional.ofNullable(data).map(OutputSerializer::serializeAsJson),
            Fields.NEXT_STEPS, Optional.ofNullable(nextSteps).filter(l -> !l.isEmpty())
        ));
    }

    static OutputJson serializeValue(Object o) {
        return () -> serializeValue(sequencedMapOf(
            Fields.CODE, OK,
            Fields.DATA, OutputSerializer.serializeAsJson(o)
        ));
    }

    @SneakyThrows
    private static String serializeValue(SequencedMap<String, ?> data) {
        return JsonUtils.objectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(data);
    }
}
