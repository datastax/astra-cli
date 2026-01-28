package com.dtsx.astra.cli.operations.config.home;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.home.ConfigHomePathOperation.ConfigPathResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class ConfigHomePathOperation implements Operation<ConfigPathResult> {
    private final CliContext ctx;

    public record ConfigPathResult(
        Path path,
        String readablePath,
        String resolver,
        boolean exists
    ) {}

    @Override
    public ConfigPathResult execute() {
        val path = ctx.properties().homeFolderLocations(ctx.isWindows()).preferred(ctx);

        return new ConfigPathResult(
            path,
            System.getProperty("cli.home-folder.path"), // this is a more user-readable path meant more for help messages and such
            System.getProperty("cli.home-folder.resolver"),
            Files.exists(path)
        );
    }
}
