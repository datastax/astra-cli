package com.dtsx.astra.cli.core.properties;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class MemoizedCliProperties implements CliProperties {
    @Delegate(excludes = Exclude.class)
    private final CliProperties delegate;

    private @Nullable PathLocations cachedRcFileLocations = null;
    private @Nullable PathLocations cachedHomeFolderLocations = null;
    private @Nullable String cachedCliName = null;

    @Override
    public PathLocations rcFileLocations(boolean isWindows) {
        if (cachedRcFileLocations == null) {
            cachedRcFileLocations = delegate.rcFileLocations(isWindows);
        }
        return cachedRcFileLocations;
    }

    @Override
    public PathLocations homeFolderLocations(boolean isWindows) {
        if (cachedHomeFolderLocations == null) {
            cachedHomeFolderLocations = delegate.homeFolderLocations(isWindows);
        }
        return cachedHomeFolderLocations;
    }

    @Override
    public String cliName() {
        if (cachedCliName == null) {
            cachedCliName = delegate.cliName();
        }
        return cachedCliName;
    }

    private interface Exclude {
        PathLocations defaultRcFile(boolean isWindows);
        PathLocations defaultHomeFolder(boolean isWindows);
        String cliName();
    }
}
