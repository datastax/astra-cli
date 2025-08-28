package com.dtsx.astra.cli.testlib;

import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.ObjectAssert;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class AssertUtils {
    public static AbstractBooleanAssert<?> assertTrue(boolean condition) {
        return assertThat(condition).isTrue();
    }

    public static <T> ObjectAssert<T> assertEquals(T actual, T expected) {
        return assertThat(actual).isEqualTo(expected);
    }

    public static <T> T assertNonNull(T t) {
        return assertThat(t).isNotNull().actual();
    }

    public static <T, R> R assertNotCalled(T t) {
        throw new AssertionError("This function should not have been called");
    }
}
