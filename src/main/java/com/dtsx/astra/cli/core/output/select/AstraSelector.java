package com.dtsx.astra.cli.core.output.select;

import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import lombok.val;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class AstraSelector {
    private final List<SelectionStrategy.Meta> strategies;
    
    public AstraSelector() {
        this.strategies = List.of(
            new ArrowKeySelectionStrategy.Meta(),
            new NumberedSelectionStrategy.Meta()
        );
    }

    public <T> SelectStatus<T> select(String prompt, NEList<String> options, Optional<String> defaultOption, Function<String, T> mapper, String fallbackFlag, boolean clearAfterSelection) {
        for (val meta : strategies) {
            if (meta.isSupported()) {
                var strategy = meta.mkInstance(prompt, options, defaultOption, mapper);
                return strategy.select(clearAfterSelection);
            }
        }

        throw new AstraCliException("");
    }
}
