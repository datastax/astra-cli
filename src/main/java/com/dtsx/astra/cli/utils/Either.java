package com.dtsx.astra.cli.utils;

import java.util.function.Function;

public sealed interface Either<L, R> {
    record Left<L, R>(L value) implements Either<L, R> {}
    record Right<L, R>(R value) implements Either<L, R> {}

    default <U> U fold(Function<L, U> leftMapper, Function<R, U> rightMapper) {
        return switch (this) {
            case Left<L, R> left -> leftMapper.apply(left.value);
            case Right<L, R> right -> rightMapper.apply(right.value);
        };
    }

    default <E extends Exception> R getRight(Function<L, E> exceptionSupplier) throws E {
        return switch (this) {
            case Left<L, R> left -> throw exceptionSupplier.apply(left.value);
            case Right<L, R> right -> right.value;
        };
    }

    default <E extends Exception> L getLeft(Function<R, E> exceptionSupplier) throws E {
        return switch (this) {
            case Left<L, R> left -> left.value;
            case Right<L, R> right -> throw exceptionSupplier.apply(right.value);
        };
    }

    default <L2, R2> Either<L2, R2> bimap(Function<L, L2> leftMapper, Function<R, R2> rightMapper) {
        return switch (this) {
            case Left<L, R> left -> new Left<>(leftMapper.apply(left.value));
            case Right<L, R> right -> new Right<>(rightMapper.apply(right.value));
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

    default <R2> Either<L, R2> flatMap(Function<R, Either<L, R2>> mapper) {
        return switch (this) {
            case Left<L, R> left -> new Left<>(left.value);
            case Right<L, R> right -> mapper.apply(right.value);
        };
    }
}
