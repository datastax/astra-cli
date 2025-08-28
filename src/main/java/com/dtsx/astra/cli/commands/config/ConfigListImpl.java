package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigListOperation;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.operations.config.ConfigListOperation.ListConfigResult;

@Command(
    description = "Lists your Astra CLI profiles (configurations), highlighting the one currently in use. Multiple profiles may be highlighted if they share the same credentials."
)
public abstract class ConfigListImpl extends AbstractConfigCmd<ListConfigResult> {
    @Override
    public final OutputHuman executeHuman(Supplier<ListConfigResult> result) {
        val cells = result.get().profiles().stream()
            .map((p) -> Map.of(
                "configuration", mkConfigDisplayName(p.name(), p.isInUse()),
                "env", p.env().name()
            ))
            .toList();

        val allProdEnv = result.get().profiles().stream()
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
            .map((p) -> Map.of(
                "name", p.name(),
                "env", p.env().name(),
                "token", p.token().unwrap(),
                "isUsedAsDefault", p.isInUse()
            ))
            .toList();

        return new ShellTable(cells).withColumns("name", "env", "token", "isUsedAsDefault");
    }

    @Override
    protected Operation<ListConfigResult> mkOperation() {
        return new ConfigListOperation(config(false));
    }

    private String mkConfigDisplayName(String name, boolean isInUse) {
        return isInUse ? ctx.colors().PURPLE_300.use(name + " (in use)") : name;
    }
}
