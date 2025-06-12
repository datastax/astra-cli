package com.dtsx.astra.cli.utils;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class MiscUtils {
    public static <A> Set<A> setAdd(Set<A> set1, A a) {
        return setUnion(set1, Set.of(a));
    }

    public static <A> Set<A> setDel(Set<A> set1, A a) {
        val newSet = new HashSet<>(set1);
        newSet.remove(a);
        return newSet;
    }

    public static <A> Set<A> setUnion(Set<A> set1, Set<A> set2) {
        val unionSet = new HashSet<>(set1);
        unionSet.addAll(set2);
        return unionSet;
    }

    public static Void toVoid(Runnable r) {
        r.run();
        return null;
    }
}
