package com.dtsx.astra.cli.core.output.formats;

import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.output.serializers.OutputSerializer;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.SequencedMap;

import static com.dtsx.astra.cli.core.output.ExitCode.OK;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@FunctionalInterface
public interface OutputCsv {
    String renderAsCsv();

    @SneakyThrows
    static OutputCsv response(CharSequence message, @Nullable SequencedMap<String, Object> rawData, ExitCode code) {
        val sb = new StringBuilder().append("code,message");

        val serializedData = Optional.ofNullable(rawData).map(OutputCsv::serializeData)
            .orElseGet(LinkedHashMap::new);

        for (val key : serializedData.keySet()) {
            sb.append(',').append(key);
        }

        sb.append(NL);
        sb.append(code.name());
        sb.append(",");
        sb.append(OutputSerializer.serializeAsCsv(trimIndent(message.toString())));

        for (val value : serializedData.values()) {
            sb.append(',').append(value);
        }

        return sb::toString;
    }

    static OutputCsv serializeValue(Object o) {
        return () -> "code,message,data" + NL + OK.name() + ",," + OutputSerializer.serializeAsCsv(o);
    }

    @SneakyThrows
    private static LinkedHashMap<String, Object> serializeData(SequencedMap<String, Object> data) {
        val map = new LinkedHashMap<String, Object>(data.size());

        for (val e : data.entrySet()) {
            map.put(
                OutputSerializer.serializeAsCsv(titleToSnakeCase(e.getKey())),
                OutputSerializer.serializeAsCsv(e.getValue())
            );
        }

        return map;
    }
}
