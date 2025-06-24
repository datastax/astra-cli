package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.core.completions.impls.ProfileKeysCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.config.ProfileNotFoundException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputHuman;
import com.dtsx.astra.cli.core.output.table.RenderableShellTable;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.core.parsers.ini.Ini;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigGetOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Optional;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.config.ConfigGetOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

@Command(
    name = "get",
    aliases = { "describe" }
)
public class ConfigGetCmd extends AbstractCmd<GetConfigResult> {
    @Parameters(completionCandidates = AvailableProfilesCompletion.class, description = "Name of the profile to display", paramLabel = "PROFILE", defaultValue = "default")
    public ProfileName profileName;

    @Option(names = { "-k", "--key" }, completionCandidates = ProfileKeysCompletion.class, description = "Specific configuration key to retrieve", paramLabel = "KEY")
    public Optional<String> key = Optional.empty();

    @Override
    public final OutputAll execute(GetConfigResult result) {
        return switch (result) {
            case SpecificKeyValue(var value) -> OutputAll.serializeValue(value);
            case ProfileSection(var section) -> OutputAll.instance(
                () -> OutputHuman.message(section.render(true)),
                () -> mkTable(section),
                () -> mkTable(section)
            );
            case ProfileNotFound() -> throw new ProfileNotFoundException(profileName);
            case KeyNotFound(var keyName, var section) -> throw new KeyNotFoundException(keyName, section);
        };
    }

    @Override
    protected Operation<GetConfigResult> mkOperation() {
        return new ConfigGetOperation(config(), new GetConfigRequest(profileName, key));
    }

    private RenderableShellTable mkTable(Ini.IniSection section) {
        val attrs = section.pairs().stream()
            .map(p -> ShellTable.attr(p.key(), p.value()))
            .toList();

        return new ShellTable(attrs).withAttributeColumns();
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
                highlight(section.name())
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
                highlight(section.name()),
                section.pairs().stream()
                    .map(p -> "- " + AstraColors.PURPLE_300.use(p.key()))
                    .collect(Collectors.joining(NL)),
                highlight("astra config get " + section.name())
            );
        }
    }
}
