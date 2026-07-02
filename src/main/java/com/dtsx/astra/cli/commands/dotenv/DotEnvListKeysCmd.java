package com.dtsx.astra.cli.commands.dotenv;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.dotenv.EnvKey;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Supplier;

@Command(
    name = "list-keys",
    description = "List all available Astra keys (no values resolved)."
)
public class DotEnvListKeysCmd extends AbstractCmd<Void> {
    @Override
    protected Operation<Void> mkOperation() {
        return () -> null;
    }

    @Override
    protected OutputAll execute(Supplier<Void> val) {
        val data = new ArrayList<Map<String, String>>();

        for (val key : EnvKey.values()) {
            data.add(Map.of("Key", key.name()));
        }

        return new ShellTable(data).withColumns("Key");
    }
}
