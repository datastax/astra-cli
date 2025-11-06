package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.core.CliConstants.$Profile;
import com.dtsx.astra.cli.core.completions.impls.AvailableProfilesCompletion;
import com.dtsx.astra.cli.core.completions.impls.ProfileKeysCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputCsv;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.prompters.specific.ProfileNamePrompter;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.core.parsers.ini.ast.IniSection;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigGetOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.core.output.ExitCode.KEY_NOT_FOUND;
import static com.dtsx.astra.cli.core.output.ExitCode.PROFILE_NOT_FOUND;
import static com.dtsx.astra.cli.operations.config.ConfigGetOperation.*;
import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "get",
    aliases = { "describe" },
    description = {
        "Get the configuration of a profile or a specific key.",
        "@|bold Warning:|@ This command may expose your sensitive Astra token.",
    }
)
@Example(
    comment = "Get the configuration of a specific profile",
    command = "${cli.name} config get my_profile"
)
@Example(
    comment = "Prompt for the profile to get the configuration of",
    command = "${cli.name} config get"
)
@Example(
    comment = "Get the value of a specific key in a profile",
    command = "${cli.name} config get my_profile --key ASTRA_DB_APPLICATION_TOKEN"
)
public class ConfigGetCmd extends AbstractConfigCmd<GetConfigResult> {
    @Parameters(
        arity = "0..1",
        description = "Name of the profile to get",
        completionCandidates = AvailableProfilesCompletion.class,
        paramLabel = $Profile.LABEL
    )
    public Optional<String> $profileName;

    @Option(
        names = { "-k", "--key" },
        description = "Specific configuration key to retrieve",
        completionCandidates = ProfileKeysCompletion.class,
        paramLabel = "KEY"
    )
    public Optional<String> $key;

    @Override
    public final OutputAll execute(Supplier<GetConfigResult> result) {
        return switch (result.get()) {
            case SpecificKeyValue(var value) -> OutputAll.serializeValue(value);
            case ProfileSection(var section) -> OutputAll.instance(
                () -> renderHuman(section),
                () -> renderJson(section),
                () -> renderCsv(section)
            );
            case KeyNotFound(var keyName, var section) -> throwKeyNotFound(keyName, section);
            case ProfileNotFound(var name) -> throwProfileNotFound(name);
        };
    }

    private OutputHuman renderHuman(IniSection section) {
        return OutputHuman.response(section.render(ctx.colors()));
    }

    private OutputJson renderJson(IniSection section) {
        return OutputJson.serializeValue(sequencedMapOf(
            "name", section.name(),
            "attributes", section.pairs().stream().map((p) -> sequencedMapOf(
                "key", p.key(),
                "unwrap", p.value(),
                "comments", p.comments()
            )).toList()
        ));
    }

    private OutputCsv renderCsv(IniSection section) {
        val data = new LinkedHashMap<String, Object>();

        for (var pair : section.pairs()) {
            data.put(pair.key(), pair.value());
        }

        return ShellTable.forAttributes(data);
    }

    private <T> T throwProfileNotFound(String name) {
        throw new AstraCliException(PROFILE_NOT_FOUND, """
          @|bold,red Error: Profile '%s' could not be found.|@
        
          Ensure that the correct configuration file is being used and that the profile name is correct.
        """.formatted(name), List.of(
            new Hint("List your profiles:", "${cli.name} config list")
        ));
    }

    private <T> T throwKeyNotFound(String key, IniSection section) {
        throw new AstraCliException(KEY_NOT_FOUND, section.pairs().isEmpty() ? mkNoKeysMsg(key, section) : mkKeysMsg(key, section));
    }

    private String mkNoKeysMsg(String key, IniSection section) {
        return """
          @|bold,red Error: Key '%s' does not exist in profile '%s'.|@
        
          Profile %s does not contain any keys.
        """.formatted(
            key,
            section.name(),
            ctx.highlight(section.name())
        );
    }

    private String mkKeysMsg(String key, IniSection section) {
        return """
          @|bold,red Error: Key '%s' does not exist in profile '%s'.|@
        
          Available keys in this profile are:
          %s
        
          %s
          %s
        """.formatted(
            key,
            section.name(),
            section.pairs().stream().map(p -> "- " + ctx.highlight(p.key())).collect(Collectors.joining(NL)),
            renderComment(ctx.colors(), "Get the values of the keys in this profile with:"),
            renderCommand(ctx.colors(), "${cli.name} config get " + section.name())
        );
    }

    @Override
    protected Operation<GetConfigResult> mkOperation() {
        return new ConfigGetOperation(config(false), new GetConfigRequest($profileName.orElseGet(this::promptForProfileName), $key));
    }

    private String promptForProfileName() {
        return ProfileNamePrompter.prompt(ctx, config(false).profiles(), "Select a profile to look at",
            (list) -> list,
            (b) -> b.fallbackIndex(0).fix(originalArgs(), "<profile>")
        );
    }
}
