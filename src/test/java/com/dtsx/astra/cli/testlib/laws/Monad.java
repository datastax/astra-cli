package com.dtsx.astra.cli.testlib.laws;

import lombok.val;
import net.jqwik.api.*;

import java.util.function.Function;

import static com.dtsx.astra.cli.testlib.AssertUtils.assertEquals;

public interface Monad<F> extends Functor<F> {
    interface FlatMapFn<F, A> {
        F flatMap(F fa, Function<A, F> f); // 'B' is implicitly wrapped in F since we don't have actual HKTs
    }

    record Params<F>(
        LiftFn<F, Object> liftFn,
        FlatMapFn<F, Object> flatMapFn,
        MapFn<F, Object, Object> mapFn
    ) {}

    Params<F> monad();

    @Property
    default void assert_monad_left_identity(@ForAll("anyA") Object a, @ForAll("flatMapFunction") Function<Object, F> f) {
        assertEquals(bind(lift(a), f), f.apply(a)); // pure a >>= f == f a
    }

    @Property
    default void assert_monad_right_identity(@ForAll("anyA") Object value) {
        val m = lift(value);

        assertEquals(bind(m, this::lift), m); // m >>= pure == m
    }

    @Property
    default void assert_monad_associativity(@ForAll("anyA") Object value, @ForAll("flatMapFunction") Function<Object, F> f, @ForAll("flatMapFunction") Function<Object, F> g) {
        val m = lift(value);

        assertEquals(bind(bind(m, f), g), bind(m, x -> bind(f.apply(x), g))); // (m >>= f) >>= g == m >>= (\x -> f x >>= g)
    }

    private <A> F lift(A a) {
        return monad().liftFn().lift(a);
    }

    @SuppressWarnings("unchecked")
    private <A> F bind(F f, Function<A, F> fn) {
        return monad().flatMapFn().flatMap(f, (Function<Object, F>) fn);
    }

    static <F> Monad.Params<F> params(LiftFn<F, Object> unitFn, FlatMapFn<F, Object> flatMapFn, MapFn<F, Object, Object> mapFn) {
        return new Monad.Params<>(unitFn, flatMapFn, mapFn);
    }

    @Override
    default Functor.Params<F> functor() {
        return Functor.params(monad().liftFn(), monad().mapFn());
    }

    @Provide
    default Arbitrary<Function<Object, F>> flatMapFunction() {
        return Arbitraries.integers().between(1, 100).map(n -> (_) -> lift(n));
    }
}
