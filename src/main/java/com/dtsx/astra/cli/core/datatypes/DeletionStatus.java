package com.dtsx.astra.cli.core.datatypes;

public sealed interface DeletionStatus<T> {
    record Deleted<T>(T value) implements DeletionStatus<T> {}
    record NotFound<T>(T value) implements DeletionStatus<T> {}

    static <T> DeletionStatus<T> deleted(T value) {
        return new Deleted<>(value);
    }

    static <T> DeletionStatus<T> notFound(T value) {
        return new NotFound<>(value);
    }
}
