package com.dtsx.astra.cli.utils;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@UtilityClass
public class MiscUtils {
    public static <A> Set<A> setAdd(Set<A> init, A a) {
        val newSet = new HashSet<>(init);
        newSet.add(a);
        return newSet;
    }

    public static <A> Set<A> setDel(Set<A> init, A a) {
        val newSet = new HashSet<>(init);
        newSet.remove(a);
        return newSet;
    }

    public static <T, R> Function<T, R> toFn(Consumer<T> r) {
        return t -> {
            r.accept(t);
            return null;
        };
    }

    public static <T1, T2, R> BiFunction<T1, T2, R> toFn(BiConsumer<T1, T2> r) {
        return (t1, t2) -> {
            r.accept(t1, t2);
            return null;
        };
    }
}
