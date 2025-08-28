package com.dtsx.astra.cli.core.output.formats;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.output.Hint;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.SequencedMap;
import java.util.function.Supplier;

public interface OutputAll extends OutputHuman, OutputJson, OutputCsv {
    static OutputAll response(CharSequence message, @Nullable SequencedMap<String, Object> data, @Nullable List<Hint> nextSteps, ExitCode exitCode) {
        return instance(() -> OutputHuman.response(message, nextSteps), () -> OutputJson.response(message, data, nextSteps, exitCode), () -> OutputCsv.response(message, data, exitCode));
    }

    static OutputAll response(CharSequence message, @Nullable SequencedMap<String, Object> data, @Nullable List<Hint> nextSteps) {
        return response(message, data, nextSteps, ExitCode.OK);
    }

    static OutputAll response(CharSequence message, @Nullable SequencedMap<String, Object> data) {
        return response(message, data, null);
    }

    static OutputAll response(CharSequence message) {
        return response(message, null);
    }

    static OutputAll serializeValue(Object o) {
        return instance(() -> OutputHuman.serializeValue(o), () -> OutputJson.serializeValue(o), () -> OutputCsv.serializeValue(o));
    }

    static OutputAll instance(Supplier<OutputHuman> human, Supplier<OutputJson> json, Supplier<OutputCsv> csv) {
        return new OutputAll() {
            @Override
            public String renderAsHuman(CliContext ctx) {
                return human.get().renderAsHuman(ctx);
            }

            @Override
            public String renderAsJson() {
                return json.get().renderAsJson();
            }

            @Override
            public String renderAsCsv() {
                return csv.get().renderAsCsv();
            }
        };
    }

    default String render(CliContext ctx) {
        return switch (ctx.outputType()) {
            case HUMAN -> renderAsHuman(ctx);
            case JSON -> renderAsJson();
            case CSV -> renderAsCsv();
        };
    }
}
