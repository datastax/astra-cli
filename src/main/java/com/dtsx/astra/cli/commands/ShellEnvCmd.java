package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.mixins.HelpMixin;
import com.dtsx.astra.cli.utils.FileUtils;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.nio.file.Path;
import java.util.Optional;

import static com.dtsx.astra.cli.core.output.ExitCode.UNSUPPORTED_EXECUTION;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

@Command(
    name = "shellenv",
    hidden = true
)
public class ShellEnvCmd implements Runnable {
    @Spec
    private CommandSpec spec;

    @Mixin
    private HelpMixin helpMixin;

    @Option(
        names = { "--ignore-multiple-paths" },
        description = "Ignore warnings about multiple home folders or astrarc files being present"
    )
    public boolean $ignoreMultiplePaths;

    @Option(
        names = { "--home" },
        description = "Sets the ASTRA_HOME variable"
    )
    public Optional<Path> $home;

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
            sb.append("export ASTRA_IGNORE_MULTIPLE_PATHS=true").append(NL);
        }

        $home.ifPresent((path) -> {
            sb.append("export ASTRA_HOME=").append(path.toAbsolutePath()).append(NL);
        });

        spec.commandLine().getOut().println(sb);
    }
}
