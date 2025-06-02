package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.completions.impls.ProfileKeysCompletion;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.config.ini.Ini;
import com.dtsx.astra.cli.output.output.OutputAll;
import com.dtsx.astra.cli.output.output.OutputHuman;
import com.dtsx.astra.cli.output.output.OutputType;
import com.dtsx.astra.cli.output.table.RenderableShellTable;
import com.dtsx.astra.cli.output.table.ShellTable;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.commands.db.DbGetCmd.DbGetKeys.*;
import static com.dtsx.astra.cli.commands.db.DbGetCmd.DbGetKeys.creation_time;
import static com.dtsx.astra.cli.commands.db.DbGetCmd.DbGetKeys.keyspace;
import static com.dtsx.astra.cli.commands.db.DbGetCmd.DbGetKeys.keyspaces;
import static com.dtsx.astra.cli.commands.db.DbGetCmd.DbGetKeys.region;
import static com.dtsx.astra.cli.commands.db.DbGetCmd.DbGetKeys.regions;
import static com.dtsx.astra.cli.commands.db.DbGetCmd.DbGetKeys.status;
import static com.dtsx.astra.cli.commands.db.DbGetCmd.DbGetKeys.vector;

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
        val section = config().getProfileSection(profileName);

        if (key.isPresent()) {
            return section
                .lookupKey(key.get())
                .map(OutputAll::serializeValue)
                .orElseThrow(() -> new ParameterException(spec.commandLine(), "Key '" + key.get() + "' not found in profile '" + profileName + "'"));
        }

        return OutputAll.instance(
            () -> OutputHuman.message(section.render(true)),
            () -> mkTable(section),
            () -> mkTable(section)
        );
    }

    private RenderableShellTable mkTable(Ini.IniSection section) {
        val attrs = section.pairs().stream()
            .map(p -> ShellTable.attr(p.key(), p.value()))
            .toList();

        return new ShellTable(attrs).withAttributeColumns();
    }
}
