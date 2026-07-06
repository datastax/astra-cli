package com.dtsx.astra.cli.commands.dotenv;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.dotenv.EnvKey;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
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
    @MustBeInvokedByOverriders
    public void prelude() {
        super.prelude();
        if (!ctx.properties().disableBetaWarnings()) {
            ctx.log().warn("${cli.name} dotenv commands are still in beta and may change without notice.");
        }
    }

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
