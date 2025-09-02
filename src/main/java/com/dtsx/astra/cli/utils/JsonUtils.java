package com.dtsx.astra.cli.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    public static ObjectMapper objectMapper() {
        com.dtsx.astra.sdk.utils.JsonUtils.getObjectMapper().configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        return com.dtsx.astra.sdk.utils.JsonUtils.getObjectMapper();
    }

    public static String escapeJson(String json) {
        return com.dtsx.astra.sdk.utils.JsonUtils.escapeJson(json);
    }

    public static <T> T unmarshallType(String body, TypeReference<T> ref) {
        return com.dtsx.astra.sdk.utils.JsonUtils.unmarshallType(body, ref);
    }

    public static <T> T unmarshallBean(String body, Class<T> ref) {
        return com.dtsx.astra.sdk.utils.JsonUtils.unmarshallBean(body, ref);
    }
}
