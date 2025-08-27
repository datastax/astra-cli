package com.dtsx.astra.cli.testlib.laws;

import lombok.val;
import net.jqwik.api.*;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public interface Functor<F> extends Kind1<Object> {
    interface LiftFn<F, A> {
        F lift(A value);
    }

    interface MapFn<F, A, B> {
        F map(F functor, Function<A, B> fn);
    }

    record Params<F>(
        LiftFn<F, Object> liftFn,
        MapFn<F, Object, Object> mapFn
    ) {}

    Params<F> functor();

    @Property
    default void assert_functor_identity(@ForAll("anyA") Object value) {
        val lifted = lift(value);

        assertThat(map(lifted, a -> a)).isEqualTo(lifted); // fmap id == id
    }

    @Property
    default void assert_functor_composition(@ForAll int value, @ForAll("intFunction") Function<Integer, Integer> f, @ForAll("intFunction") Function<Integer, Integer> g) {
        var lifted = lift(value);

        var mappedCompose = map(lifted, f.compose(g)); // fmap (f . g) == fmap f . fmap g
        var mappedSeparate = map(map(lifted, g), f);

        assertThat(mappedCompose).isEqualTo(mappedSeparate);
    }

    private <A> F lift(A a) {
        return functor().liftFn().lift(a);
    }

    @SuppressWarnings("unchecked")
    private <A, B> F map(F f, Function<A, B> fn) {
        return functor().mapFn().map(f, (Function<Object, Object>) fn);
    }

    static <F> Params<F> params(LiftFn<F, Object> mkValue, MapFn<F, Object, Object> mapFn) {
        return new Params<>(mkValue, mapFn);
    }

    @Provide
    default Arbitrary<Function<Integer, Integer>> intFunction() {
        return Arbitraries.of(
            (x) -> x,
            (x) -> x + 1,
            (x) -> x * 2,
            (x) -> x - 5,
            (x) -> x / 2,
            (x) -> x * x
        );
    }

    @Override
    default Arbitrary<Object> anyA() {
        return LawUtils.anyT(getClass(), Kind1.class, 0);
    }
}
