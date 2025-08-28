package com.dtsx.astra.cli.core.output.prompters;

import com.dtsx.astra.cli.core.CliContext;

import java.util.Optional;

public interface SelectionStrategy<T> {
    Optional<T> select();

    sealed interface Meta {
        boolean isSupported(CliContext ctx);

        non-sealed interface Open extends Meta {
            <T> SelectionStrategy<T> mkInstance(CliContext ctx, PromptRequest.Open<T> request);
        }

        non-sealed interface Closed extends Meta {
            <T> SelectionStrategy<T> mkInstance(CliContext ctx, PromptRequest.Closed<T> request);
        }
    }
}
