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

    public static <K, V> LinkedHashMap<K, V> sequencedMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        val map = new LinkedHashMap<K, V>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);
        return map;
    }

    public static <K, V> LinkedHashMap<K, V> sequencedMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        val map = new LinkedHashMap<K, V>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);
        map.put(k7, v7);
        return map;
    }

    public static <K, V> LinkedHashMap<K, V> sequencedMapOf(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        val map = new LinkedHashMap<K, V>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        map.put(k5, v5);
        map.put(k6, v6);
        map.put(k7, v7);
        map.put(k8, v8);
        return map;
    }
}
