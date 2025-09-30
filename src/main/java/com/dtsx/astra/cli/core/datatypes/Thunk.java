package com.dtsx.astra.cli.core.datatypes;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class Thunk<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private Optional<T> cache = Optional.empty();

    public T get() {
        if (cache.isEmpty()) {
            cache = Optional.ofNullable(supplier.get());
        }
        return cache.orElseThrow();
    }
}
