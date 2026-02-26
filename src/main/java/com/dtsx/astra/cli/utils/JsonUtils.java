package com.dtsx.astra.cli.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.LinkedHashSet;
import java.util.Set;

public class JsonUtils {
    private static @Nullable ObjectMapper OBJECT_MAPPER = null;

    @SuppressWarnings({ "deprecation" })
    public static ObjectMapper objectMapper() {
        if (OBJECT_MAPPER != null) {
            return OBJECT_MAPPER;
        }

        val objectMapper = com.dtsx.astra.sdk.utils.JsonUtils.getObjectMapper();
        val module = new SimpleModule();

        module.addAbstractTypeMapping(Set.class, LinkedHashSet.class);

        objectMapper.registerModule(module);
        objectMapper.registerModule(new Jdk8Module());

        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
//        objectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

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
    public static <T> T readValue(String body, TypeReference<T> ref) {
        return objectMapper().readValue(body, ref);
    }

    @SneakyThrows
    public static JsonNode readTree(String body) {
        return objectMapper().readTree(body);
    }

    @SneakyThrows
    public static String formatJsonCompact(Object o) {
        return objectMapper().writeValueAsString(o);
    }

    @SneakyThrows
    public static String formatJsonPretty(Object o) {
        return objectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
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
}
