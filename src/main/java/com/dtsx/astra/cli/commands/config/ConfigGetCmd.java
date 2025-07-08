package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.core.completions.impls.ProfileKeysCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputCsv;
import com.dtsx.astra.cli.core.output.output.OutputHuman;
import com.dtsx.astra.cli.core.output.output.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.core.parsers.ini.Ini;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigGetOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.KEY_NOT_FOUND;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.config.ConfigGetOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "get",
    aliases = { "describe" },
    description = "Get the configuration of a profile or a specific key. Warning: this command may expose your sensitive Astra token."
)
@Example(
    comment = "Get the configuration of the default profile",
    command = "astra config get"
)
@Example(
    comment = "Get the configuration of a specific profile",
    command = "astra config get my_profile"
)
@Example(
    comment = "Get the unwrap of a specific key",
    command = "astra config get my_profile -k ASTRA_DB_APPLICATION_TOKEN"
)
public class ConfigGetCmd extends AbstractConfigCmd<GetConfigResult> {
    @Parameters(
        description = { "Name of the profile to display", DEFAULT_VALUE },
        completionCandidates = AvailableProfilesCompletion.class,
        paramLabel = "PROFILE",
        defaultValue = "default"
    )
    public ProfileName $profileName;

    @Option(
        names = { "-k", "--key" },
        description = "Specific configuration key to retrieve",
        completionCandidates = ProfileKeysCompletion.class,
        paramLabel = "KEY"
    )
    public Optional<String> $key = Optional.empty();

    @Override
    public final OutputAll execute(GetConfigResult result) {
        return switch (result) {
            case SpecificKeyValue(var value) -> OutputAll.serializeValue(value);
            case ProfileSection(var section) -> OutputAll.instance(
                () -> renderHuman(section),
                () -> renderJson(section),
                () -> renderCsv(section)
            );
            case KeyNotFound(var keyName, var section) -> throwKeyNotFound(keyName, section);
        };
    }

    private OutputHuman renderHuman(Ini.IniSection section) {
        return OutputHuman.message(section.render(true));
    }

    private OutputJson renderJson(Ini.IniSection section) {
        return OutputJson.serializeValue(Map.of(
            "name", section.name(),
            "attributes", section.pairs().stream().map((p) -> Map.of(
                "key", p.key(),
                "unwrap", p.value(),
                "comments", p.comments()
            )).toList()
        ));
    }

    private OutputCsv renderCsv(Ini.IniSection section) {
        val data = new LinkedHashMap<String, Object>();

        for (var pair : section.pairs()) {
            data.put(pair.key(), pair.value());
        }

        return ShellTable.forAttributes(data);
    }

    private <T> T throwKeyNotFound(String key, Ini.IniSection section) {
        throw new AstraCliException(KEY_NOT_FOUND, section.pairs().isEmpty() ? mkNoKeysMsg(key, section) : mkKeysMsg(key, section));
    }

    private String mkNoKeysMsg(String key, Ini.IniSection section) {
        return """
          @|bold,red Error: Key '%s' does not exist in profile '%s'.|@
        
          Profile %s does not contain any keys.
        """.formatted(
            key,
            section.name(),
            highlight(section.name())
        );
    }

    private String mkKeysMsg(String key, Ini.IniSection section) {
        return """
          @|bold,red Error: Key '%s' does not exist in profile '%s'.|@
        
          Available keys in this profile are:
          %s
        
          %s
          %s
        """.formatted(
            key,
            section.name(),
            section.pairs().stream().map(p -> "- " + highlight(p.key())).collect(Collectors.joining(NL)),
            renderComment("Get the values of the keys in this profile with:"),
            renderCommand("astra config get " + section.name())
        );
    }

    @Override
    protected Operation<GetConfigResult> mkOperation() {
        return new ConfigGetOperation(config(), new GetConfigRequest($profileName, $key));
    }
}
