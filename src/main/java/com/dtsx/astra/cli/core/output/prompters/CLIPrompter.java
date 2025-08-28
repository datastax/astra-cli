package com.dtsx.astra.cli.core.output.prompters;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.prompters.SelectionStrategy.Meta.Closed;
import com.dtsx.astra.cli.core.output.prompters.SelectionStrategy.Meta.Open;
import com.dtsx.astra.cli.core.output.prompters.strategies.ArrowKeySelectionStrategy;
import com.dtsx.astra.cli.core.output.prompters.strategies.NumberedSelectionStrategy;
import com.dtsx.astra.cli.core.output.prompters.strategies.TextSelectionStrategy;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.graalvm.collections.Pair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.dtsx.astra.cli.core.output.ExitCode.UNSUPPORTED_EXECUTION;
import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

@RequiredArgsConstructor
public class CLIPrompter {
    private final CliContext ctx;
    private final boolean noInput;
    
    public <T> T prompt(String prompt, Optional<String> defaultOption, Function<String, T> mapper, boolean echoOff, Function<String, String> displayContentWhenDone, String fallback, Pair<? extends Iterable<String>, String> fix, boolean clearAfterSelection) {
        val strategies = NEList.of(
            new TextSelectionStrategy.Meta()
        );

        return runPrompter(strategies, prompt, fallback, fix, (meta, updatedPrompt) -> {
            return meta.mkInstance(
                ctx, new PromptRequest.Open<>(updatedPrompt, defaultOption, mapper, clearAfterSelection, echoOff, displayContentWhenDone)
            ).select();
        });
    }

    public boolean confirm(String prompt, Optional<Boolean> defaultOption, String fallback, Pair<? extends Iterable<String>, String> fix, boolean clearAfterSelection) {
        val strategies = NEList.of(
            new ArrowKeySelectionStrategy.Meta(),
            new TextSelectionStrategy.Meta()
        );

        return runPrompter(strategies, prompt, fallback, fix, (meta, updatedPrompt) -> {
            val defaultOptionStr = defaultOption.map(b -> b ? "yes" : "no");

            return switch (meta) {
                case Closed closed -> {
                    val options = new LinkedHashMap<String, Boolean>() {{
                        put("yes", true);
                        put("no", false);
                    }};

                    yield closed.mkInstance(
                        ctx, new PromptRequest.Closed<>(updatedPrompt, defaultOptionStr, options::get, clearAfterSelection, NEList.parse(options.keySet()).orElseThrow(), true)
                    ).select();
                }
                case Open open -> {
                    val yeses = List.of("y", "yes", "true", "1", "ok");

                    val promptSuffix = ctx.highlight(
                        defaultOption
                            .map(d -> d ? "[Y/n]" : "[y/N]")
                            .orElse("[y/n]")
                    );

                    yield open.mkInstance(
                        ctx, new PromptRequest.Open<>(updatedPrompt + " " + promptSuffix, defaultOptionStr, r -> yeses.contains(r.trim().toLowerCase()), clearAfterSelection, false, null)
                    ).select();
                }
            };
        });
    }

    public <T> T select(String prompt, NEList<T> options, Optional<T> defaultOption, Function<T, String> mapper, String fallback, Pair<? extends Iterable<String>, String> fix, boolean clearAfterSelection) {
        val stringOptions = options.map(mapper);
        val defaultStringOption = defaultOption.map(mapper);

        Function<String, T> reverseMapper = (str) -> options.stream().filter(opt -> mapper.apply(opt).equals(str)).findFirst().orElse(null);

        val strategies = NEList.of(
            new ArrowKeySelectionStrategy.Meta(),
            new NumberedSelectionStrategy.Meta()
        );

        return runPrompter(strategies, prompt, fallback, fix, (meta, updatedPrompt) -> {
            return meta.mkInstance(
                ctx, new PromptRequest.Closed<>(updatedPrompt, defaultStringOption, reverseMapper, clearAfterSelection, stringOptions, false)
            ).select();
        });
    }

    private <M extends SelectionStrategy.Meta, T> T runPrompter(NEList<M> strategies, String prompt, String fallback, Pair<? extends Iterable<String>, String> fix, BiFunction<M, String, Optional<T>> run) {
        assertCanPrompt(fallback, fix);

        for (val meta : strategies) {
            if (meta.isSupported(ctx)) {
                val updatedPrompt = ctx.console().format(trimIndent(prompt));
                return run.apply(meta, updatedPrompt).orElseThrow(() -> noAnswerGiven(fallback, fix));
            }
        }

        throw noStrategyFound(fallback, fix);
    }

    private void assertCanPrompt(String fallback, Pair<? extends Iterable<String>, String> fix) {
        val hint = new Hint("Example fix", fix.getLeft(), fix.getRight());

        if (noInput) {
            throw new AstraCliException(UNSUPPORTED_EXECUTION, """
              @|bold,red Error: Can not interactively select an option when the `--no-input` flag is set|@
            
              Please programmatically pass an option using the %s, or interactively run the program instead.
            """.formatted(fallback), List.of(hint));
        }

        if (ctx.isNotTty()) {
            throw new AstraCliException(UNSUPPORTED_EXECUTION, """
              @|bold,red Error: Can not interactively select an option when the program is not running interactively|@
            
              Please programmatically pass an option using the %s, or interactively run the program instead.
            """.formatted(fallback), List.of(hint));
        }

        if (ctx.outputIsNotHuman()) {
            throw new AstraCliException(UNSUPPORTED_EXECUTION, """
              @|bold,red Error: Can not interactively select an option when the output type is not 'human'|@
            
              Please programmatically pass an option using the %s, or use the 'human' output format instead.
            """.formatted(fallback), List.of(hint));
        }
    }

    private AstraCliException noAnswerGiven(String fallback, Pair<? extends Iterable<String>, String> fix) {
        return new AstraCliException(UNSUPPORTED_EXECUTION, """
          @|bold,red Error: No input provided and no default option is available|@
        
          Please interactively answer the question, or programmatically pass an option using the %s.
        """.formatted(fallback), List.of(
            new Hint("Potential fix", fix.getLeft(), fix.getRight())
        ));
    }

    private AstraCliException noStrategyFound(String fallback, Pair<? extends Iterable<String>, String> fix) {
        return new AstraCliException(UNSUPPORTED_EXECUTION, """
          @|bold,red Error: No interactive selection strategy is supported on this terminal|@
        
          Please programmatically pass an option using the %s.
        """.formatted(fallback), List.of(
            new Hint("Potential fix", fix.getLeft(), fix.getRight())
        ));
    }
}
