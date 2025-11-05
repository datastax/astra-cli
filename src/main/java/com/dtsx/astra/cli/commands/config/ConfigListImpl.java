package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.core.CliConstants.$Env;
import com.dtsx.astra.cli.core.completions.impls.AstraEnvCompletion;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigListOperation;
import com.dtsx.astra.cli.operations.config.ConfigListOperation.CreateListRequest;
import com.dtsx.astra.cli.operations.config.ConfigListOperation.ProfileInfo;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.operations.config.ConfigListOperation.ListConfigResult;
import static com.dtsx.astra.cli.utils.CollectionUtils.listAdd;
import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    description = {
        "Lists your Astra CLI profiles (configurations), highlighting the one currently in use.",
        "Multiple profiles may be highlighted if they share the same credentials.",
    }
)
public abstract class ConfigListImpl extends AbstractConfigCmd<ListConfigResult> {
    @Option(
        names = { $Env.LONG, $Env.SHORT },
        description = "Filter by Astra environment",
        completionCandidates = AstraEnvCompletion.class,
        paramLabel = $Env.LABEL
    )
    public Optional<AstraEnvironment> $env;

    @Override
    public final OutputHuman executeHuman(Supplier<ListConfigResult> result) {
        val res = result.get();

        val profilesWithoutDefault = res.profiles().stream()
            .filter(p -> !p.name().equals("default"))
            .toList();

        var cells = profilesWithoutDefault.stream()
            .map((p) -> sequencedMapOf(
                "configuration", mkConfigDisplayName(p.name(), p.isInUse()),
                "env", p.env().name()
            ))
            .toList();

        val defaultInUse = profilesWithoutDefault.stream()
            .anyMatch(ProfileInfo::isInUse);

        if (!defaultInUse && res.defaultProfile().isPresent()) {
            cells = listAdd(cells, sequencedMapOf(
                "configuration", "default",
                "env", res.defaultProfile().get().env().name()
            ));
        }

        val allProdEnv = res.profiles().stream()
            .allMatch(p -> p.env() == AstraEnvironment.PROD);

        val table = new ShellTable(cells);

        if (!allProdEnv) {
            return table.withColumns("env", "configuration");
        } else {
            return table.withColumns("configuration");
        }
    }

    @Override
    public final OutputAll executeJson(Supplier<ListConfigResult> result) {
        val cells = result.get().profiles().stream()
            .map((p) -> sequencedMapOf(
                "name", p.name(),
                "env", p.env().name(),
                "token", p.token().unsafeUnwrap(),
                "isUsedAsDefault", p.isInUse()
            ))
            .toList();

        return new ShellTable(cells).withColumns("name", "env", "token", "isUsedAsDefault");
    }

    @Override
    protected Operation<ListConfigResult> mkOperation() {
        return new ConfigListOperation(config(false), new CreateListRequest($env));
    }

    private String mkConfigDisplayName(String name, boolean isInUse) {
        return isInUse ? ctx.colors().PURPLE_300.use(name + " (in use)") : name;
    }
}
