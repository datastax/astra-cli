package com.dtsx.astra.cli.core.completions;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileContext;
import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource;
import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource.DefaultFile;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.ProfileName;
import lombok.val;
import org.jetbrains.annotations.VisibleForTesting;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public abstract class ProfileLinkedCompletionsCache extends CompletionsCache {
    private final Path primaryCacheFile;
    private final List<Path> mirrorCacheFiles;

    public ProfileLinkedCompletionsCache(CliContext ctx, ProfileContext profileCtx) {
        super(ctx);

        val source = profileCtx.source();
        val mirrors = profileCtx.mirrors();

        this.primaryCacheFile = pathForProfile(ctx, source)
            .map(p -> p.resolve(useCacheFileName()))
            .orElse(null);

        this.mirrorCacheFiles = mirrors.stream()
            .flatMap(name -> pathForProfile(ctx, copySource(source, name)).stream())
            .map(p -> p.resolve(useCacheFileName()))
            .toList();
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
        return mirrorCacheFiles;
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
