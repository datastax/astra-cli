package com.dtsx.astra.cli.core.output.prompters;

import com.dtsx.astra.cli.core.CliEnvironment;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.core.output.prompters.SelectionStrategy.Meta.Closed;
import com.dtsx.astra.cli.core.output.prompters.SelectionStrategy.Meta.Open;
import com.dtsx.astra.cli.core.output.prompters.strategies.ArrowKeySelectionStrategy;
import com.dtsx.astra.cli.core.output.prompters.strategies.NumberedSelectionStrategy;
import com.dtsx.astra.cli.core.output.prompters.strategies.TextSelectionStrategy;
import lombok.val;
import org.graalvm.collections.Pair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.core.output.ExitCode.UNSUPPORTED_EXECUTION;
import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

public class CLIPrompter {
    public static <T> T prompt(String prompt, boolean noInput, Optional<String> defaultOption, Function<String, T> mapper, boolean echoOff, Function<String, String> displayContentWhenDone, String fallback, Pair<? extends Iterable<String>, String> fix, boolean clearAfterSelection) {
        val strategies = NEList.of(
            new TextSelectionStrategy.Meta()
        );

        return runPrompter(strategies, prompt, noInput, fallback, fix, (meta, updatedPrompt) -> {
            return meta.mkInstance(
                new PromptRequest.Open<>(updatedPrompt, defaultOption, mapper, clearAfterSelection, echoOff, displayContentWhenDone)
            ).select();
        });
    }

    public static boolean confirm(String prompt, boolean noInput, Optional<Boolean> defaultOption, String fallback, Pair<? extends Iterable<String>, String> fix, boolean clearAfterSelection) {
        val strategies = NEList.of(
            new ArrowKeySelectionStrategy.Meta(),
            new TextSelectionStrategy.Meta()
        );

        return runPrompter(strategies, prompt, noInput, fallback, fix, (meta, updatedPrompt) -> {
            val defaultOptionStr = defaultOption.map(b -> b ? "yes" : "no");

            return switch (meta) {
                case Closed closed -> {
                    val options = new LinkedHashMap<String, Boolean>() {{
                        put("yes", true);
                        put("no", false);
                    }};

                    yield closed.mkInstance(
                        new PromptRequest.Closed<>(updatedPrompt, defaultOptionStr, options::get, clearAfterSelection, NEList.of(options.keySet()), true)
                    ).select();
                }
                case Open open -> {
                    val yeses = List.of("y", "yes", "true", "1", "ok");

                    val promptSuffix = highlight(
                        defaultOption
                            .map(d -> d ? "[Y/n]" : "[y/N]")
                            .orElse("[y/n]")
                    );

                    yield open.mkInstance(
                        new PromptRequest.Open<>(updatedPrompt + " " + promptSuffix, defaultOptionStr, r -> yeses.contains(r.trim().toLowerCase()), clearAfterSelection, false, null)
                    ).select();
                }
            };
        });
    }

    public static <T> T select(String prompt, boolean noInput, NEList<T> options, Optional<T> defaultOption, Function<T, String> mapper, String fallback, Pair<? extends Iterable<String>, String> fix, boolean clearAfterSelection) {
        val stringOptions = options.map(mapper);
        val defaultStringOption = defaultOption.map(mapper);

        Function<String, T> reverseMapper = (str) -> options.stream().filter(opt -> mapper.apply(opt).equals(str)).findFirst().orElse(null);

        val strategies = NEList.of(
            new ArrowKeySelectionStrategy.Meta(),
            new NumberedSelectionStrategy.Meta()
        );

        return runPrompter(strategies, prompt, noInput, fallback, fix, (meta, updatedPrompt) -> {
            return meta.mkInstance(
                new PromptRequest.Closed<>(updatedPrompt, defaultStringOption, reverseMapper, clearAfterSelection, stringOptions, false)
            ).select();
        });
    }

    private static <M extends SelectionStrategy.Meta, T> T runPrompter(NEList<M> strategies, String prompt, boolean noInput, String fallback, Pair<? extends Iterable<String>, String> fix, BiFunction<M, String, Optional<T>> run) {
        assertCanPrompt(noInput, fallback, fix);

        for (val meta : strategies) {
            if (meta.isSupported()) {
                val updatedPrompt = AstraConsole.format(trimIndent(prompt));
                return run.apply(meta, updatedPrompt).orElseThrow(() -> noAnswerGiven(fallback, fix));
            }
        }

        throw noStrategyFound(fallback, fix);
    }

    private static void assertCanPrompt(boolean noInput, String fallback, Pair<? extends Iterable<String>, String> fix) {
        val hint = new Hint("Example fix", fix.getLeft(), fix.getRight());

        if (noInput) {
            throw new AstraCliException(UNSUPPORTED_EXECUTION, """
              @|bold,red Error: Can not interactively select an option when the `--no-input` flag is set|@
            
              Please programmatically pass an option using the %s, or interactively run the program instead.
            """.formatted(fallback), List.of(hint));
        }

        if (!CliEnvironment.isTty()) {
            throw new AstraCliException(UNSUPPORTED_EXECUTION, """
              @|bold,red Error: Can not interactively select an option when the program is not running interactively|@
            
              Please programmatically pass an option using the %s, or interactively run the program instead.
            """.formatted(fallback), List.of(hint));
        }

        if (OutputType.isNotHuman()) {
            throw new AstraCliException(UNSUPPORTED_EXECUTION, """
              @|bold,red Error: Can not interactively select an option when the output type is not 'human'|@
            
              Please programmatically pass an option using the %s, or use the 'human' output format instead.
            """.formatted(fallback), List.of(hint));
        }
    }

    private static AstraCliException noAnswerGiven(String fallback, Pair<? extends Iterable<String>, String> fix) {
        return new AstraCliException(UNSUPPORTED_EXECUTION, """
          @|bold,red Error: No input provided and no default option is available|@
        
          Please interactively answer the question, or programmatically pass an option using the %s.
        """.formatted(fallback), List.of(
            new Hint("Potential fix", fix.getLeft(), fix.getRight())
        ));
    }

    private static AstraCliException noStrategyFound(String fallback, Pair<? extends Iterable<String>, String> fix) {
        return new AstraCliException(UNSUPPORTED_EXECUTION, """
          @|bold,red Error: No interactive selection strategy is supported on this terminal|@
        
          Please programmatically pass an option using the %s.
        """.formatted(fallback), List.of(
            new Hint("Potential fix", fix.getLeft(), fix.getRight())
        ));
    }
}
