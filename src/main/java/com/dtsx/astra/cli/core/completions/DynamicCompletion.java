package com.dtsx.astra.cli.core.completions;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class DynamicCompletion implements Iterable<String> {
    protected DynamicCompletion(String bash) {
        this.bash = bash;
    }

    private static final Set<DynamicCompletion> INSTANCES = new HashSet<>();

    protected static void register(DynamicCompletion instance) {
        INSTANCES.add(instance);
    }

    public static Set<DynamicCompletion> mkInstances() {
        return INSTANCES;
    }

    @Getter
    private final String bash;

    public static String marker(DynamicCompletion instance) {
        return "!$!this-is-a-dynamic-completion!$!:" + instance.getClass().getName();
    }

    @Override
    public @NotNull Iterator<String> iterator() {
        return List.of(marker(this)).iterator();
    }
}
