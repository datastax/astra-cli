package com.dtsx.astra.cli.core.output.select;

import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.CliExceptionCode;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.output.Hint;
import com.dtsx.astra.cli.core.output.output.OutputType;
import lombok.val;
import org.graalvm.collections.Pair;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.ILLEGAL_OPERATION;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

public class AstraSelector {
    private final List<SelectionStrategy.Meta> strategies;
    
    public AstraSelector() {
        this.strategies = List.of(
            new ArrowKeySelectionStrategy.Meta()
//            new NumberedSelectionStrategy.Meta()
        );
    }

    public <T> Optional<T> select(String prompt, NEList<String> options, Optional<String> defaultOption, Function<String, T> mapper, String fallback, Pair<? extends Iterable<String>, String> fix, boolean clearAfterSelection) {
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

        for (val meta : strategies) {
            if (meta.isSupported()) {
                var strategy = meta.mkInstance(
                    new SelectionStrategy.SelectionRequest<>(prompt, options, defaultOption, mapper, clearAfterSelection)
                );

                return strategy.select();
            }
        }

        throw new AstraCliException("");
    }
}
