package com.dtsx.astra.cli.core.datatypes;

public sealed interface CreationStatus<T> {
    record Created<T>(T value) implements CreationStatus<T> {}
    record AlreadyExists<T>(T value) implements CreationStatus<T> {}

    static <T> CreationStatus<T> created(T value) {
        return new Created<>(value);
    }

    static <T> CreationStatus<T> alreadyExists(T value) {
        return new AlreadyExists<>(value);
    }
}
