package com.dtsx.astra.cli.testlib.laws;

import lombok.val;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import java.lang.reflect.ParameterizedType;

public class LawUtils {
    @SuppressWarnings("unchecked")
    public static <T> Arbitrary<T> anyT(Class<?> clazz, Class<?> interfaceClass, int typeArgIndex) {
        val parameterizedType = findGenericInterface(clazz, interfaceClass);

        val typeArg = parameterizedType.getActualTypeArguments()[typeArgIndex];

        val arbClass = (Class<T>) ((typeArg instanceof ParameterizedType ptype)
            ? ptype.getRawType()
            : typeArg);

        return Arbitraries.defaultFor(arbClass);
    }

    private static ParameterizedType findGenericInterface(Class<?> clazz, Class<?> targetInterface) {
        for (val type : clazz.getGenericInterfaces()) {
            if (type instanceof ParameterizedType pType) {
                if (pType.getRawType() == targetInterface) {
                    return pType;
                } else {
                    val found = findGenericInterface((Class<?>) pType.getRawType(), targetInterface);
                    if (found != null) return found;
                }
            } else if (type instanceof Class<?> intfClass) {
                val found = findGenericInterface(intfClass, targetInterface);
                if (found != null) return found;
            }
        }

        val superclass = clazz.getSuperclass();

        if (superclass != null && superclass != Object.class) {
            return findGenericInterface(superclass, targetInterface);
        }

        throw new IllegalArgumentException("Could not find interface: " + targetInterface.getName() + " in class: " + clazz.getName());
    }
}
