package com.dtsx.astra.cli.core.completions;

import com.dtsx.astra.cli.core.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.utils.FileUtils;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.List;

@RequiredArgsConstructor
public abstract class ProfileLinkedCompletionsCache extends CompletionsCache {
    private final ProfileName profileName;

    public static List<ProfileLinkedCompletionsCache> mkInstances(ProfileName profileName) {
        return List.of(
            new DbCompletionsCache(profileName)
        );
    }

    protected File useCacheDir() {
        return new File(super.useCacheDir(), FileUtils.sanitizeFileName(profileName.unwrap()));
    }
}
