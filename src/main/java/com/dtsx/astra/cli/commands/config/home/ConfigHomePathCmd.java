package com.dtsx.astra.cli.commands.config.home;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.properties.CliProperties.PathLocationResolver;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.home.ConfigHomePathOperation;
import com.dtsx.astra.cli.operations.config.home.ConfigHomePathOperation.ConfigPathResult;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

@Command(
    name = "path",
    description = {
        "Prints the expected path of the astra home folder, even if does not exist.",
        "Checks @|code ASTRA_HOME|@ @|faint,italic (ASTRA_HOME=~/my_folder)|@ -> @|code XDG_DATA_HOME|@ @|faint,italic ($XDG_DATA_HOME/astra)|@ -> @|code HOME|@ @|faint,italic (~/.astra)|@.",
    }
)
@Example(
    comment = "Get information about the path to the astra home folder",
    command = "${cli.name} config home path"
)
@Example(
    comment = "Get only the path to the astra home folder",
    command = "${cli.name} config home path -p"
)
public class ConfigHomePathCmd extends AbstractCmd<ConfigPathResult> {
    @Option(
        names = { "-p", "--path-only" },
        description = "Display only the expected path to the home folder, without any additional synopsis."
    )
    public boolean pathOnly;

    @Override
    protected OutputAll execute(Supplier<ConfigPathResult> res) {
        val data = mkData(res.get());

        if (pathOnly) {
            return OutputAll.response(res.get().path(), data);
        }

        val foundAtMsg = (res.get().exists())
            ? "Home folder found at"
            : "Your home folder would be at";

        val usingMsg = switch (PathLocationResolver.valueOf(res.get().resolver())) {
            case PathLocationResolver.CUSTOM -> "using the ASTRA_HOME environment variable";
            case PathLocationResolver.XDG -> "following the XDG spec ($XDG_DATA_HOME)";
            case PathLocationResolver.HOME -> "using the default location of the user's home directory";
        };

        val existsMsg = (!res.get().exists())
            ? (NL + NL + "The folder does not actually exist yet, but will be created when a command may need it.")
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
        return new ConfigHomePathOperation(ctx);
    }
}
