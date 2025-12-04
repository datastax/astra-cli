package com.dtsx.astra.cli.core.output.prompters.builders;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.output.prompters.CLIPrompter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
public class SelectorBuilder {
    private final CliContext ctx;
    private final boolean noInput;
    private final String prompt;

    public <T> NeedsOptions<T> options(NEList<T> options) {
        return new NeedsOptions<>(options);
    }

    @SafeVarargs
    public final <T> NeedsOptions<T> options(T... options) {
        return new NeedsOptions<>(NEList.of(options));
    }

    @RequiredArgsConstructor
    public class NeedsOptions<T> {
        private final NEList<T> options;

        public NeedsMapper<T> defaultOption(@Nullable T defaultOption) {
            return new NeedsMapper<>(options, Optional.ofNullable(defaultOption));
        }

        public NeedsMapper<T> requireAnswer() {
            return new NeedsMapper<>(options, Optional.empty());
        }
    }

    @RequiredArgsConstructor
    public class NeedsMapper<T> {
        private final NEList<T> options;
        private final Optional<T> defaultOption;

        public NeedsFallback<T> mapper(Function<T, String> mapper) {
            return new NeedsFallback<>(options, defaultOption, mapper);
        }
    }

    @RequiredArgsConstructor
    public class NeedsFallback<T> {
        private final NEList<T> options;
        private final Optional<T> defaultOption;
        private final Function<T, String> mapper;

        public NeedsFix<T> fallbackFlag(String flag) {
            return new NeedsFix<>(options, defaultOption, mapper, ctx.highlight(flag) + " flag");
        }

        public NeedsFix<T> fallbackIndex(int index) {
            return new NeedsFix<>(options, defaultOption, mapper, "parameter at index " + ctx.highlight(index));
        }
    }

    @RequiredArgsConstructor
    public class NeedsFix<T> {
        private final NEList<T> options;
        private final Optional<T> defaultOption;
        private final Function<T, String> mapper;
        private final String fallback;

        public NeedsClearAfterSelection<T> fix(Iterable<String> originalArgs, String newArg) {
            return new NeedsClearAfterSelection<>(options, defaultOption, mapper, fallback, Pair.of(originalArgs, newArg));
        }
    }

    @RequiredArgsConstructor
    public class NeedsClearAfterSelection<T> {
        private final NEList<T> options;
        private final Optional<T> defaultOption;
        private final Function<T, String> mapper;
        private final String fallback;
        private final Pair<? extends Iterable<String>, String> fix;

        public T clearAfterSelection() {
            return clearAfterSelection(true);
        }

        public T dontClearAfterSelection() {
            return clearAfterSelection(false);
        }

        private T clearAfterSelection(boolean clearAfterSelection) {
            return new CLIPrompter(ctx, noInput).select(
                prompt,
                options,
                defaultOption,
                mapper,
                fallback,
                fix,
                clearAfterSelection
            );
        }
    }
}
