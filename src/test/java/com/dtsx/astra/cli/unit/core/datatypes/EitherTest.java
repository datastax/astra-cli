package com.dtsx.astra.cli.unit.core.datatypes;

import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.testlib.AssertUtils;
import com.dtsx.astra.cli.testlib.laws.Monad;
import lombok.val;
import net.jqwik.api.ForAll;
import net.jqwik.api.Group;
import net.jqwik.api.Property;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Group
public class EitherTest implements Monad<Either<Object, Object>> {
    @Group
    public class factories {
        @Property
        public <T> void left_lifts_anything_into_either(@ForAll T t) {
            val left = Either.left(t);

            assertThat(left).isInstanceOf(Either.Left.class);
            assertThat(((Either.Left<T, ?>) left).unwrap()).isEqualTo(t);
        }

        @Property
        public <T> void right_lifts_anything_into_either(@ForAll T t) {
            val right = Either.right(t);

            assertThat(right).isInstanceOf(Either.Right.class);
            assertThat(((Either.Right<?, T>) right).unwrap()).isEqualTo(t);
        }

        @Property
        public <T> void tryCatch_returns_right_on_success(@ForAll T t) {
            val either = Either.tryCatch(() -> t, Function.identity());

            assertThat(either).isInstanceOf(Either.Right.class);
            assertThat(((Either.Right<?, T>) either).unwrap()).isEqualTo(t);
        }

        @Property
        public void tryCatch_returns_left_on_exception(@ForAll Exception e) {
            val either = Either.tryCatch(() -> { throw e; }, Function.identity());

            assertThat(either).isInstanceOf(Either.Left.class);
            assertThat(((Either.Left<Exception, ?>) either).unwrap()).isEqualTo(e);
        }
    }

    @Group
    public class fold {
        @Property
        public <L, R> void fold_dispatches_to_left(@ForAll L l) {
            val either = Either.<L, R>left(l);

            val result = either.fold(
                leftVal -> "Left: " + leftVal.toString(),
                _ -> fail("Right should not be called")
            );

            assertThat(result).isEqualTo("Left: " + l.toString());
        }

        @Property
        public <L, R> void fold_dispatches_to_right(@ForAll R r) {
            val either = Either.<L, R>right(r);

            val result = either.fold(
                _ -> fail("Left should not be called"),
                rightVal -> "Right: " + rightVal.toString()
            );

            assertThat(result).isEqualTo("Right: " + r.toString());
        }
    }

    @Group
    public class getters {
        @Property
        public <L, R> void getRight_returns_value_on_right(@ForAll R r, @ForAll Exception e) throws Exception {
            val either = Either.<L, R>right(r);

            val result = either.getRight(_ -> e);

            assertThat(result).isEqualTo(r);
        }

        @Property
        public <L, R> void getRight_throws_exception_on_left(@ForAll L l) {
            val either = Either.<L, R>left(l);

            try {
                either.getRight(leftVal -> new Exception("Left value: " + leftVal.toString()));
            } catch (Exception e) {
                assertThat(e.getMessage()).isEqualTo("Left value: " + l.toString());
            }
        }

        @Property
        public <L, R> void getLeft_returns_value_on_left(@ForAll L l, @ForAll Exception e) throws Exception {
            val either = Either.<L, R>left(l);

            val result = either.getLeft(_ -> e);

            assertThat(result).isEqualTo(l);
        }

        @Property
        public <L, R> void getLeft_throws_exception_on_right(@ForAll R r) {
            val either = Either.<L, R>right(r);

            try {
                either.getLeft(rightVal -> new Exception("Right value: " + rightVal.toString()));
            } catch (Exception e) {
                assertThat(e.getMessage()).isEqualTo("Right value: " + r.toString());
            }
        }
    }

    @Group
    public class isX {
        @Property
        public <L, R> void isLeft_returns_true_for_left(@ForAll L l) {
            val either = Either.<L, R>left(l);

            assertThat(either.isLeft()).isTrue();
            assertThat(either.isRight()).isFalse();
        }

        @Property
        public <L, R> void isRight_returns_true_for_right(@ForAll R r) {
            val either = Either.<L, R>right(r);

            assertThat(either.isRight()).isTrue();
            assertThat(either.isLeft()).isFalse();
        }
    }

    @Group
    public class bimap {
        @Property
        public <L, R, L2> void bimap_maps_left(@ForAll L initL) {
            val either = Either.<L, R>left(initL);

            val result = either.bimap(l -> "Left: " + l, AssertUtils::assertNotCalled);

            assertThat(result)
                .isInstanceOf(Either.Left.class)
                .isEqualTo(Either.left("Left: " + initL));
        }

        @Property
        public <L, R, R2> void bimap_maps_right(@ForAll R initR) {
            val either = Either.<L, R>right(initR);

            val result = either.bimap(AssertUtils::assertNotCalled, r -> "Right: " + r);

            assertThat(result)
                .isInstanceOf(Either.Right.class)
                .isEqualTo(Either.right("Right: " + initR));
        }
    }

    @Override
    public Monad.Params<Either<Object, Object>> monad() {
        return Monad.params(Either::right, Either::flatMap, Either::map);
    }
}
