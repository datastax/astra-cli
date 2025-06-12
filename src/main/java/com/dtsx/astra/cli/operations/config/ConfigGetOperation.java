package com.dtsx.astra.cli.operations.config;

import com.dtsx.astra.cli.config.AstraConfig;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.config.ini.Ini;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.config.ProfileNotFoundException;
import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ConfigGetOperation {
    private final AstraConfig config;

    public sealed interface GetConfigResult {}
    
    public record ProfileSection(Ini.IniSection section) implements GetConfigResult {}
    public record SpecificKeyValue(String value) implements GetConfigResult {}

    public GetConfigResult execute(ProfileName profileName, Optional<String> key) {
        val section = config.getProfileSection(profileName).orElseThrow(() -> new ProfileNotFoundException(profileName));

        if (key.isPresent()) {
            val keyValue = section.lookupKey(key.get());

            if (keyValue.isPresent()) {
                return new SpecificKeyValue(keyValue.get());
            } else {
                throw new KeyNotFoundException(key.get(), section);
            }
        }

        return new ProfileSection(section);
    }

    public static class KeyNotFoundException extends AstraCliException {
        public KeyNotFoundException(String key, Ini.IniSection section) {
            super(section.pairs().isEmpty() ? mkNoKeysMsg(key, section) : mkKeysMsg(key, section));
        }

        private static String mkNoKeysMsg(String key, Ini.IniSection section) {
            return """
              @|bold,red Error: Key '%s' does not exist in profile '%s'.|@
            
              Profile %s does not contain any keys.
            """.formatted(
                key,
                section.name(),
                AstraColors.highlight(section.name())
            );
        }

        private static String mkKeysMsg(String key, Ini.IniSection section) {
            return """
              @|bold,red Error: Key '%s' does not exist in profile '%s'.|@
            
              Available keys in profile %s:
              %s
            
              Use %s to get the values of all keys in the profile.
            """.formatted(
                key,
                section.name(),
                AstraColors.highlight(section.name()),
                section.pairs().stream()
                    .map(p -> "- " + AstraColors.PURPLE_300.use(p.key()))
                    .collect(Collectors.joining("\n")),
                AstraColors.highlight("astra config get " + section.name())
            );
        }
    }
}
