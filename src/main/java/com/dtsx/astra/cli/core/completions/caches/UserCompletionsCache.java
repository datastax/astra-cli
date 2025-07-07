package com.dtsx.astra.cli.core.completions.caches;

import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.completions.ProfileLinkedCompletionsCache;

import java.io.File;
import java.util.Optional;

public class UserCompletionsCache extends ProfileLinkedCompletionsCache {
    public UserCompletionsCache(Optional<ProfileName> profileName) {
        super(profileName);
    }

    @Override
    protected Optional<File> useCacheFile() {
        return super.useCacheDir().map((dir) -> new File(dir, "user_emails"));
    }
}
