package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.config.ini.Ini;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.core.completions.impls.ProfileKeysCompletion;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputHuman;
import com.dtsx.astra.cli.core.output.table.RenderableShellTable;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.config.ConfigGetOperation;
import com.dtsx.astra.cli.operations.config.ConfigGetOperation.ProfileSection;
import com.dtsx.astra.cli.operations.config.ConfigGetOperation.SpecificKeyValue;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Optional;

@Command(
    name = "get",
    aliases = { "describe" }
)
public class ConfigGetCmd extends AbstractCmd {
    @Parameters(completionCandidates = AvailableProfilesCompletion.class, description = "Name of the profile to display", paramLabel = "<profile>")
    private ProfileName profileName;

    @Option(names = { "-k", "--key" }, completionCandidates = ProfileKeysCompletion.class, description = "Specific configuration key to retrieve", paramLabel = "<key>")
    private Optional<String> key = Optional.empty();

    @Override
    public OutputAll execute() {
        val operation = new ConfigGetOperation(config());
        val result = operation.execute(profileName, key);

        return switch (result) {
            case SpecificKeyValue(var value) -> {
                yield OutputAll.serializeValue(value);
            }
            case ProfileSection(var section) -> {
                yield OutputAll.instance(
                    () -> OutputHuman.message(section.render(true)),
                    () -> mkTable(section),
                    () -> mkTable(section)
                );
            }
        };
    }

    private RenderableShellTable mkTable(Ini.IniSection section) {
        val attrs = section.pairs().stream()
            .map(p -> ShellTable.attr(p.key(), p.value()))
            .toList();

        return new ShellTable(attrs).withAttributeColumns();
    }
}
