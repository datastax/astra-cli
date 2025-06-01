package com.dtsx.astra.cli.completions;

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
        if (!getAstraDir().exists()) {
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

    private File getAstraDir() {
        val homeDir = System.getProperty("user.home");
        return new File(homeDir, ".astra");
    }

    protected File getCacheDir() {
        return new File(getAstraDir(), "completions-cache");
    }
}
