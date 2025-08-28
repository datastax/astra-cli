package com.dtsx.astra.cli.core.completions;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.core.completions.caches.TenantCompletionsCache;
import com.dtsx.astra.cli.core.completions.caches.UserCompletionsCache;
import com.dtsx.astra.cli.core.config.ProfileName;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public abstract class ProfileLinkedCompletionsCache extends CompletionsCache {
    private final Optional<ProfileName> profileName;

    public ProfileLinkedCompletionsCache(CliContext ctx, Optional<ProfileName> profileName) {
        super(ctx);
        this.profileName = profileName;
    }

    public static List<ProfileLinkedCompletionsCache> mkInstances(CliContext ctx, ProfileName profileName) {
        return List.of(
            new DbCompletionsCache(ctx, Optional.of(profileName)),
            new UserCompletionsCache(ctx, Optional.of(profileName)),
            new TenantCompletionsCache(ctx, Optional.of(profileName))
        );
    }

    @Override
    protected Optional<Path> useCacheDir() {
        return profileName.flatMap((name) -> (
            super.useCacheDir().map((dir) -> dir.resolve(sanitizeFileName(name.unwrap()))
        )));
    }

    public String sanitizeFileName(String name) {
        while (name.contains("..")) {
            name = name.replace("..", "__");
        }

        name = name.replace("\\", "_");
        name = name.replace("/", "_");

        name = name.replaceAll("[:*?\"<>|]", "_");
        name = name.replaceAll("\\p{Cntrl}", "_");

        if (name.length() > 66) {
            name = name.substring(0, 66);
        }

        return name;
    }
}
