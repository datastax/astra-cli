package com.dtsx.astra.cli.utils;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.LinkedHashMap;

@UtilityClass
public class MapUtils {
    public static <K, V> LinkedHashMap<K, V> sequencedMapOf() {
        return new LinkedHashMap<>();
    }

    public static <K, V> LinkedHashMap<K, V> sequencedMapOf(K k1, V v1) {
        val map = new LinkedHashMap<K, V>();
        map.put(k1, v1);
        return map;
    }

    public static <K, V> LinkedHashMap<K, V> sequencedMapOf(K k1, V v1, K k2, V v2) {
        val map = new LinkedHashMap<K, V>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    public static <K, V> LinkedHashMap<K, V> sequencedMapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        val map = new LinkedHashMap<K, V>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }

    public static <K, V> LinkedHashMap<K, V> sequencedMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        val map = new LinkedHashMap<K, V>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        return map;
    }

    public static <K, V> LinkedHashMap<K, V> sequencedMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        val map = new LinkedHashMap<K, V>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        return map;
    }
}
