package com.dtsx.astra.cli.core.output.formats;

import com.dtsx.astra.cli.core.completions.impls.OutputTypeCompletion;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Option;

public enum OutputType {
    HUMAN,
    JSON,
    CSV;

    private static @Nullable OutputType requested = null;

    public static OutputType requested() {
        if (requested == null) {
            throw new CongratsYouFoundABugException("Can not access OutputType.requested() yet");
        }
        return requested;
    }

    public static boolean isHuman() {
        return requested() == HUMAN;
    }

    public static boolean isNotHuman() {
        return requested() != HUMAN;
    }

    public static class Mixin {
        @Option(names = { "--output", "-o" }, completionCandidates = OutputTypeCompletion.class, defaultValue = "human", description = "One of: ${COMPLETION-CANDIDATES}", paramLabel = "FORMAT")
        public void setRequested(OutputType type) {
            if (OutputType.requested == null) {
                OutputType.requested = type;
            }
        }
    }
}
