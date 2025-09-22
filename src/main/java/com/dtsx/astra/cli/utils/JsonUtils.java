package com.dtsx.astra.cli.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class JsonUtils {
    private static @Nullable ObjectMapper OBJECT_MAPPER = null;

    @SuppressWarnings({ "RedundantCast", "unchecked" })
    public static ObjectMapper objectMapper() {
        if (OBJECT_MAPPER != null) {
            return OBJECT_MAPPER;
        }

        val objectMapper = com.dtsx.astra.sdk.utils.JsonUtils.getObjectMapper();

        val module = new SimpleModule();
        module.addSerializer((Class<? extends Set<Object>>) (Class<?>) Set.class, new SortedSetJsonSerializer());

        objectMapper.registerModule(module);
        objectMapper.registerModule(new Jdk8Module());

        return OBJECT_MAPPER = objectMapper;
    }

    public static String escapeJson(String json) {
        return com.dtsx.astra.sdk.utils.JsonUtils.escapeJson(json);
    }

    @SneakyThrows
    public static <T> T readValue(String body, Class<T> ref) {
        return objectMapper().readValue(body, ref);
    }

    @SneakyThrows
    public static JsonNode readTree(String body) {
        return objectMapper().readTree(body);
    }

    @SneakyThrows
    public static String writeValue(Object o) {
        return objectMapper().writeValueAsString(o);
    }

    public static boolean isValidJson(String str) {
        try {
            objectMapper().readTree(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @SneakyThrows
    @VisibleForTesting
    public static <T> T clone(T value, Class<T> ref) {
        val asString = objectMapper().writeValueAsString(value); // should ONLY be used for testing purposes
        return objectMapper().readValue(asString, ref);
    }

    static class SortedSetJsonSerializer extends JsonSerializer<Set<Object>> {
        @Override
        public void serialize(Set<Object> set, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (set == null) {
                gen.writeNull();
                return;
            }

            gen.writeStartArray();

            if (!set.isEmpty()) {
                if (!SortedSet.class.isAssignableFrom(set.getClass())) {
                    val item = set.iterator().next();

                    if (Comparable.class.isAssignableFrom(item.getClass())) {
                        set = new TreeSet<>(set);
                    } else {
                        set = new TreeSet<>(Comparator.comparing(Object::hashCode)); // as long as it's deterministic, I don't care
                    }
                }

                for (val item : set) {
                    gen.writeObject(item);
                }
            }

            gen.writeEndArray();
        }
    }
}
