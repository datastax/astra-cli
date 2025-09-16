package com.dtsx.astra.cli.core.completions;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.utils.FileUtils;
import com.dtsx.astra.cli.utils.JsonUtils;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

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

@RequiredArgsConstructor
public abstract class CompletionsCache {
    private final CliContext ctx;

    private @Nullable Set<String> cachedCandidates;

    @SneakyThrows
    public void update(Function<Set<String>, Set<String>> mkCandidates) {
        val cacheFile = resolveCacheFile();

        if (cacheFile.isEmpty()) {
            return;
        }

        var currentCandidates = Set.<String>of();

        try {
            FileUtils.createFileIfNotExists(cacheFile.get(), "Error creating completions cache file");

            currentCandidates = Files.readAllLines(cacheFile.get()).stream().map(this::readJsonString).collect(Collectors.toSet());
        } catch (Exception e) {
            ctx.log().exception("An error occurred reading cache file '%s'".formatted(cacheFile), e);
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

            @Cleanup val writer = Files.newBufferedWriter(cacheFile.get());
            writer.write(String.join(NL, candidates.stream().map(this::writeJsonString).toList()));
        } catch (Exception e) {
            try {
                ctx.log().exception("An error occurred updating cache file '%s'".formatted(cacheFile), e);
                Files.deleteIfExists(cacheFile.get());
            } catch (Exception _) {}
        }
    }

    @VisibleForTesting
    public Optional<Path> resolveCacheFile() {
        return useCacheDir().map(dir -> dir.resolve(useCacheFileName()));
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

    protected abstract String useCacheFileName();

    protected Optional<Path> useCacheDir() {
        return Optional.of(ctx.home().Dirs.useCompletionsCache());
    }

    @SneakyThrows
    private String readJsonString(String value) {
        return JsonUtils.readValue(value, String.class); // easy way to unescape strings
    }

    @SneakyThrows
    protected String writeJsonString(String value) {
        return JsonUtils.writeValue(value); // easy way to escape strings
    }
}
