package com.dtsx.astra.cli.core.output.serializers;

import lombok.val;

import java.util.List;

@SuppressWarnings("unchecked")
public interface OutputSerializer<T> {
    List<OutputSerializer<?>> SERIALIZERS = List.of(
        StringSerializer.INSTANCE,
        ListSerializer.INSTANCE,
        BoolSerializer.INSTANCE,
        EnumSerializer.INSTANCE,
        ProfileNameSerializer.INSTANCE
    );

    boolean canSerialize(Object o);

    default String serializeAsHuman(Object t) {
        return serializeAsHumanInternal((T) t);
    }

    default Object serializeAsJson(Object t) {
        return serializeAsJsonInternal((T) t);
    }

    default String serializeAsCsv(Object t) {
        return serializeAsCsvInternal((T) t);
    }

    String serializeAsHumanInternal(T t);
    Object serializeAsJsonInternal(T t);
    String serializeAsCsvInternal(T t);

    static OutputSerializer<?> findSerializerForObj(Object o) {
        for (val serializer : SERIALIZERS) {
            if (serializer.canSerialize(o)) {
                return serializer;
            }
        }
        throw new IllegalArgumentException("No serializer found for object: " + o.getClass().getName());
    }

    static String trySerializeAsHuman(Object o) {
        return findSerializerForObj(o).serializeAsHuman(o);
    }

    static Object trySerializeAsJson(Object o) {
        return findSerializerForObj(o).serializeAsJson(o);
    }

    static String trySerializeAsCsv(Object o) {
        return findSerializerForObj(o).serializeAsCsv(o);
    }
}
