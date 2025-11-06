package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.mixins.HelpMixin;
import com.dtsx.astra.cli.core.properties.CliProperties.ConstEnvVars;
import com.dtsx.astra.cli.utils.FileUtils;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.nio.file.Path;
import java.util.Optional;

import static com.dtsx.astra.cli.commands.AbstractCmd.SHOW_CUSTOM_DEFAULT;
import static com.dtsx.astra.cli.core.output.ExitCode.UNSUPPORTED_EXECUTION;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

@Command(
    name = "shellenv",
    description = {
        "Completions, configuration, and more",
        "",
        "Eval-ing the output of this command will:",
        " @|blue:300 *|@ Add the binary to your PATH",
        " @|blue:300 *|@ Enable shell completions",
        " @|blue:300 *|@ Optionally set any other configuration environment variables.",
        "",
        "Intended to be added to your shell profile (@|code .zshrc|@, @|code .zprofile|@, @|code .bashrc|@, etc.)",
    },
    descriptionHeading = "%n"
)
@Example(
    comment = "Put this in your shell profile (e.g. @|code ~/.zprofile|@) to generate completions and set your PATH",
    command = "eval \"$(${cli.path} shellenv)\""
)
@Example(
    comment = "Set a custom @|code ASTRA_HOME|@ path",
    command = "eval \"$(${cli.path} shellenv --home /path/to/astra/home)\""
)
@Example(
    comment = "Disable update notifications",
    command = "eval \"$(${cli.path} shellenv --no-update-notifier)\""
)
@Example(
    comment = "Ignore warnings about multiple home folders or astrarc files",
    command = "eval \"$(${cli.path} shellenv --ignore-multiple-paths)\""
)
public class ShellEnvCmd implements Runnable {
    @Spec
    private CommandSpec spec;

    @Mixin
    private HelpMixin helpMixin;

    @Option(
        names = { "--home" },
        description = { "Sets the @|code ASTRA_HOME|@ env var. See @|code astra config home path -h|@ for how this is resolved.", SHOW_CUSTOM_DEFAULT + "${cli.home-folder.path}" }
    )
    public Optional<Path> $home;

    @Option(
        names = { "--ignore-multiple-paths" },
        description = "Ignore warnings about multiple home folders or astrarc files being present. Sets @|code " + ConstEnvVars.IGNORE_MULTIPLE_PATHS + "=true|@ under the hood."
    )
    public boolean $ignoreMultiplePaths;

    @Option(
        names = { "--no-update-notifier" },
        description = "Disables background update checks notifications. Sets @|code " + ConstEnvVars.NO_UPDATE_NOTIFIER + "=true|@ under the hood."
    )
    public boolean $noUpdateNotifier;

    @Override
    public void run() {
        val binaryPath = FileUtils.getCurrentBinaryPath();

        if (binaryPath.isEmpty()) {
            throw new AstraCliException(UNSUPPORTED_EXECUTION, """
              @|bold,red Error: Can not run this command when not executing from a binary|@
            """);
        }

        val sb = new StringBuilder();

        sb.append("export PATH=").append(binaryPath.get().getParent()).append(":$PATH").append(NL);
        sb.append("source <(").append(binaryPath.get()).append(" compgen)").append(NL);

        if ($ignoreMultiplePaths) {
            sb.append("export ").append(ConstEnvVars.IGNORE_MULTIPLE_PATHS).append("=true").append(NL);
        }

        if ($noUpdateNotifier) {
            sb.append("export ").append(ConstEnvVars.NO_UPDATE_NOTIFIER).append("=true").append(NL);
        }

        $home.ifPresent((path) -> {
            sb.append("export ASTRA_HOME=").append(path.toAbsolutePath()).append(NL);
        });

        spec.commandLine().getOut().println(sb);
    }
}
