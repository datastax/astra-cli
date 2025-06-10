package com.dtsx.astra.cli.core.completions;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class StaticCompletion implements Iterable<String> {
    private final List<String> candidates;

    public StaticCompletion(String... candidates) {
        this(Arrays.asList(candidates));
    }

    public StaticCompletion(List<String> candidates) {
        this.candidates = candidates;
    }

    @Override
    public @NotNull Iterator<String> iterator() {
        return candidates.iterator();
    }
}
