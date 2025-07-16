package com.dtsx.astra.cli.core.output.select;

import java.util.Optional;

public sealed interface SelectStatus<T> {
    record Selected<T>(T value) implements SelectStatus<T> {}
    record NoAnswer<T>() implements SelectStatus<T> {}

    default Optional<T> toOptional() {
        if (this instanceof Selected<T>(T value)) {
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }
}
