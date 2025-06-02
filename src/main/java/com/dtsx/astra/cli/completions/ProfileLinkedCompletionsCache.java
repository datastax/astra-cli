package com.dtsx.astra.cli.completions;

import com.dtsx.astra.cli.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.config.ProfileName;
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

    protected File getCacheDir() {
        return new File(super.getCacheDir(), profileName.unwrap());
    }
}
