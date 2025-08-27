package com.dtsx.astra.cli.testlib.laws;

import lombok.val;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public interface Idempotent<A> extends Kind1<A> {
    record Params<A>(Function<A, Function<A, A>> mkFn) {}

    Params<A> idempotency();

    @Property
    default void assert_idempotent(@ForAll("anyA") A initial) {
        val fn = idempotency().mkFn.apply(initial);

        var value = fn.apply(initial);

        for (int i = 0; i < 5; i++) {
            var nextValue = fn.apply(value);
            assertThat(nextValue).isEqualTo(value);
            value = nextValue;
        }
    }

    static <A> Idempotent.Params<A> fn(Function<A, A> fn) {
        return new Idempotent.Params<>(_ -> fn);
    }

    static <A> Idempotent.Params<A> mkFn(Function<A, Function<A, A>> mkFn) {
        return new Idempotent.Params<>(mkFn);
    }

    @Override
    default Arbitrary<A> anyA() {
        return LawUtils.anyT(getClass(), Idempotent.class, 0);
    }
}
