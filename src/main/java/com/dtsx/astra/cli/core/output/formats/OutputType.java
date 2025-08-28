package com.dtsx.astra.cli.core.output.formats;

import com.dtsx.astra.cli.core.completions.impls.OutputTypeCompletion;
import lombok.Getter;
import lombok.experimental.Accessors;
import picocli.CommandLine.Option;

public enum OutputType {
    HUMAN,
    JSON,
    CSV;

    public boolean isHuman() {
        return this == HUMAN;
    }

    public boolean isNotHuman() {
        return this != HUMAN;
    }

    @Accessors(fluent = true)
    public static class Mixin {
        @Option(
            names = { "--output", "-o" },
            completionCandidates = OutputTypeCompletion.class,
            defaultValue = "human",
            description = "One of: ${COMPLETION-CANDIDATES}",
            paramLabel = "FORMAT"
        )
        @Getter
        private OutputType requested;
    }
}
