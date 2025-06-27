package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigListOperation;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;

import static com.dtsx.astra.cli.operations.config.ConfigListOperation.*;

@Command(
    description = "Lists your Astra CLI profiles (configurations), highlighting the one currently in use. Multiple profiles may be highlighted if they share the same credentials."
)
public abstract class ConfigListImpl extends AbstractCmd<ListConfigResult> {
    @Override
    public final OutputAll execute(ListConfigResult result) {
        val cells = result.profiles().stream()
            .map((p) -> Map.of(
                "configuration", mkConfigDisplayName(p.name(), p.isInUse()),
                "env", p.env().name()
            ))
            .toList();

        val allProdEnv = result.profiles().stream()
            .allMatch(p -> p.env() == AstraEnvironment.PROD);

        val table = new ShellTable(cells);

        if (!allProdEnv) {
            return table.withColumns("env", "configuration");
        } else {
            return table.withColumns("configuration");
        }
    }

    @Override
    protected Operation<ListConfigResult> mkOperation() {
        return new ConfigListOperation(config());
    }

    private String mkConfigDisplayName(String name, boolean isInUse) {
        return isInUse ? AstraColors.PURPLE_300.use(name + " (in use)") : name;
    }
}
