package com.dtsx.astra.cli.commands.config.home;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.properties.CliProperties.PathLocationResolver;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.config.home.ConfigHomePathOperation;
import com.dtsx.astra.cli.operations.config.home.ConfigHomePathOperation.ConfigPathResult;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

@Command(
    name = "path",
    description = {
        "Prints the expected path of the astra home folder, even if does not exist.",
        "",
        "The folder is resolved in the following order:",
        " @|blue:300 1.|@ The @|code ASTRA_HOME|@ environment variable @|faint,italic (e.g. ASTRA_HOME=~/my_folder)|@",
        " @|blue:300 2.|@ The @|code XDG_DATA_HOME|@ spec @|faint,italic (e.g. $XDG_DATA_HOME/astra)|@",
        " @|blue:300 3.|@ The default home directory @|faint,italic (e.g. ~/.astra)|@",
        "",
        "By default, shows informational output when running in a TTY (interactive terminal), and plain path output when piped or redirected."
    }
)
@Example(
    comment = "Get information about the path to the astra home folder",
    command = "${cli.name} config home path"
)
@Example(
    comment = "Force only the plain path to the .astrarc file, without additional information",
    command = "${cli.name} config home path -p"
)
@Example(
    comment = "Force informational output even when piped or redirected",
    command = "${cli.name} config home path -i"
)
public class ConfigHomePathCmd extends AbstractCmd<ConfigPathResult> {
    @ArgGroup
    public @Nullable OutputMode outputMode;

    public static class OutputMode {
        @Option(
            names = { "-p", "--plain" },
            description = "Print only the path to the home folder, without additional information (always)"
        )
        public boolean pathOnly;

        @Option(
            names = { "-i", "--info" },
            description = "Print informational output with context about the path (always)"
        )
        public boolean info;
    }

    @Override
    protected OutputAll execute(Supplier<ConfigPathResult> res) {
        val data = mkData(res.get());

        val pathOnly = outputMode != null && outputMode.pathOnly;
        val info = outputMode != null && outputMode.info;

        if (pathOnly || (!info && ctx.isNotTty())) {
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
