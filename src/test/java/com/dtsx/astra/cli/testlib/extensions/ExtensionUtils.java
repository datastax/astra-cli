package com.dtsx.astra.cli.testlib.extensions;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ExtensionUtils {
    public static List<Field> getAllFields(Object o) {
        return getAllFields(o.getClass());
    }

    public static List<Field> getAllFields(Class<?> type) {
        val fields = new ArrayList<Field>();

        for (var c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            fields.addAll(List.of(c.getDeclaredFields()));
        }

        return fields;
    }
}
