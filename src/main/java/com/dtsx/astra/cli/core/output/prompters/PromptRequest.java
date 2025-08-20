package com.dtsx.astra.cli.core.output.prompters;

import com.dtsx.astra.cli.core.datatypes.NEList;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Optional;
import java.util.function.Function;

public abstract class PromptRequest {
    @Value
    @Accessors(fluent = true)
    public static class Open<T> {
        String prompt;
        Optional<String> defaultOption;
        Function<String, T> mapper;
        boolean clearAfterSelection;
        boolean echoOff;
        Function<String, String> displayContentWhenDone;
    }

    @Value
    @Accessors(fluent = true)
    public static class Closed<T> {
        String prompt;
        Optional<String> defaultOption;
        Function<String, T> mapper;
        boolean clearAfterSelection;
        NEList<String> options;
        boolean labelDefault;
    }
}
