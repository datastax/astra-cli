package com.dtsx.astra.cli.core.completions;

import com.dtsx.astra.cli.config.AstraHome;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Set;
import java.util.function.Function;

public abstract class CompletionsCache {
    private @Nullable Set<String> cachedCandidates;

    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void update(Function<Set<String>, Set<String>> mkCandidates) {
        if (!AstraHome.exists()) {
            return;
        }

        val cacheFile = useCacheFile();

        var currentCandidates = Set.<String>of();

        try {
            currentCandidates = Set.of(Files.readString(cacheFile.toPath()).split(" "));
        } catch (Exception _) {}

        val candidates = mkCandidates.apply(currentCandidates);

        try {
            if (cachedCandidates != null && cachedCandidates.equals(candidates)) {
                return;
            }

            cachedCandidates = Set.copyOf(candidates);

            if (candidates.isEmpty()) {
                cacheFile.delete();
                return;
            }

            cacheFile.getParentFile().mkdirs();

            @Cleanup val writer = new FileWriter(cacheFile);
            writer.write(String.join(" ", candidates));
        } catch (Exception _) {
            try {
                cacheFile.delete();
            } catch (Exception _) {}
        }
    }

    protected abstract File useCacheFile();

    protected File useCacheDir() {
        return AstraHome.Dirs.useCompletionsCache();
    }
}
