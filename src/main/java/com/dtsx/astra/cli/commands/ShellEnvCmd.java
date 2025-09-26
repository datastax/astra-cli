package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.nio.file.Path;

import static com.dtsx.astra.cli.core.output.ExitCode.ILLEGAL_OPERATION;
import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

@Command(
    name = "shellenv",
    hidden = true
)
public class ShellEnvCmd implements Runnable {
    @Spec
    private CommandSpec spec;

    @Override
    public void run() {
        val file = ProcessHandle.current().info().command().map(Path::of);

        if (file.isEmpty()) {
            throw new AstraCliException(ILLEGAL_OPERATION, """
              @|bold,red Error: Can not run this command when not executing from a binary|@
            """);
        }

        spec.commandLine().getOut().println(trimIndent("""
          export PATH=%s:$PATH
          source <(%s compgen)
        """.formatted(
            file.get().getParent(),
            file.get()
        )));
    }
}
