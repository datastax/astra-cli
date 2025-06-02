package com.dtsx.astra.cli.completions.caches;

import com.dtsx.astra.cli.completions.ProfileLinkedCompletionsCache;
import com.dtsx.astra.cli.config.ProfileName;

import java.io.File;

public class DbCompletionsCache extends ProfileLinkedCompletionsCache {
    public DbCompletionsCache(ProfileName profileName) {
        super(profileName);
    }

    @Override
    protected File getCacheFile() {
        return new File(getCacheDir(), "db_names");
    }
}
