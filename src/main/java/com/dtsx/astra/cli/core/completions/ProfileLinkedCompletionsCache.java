package com.dtsx.astra.cli.core.completions;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource;
import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource.DefaultFile;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.core.completions.caches.PcuGroupsCompletionsCache;
import com.dtsx.astra.cli.core.completions.caches.TenantCompletionsCache;
import com.dtsx.astra.cli.core.completions.caches.UserCompletionsCache;
import com.dtsx.astra.cli.core.config.ProfileName;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public abstract class ProfileLinkedCompletionsCache extends CompletionsCache {
    private final Optional<ProfileName> profileName;

    public ProfileLinkedCompletionsCache(CliContext ctx, ProfileSource profileSource) {
        super(ctx);

        this.profileName = switch (profileSource) {
            case DefaultFile(var name) -> Optional.of(name);
            default -> Optional.empty();
        };
    }

    public static List<ProfileLinkedCompletionsCache> mkInstances(CliContext ctx, ProfileSource profileSource) {
        return List.of(
            new DbCompletionsCache(ctx, profileSource),
            new UserCompletionsCache(ctx, profileSource),
            new TenantCompletionsCache(ctx, profileSource),
            new PcuGroupsCompletionsCache(ctx, profileSource)
        );
    }

    @Override
    protected Optional<Path> useCacheDir() {
        return profileName.flatMap((name) -> (
            super.useCacheDir().map((dir) -> dir.resolve(sanitizeFileName(name.unwrap()))
        )));
    }

    private String sanitizeFileName(String name) {
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
