package com.dtsx.astra.cli.core.output.prompters.builders;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.output.prompters.CLIPrompter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

@RequiredArgsConstructor
public class ConfirmerBuilder {
    private final CliContext ctx;
    private final boolean noInput;
    private final String prompt;

    public NeedsFallback defaultYes() {
        return new NeedsFallback(Optional.of(true));
    }

    public NeedsFallback defaultNo() {
        return new NeedsFallback(Optional.of(false));
    }

    public NeedsFallback requireAnswer() {
        return new NeedsFallback(Optional.empty());
    }

    @RequiredArgsConstructor
    public class NeedsFallback {
        private final Optional<Boolean> defaultOption;

        public NeedsFix fallbackFlag(String flag) {
            return new NeedsFix(defaultOption, ctx.highlight(flag) + " flag");
        }

        public NeedsFix fallbackIndex(int index) {
            return new NeedsFix(defaultOption, "parameter at index " + ctx.highlight(index));
        }
    }

    @RequiredArgsConstructor
    public class NeedsFix {
        private final Optional<Boolean> defaultOption;
        private final String fallback;

        public NeedsClearAfterSelection fix(Iterable<String> originalArgs, String newArg) {
            return new NeedsClearAfterSelection(defaultOption, fallback, Pair.of(originalArgs, newArg));
        }
    }

    @RequiredArgsConstructor
    public class NeedsClearAfterSelection {
        private final Optional<Boolean> defaultOption;
        private final String fallback;
        private final Pair<? extends Iterable<String>, String> fix;

        public boolean clearAfterSelection() {
            return clearAfterSelection(true);
        }

        public boolean dontClearAfterSelection() {
            return clearAfterSelection(false);
        }

        public boolean clearAfterSelection(boolean clearAfterSelection) {
            return new CLIPrompter(ctx, noInput).confirm(
                prompt,
                defaultOption,
                fallback,
                fix,
                clearAfterSelection
            );
        }
    }
}
