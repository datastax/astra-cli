package com.dtsx.astra.cli.core.completions;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource;
import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource.DefaultFile;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.config.ProfileName;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.VisibleForTesting;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public abstract class ProfileLinkedCompletionsCache extends CompletionsCache {
    private final Path primaryCacheFile;
    private final Optional<Path> mirrorCacheFile;

    public ProfileLinkedCompletionsCache(CliContext ctx, Pair<Profile, ProfileSource> profileAndSource) {
        super(ctx);

        val profile = profileAndSource.getLeft();
        val source = profileAndSource.getRight();

        this.primaryCacheFile = pathForProfile(ctx, source)
            .map(p -> p.resolve(useCacheFileName()))
            .orElse(null);

        this.mirrorCacheFile = profile.sourceForDefault()
            .flatMap(defaultSource ->
                pathForProfile(ctx, copySource(source, defaultSource))
                    .map(p -> p.resolve(useCacheFileName()))
            );
    }

    protected abstract String useCacheFileName();

    private ProfileSource copySource(ProfileSource source, ProfileName target) {
        return switch (source) {
            case DefaultFile(_) -> new DefaultFile(target);
            default -> source; // doesn't matter since it won't be used anyway
        };
    }

    public static Optional<Path> pathForProfile(CliContext ctx, ProfileSource profileName) {
        return switch (profileName) {
            case DefaultFile(var name) -> Optional.of(defaultCacheDir(ctx).resolve(sanitizeFileName(name.unwrap())));
            default -> Optional.empty();
        };
    }

    @Override
    @VisibleForTesting
    public Optional<Path> primaryCacheFile() {
        return Optional.ofNullable(primaryCacheFile);
    }

    @Override
    @VisibleForTesting
    public List<Path> mirrorCacheFiles() {
        return mirrorCacheFile.map(List::of).orElse(List.of());
    }

    private static String sanitizeFileName(String name) {
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
