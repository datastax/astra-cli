package com.dtsx.astra.cli.core.completions;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.utils.JsonUtils;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.dtsx.astra.cli.utils.CollectionUtils.setAdd;
import static com.dtsx.astra.cli.utils.CollectionUtils.setDel;
import static com.dtsx.astra.cli.utils.StringUtils.NL;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
public abstract class CompletionsCache {
    protected final CliContext ctx;

    private @Nullable Set<String> cachedCandidates;

    @SneakyThrows
    private void update(Function<Set<String>, Set<String>> mkCandidates) {
        val primaryCacheFile = primaryCacheFile();

        if (primaryCacheFile.isEmpty()) {
            return;
        }

        val cacheFile = primaryCacheFile.get();
        var currentCandidates = Set.<String>of();

        try {
            if (Files.exists(cacheFile)) {
                currentCandidates = Files.readAllLines(cacheFile).stream().map(this::readJsonString).collect(toSet());
            }
        } catch (Exception e) {
            ctx.log().exception("An error occurred reading cache file '%s'".formatted(cacheFile), e);
            return;
        }

        val candidates = mkCandidates.apply(currentCandidates);

        if (cachedCandidates != null && cachedCandidates.equals(candidates)) {
            return;
        }

        cachedCandidates = Set.copyOf(candidates);

        writeCacheFile(cacheFile, candidates);

        for (val mirrorCacheFile : mirrorCacheFiles()) {
            writeCacheFile(mirrorCacheFile, candidates);
        }
    }

    private void writeCacheFile(Path cacheFile, Set<String> candidates) {
        try {
            if (candidates.isEmpty()) {
                Files.deleteIfExists(cacheFile);
                return;
            }

            Files.createDirectories(cacheFile.getParent());

            @Cleanup val writer = Files.newBufferedWriter(cacheFile);
            writer.write(String.join(NL, candidates.stream().map(this::writeJsonString).toList()));
        } catch (Exception e) {
            try {
                ctx.log().exception("An error occurred updating cache file '%s'".formatted(cacheFile), e);
                Files.deleteIfExists(cacheFile);
            } catch (Exception _) {}
        }
    }

    protected abstract Optional<Path> primaryCacheFile();

    protected abstract List<Path> mirrorCacheFiles();

    public final void setCache(List<String> completions) {
        update((_) -> completions.stream().filter(c -> c != null && !c.isBlank()).collect(toSet()));
    }

    public final void addToCache(String completion) {
        if (completion == null || completion.isBlank()) {
            return;
        }
        update((s) -> setAdd(s, completion));
    }

    public final void removeFromCache(String completion) {
        if (completion == null || completion.isBlank()) {
            return;
        }
        update((s) -> setDel(s, completion));
    }

    protected static Path defaultCacheDir(CliContext ctx) {
        return ctx.home().dirs().useCompletionsCache();
    }

    @SneakyThrows
    private String readJsonString(String value) {
        return JsonUtils.readValue(value, String.class); // easy way to unescape strings
    }

    @SneakyThrows
    private String writeJsonString(String value) {
        return JsonUtils.writeValue(value); // easy way to escape strings
    }
}
