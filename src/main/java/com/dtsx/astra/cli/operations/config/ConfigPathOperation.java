package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigPathOperation.ConfigPathResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.nio.file.Files;
import java.nio.file.Path;

@RequiredArgsConstructor
public class ConfigPathOperation implements Operation<ConfigPathResult> {
    private final CliContext ctx;

    public record ConfigPathResult(
        Path path,
        String readablePath,
        String resolver,
        boolean exists
    ) {}

    @Override
    public ConfigPathResult execute() {
        val path = ctx.properties().rcFileLocations(ctx.isWindows()).preferred(ctx);

        return new ConfigPathResult(
            path,
            System.getProperty("cli.rc-file.path"), // this is a more user-readable path meant more for help messages and such
            System.getProperty("cli.rc-file.resolver"),
            Files.exists(path)
        );
    }
}
