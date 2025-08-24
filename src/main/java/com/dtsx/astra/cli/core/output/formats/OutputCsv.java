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
import static com.dtsx.astra.cli.utils.StringUtils.NL;
import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

@FunctionalInterface
public interface OutputCsv {
    String renderAsCsv();

    @SneakyThrows
    static OutputCsv response(CharSequence message, @Nullable SequencedMap<String, Object> data, ExitCode code) {
        val sb = new StringBuilder().append("code,message");

        val lhm = Optional.ofNullable(data).map(OutputCsv::serializeData)
            .orElseGet(LinkedHashMap::new);

        for (val key : lhm.keySet()) {
            sb.append(',').append(key);
        }

        sb.append(NL);
        sb.append(code.name()).append(OutputSerializer.trySerializeAsCsv(trimIndent(message.toString())));

        for (val value : lhm.values()) {
            sb.append(',').append(value);
        }

        return sb::toString;
    }

    static OutputCsv serializeValue(Object o) {
        return () -> "code,data" + NL + OK.name() + "," + OutputSerializer.trySerializeAsCsv(o);
    }

    @SneakyThrows
    private static LinkedHashMap<String, Object> serializeData(SequencedMap<String, Object> data) {
        val map = new LinkedHashMap<String, Object>(data.size());

        for (val e : data.entrySet()) {
            map.put(
                OutputSerializer.trySerializeAsCsv("data." + e.getKey()),
                OutputSerializer.trySerializeAsCsv(e.getValue())
            );
        }

        return map;
    }
}
