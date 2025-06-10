package com.dtsx.astra.cli.core.completions;

import com.dtsx.astra.cli.config.AstraHome;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public abstract class CompletionsCache {
    @SneakyThrows
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void update(@Nullable List<String> candidates) {
        if (!AstraHome.exists()) {
            return;
        }

        val cacheFile = getCacheFile();

        if (candidates == null || candidates.isEmpty()) {
            cacheFile.delete();
            return;
        }

        cacheFile.getParentFile().mkdirs();

        try (val writer = new FileWriter(cacheFile)) {
            writer.write(String.join(" ", candidates));
        }
    }

    public void delete() {
        update(List.of());
    }

    protected abstract File getCacheFile();

    protected File getCacheDir() {
        return AstraHome.Dirs.COMPLETIONS_CACHE;
    }
}
