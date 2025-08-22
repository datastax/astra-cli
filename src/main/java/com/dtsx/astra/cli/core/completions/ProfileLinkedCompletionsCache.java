package com.dtsx.astra.cli.core.completions;

import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.core.completions.caches.TenantCompletionsCache;
import com.dtsx.astra.cli.core.completions.caches.UserCompletionsCache;
import com.dtsx.astra.cli.utils.FileUtils;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class ProfileLinkedCompletionsCache extends CompletionsCache {
    private final Optional<ProfileName> profileName;

    public static List<ProfileLinkedCompletionsCache> mkInstances(ProfileName profileName) {
        return List.of(
            new DbCompletionsCache(Optional.of(profileName)),
            new UserCompletionsCache(Optional.of(profileName)),
            new TenantCompletionsCache(Optional.of(profileName))
        );
    }

    @Override
    protected Optional<File> useCacheDir() {
        return profileName.flatMap((name) -> (
            super.useCacheDir().map((dir) -> new File(dir, FileUtils.sanitizeFileName(name.unwrap()))
        )));
    }
}
