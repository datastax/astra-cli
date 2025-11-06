package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.parsers.ini.ast.IniSection;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigGetOperation.GetConfigResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;

@RequiredArgsConstructor
public class ConfigGetOperation implements Operation<GetConfigResult> {
    private final AstraConfig config;
    private final GetConfigRequest request;

    public sealed interface GetConfigResult {}
    public record ProfileSection(IniSection section) implements GetConfigResult {}
    public record SpecificKeyValue(String value) implements GetConfigResult {}
    public record KeyNotFound(String key, IniSection section) implements GetConfigResult {}
    public record ProfileNotFound(String profileName) implements GetConfigResult {}

    public record GetConfigRequest(
        String profileName,
        Optional<String> key
    ) {}

    @Override
    public GetConfigResult execute() {
        val section = config.lookupSection(request.profileName);

        if (section.isEmpty()) {
            return new ProfileNotFound(request.profileName);
        }

        if (request.key.isPresent()) {
            val keyValue = section.get().lookupKey(request.key.get());

            if (keyValue.isPresent()) {
                return new SpecificKeyValue(keyValue.get());
            } else {
                return new KeyNotFound(request.key.get(), section.get());
            }
        }

        return new ProfileSection(section.get());
    }
}
