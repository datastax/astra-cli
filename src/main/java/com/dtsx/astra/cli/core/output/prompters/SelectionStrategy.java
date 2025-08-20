package com.dtsx.astra.cli.core.output.prompters;

import java.util.Optional;

public interface SelectionStrategy<T> {
    Optional<T> select();

    sealed interface Meta {
        boolean isSupported();

        non-sealed interface Open extends Meta {
            <T> SelectionStrategy<T> mkInstance(PromptRequest.Open<T> request);
        }

        non-sealed interface Closed extends Meta {
            <T> SelectionStrategy<T> mkInstance(PromptRequest.Closed<T> request);
        }
    }
}
