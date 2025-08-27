package com.dtsx.astra.cli.core.completions;

import com.dtsx.astra.cli.core.config.AstraHome;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
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
    public void update(Function<Set<String>, Set<String>> mkCandidates) {
        if (!AstraHome.exists()) {
            return;
        }

        val cacheFile = useCacheDir().map(dir -> dir.resolve(useCacheFile()));

        if (cacheFile.isEmpty()) {
            return;
        }

        var currentCandidates = Set.<String>of();

        try {
            currentCandidates = Files.readAllLines(cacheFile.get()).stream().map(this::readJsonString).collect(Collectors.toSet());
        } catch (Exception e) {
            AstraLogger.exception("An error occurred reading cache file '%s'".formatted(cacheFile), e);
            return;
        }

        val candidates = mkCandidates.apply(currentCandidates);

        if (cachedCandidates != null && cachedCandidates.equals(candidates)) {
            return;
        }

        cachedCandidates = Set.copyOf(candidates);

        try {
            if (candidates.isEmpty()) {
                Files.deleteIfExists(cacheFile.get());
                return;
            }

            Files.createDirectories(cacheFile.get().getParent());

            @Cleanup val writer = Files.newBufferedWriter(cacheFile.get());
            writer.write(String.join(NL, candidates.stream().map(this::writeJsonString).toList()));
        } catch (Exception e) {
            try {
                Files.deleteIfExists(cacheFile.get());
                AstraLogger.exception("An error occurred updating cache file '%s'".formatted(cacheFile), e);
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

    protected abstract String useCacheFile();

    protected Optional<Path> useCacheDir() {
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
