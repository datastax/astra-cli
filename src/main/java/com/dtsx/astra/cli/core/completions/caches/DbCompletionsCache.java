package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;

import java.io.File;
import java.util.Optional;

public class DbCompletionsCache extends ProfileLinkedCompletionsCache {
    public DbCompletionsCache(Optional<ProfileName> profileName) {
        super(profileName);
    }

    @Override
    protected Optional<File> useCacheFile() {
        return super.useCacheDir().map((dir) -> new File(dir, "db_names"));
    }
}
