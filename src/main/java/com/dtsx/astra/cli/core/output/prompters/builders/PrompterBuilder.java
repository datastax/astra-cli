package com.dtsx.astra.cli.core.output.prompters.builders;

import com.dtsx.astra.cli.core.output.prompters.CLIPrompter;
import lombok.RequiredArgsConstructor;
import org.graalvm.collections.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@RequiredArgsConstructor
public class PrompterBuilder {
    private final String prompt;
    private final boolean noInput;

    public <T> NeedsDefaultsOrEchoOff<T> mapper(Function<String, T> mapper) {
        return new NeedsDefaultsOrEchoOff<>(mapper);
    }

    @RequiredArgsConstructor
    public class NeedsDefaultsOrEchoOff<T> {
        private final Function<String, T> mapper;

        public NeedsDefaults<T> echoOff(Function<String, String> displayContentWhenDone) {
            return new NeedsDefaults<>(mapper, true, displayContentWhenDone);
        }

        public NeedsFallback<T> defaultOption(@Nullable String defaultOption) {
            return new NeedsFallback<>(mapper, Optional.ofNullable(defaultOption), false, null);
        }

        public NeedsFallback<T> requireAnswer() {
            return new NeedsFallback<>(mapper, Optional.empty(), false, null);
        }
    }

    @RequiredArgsConstructor
    public class NeedsDefaults<T> {
        private final Function<String, T> mapper;
        private final boolean echoOff;
        private final Function<String, String> displayContentWhenDone;

        public NeedsFallback<T> defaultOption(@Nullable String defaultOption) {
            return new NeedsFallback<>(mapper, Optional.ofNullable(defaultOption), echoOff, displayContentWhenDone);
        }

        public NeedsFallback<T> requireAnswer() {
            return new NeedsFallback<>(mapper, Optional.empty(), echoOff, displayContentWhenDone);
        }
    }

    @RequiredArgsConstructor
    public class NeedsFallback<T> {
        private final Function<String, T> mapper;
        private final Optional<String> defaultOption;
        private final boolean echoOff;
        private final Function<String, String> displayContentWhenDone;

        public NeedsFix<T> fallbackFlag(String flag) {
            return new NeedsFix<>(defaultOption, mapper, echoOff, displayContentWhenDone, highlight(flag) + " flag");
        }

        public NeedsFix<T> fallbackIndex(int index) {
            return new NeedsFix<>(defaultOption, mapper, echoOff, displayContentWhenDone, "parameter at index " + highlight(index));
        }
    }

    @RequiredArgsConstructor
    public class NeedsFix<T> {
        private final Optional<String> defaultOption;
        private final Function<String, T> mapper;
        private final boolean echoOff;
        private final Function<String, String> displayContentWhenDone;
        private final String fallback;

        public NeedsClearAfterSelection<T> fix(Iterable<String> originalArgs, String newArg) {
            return new NeedsClearAfterSelection<>(defaultOption, mapper, echoOff, displayContentWhenDone, fallback, Pair.create(originalArgs, newArg));
        }
    }

    @RequiredArgsConstructor
    public class NeedsClearAfterSelection<T> {
        private final Optional<String> defaultOption;
        private final Function<String, T> mapper;
        private final boolean echoOff;
        private final Function<String, String> displayContentWhenDone;
        private final String fallback;
        private final Pair<? extends Iterable<String>, String> fix;

        public T clearAfterSelection() {
            return clearAfterSelection(true);
        }

        public T dontClearAfterSelection() {
            return clearAfterSelection(false);
        }

        private T clearAfterSelection(boolean clearAfterSelection) {
            return CLIPrompter.prompt(
                prompt,
                noInput,
                defaultOption,
                mapper,
                echoOff,
                displayContentWhenDone,
                fallback,
                fix,
                clearAfterSelection
            );
        }
    }
}
