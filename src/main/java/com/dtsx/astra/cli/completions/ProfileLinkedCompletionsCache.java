package com.dtsx.astra.cli.completions;

import com.dtsx.astra.cli.completions.caches.DbCompletionsCache;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
public abstract class ProfileLinkedCompletionsCache extends CompletionsCache {
    private final String profileName;

    public static List<ProfileLinkedCompletionsCache> mkInstances(String profileName) {
        return List.of(
            new DbCompletionsCache(profileName)
        );
    }

    protected File getCacheDir() {
        return new File(super.getCacheDir(), profileName);
    }
}
