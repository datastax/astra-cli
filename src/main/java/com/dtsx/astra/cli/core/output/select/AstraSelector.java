package com.dtsx.astra.cli.core.output.select;

import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.output.Hint;
import com.dtsx.astra.cli.core.output.output.OutputType;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.graalvm.collections.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.dtsx.astra.cli.core.output.ExitCode.ILLEGAL_OPERATION;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

public class AstraSelector {
    private static final List<SelectionStrategy.Meta> STRATEGIES = List.of(
        new ArrowKeySelectionStrategy.Meta()
    );
    
    public <T> Optional<T> select(String prompt, NEList<T> options, Optional<T> defaultOption, Function<T, String> mapper, String fallback, Pair<? extends Iterable<String>, String> fix, boolean clearAfterSelection) {
        if (!AstraConsole.isTty()) {
            throw new AstraCliException(ILLEGAL_OPERATION, """
              @|bold,red Error: Can not interactively select an option when the program is not running interactively|@
            
              Please programmatically pass an option using the %s.
            """.formatted(fallback), List.of(
                new Hint("Example fix", fix.getLeft(), fix.getRight())
            ));
        }

        if (OutputType.isNotHuman()) {
            throw new AstraCliException(ILLEGAL_OPERATION, """
              @|bold,red Error: Can not interactively select an option when the output type is not 'human'|@
            
              Please programmatically pass an option using the %s, or use the 'human' output format instead.
            """.formatted(fallback), List.of(
                new Hint("Example fix", fix.getLeft(), fix.getRight())
            ));
        }

        val stringOptions = options.map(mapper);
        val defaultStringOption = defaultOption.map(mapper);
        
        for (val meta : STRATEGIES) {
            if (meta.isSupported()) {
                var strategy = meta.mkInstance(
                    new SelectionStrategy.SelectionRequest<>(prompt, stringOptions, defaultStringOption, 
                        str -> options.stream().filter(opt -> mapper.apply(opt).equals(str)).findFirst().orElse(null), 
                        clearAfterSelection)
                );

                return strategy.select();
            }
        }

        throw new AstraCliException("");
    }

    @RequiredArgsConstructor
    public static class Builder {
        private final String prompt;

        public <T> NeedsOptionsOrMapper<T> options(NEList<T> options) {
            return new NeedsOptionsOrMapper<>(options);
        }

        @SafeVarargs
        public final <T> NeedsOptionsOrMapper<T> options(T... options) {
            return new NeedsOptionsOrMapper<>(NEList.of(options));
        }

        @RequiredArgsConstructor
        public class NeedsOptionsOrMapper<T> {
            private final NEList<T> options;

            public NeedsMapper<T> defaultOption(@Nullable T defaultOption) {
                return new NeedsMapper<>(options, Optional.ofNullable(defaultOption));
            }

            public NeedsFallback<T> mapper(Function<T, String> mapper) {
                return new NeedsFallback<>(options, Optional.empty(), mapper);
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
                return new NeedsFix<>(options, defaultOption, mapper, highlight(flag) + " flag");
            }

            public NeedsFix<T> fallbackIndex(int index) {
                return new NeedsFix<>(options, defaultOption, mapper, "parameter at index " + highlight(index));
            }
        }

        @RequiredArgsConstructor
        public class NeedsFix<T> {
            private final NEList<T> options;
            private final Optional<T> defaultOption;
            private final Function<T, String> mapper;
            private final String fallback;

            public NeedsClearAfterSelection<T> fix(Iterable<String> originalArgs, String newArg) {
                return new NeedsClearAfterSelection<>(options, defaultOption, mapper, fallback, Pair.create(originalArgs, newArg));
            }
        }

        @RequiredArgsConstructor
        public class NeedsClearAfterSelection<T> {
            private final NEList<T> options;
            private final Optional<T> defaultOption;
            private final Function<T, String> mapper;
            private final String fallback;
            private final Pair<? extends Iterable<String>, String> fix;

            public Optional<T> clearAfterSelection() {
                return clearAfterSelection(true);
            }

            public Optional<T> dontClearAfterSelection() {
                return clearAfterSelection(false);
            }

            private Optional<T> clearAfterSelection(boolean clearAfterSelection) {
                return new AstraSelector().select(
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
}
