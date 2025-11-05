package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.properties.CliProperties.PathLocationResolver;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.ConfigPathOperation;
import com.dtsx.astra.cli.operations.config.ConfigPathOperation.ConfigPathResult;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

@Command(
    name = "path",
    description = {
        "Prints the expected path of the .astrarc file, even if does not exist.",
        "Checks @|code ASTRARC|@ @|faint,italic (ASTRARC=~/my_folder/.my_rc)|@ -> @|code XDG_CONFIG_HOME|@ @|faint,italic ($XDG_CONFIG_HOME/astra/.astrarc)|@ -> @|code HOME|@ @|faint,italic (~/.astrarc)|@.",
    }
)
@Example(
    comment = "Get information about the path to the .astrarc file",
    command = "${cli.name} config path"
)
@Example(
    comment = "Print only the path to the .astrarc file, without additional information",
    command = "${cli.name} config path -p"
)
public class ConfigPathCmd extends AbstractCmd<ConfigPathResult> {
    @Option(
        names = { "-p", "--plain" },
        description = "Print only the path to the .astrarc file, without additional information"
    )
    public boolean pathOnly;

    @Override
    protected OutputAll execute(Supplier<ConfigPathResult> res) {
        val data = mkData(res.get());

        if (pathOnly) {
            return OutputAll.response(res.get().path(), data);
        }

        val foundAtMsg = (res.get().exists())
            ? ".astrarc found at"
            : "Your .astrarc would be at";

        val usingMsg = switch (PathLocationResolver.valueOf(res.get().resolver())) {
            case PathLocationResolver.CUSTOM -> "using the ASTRARC environment variable";
            case PathLocationResolver.XDG -> "following the XDG spec ($XDG_CONFIG_HOME)";
            case PathLocationResolver.HOME -> "using the default location of the user's home directory";
        };

        val existsMsg = (!res.get().exists())
            ? (NL + NL + "The file does not actually exist yet, but may be created with @'!astra setup!@ or @'!astra config create!@.")
            : "";

        val msg = "%s @|underline @'!%s!@|@ %s.%s".formatted(foundAtMsg, res.get().path(), usingMsg, existsMsg);

        return OutputAll.response(msg, data);
    }

    private LinkedHashMap<String, Object> mkData(ConfigPathResult res) {
        return sequencedMapOf(
            "path", res.path(),
            "resolver", res.resolver(),
            "exists", res.exists()
        );
    }

    @Override
    protected Operation<ConfigPathResult> mkOperation() {
        return new ConfigPathOperation(ctx);
    }
}
