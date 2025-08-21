package com.dtsx.astra.cli.core.datatypes;

import java.util.concurrent.Callable;
import java.util.function.Function;

public sealed interface Either<L, R> {
    record Left<L, R>(L unwrap) implements Either<L, R> {}
    record Right<L, R>(R unwrap) implements Either<L, R> {}

    default <U> U fold(Function<L, U> leftMapper, Function<R, U> rightMapper) {
        return switch (this) {
            case Left<L, R> left -> leftMapper.apply(left.unwrap);
            case Right<L, R> right -> rightMapper.apply(right.unwrap);
        };
    }

    default <E extends Exception> R getRight(Function<L, E> exceptionSupplier) throws E {
        return switch (this) {
            case Left<L, R> left -> throw exceptionSupplier.apply(left.unwrap);
            case Right<L, R> right -> right.unwrap;
        };
    }

    default <E extends Exception> L getLeft(Function<R, E> exceptionSupplier) throws E {
        return switch (this) {
            case Left<L, R> left -> left.unwrap;
            case Right<L, R> right -> throw exceptionSupplier.apply(right.unwrap);
        };
    }

    default R getRight() {
        return getRight((_) -> new RuntimeException("Expected Right but found Left"));
    }

    default L getLeft() {
        return getLeft((_) -> new RuntimeException("Expected Left but found Right"));
    }

    default <L2, R2> Either<L2, R2> bimap(Function<L, L2> leftMapper, Function<R, R2> rightMapper) {
        return switch (this) {
            case Left<L, R> left -> new Left<>(leftMapper.apply(left.unwrap));
            case Right<L, R> right -> new Right<>(rightMapper.apply(right.unwrap));
        };
    }

    default <R2> Either<L, R2> map(Function<R, R2> rightMapper) {
        return bimap(left -> left, rightMapper);
    }


    default L foldMap(Function<R, L> rightMapper) {
        return switch (this) {
            case Left<L, R> left -> left.unwrap;
            case Right<L, R> right -> rightMapper.apply(right.unwrap);
        };
    }

    default boolean isLeft() {
        return this instanceof Left<?, ?>;
    }

    default boolean isRight() {
        return this instanceof Right<?, ?>;
    }

    static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }

    static <L, R> Either<L, R> tryCatch(Callable<R> supplier, Function<Exception, L> exceptionHandler) {
        try {
            return right(supplier.call());
        } catch (Exception e) {
            return left(exceptionHandler.apply(e));
        }
    }

    default <R2> Either<L, R2> flatMap(Function<R, Either<L, R2>> mapper) {
        return switch (this) {
            case Left<L, R> left -> new Left<>(left.unwrap);
            case Right<L, R> right -> mapper.apply(right.unwrap);
        };
    }
}
