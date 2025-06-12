package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.config.ConfigListOperation;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;

import java.util.Map;

public class ConfigListImpl extends AbstractCmd {
    @Override
    public OutputAll execute() {
        val result = new ConfigListOperation(config()).execute();

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

    private String mkConfigDisplayName(String name, boolean isInUse) {
        return isInUse ? AstraColors.PURPLE_300.use(name + " (in use)") : name;
    }
}
