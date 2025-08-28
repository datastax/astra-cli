package com.dtsx.astra.cli.core.datatypes;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Delegate;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@ToString
@EqualsAndHashCode
public class NEList<E> implements List<E> {
    @Delegate
    private final List<E> delegate;

    private NEList(List<E> delegate) {
        this.delegate = Collections.unmodifiableList(delegate);
    }

    public static <E> Optional<NEList<E>> parse(@NonNull Collection<E> list) {
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new NEList<>(List.copyOf(list)));
    }

    @SafeVarargs
    public static <T> NEList<T> of(@NonNull T... elements) {
        if (elements.length == 0) {
            throw new IllegalArgumentException("Attempted to create an NEList from an empty array");
        }
        return new NEList<>(List.of(elements));
    }

    public <R> NEList<R> map(Function<E, R> mapper) {
        return new NEList<>(stream().map(mapper).toList());
    }
}
