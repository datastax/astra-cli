package com.dtsx.astra.cli.utils;

import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class EnumFolder<E extends Enum<E>, R> {
    private final Map<E, @Nullable Supplier<R>> onEnumValues = new HashMap<>();

    public EnumFolder(Class<? extends E> enumClass) {
        for (val value : enumClass.getEnumConstants()) {
            onEnumValues.put(value, null);
        }
    }

    public EnumFolder<E, R> on(E value, Supplier<R> action) {
        onEnumValues.put(value, action);
        return this;
    }

    public EnumFolder<E, R> on(E value1, E value2, Supplier<R> action) {
        on(value1, action);
        return on(value2, action);
    }

    public EnumFolder<E, R> on(E value1, E value2, E value3, Supplier<R> action) {
        on(value1, value2, action);
        return on(value3, action);
    }

    public EnumFolderRunner exhaustive(BiFunction<List<E>, List<E>, R> onUnusedValue) {
        return new EnumFolderRunner(onUnusedValue);
    }

    public class EnumFolderRunner {
        private final BiFunction<List<E>, List<E>, R> onUnusedValue;

        public EnumFolderRunner(BiFunction<List<E>, List<E>, R> onUnusedValue) {
            this.onUnusedValue = onUnusedValue;
        }

        public R run(E value) {
            val usedValues = onEnumValues.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(Map.Entry::getKey)
                .toList();

            val unusedValues = onEnumValues.entrySet().stream()
                .filter(entry -> entry.getValue() == null)
                .map(Map.Entry::getKey)
                .toList();

            val action = onEnumValues.get(value);

            if (action != null) {
                return action.get();
            }

            return onUnusedValue.apply(usedValues, unusedValues);
        }
    }
}
