package com.dtsx.astra.cli.core.output.serializers;

import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import lombok.val;

import java.util.List;

@SuppressWarnings("unchecked")
public interface OutputSerializer<T> {
    List<OutputSerializer<?>> SERIALIZERS = List.of(
        OptionalSerializer.INSTANCE,
        SupplierSerializer.INSTANCE,
        EnumSerializer.INSTANCE,
        CollectionSerializer.INSTANCE,
        CatchAllSerializer.INSTANCE
    );

    boolean canSerialize(Object o);

    String serializeAsHumanInternal(T t);
    Object serializeAsJsonInternal(T t);
    String serializeAsCsvInternal(T t);

    static OutputSerializer<Object> findSerializerForObj(Object o) {
        for (val serializer : SERIALIZERS) {
            if (serializer.canSerialize(o)) {
                return (OutputSerializer<Object>) serializer;
            }
        }
        throw new CongratsYouFoundABugException("No serializer found for object: " + o.getClass().getName());
    }

    static String serializeAsHuman(Object o) {
        return findSerializerForObj(o).serializeAsHumanInternal(o);
    }

    static Object serializeAsJson(Object o) {
        return findSerializerForObj(o).serializeAsJsonInternal(o);
    }

    static String serializeAsCsv(Object o) {
        return findSerializerForObj(o).serializeAsCsvInternal(o);
    }
}
