package com.dtsx.astra.cli.core.completions;

import com.dtsx.astra.cli.core.config.AstraHome;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.utils.MiscUtils.setAdd;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

public abstract class CompletionsCache {
    private @Nullable Set<String> cachedCandidates;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void update(Function<Set<String>, Set<String>> mkCandidates) {
        if (!AstraHome.exists()) {
            return;
        }

        val cacheFile = useCacheFile();

        if (cacheFile.isEmpty()) {
            return;
        }

        var currentCandidates = Set.<String>of();

        try {
            currentCandidates = Files.readAllLines(cacheFile.get().toPath()).stream().map(this::readJsonString).collect(Collectors.toSet());
        } catch (Exception _) {}

        val candidates = mkCandidates.apply(currentCandidates);

        try {
            if (cachedCandidates != null && cachedCandidates.equals(candidates)) {
                return;
            }

            cachedCandidates = Set.copyOf(candidates);

            if (candidates.isEmpty()) {
                cacheFile.get().delete();
                return;
            }

            cacheFile.get().getParentFile().mkdirs();

            @Cleanup val writer = new FileWriter(cacheFile.get());
            writer.write(String.join(NL, candidates.stream().map(this::writeJsonString).toList()));
        } catch (Exception _) {
            try {
                cacheFile.get().delete();
            } catch (Exception _) {}
        }
    }

    public void setCache(List<String> completions) {
        update((_) -> new HashSet<>(completions));
    }

    public void addToCache(String completion) {
        update((s) -> setAdd(s, completion));
    }

    public void removeFromCache(String completion) {
        update((s) -> setAdd(s, completion));
    }

    protected abstract Optional<File> useCacheFile();

    protected Optional<File> useCacheDir() {
        return Optional.of(AstraHome.Dirs.useCompletionsCache());
    }

    @SneakyThrows
    private String readJsonString(String value) {
        return MAPPER.readValue(value, String.class);
    }

    @SneakyThrows
    protected String writeJsonString(String value) {
        return MAPPER.writeValueAsString(value);
    }
}
