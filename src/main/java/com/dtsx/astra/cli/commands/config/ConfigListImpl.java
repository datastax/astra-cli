package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.config.AstraConfig.Profile;
import com.dtsx.astra.cli.config.ProfileName;
import com.dtsx.astra.cli.output.output.OutputAll;
import com.dtsx.astra.cli.output.table.ShellTable;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;

import java.util.Map;

public class ConfigListImpl extends AbstractCmd {
    @Override
    public OutputAll execute() {
        val defaultToken = config()
            .lookupProfile(ProfileName.DEFAULT)
            .map(Profile::token)
            .orElse(null);

        val cells = config().getProfiles().stream()
            .filter((p) -> (
                !p.name().equals(ProfileName.DEFAULT)
            ))
            .map((p) -> Map.of(
                "configuration", (p.token().equals(defaultToken)) ? ShellTable.highlight(p.name() + " (in use)") : p.name(),
                "env", p.env().name()
            ))
            .toList();

        val table = new ShellTable(cells);

        if (shouldShowEnvColumn()) {
            return table.withColumns("env", "configuration");
        } else {
            return table.withColumns("configuration");
        }
    }

    public boolean shouldShowEnvColumn() {
        return !config().getProfiles().stream().allMatch((p) -> p.env() == AstraEnvironment.PROD);
    }
}
