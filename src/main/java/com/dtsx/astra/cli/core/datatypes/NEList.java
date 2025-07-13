package com.dtsx.astra.cli.core.datatypes;

import lombok.NonNull;
import lombok.experimental.Delegate;

import java.util.Collections;
import java.util.List;

public class NEList<E> implements List<E> {
    @Delegate
    private final List<E> delegate;

    private NEList(List<E> delegate) {
        this.delegate = Collections.unmodifiableList(delegate);
    }

    public static <E> NEList<E> of(@NonNull List<E> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Attempted to create an NEList from an empty list");
        }
        return new NEList<>(list);
    }

    @SafeVarargs
    public static <T> NEList<T> of(@NonNull T... elements) {
        if (elements.length == 0) {
            throw new IllegalArgumentException("Attempted to create an NEList from an empty array");
        }
        return new NEList<>(List.of(elements));
    }
}
