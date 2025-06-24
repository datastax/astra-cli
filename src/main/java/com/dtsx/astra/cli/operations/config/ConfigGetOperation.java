package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.config.AstraConfig;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.parsers.ini.Ini;
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
    public record ProfileSection(Ini.IniSection section) implements GetConfigResult {}
    public record SpecificKeyValue(String value) implements GetConfigResult {}
    public record ProfileNotFound() implements GetConfigResult {}
    public record KeyNotFound(String key, Ini.IniSection section) implements GetConfigResult {}

    public record GetConfigRequest(
        ProfileName profileName,
        Optional<String> key
    ) {}

    @Override
    public GetConfigResult execute() {
        val section = config.getProfileSection(request.profileName);

        if (section.isEmpty()) {
            return new ProfileNotFound();
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
