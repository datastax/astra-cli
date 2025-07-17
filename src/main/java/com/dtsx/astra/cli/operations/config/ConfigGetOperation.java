package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.config.AstraConfig;
import com.dtsx.astra.cli.config.AstraConfig.InvalidProfile;
import com.dtsx.astra.cli.config.AstraConfig.Profile;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.parsers.ini.Ini;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigGetOperation.GetConfigResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
public class ConfigGetOperation implements Operation<GetConfigResult> {
    private final AstraConfig config;
    private final GetConfigRequest request;

    public sealed interface GetConfigResult {}
    public record ProfileSection(Ini.IniSection section) implements GetConfigResult {}
    public record SpecificKeyValue(String value) implements GetConfigResult {}
    public record KeyNotFound(String key, Ini.IniSection section) implements GetConfigResult {}
    public record ProfileNotFound(String profileName) implements GetConfigResult {}

    public record GetConfigRequest(
        Optional<String> profileName,
        Optional<String> key,
        Function<NEList<Either<InvalidProfile, Profile>>, String> promptProfileName
    ) {}

    @Override
    public GetConfigResult execute() {
        val profileName = request.profileName.orElseGet(() -> {
            val candidates = NEList.parse(config.profiles());

            if (candidates.isEmpty()) {
                throw new IllegalStateException("No profile available in configuration");
            }

            return request.promptProfileName.apply(candidates.get());
        });

        val section = config.getProfileSection(profileName);

        if (section.isEmpty()) {
            return new ProfileNotFound(profileName);
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
