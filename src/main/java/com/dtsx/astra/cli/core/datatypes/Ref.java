package com.dtsx.astra.cli.core.datatypes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Ref<T> {
    private T value;

    private final List<Consumer<T>> listeners = new ArrayList<>();

    public Ref(Function<Supplier<T>, T> ctxFn) {
        this.value = ctxFn.apply(this::get);
    }

    public T get() {
        return value;
    }

    public Ref<T> modify(Function<T, T> fn) {
        value = fn.apply(value);
        listeners.forEach(l -> l.accept(value));
        return this;
    }

    public void onUpdate(Consumer<T> listener) {
        listeners.add(listener);
    }

    public void nowAndOnUpdate(Consumer<T> listener) {
        listener.accept(value);
        listeners.add(listener);
    }
}
